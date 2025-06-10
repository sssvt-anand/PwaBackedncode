package com.room.app.dto;


import java.math.BigDecimal;

public class MemberBudgetRequest {
    private Long memberId;
    private BigDecimal monthlyBudget;

    // Constructors, getters, and setters
    public MemberBudgetRequest() {
    }

    public MemberBudgetRequest(Long memberId, BigDecimal monthlyBudget) {
        this.memberId = memberId;
        this.monthlyBudget = monthlyBudget;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public BigDecimal getMonthlyBudget() {
        return monthlyBudget;
    }

    public void setMonthlyBudget(BigDecimal monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }
}
