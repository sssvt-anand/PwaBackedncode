package com.room.app.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExpenseDTO {
    private Long id;
    private String description;
    private LocalDate date;
    private BigDecimal amount;
    private BigDecimal clearedAmount;
    private BigDecimal remainingAmount;
    private boolean fullyCleared;
    private String memberName;
    private Long memberId;
    private LocalDateTime lastClearedAt;
    private BigDecimal lastClearedAmount;
    private String lastClearedByName;

    public ExpenseDTO() {}

    public ExpenseDTO(
            Long id,
            String description,
            LocalDate date,
            BigDecimal amount,
            BigDecimal clearedAmount,
            BigDecimal remainingAmount,
            boolean fullyCleared,
            String memberName,
            Long memberId,
            LocalDateTime lastClearedAt,
            BigDecimal lastClearedAmount,
            String lastClearedByName
    ) {
        this.id = id;
        this.description = description;
        this.date = date;
        this.amount = amount;
        this.clearedAmount = clearedAmount;
        this.remainingAmount = remainingAmount;
        this.fullyCleared = fullyCleared;
        this.memberName = memberName;
        this.memberId = memberId;
        this.lastClearedAt = lastClearedAt;
        this.lastClearedAmount = lastClearedAmount;
        this.lastClearedByName = lastClearedByName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getClearedAmount() {
        return clearedAmount;
    }

    public void setClearedAmount(BigDecimal clearedAmount) {
        this.clearedAmount = clearedAmount;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public boolean isFullyCleared() {
        return fullyCleared;
    }

    public void setFullyCleared(boolean fullyCleared) {
        this.fullyCleared = fullyCleared;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public LocalDateTime getLastClearedAt() {
        return lastClearedAt;
    }

    public void setLastClearedAt(LocalDateTime lastClearedAt) {
        this.lastClearedAt = lastClearedAt;
    }

    public BigDecimal getLastClearedAmount() {
        return lastClearedAmount;
    }

    public void setLastClearedAmount(BigDecimal lastClearedAmount) {
        this.lastClearedAmount = lastClearedAmount;
    }

    public String getLastClearedByName() {
        return lastClearedByName;
    }

    public void setLastClearedByName(String lastClearedByName) {
        this.lastClearedByName = lastClearedByName;
    }
}
