package com.room.app.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.room.app.dto.Expense;
import com.room.app.dto.ExpenseRequest;
import com.room.app.dto.Member;
import com.room.app.dto.PaymentHistory;
import com.room.app.dto.User;
import com.room.app.exception.AccessDeniedException;
import com.room.app.repository.ExpenseRepository;
import com.room.app.repository.MemberRepository;
import com.room.app.repository.PaymentHistoryRepository;

import jakarta.transaction.Transactional;

@Service
public class ExpenseService {
	private final ExpenseRepository expenseRepository;
	private final MemberService memberService;
	private final MemberRepository memberRepository;
	private final PaymentHistoryRepository paymentHistoryRepository;
	 private final BudgetService budgetService ;

	public ExpenseService(ExpenseRepository expenseRepository, MemberService memberService,
			MemberRepository memberRepository, PaymentHistoryRepository paymentHistoryRepository,BudgetService budgetService) {
		this.expenseRepository = expenseRepository;
		this.memberService = memberService;
		this.memberRepository = memberRepository;
		this.paymentHistoryRepository = paymentHistoryRepository;
		this.budgetService = budgetService;
	}

	public List<Expense> getAllActiveExpenses() {
		return expenseRepository.findAllActive();
	}

	@Transactional
    public void softDeleteExpense(Long id, User deletedBy) throws ResourceNotFoundException {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        
        // Refund to budget if expense wasn't cleared
        if (!expense.isCleared()) {
            budgetService.refundToBudget(expense.getAmount());
        }
        
        expense.setIsDeleted("Y");
        expense.setDeletedBy(deletedBy);
        expense.setDeletedAt(LocalDateTime.now());
        expenseRepository.save(expense);
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
		return expenseRepository.findAllActive().stream().filter(expense -> expense.getClearedAmount() != null)
				.collect(Collectors.groupingBy(expense -> expense.getMember().getName(),
						Collectors.reducing(BigDecimal.ZERO, Expense::getClearedAmount, BigDecimal::add)));
	}

	public Map<String, BigDecimal> getExpenseSummaryByMember() {
		return expenseRepository.findAllActive().stream().filter(expense -> expense.getMember() != null)
				.collect(Collectors.groupingBy(expense -> expense.getMember().getName(),
						Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)));
	}

	public void markExpenseCleared(Long expenseId, BigDecimal amount) throws ResourceNotFoundException {
		Expense expense = expenseRepository.findById(expenseId)
				.orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

		expense.setClearedAmount(amount);
		expense.setCleared(true); // Mark the expense as cleared
		expenseRepository.save(expense);
	}

	 @Transactional
	    public Expense addExpense(ExpenseRequest expenseRequest) throws ResourceNotFoundException {
	        Member member = memberService.getMemberById(expenseRequest.getMemberId());

	       
	        budgetService.deductFromBudget(expenseRequest.getAmount());

	        Expense expense = new Expense();
	        expense.setMember(member);
	        expense.setDescription(expenseRequest.getDescription());
	        expense.setDate(expenseRequest.getDate());
	        expense.setAmount(expenseRequest.getAmount());
	        expense.setRemainingAmount(expenseRequest.getAmount());
	        
	        return expenseRepository.save(expense);
	    }

	 @Transactional
	    public Expense updateExpense(Long id, ExpenseRequest request, User user)
	            throws ResourceNotFoundException, AccessDeniedException {
	        if (!user.getRole().equalsIgnoreCase("ROLE_ADMIN") && !user.getRole().equalsIgnoreCase("ADMIN")) {
	            throw new AccessDeniedException("Only admins can update expenses");
	        }

	        Expense existingExpense = expenseRepository.findById(id)
	                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

	        if (existingExpense.isCleared()) {
	            throw new AccessDeniedException("Cannot modify cleared expenses");
	        }

	        Member member = memberRepository.findById(request.getMemberId())
	                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

	        // Calculate amount difference and adjust budget
	        BigDecimal amountDifference = request.getAmount().subtract(existingExpense.getAmount());
	        if (amountDifference.compareTo(BigDecimal.ZERO) > 0) {
	            budgetService.deductFromBudget(amountDifference);
	        } else if (amountDifference.compareTo(BigDecimal.ZERO) < 0) {
	            budgetService.refundToBudget(amountDifference.abs());
	        }

	        // Update expense fields
	        existingExpense.setMember(member);
	        existingExpense.setDescription(request.getDescription());
	        existingExpense.setAmount(request.getAmount());
	        existingExpense.setDate(request.getDate());
	        
	        // Recalculate remaining amount if there were cleared payments
	        if (existingExpense.getClearedAmount() != null) {
	            BigDecimal newRemainingAmount = request.getAmount().subtract(existingExpense.getClearedAmount());
	            existingExpense.setRemainingAmount(newRemainingAmount.max(BigDecimal.ZERO));
	            existingExpense.setCleared(newRemainingAmount.compareTo(BigDecimal.ZERO) <= 0);
	        }

	        return expenseRepository.save(existingExpense);
	    }


	public void deleteExpense(Long id, User user) throws ResourceNotFoundException, AccessDeniedException {
		if (!user.getRole().equalsIgnoreCase("ROLE_ADMIN") && !user.getRole().equalsIgnoreCase("ADMIN")) {
			throw new AccessDeniedException("Only admins can delete expenses");
		}

		if (!expenseRepository.existsById(id)) {
			throw new ResourceNotFoundException("Expense not found");
		}
		expenseRepository.deleteById(id);
	}

	@Transactional
	public Expense clearExpense(Long expenseId, Long memberId, BigDecimal amount) throws ResourceNotFoundException {
		// Fetch expense and member
		Expense expense = expenseRepository.findById(expenseId)
				.orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new ResourceNotFoundException("Member not found"));

		// Validate payment amount
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Payment amount must be positive");
		}

		BigDecimal remainingBeforePayment = expense.getRemainingAmount();
		if (amount.compareTo(remainingBeforePayment) > 0) {
			throw new IllegalArgumentException(String.format("Payment amount ₹%.2f exceeds remaining balance ₹%.2f",
					amount, remainingBeforePayment));
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
		return expenseRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
	}

}
