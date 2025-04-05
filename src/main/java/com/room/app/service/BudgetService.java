package com.room.app.service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.time.YearMonth;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.room.app.dto.Budget;
import com.room.app.dto.Member;
import com.room.app.exception.InsufficientBudgetException;
import com.room.app.exception.ResourceNotFoundException;
import com.room.app.repository.BudgetRepository;
import com.room.app.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service

public class BudgetService {
	private final BudgetRepository budgetRepository;
	private final MemberRepository memberRepository;

	public BudgetService(BudgetRepository budgetRepository, MemberRepository memberRepository) {
		this.budgetRepository = budgetRepository;
		this.memberRepository = memberRepository;
	}

	@Scheduled(cron = "0 0 0 1 * ?") // Runs on 1st of each month at midnight
	@Transactional
	public void initializeNewMonthBudget() {
	    String currentMonthYear = YearMonth.now().toString();

	    if (budgetRepository.findByMonthYear(currentMonthYear).isEmpty()) {
	        List<Member> members = memberRepository.findAll();
	        BigDecimal totalBudget = members.stream()
	                .filter(m -> m.getMonthlyBudget() != null)
	                .map(Member::getMonthlyBudget)
	                .reduce(BigDecimal.ZERO, BigDecimal::add);

	        Budget newBudget = new Budget();
	        newBudget.setMonthYear(currentMonthYear);
	        newBudget.setTotalBudget(totalBudget);
	        newBudget.setRemainingBudget(totalBudget);
	        budgetRepository.save(newBudget);

	        budgetRepository.save(newBudget);
	    }
	}

	@Transactional(readOnly = true)
	public Budget getCurrentBudget() {
		return budgetRepository.findTopByOrderByCreatedAtDesc()
				.orElseThrow(() -> new ResourceNotFoundException("No budget available"));
	}

	@Transactional
	public void deductFromBudget(BigDecimal amount) {
		Budget currentBudget = getCurrentBudget();

		currentBudget.setRemainingBudget(currentBudget.getRemainingBudget().subtract(amount));
		budgetRepository.save(currentBudget);
	}

	@Transactional
	public void refundToBudget(BigDecimal amount) {
		Budget currentBudget = getCurrentBudget();
		currentBudget.setRemainingBudget(currentBudget.getRemainingBudget().add(amount));
		budgetRepository.save(currentBudget);
	}
}