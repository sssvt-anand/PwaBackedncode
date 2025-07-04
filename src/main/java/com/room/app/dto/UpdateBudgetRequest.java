package com.room.app.dto;

import java.math.BigDecimal;

public class UpdateBudgetRequest {
	private BigDecimal monthlyBudget;

	public BigDecimal getMonthlyBudget() {
		return monthlyBudget;
	}

	public void setMonthlyBudget(BigDecimal monthlyBudget) {
		this.monthlyBudget = monthlyBudget;
	}
	
}
