package com.room.app.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy; // ✅ Correct import
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.room.app.dto.BudgetStatusResponse;
import com.room.app.dto.MemberBudgetDetail;
import com.room.app.entity.Budget;
import com.room.app.entity.Member;
import com.room.app.exception.ResourceNotFoundException;
import com.room.app.repository.BudgetRepository;
import com.room.app.repository.MemberRepository;

@Service
public class BudgetService {

	@Autowired
	private BudgetRepository budgetRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	@Lazy
	private ExpenseService expenseService;

	@Scheduled(cron = "0 0 0 1 * ?") // Runs on 1st of each month at midnight
	@Transactional
	public void initializeNewMonthBudget() {
		String currentMonthYear = YearMonth.now().toString();

		// Check if a budget exists (active or deleted)
		Optional<Budget> existingBudget = budgetRepository.findByMonthYear(currentMonthYear);

		if (existingBudget.isEmpty()) {
			// No budget exists → create a new one
			createNewBudget(currentMonthYear);
		} else if ("Y".equals(existingBudget.get().getDeleted())) {
			// Budget exists but is deleted → update it instead of inserting a new one
			Budget budget = existingBudget.get();
			budget.setDeleted("N"); // Mark as active
			budget.setArchived(false);
			budget.setArchivedAt(null);

			// Recalculate budget (optional)
			List<Member> members = memberRepository.findAll();
			BigDecimal totalBudget = members.stream().filter(m -> m.getMonthlyBudget() != null)
					.map(Member::getMonthlyBudget).reduce(BigDecimal.ZERO, BigDecimal::add);

			budget.setTotalBudget(totalBudget);
			budget.setRemainingBudget(BigDecimal.ZERO);
			budgetRepository.save(budget); // UPDATE (no duplicate key error)
		}
		// Else: Active budget already exists → do nothing
	}

	// In BudgetService.java
	@Transactional
	public void recalculateBudget() {
		Budget currentBudget = getCurrentBudget();

		// Recalculate total budget from members
		BigDecimal newTotal = memberRepository.findAll().stream().filter(m -> m.getMonthlyBudget() != null)
				.map(Member::getMonthlyBudget).reduce(BigDecimal.ZERO, BigDecimal::add);

		// Get total expenses
		BigDecimal totalExpenses = expenseService.getTotalExpensesForCurrentMonth();

		// Update budget
		currentBudget.setTotalBudget(newTotal);
		currentBudget.setRemainingBudget(newTotal.subtract(totalExpenses));
		budgetRepository.save(currentBudget);
	}

	// In BudgetService.java
	private void createNewBudget(String monthYear) {
		List<Member> members = memberRepository.findAll();
		BigDecimal totalBudget = members.stream().filter(m -> m.getMonthlyBudget() != null)
				.map(Member::getMonthlyBudget).reduce(BigDecimal.ZERO, BigDecimal::add);

		Budget newBudget = new Budget();
		newBudget.setMonthYear(monthYear);
		newBudget.setTotalBudget(totalBudget);
		newBudget.setRemainingBudget(BigDecimal.ZERO); // Initialize remaining = total
		newBudget.setDeleted("N");
		budgetRepository.save(newBudget);
	}

	@Transactional(readOnly = true)
	public Budget getCurrentBudget() {
		return budgetRepository.findTopByOrderByCreatedAtDesc()
				.orElseThrow(() -> new ResourceNotFoundException("No budget available"));
	}

	@Transactional
	public void deductFromBudget(BigDecimal amount) {
		Budget currentBudget = getCurrentBudget();
		BigDecimal newRemaining = currentBudget.getRemainingBudget().subtract(amount);
		currentBudget.setRemainingBudget(newRemaining);
		budgetRepository.save(currentBudget); // Will throw OptimisticLockException if conflicted
	}

	@Transactional
	public void refundToBudget(BigDecimal amount) {
		Budget currentBudget = getCurrentBudget();
		currentBudget.setRemainingBudget(currentBudget.getRemainingBudget().add(amount));
		budgetRepository.save(currentBudget);
	}

	@Transactional(readOnly = true)
	public BudgetStatusResponse getMemberBudgetStatus(Long memberId) throws ResourceNotFoundException {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new ResourceNotFoundException("Member not found"));

		Budget currentBudget = getCurrentBudget();

		// Calculate member's share of the budget based on their contribution
		BigDecimal memberRatio = member.getMonthlyBudget().divide(currentBudget.getTotalBudget(), 4,
				RoundingMode.HALF_UP);
		BigDecimal memberRemaining = currentBudget.getRemainingBudget().multiply(memberRatio);

