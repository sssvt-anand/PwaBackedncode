package com.room.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

import com.room.app.entity.Budget;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetStatusResponse {
	private BigDecimal totalBudget;
	private BigDecimal remainingBudget;
	private String monthYear;
	private boolean isOverBudget;
	private BigDecimal overBudgetAmount;

	public BudgetStatusResponse(Budget budget) {
		this.totalBudget = budget.getTotalBudget();
		this.remainingBudget = budget.getRemainingBudget();
		this.monthYear = budget.getMonthYear();
		this.isOverBudget = budget.getRemainingBudget().compareTo(BigDecimal.ZERO) < 0;
		this.overBudgetAmount = this.isOverBudget ? budget.getRemainingBudget().abs() : BigDecimal.ZERO;
	}

	public BudgetStatusResponse(BigDecimal monthlyBudget, BigDecimal memberRemaining, String monthYear2, boolean b,
			Object object) {
		// TODO Auto-generated constructor stub
	}

}