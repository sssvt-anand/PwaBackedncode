package com.room.app.dto;


import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor

public class MemberBudgetDetail {
    public MemberBudgetDetail(Long id, String name, BigDecimal monthlyBudget2, BigDecimal usedBudget2,
			BigDecimal remainingBudget2, boolean overBudget2, BigDecimal overBudgetAmount2) {
		// TODO Auto-generated constructor stub
	}
	private Long memberId;
    private String memberName;
    private BigDecimal monthlyBudget;
    private BigDecimal usedBudget;
    private BigDecimal remainingBudget;
    private boolean overBudget;
    private BigDecimal overBudgetAmount;
}
