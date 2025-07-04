package com.room.app.dto;



import java.math.BigDecimal;

public class InitializeBudgetRequest {
    private BigDecimal budgetAmount;

    public BigDecimal getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(BigDecimal budgetAmount) {
        this.budgetAmount = budgetAmount;
    }
}
