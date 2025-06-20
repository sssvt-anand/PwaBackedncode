package com.room.app.dto;

import java.math.BigDecimal;

public class MemberBudgetDetail {
	private Long memberId;
	private String memberName;
	private BigDecimal monthlyBudget;
	private BigDecimal usedBudget;
	private BigDecimal remainingBudget;
	private boolean overBudget;
	private BigDecimal overBudgetAmount;

	public MemberBudgetDetail(Long memberId, String memberName, BigDecimal monthlyBudget, BigDecimal usedBudget,
			BigDecimal remainingBudget, boolean overBudget, BigDecimal overBudgetAmount) {
		this.memberId = memberId;
		this.memberName = memberName;
		this.monthlyBudget = monthlyBudget;
		this.usedBudget = usedBudget;
		this.remainingBudget = remainingBudget;
		this.overBudget = overBudget;
		this.overBudgetAmount = overBudgetAmount;
	}
}