package com.room.app.dto;

import java.math.BigDecimal;

public interface MemberBalanceSummary {
    String getMemberName();
    BigDecimal getTotalAmount();
    BigDecimal getClearedAmount();
    default BigDecimal getRemainingAmount() {
        return getTotalAmount().subtract(getClearedAmount());
    }
}
