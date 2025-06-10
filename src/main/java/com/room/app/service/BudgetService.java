package com.room.app.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
<<<<<<< HEAD
import java.util.stream.Collectors;
=======
>>>>>>> 28c67bb7aebff37f806529c6b69a90919a8d67b2

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.room.app.dto.Budget;
import com.room.app.dto.BudgetStatusResponse;
import com.room.app.dto.Member;
<<<<<<< HEAD
import com.room.app.dto.MemberBudgetDetail;
=======
>>>>>>> 28c67bb7aebff37f806529c6b69a90919a8d67b2
import com.room.app.exception.ResourceNotFoundException;
import com.room.app.repository.BudgetRepository;
import com.room.app.repository.MemberRepository;

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
	@Transactional(readOnly = true)
	public BudgetStatusResponse getMemberBudgetStatus(Long memberId) throws ResourceNotFoundException {
	    Member member = memberRepository.findById(memberId)
	            .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
	    
	    Budget currentBudget = getCurrentBudget();
	    
	    // Calculate member's share of the budget based on their contribution
	    BigDecimal memberRatio = member.getMonthlyBudget().divide(currentBudget.getTotalBudget(), 4, RoundingMode.HALF_UP);
	    BigDecimal memberRemaining = currentBudget.getRemainingBudget().multiply(memberRatio);
	    
	    return new BudgetStatusResponse(
	            member.getMonthlyBudget(),
	            memberRemaining,
	            currentBudget.getMonthYear(),
	            memberRemaining.compareTo(BigDecimal.ZERO) < 0,
	            memberRemaining.compareTo(BigDecimal.ZERO) < 0 ? memberRemaining.abs() : BigDecimal.ZERO
	    );
	}

	@Transactional
	public void recalculateTotalBudget() {
	    String currentMonthYear = YearMonth.now().toString();
	    Budget currentBudget = budgetRepository.findByMonthYear(currentMonthYear)
	            .orElseThrow(() -> new ResourceNotFoundException("Current budget not found"));
	    
	    List<Member> members = memberRepository.findAll();
	    BigDecimal newTotalBudget = members.stream()
	            .filter(m -> m.getMonthlyBudget() != null)
	            .map(Member::getMonthlyBudget)
	            .reduce(BigDecimal.ZERO, BigDecimal::add);
	    
	    // Calculate the difference and adjust remaining budget
	    BigDecimal difference = newTotalBudget.subtract(currentBudget.getTotalBudget());
	    currentBudget.setTotalBudget(newTotalBudget);
	    currentBudget.setRemainingBudget(currentBudget.getRemainingBudget().add(difference));
	    
	    budgetRepository.save(currentBudget);
	}
	@Transactional
	public Member updateMemberBudget(Long memberId, BigDecimal monthlyBudget) throws ResourceNotFoundException {
	    // Validate input
	    if (monthlyBudget == null) {
	        throw new IllegalArgumentException("Monthly budget cannot be null");
	    }
	    if (monthlyBudget.compareTo(BigDecimal.ZERO) < 0) {
	        throw new IllegalArgumentException("Monthly budget cannot be negative");
	    }

	    // Get the member
	    Member member = memberRepository.findById(memberId)
	            .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));

	    // Get the current budget
	    Budget currentBudget = budgetRepository.findTopByOrderByCreatedAtDesc()
	            .orElseThrow(() -> new ResourceNotFoundException("No budget available"));

	    // Calculate the difference this change will make
	    BigDecimal oldMemberBudget = member.getMonthlyBudget() != null ? member.getMonthlyBudget() : BigDecimal.ZERO;
	    BigDecimal budgetDifference = monthlyBudget.subtract(oldMemberBudget);

	    // Update the member's budget
	    member.setMonthlyBudget(monthlyBudget);
	    Member updatedMember = memberRepository.save(member);

	    // Update the total budget if we're in the current month
	    String currentMonthYear = YearMonth.now().toString();
	    if (currentBudget.getMonthYear().equals(currentMonthYear)) {
	        currentBudget.setTotalBudget(currentBudget.getTotalBudget().add(budgetDifference));
	        currentBudget.setRemainingBudget(currentBudget.getRemainingBudget().add(budgetDifference));
	        budgetRepository.save(currentBudget);
	    }

	    return updatedMember;
	}

	@Transactional(readOnly = true)
	public List<MemberBudgetDetail> getAllMembersWithBudgets() {
	    // Get current budget
	    Budget currentBudget = getCurrentBudget();
	    
	    // Calculate total used budget
	    BigDecimal totalUsedBudget = currentBudget.getTotalBudget().subtract(currentBudget.getRemainingBudget());
	    
	    // Get all members
	    List<Member> members = memberRepository.findAll();
	    
	    return members.stream()
	            .map(member -> {
	                BigDecimal monthlyBudget = member.getMonthlyBudget() != null ? 
	                        member.getMonthlyBudget() : BigDecimal.ZERO;
	                
	                // Calculate ratio of this member's contribution
	                BigDecimal ratio = currentBudget.getTotalBudget().compareTo(BigDecimal.ZERO) != 0 ?
	                        monthlyBudget.divide(currentBudget.getTotalBudget(), 4, RoundingMode.HALF_UP) :
	                        BigDecimal.ZERO;
	                
	                // Calculate used and remaining budget
	                BigDecimal usedBudget = totalUsedBudget.multiply(ratio);
	                BigDecimal remainingBudget = monthlyBudget.subtract(usedBudget);
	                boolean overBudget = remainingBudget.compareTo(BigDecimal.ZERO) < 0;
	                BigDecimal overBudgetAmount = overBudget ? remainingBudget.abs() : BigDecimal.ZERO;
	                
	                return new MemberBudgetDetail(
	                        member.getId(),
	                        member.getName(),
	                        monthlyBudget,
	                        usedBudget,
	                        remainingBudget,
	                        overBudget,
	                        overBudgetAmount
	                );
	            })
	            .collect(Collectors.toList());
	}
	

	
}