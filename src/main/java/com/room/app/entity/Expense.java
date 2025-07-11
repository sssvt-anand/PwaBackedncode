package com.room.app.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(indexes = {
		
		@Index(name = "idx_expense_is_deleted", columnList = "is_deleted"),
		@Index(name = "idx_expense_active", columnList = "active"),
		@Index(name = "idx_expense_cleared", columnList = "cleared"),

		// Date-related indexes
		@Index(name = "idx_expense_date", columnList = "date"), // Single column
		@Index(name = "idx_expense_date_status", columnList = "date, is_deleted, active"), // Composite

		// Member-related indexes
		@Index(name = "idx_expense_member_id", columnList = "member_id"),
		@Index(name = "idx_expense_member_status", columnList = "member_id, is_deleted, active"),

		// Special fields
		@Index(name = "idx_expense_message_id", columnList = "message_id", unique = true),

		// Payment-related indexes
		@Index(name = "idx_expense_cleared_amount", columnList = "cleared_amount"),
		@Index(name = "idx_expense_amount_status", columnList = "amount, is_deleted") })
public class Expense {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String description;

	@Column(name = "is_deleted", columnDefinition = "CHAR(1)")
	private String isDeleted = "N";

	@ManyToOne
	@JoinColumn(name = "deleted_by")
	private User deletedBy;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Column(name = "date", columnDefinition = "TIMESTAMP")
	private LocalDate date;

	@Column(name = "created_at", nullable = false, updatable = false)
	@CreationTimestamp
	private LocalDateTime createdAt;

	@PrePersist
	protected void initializeEntity() {
		this.createdAt = LocalDateTime.now();

		// Initialize amounts
		if (this.remainingAmount == null) {
			this.remainingAmount = this.amount;
		}
		if (this.clearedAmount == null) {
			this.clearedAmount = BigDecimal.ZERO;
		}
		if (this.lastClearedAmount == null) {
			this.lastClearedAmount = BigDecimal.ZERO;
		}
		if (this.isDeleted == null) {
			this.isDeleted = "N";
		}
		if (this.cleared == null) {
			this.cleared = false;
		}
	}

	private BigDecimal amount;

	@Column(name = "message_id")
	private Integer messageId;

	@Column(name = "cleared_amount")
	private BigDecimal clearedAmount = BigDecimal.ZERO;

	@Column(name = "cleared")
	private Boolean cleared = false;

	@ManyToOne
	@JoinColumn(name = "member_id")
	@JsonIgnoreProperties({ "paymentHistories", "expenses" })
	private Member member;

	@ManyToOne
	@JoinColumn(name = "cleared_by_member_id")
	private Member clearedBy;

	private LocalDateTime clearedAt;
	@Column(name = "remaining_amount", precision = 10, scale = 2)
	private BigDecimal remainingAmount;

	@OneToMany(mappedBy = "expense")
	@JsonIgnore
	private List<PaymentHistory> paymentHistories;

	@ManyToOne
	@JoinColumn(name = "last_cleared_by_member_id")
	private Member lastClearedBy;

	@Column(name = "last_cleared_at")
	private LocalDateTime lastClearedAt;

	@Column(name = "last_cleared_amount", precision = 10, scale = 2)
	private BigDecimal lastClearedAmount = BigDecimal.ZERO;

	public String getFormattedCreatedAt() {
		if (this.createdAt == null) {
			return null;
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, hh:mm a");
		return this.createdAt.format(formatter);
	}

	@Column(nullable = true) // Make it nullable initially
	private Boolean active;

	public Member getLastClearedBy() {
		return lastClearedBy;
	}

	public void setLastClearedBy(Member lastClearedBy) {
		this.lastClearedBy = lastClearedBy;
	}

	public LocalDateTime getLastClearedAt() {
		return lastClearedAt;
	}

	public void setLastClearedAt(LocalDateTime lastClearedAt) {
		this.lastClearedAt = lastClearedAt;
	}

	public BigDecimal getRemainingAmount() {
		return remainingAmount;
	}

	public void setRemainingAmount(BigDecimal remainingAmount) {
		this.remainingAmount = remainingAmount;
	}

	public Expense() {
	}

	public Expense(Long id, String description, LocalDate date, BigDecimal amount, Member member) {
		this.id = id;
		this.description = description;
		this.date = date;
		this.amount = amount;
		this.member = member;
		this.clearedAmount = BigDecimal.ZERO; // Default cleared amount
		this.cleared = false; // Default not cleared
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

	public Integer getMessageId() {
		return messageId;
	}

	public void setMessageId(Integer messageId) {
		this.messageId = messageId;
	}

	public BigDecimal getClearedAmount() {
		return clearedAmount != null ? clearedAmount : BigDecimal.ZERO;
	}

	public void setClearedAmount(BigDecimal clearedAmount) {
		this.clearedAmount = clearedAmount;
	}

	public Boolean getCleared() {
		return cleared;
	}

	public void setCleared(Boolean cleared) {
		this.cleared = cleared;
	}

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public Member getClearedBy() {
		return clearedBy;
	}

	public void setClearedBy(Member clearedBy) {
		this.clearedBy = clearedBy;
	}

	public LocalDateTime getClearedAt() {
		return clearedAt;
	}

	public void setClearedAt(LocalDateTime clearedAt) {
		this.clearedAt = clearedAt;
	}

	public boolean isCleared() {
		return cleared;
	}

	public String getMemberName() {
		if (member == null) {
			throw new IllegalStateException("Expense " + id + " has no associated member");
		}
		return member.getName();
	}

	public String getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(String isDeleted) {
		this.isDeleted = isDeleted;
	}

	public User getDeletedBy() {
		return deletedBy;
	}

	public void setDeletedBy(User deletedBy) {
		this.deletedBy = deletedBy;
	}

	public LocalDateTime getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(LocalDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}

	public BigDecimal getLastClearedAmount() {
		return lastClearedAmount;
	}

	public void setLastClearedAmount(BigDecimal lastClearedAmount) {
		this.lastClearedAmount = lastClearedAmount;
	}

	public List<PaymentHistory> getPaymentHistories() {
		return paymentHistories;
	}

	public void setPaymentHistories(List<PaymentHistory> paymentHistories) {
		this.paymentHistories = paymentHistories;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

}