		return new BudgetStatusResponse(member.getMonthlyBudget(), memberRemaining, currentBudget.getMonthYear(),
				memberRemaining.compareTo(BigDecimal.ZERO) < 0,
				memberRemaining.compareTo(BigDecimal.ZERO) < 0 ? memberRemaining.abs() : BigDecimal.ZERO);
	}

	@Transactional
	public void recalculateTotalBudget() {
		String currentMonthYear = YearMonth.now().toString();
		Budget currentBudget = budgetRepository.findByMonthYear(currentMonthYear)
				.orElseThrow(() -> new ResourceNotFoundException("Current budget not found"));

		List<Member> members = memberRepository.findAll();
		BigDecimal newTotalBudget = members.stream().filter(m -> m.getMonthlyBudget() != null)
				.map(Member::getMonthlyBudget).reduce(BigDecimal.ZERO, BigDecimal::add);

		// Calculate the difference and adjust remaining budget
		BigDecimal difference = newTotalBudget.subtract(currentBudget.getTotalBudget());
		currentBudget.setTotalBudget(newTotalBudget);
		currentBudget.setRemainingBudget(currentBudget.getRemainingBudget().add(difference));

		budgetRepository.save(currentBudget);
	}

	// In BudgetService.java
	@Transactional
	public Member updateMemberBudget(Long memberId, BigDecimal monthlyBudget) throws ResourceNotFoundException {
		// Validate input
		if (monthlyBudget == null) {
			throw new IllegalArgumentException("Monthly budget cannot be null");
		}
		if (monthlyBudget.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Monthly budget cannot be negative");
		}

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new ResourceNotFoundException("Member not found"));

		Budget currentBudget = getCurrentBudget();
		BigDecimal oldBudget = member.getMonthlyBudget() != null ? member.getMonthlyBudget() : BigDecimal.ZERO;
		BigDecimal difference = monthlyBudget.subtract(oldBudget);

		// Update member
		member.setMonthlyBudget(monthlyBudget);
		memberRepository.save(member);

		// Update budget if current month
		if (currentBudget.getMonthYear().equals(YearMonth.now().toString())) {
			BigDecimal newTotalBudget = currentBudget.getTotalBudget().add(difference);
			currentBudget.setTotalBudget(newTotalBudget);

			// Only adjust remaining budget if no expenses exist yet
			if (expenseService.getTotalExpensesForCurrentMonth().compareTo(BigDecimal.ZERO) == 0) {
				currentBudget.setRemainingBudget(BigDecimal.ZERO);
			}

			budgetRepository.save(currentBudget);
		}

		return member;
	}

	@Transactional
	public void addExpense(BigDecimal amount) {
		Budget currentBudget = getCurrentBudget();
		currentBudget.setRemainingBudget(currentBudget.getRemainingBudget().subtract(amount));
		budgetRepository.save(currentBudget);
	}

	@Transactional
	public void recalculateRemainingBudget() {
		Budget currentBudget = getCurrentBudget();
		// Assuming you have an expense service to get total expenses
		BigDecimal totalExpenses = expenseService.getTotalExpensesForCurrentMonth();
		currentBudget.setRemainingBudget(currentBudget.getTotalBudget().subtract(totalExpenses));
		budgetRepository.save(currentBudget);
	}

	@Transactional(readOnly = true)
	public List<MemberBudgetDetail> getAllMembersWithBudgets() {
		// Get current budget
		Budget currentBudget = getCurrentBudget();

		// Calculate total used budget
		BigDecimal totalUsedBudget = currentBudget.getTotalBudget().subtract(currentBudget.getRemainingBudget());

		// Get all members
		List<Member> members = memberRepository.findAll();

		return members.stream().map(member -> {
			BigDecimal monthlyBudget = member.getMonthlyBudget() != null ? member.getMonthlyBudget() : BigDecimal.ZERO;

			// Calculate ratio of this member's contribution
			BigDecimal ratio = currentBudget.getTotalBudget().compareTo(BigDecimal.ZERO) != 0
					? monthlyBudget.divide(currentBudget.getTotalBudget(), 4, RoundingMode.HALF_UP)
					: BigDecimal.ZERO;

			// Calculate used and remaining budget
			BigDecimal usedBudget = totalUsedBudget.multiply(ratio);
			BigDecimal remainingBudget = monthlyBudget.subtract(usedBudget);
			boolean overBudget = remainingBudget.compareTo(BigDecimal.ZERO) < 0;
			BigDecimal overBudgetAmount = overBudget ? remainingBudget.abs() : BigDecimal.ZERO;

			return new MemberBudgetDetail(member.getId(), member.getName(), monthlyBudget, usedBudget, remainingBudget,
					overBudget, overBudgetAmount);
		}).collect(Collectors.toList());
	}

	@Transactional
	public void clearAllBudgetsAndExpenses() {
		// Archive current budget without creating a new one
		Budget currentBudget = getCurrentBudget();
		currentBudget.setArchived(true);
		currentBudget.setArchivedAt(LocalDateTime.now());
		currentBudget.setDeleted("Y");

	}

	@Transactional
	public void archiveCurrentBudget() {
		Budget currentBudget = getCurrentBudget();
		currentBudget.setArchived(true);
		currentBudget.setArchivedAt(LocalDateTime.now());
		budgetRepository.save(currentBudget);

		initializeNewMonthBudget();
	}

}