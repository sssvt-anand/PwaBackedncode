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

	public Long getMemberId() {
		return memberId;
	}

	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}

	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	public BigDecimal getMonthlyBudget() {
		return monthlyBudget;
	}

	public void setMonthlyBudget(BigDecimal monthlyBudget) {
		this.monthlyBudget = monthlyBudget;
	}

	public BigDecimal getUsedBudget() {
		return usedBudget;
	}

	public void setUsedBudget(BigDecimal usedBudget) {
		this.usedBudget = usedBudget;
	}

	public BigDecimal getRemainingBudget() {
		return remainingBudget;
	}

	public void setRemainingBudget(BigDecimal remainingBudget) {
		this.remainingBudget = remainingBudget;
	}

	public boolean isOverBudget() {
		return overBudget;
	}

	public void setOverBudget(boolean overBudget) {
		this.overBudget = overBudget;
	}

	public BigDecimal getOverBudgetAmount() {
		return overBudgetAmount;
	}

	public void setOverBudgetAmount(BigDecimal overBudgetAmount) {
		this.overBudgetAmount = overBudgetAmount;
	}
}