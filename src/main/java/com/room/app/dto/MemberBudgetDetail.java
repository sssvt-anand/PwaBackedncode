package com.room.app.dto;


import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberBudgetDetail {
    private Long memberId;
    private String memberName;
    private BigDecimal monthlyBudget;
    private BigDecimal usedBudget;
    private BigDecimal remainingBudget;
    private boolean overBudget;
    private BigDecimal overBudgetAmount;
}
