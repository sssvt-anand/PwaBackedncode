package com.room.app.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.room.app.dto.ExpenseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.room.app.dto.ExpenseRequest;
import com.room.app.dto.MemberBalanceSummary;
import com.room.app.entity.Expense;
import com.room.app.entity.Member;
import com.room.app.entity.PaymentHistory;
import com.room.app.entity.User;
import com.room.app.exception.AccessDeniedException;
import com.room.app.repository.BudgetRepository;
import com.room.app.repository.ExpenseRepository;
import com.room.app.repository.MemberRepository;
import com.room.app.repository.PaymentHistoryRepository;

import jakarta.transaction.Transactional;

@Service
public class ExpenseService {

    private ExpenseRepository expenseRepository;
    private MemberService memberService;
    private MemberRepository memberRepository;
    private PaymentHistoryRepository paymentHistoryRepository;
    private BudgetService budgetService;
    private BudgetRepository budgetRepository;

    @Autowired
    public void setExpenseRepository(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Autowired
    public void setMemberService(MemberService memberService) {
        this.memberService = memberService;
    }

    @Autowired
    public void setMemberRepository(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Autowired
    public void setPaymentHistoryRepository(PaymentHistoryRepository paymentHistoryRepository) {
        this.paymentHistoryRepository = paymentHistoryRepository;
    }

    @Autowired
    public void setBudgetService(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    public List<Expense> getAllActiveExpenses() {
        return expenseRepository.findAllActive();
    }


    public List<Expense> getMonthlyExpenses() {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        return expenseRepository.findByDateBetween(start, end);
    }

    public List<Expense> getYearlyExpenses() {
        LocalDate start = LocalDate.now().withDayOfYear(1);
        LocalDate end = start.plusYears(1).minusDays(1);
        return expenseRepository.findByDateBetween(start, end);
    }

    @Cacheable(value = "expensesByMember", key = "#memberId")
    public List<Expense> getExpensesByMember(Long memberId) {
        return expenseRepository.findByMemberId(memberId);
    }

    public List<Expense> getExpensesWithoutMember() {
        return expenseRepository.findByMemberIsNull();
    }

    public Optional<Expense> getExpenseByMessageId(Integer messageId) {
        return expenseRepository.findByMessageId(messageId);
    }

    public Expense saveExpense(Expense expense) {
        return expenseRepository.save(expense);
    }


    public Map<String, BigDecimal> getClearedSummaryByMember() {
        return expenseRepository.getClearedSummaryByMember().stream().collect(Collectors.toMap(arr -> (String) arr[0], arr -> (BigDecimal) arr[1] // sum of cleared amounts
        ));
    }


    public Map<String, BigDecimal> getExpenseSummaryByMember() {
        return expenseRepository.getExpenseSummaryByMember().stream().collect(Collectors.toMap(arr -> (String) arr[0], arr -> (BigDecimal) arr[1]));
    }

    public void markExpenseCleared(Long expenseId, BigDecimal amount) throws ResourceNotFoundException {
        Expense expense = expenseRepository.findById(expenseId).orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        expense.setClearedAmount(amount);
        expense.setCleared(true); // Mark the expense as cleared
        expenseRepository.save(expense);
    }

    @Transactional
    public Expense addExpense(ExpenseRequest expenseRequest) throws ResourceNotFoundException {
        Member member = memberService.getMemberById(expenseRequest.getMemberId());

        Expense expense = new Expense();
        expense.setMember(member);
        expense.setDescription(expenseRequest.getDescription());
        expense.setDate(expenseRequest.getDate());
        expense.setAmount(expenseRequest.getAmount());
        expense.setRemainingAmount(expenseRequest.getAmount());
        budgetService.deductFromBudget(expenseRequest.getAmount());

        return expenseRepository.save(expense);
    }

    public BigDecimal getTotalExpensesForCurrentMonth() {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        return expenseRepository.findByDateBetween(start, end).stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public Expense updateExpense(Long id, ExpenseRequest request, User user) throws ResourceNotFoundException, AccessDeniedException {
        if (!isAdmin(user)) {
            throw new AccessDeniedException("Only admins can update expenses");
        }

        Expense existingExpense = expenseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        if (existingExpense.isCleared()) {
            throw new AccessDeniedException("Cannot modify cleared expenses");
        }

        Member member = memberRepository.findById(request.getMemberId()).orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        // Calculate amount difference and adjust budget
        BigDecimal amountDifference = request.getAmount().subtract(existingExpense.getAmount());
        if (amountDifference.compareTo(BigDecimal.ZERO) > 0) {
            budgetService.deductFromBudget(amountDifference);
        } else if (amountDifference.compareTo(BigDecimal.ZERO) < 0) {
            budgetService.refundToBudget(amountDifference.abs());
        }

        existingExpense.setMember(member);
        existingExpense.setDescription(request.getDescription());
        existingExpense.setAmount(request.getAmount());
        existingExpense.setDate(request.getDate());

        if (existingExpense.getClearedAmount() != null) {
            BigDecimal newRemainingAmount = request.getAmount().subtract(existingExpense.getClearedAmount());
            existingExpense.setRemainingAmount(newRemainingAmount.max(BigDecimal.ZERO));
            existingExpense.setCleared(newRemainingAmount.compareTo(BigDecimal.ZERO) <= 0);
        }

        return expenseRepository.save(existingExpense);
    }

    @Transactional
    public void softDeleteExpense(Long id, User user) throws ResourceNotFoundException, AccessDeniedException, IllegalStateException {
        if (!isAdmin(user)) {
            throw new AccessDeniedException("Only admins can delete expenses");
        }
        Expense expense = expenseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        if (expense.isCleared()) {
            throw new IllegalStateException("Cannot delete expense that has been cleared (fully cleared)");
        }

        budgetService.refundToBudget(expense.getAmount());

        expense.setIsDeleted("Y");
        expense.setDeletedBy(user); // Using the same user as deletedBy
        expense.setDeletedAt(LocalDateTime.now());
        expenseRepository.save(expense);
    }

    private boolean isAdmin(User user) {
        return user.getRole().equalsIgnoreCase("ROLE_ADMIN") || user.getRole().equalsIgnoreCase("ADMIN");
    }

    @Transactional
    public Expense clearExpense(Long expenseId, Long memberId, BigDecimal amount) throws ResourceNotFoundException {
        // Fetch expense and member
        Expense expense = expenseRepository.findById(expenseId).orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        // Validate payment amount
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }

        BigDecimal remainingBeforePayment = expense.getRemainingAmount();
        if (amount.compareTo(remainingBeforePayment) > 0) {
            throw new IllegalArgumentException(String.format("Payment amount ₹%.2f exceeds remaining balance ₹%.2f", amount, remainingBeforePayment));
        }

        // Update cumulative cleared amount and remaining balance
        BigDecimal newClearedAmount = expense.getClearedAmount().add(amount);
        expense.setClearedAmount(newClearedAmount);
        expense.setRemainingAmount(expense.getAmount().subtract(newClearedAmount));

        // Update last payment details
        expense.setLastClearedAmount(amount); // Track individual payment
        expense.setLastClearedBy(member);
        expense.setLastClearedAt(LocalDateTime.now());

        // Mark as fully cleared if applicable
        if (expense.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0) {
            expense.setCleared(true);
            expense.setClearedBy(member);
            expense.setClearedAt(LocalDateTime.now());
        }

        // Record payment history
        PaymentHistory payment = new PaymentHistory();
        payment.setAmount(amount);
        payment.setClearedBy(member);
        payment.setTimestamp(LocalDateTime.now());
        payment.setExpense(expense);
        paymentHistoryRepository.save(payment); // Use the correct repository

        return expenseRepository.save(expense);
    }


    public List<Expense> getExpensesByMemberName(String memberName) {
        return expenseRepository.findByMemberNameContainingIgnoreCase(memberName);
    }


    public List<PaymentHistory> getPaymentHistoryByExpense(Long expenseId) {
        return paymentHistoryRepository.findByExpenseId(expenseId);
    }


    public Expense getExpenseById(Long id) throws ResourceNotFoundException {
        return expenseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
    }

    public Map<String, Map<String, BigDecimal>> getMemberBalances() {
        return expenseRepository.getMemberBalanceSummary().stream().collect(Collectors.toMap(MemberBalanceSummary::getMemberName, summary -> Map.of("total", summary.getTotalAmount(), "cleared", summary.getClearedAmount(), "remaining", summary.getRemainingAmount())));
    }

    @Transactional
    public void clearAllExpenses(User user) {
        expenseRepository.softDeleteAllExpenses(LocalDateTime.now(), user.getId());
    }

    public List<ExpenseDTO> getAllActiveExpensesUi() {
        return expenseRepository.findAllActiveUi(); // no mapping needed
    }


    public ExpenseDTO convertToDto(Expense expense) {
        return new ExpenseDTO(expense.getId(), expense.getDescription(), expense.getDate(), expense.getAmount(), expense.getClearedAmount(), expense.getRemainingAmount(), expense.getClearedAmount().compareTo(expense.getAmount()) >= 0, expense.getMember() != null ? expense.getMember().getName() : "Unknown", expense.getMember() != null ? expense.getMember().getId() : null, expense.getLastClearedAt(), expense.getLastClearedAmount(), expense.getLastClearedBy() != null ? expense.getLastClearedBy().getName() : "Unknown");
    }

}
