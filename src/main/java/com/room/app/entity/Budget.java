package com.room.app.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;

@Entity
@Table(name = "budgets")
@Builder
public class Budget {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "month_year", nullable = false, unique = true)
	private String monthYear; // Format: "YYYY-MM"

	@Column(name = "total_budget", nullable = false, precision = 10, scale = 2)
	private BigDecimal totalBudget;

	@Column(name = "remaining_budget", nullable = false, precision = 10, scale = 2)
	private BigDecimal remainingBudget;

	@CreationTimestamp
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(nullable = true)
	private Boolean archived = false;

	@Column(name = "archived_at")
	private LocalDateTime archivedAt;

	@Column(name = "is_deleted", columnDefinition = "CHAR(1)")
	private String deleted = "N";

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMonthYear() {
		return monthYear;
	}

	public void setMonthYear(String monthYear) {
		this.monthYear = monthYear;
	}

	public BigDecimal getTotalBudget() {
		return totalBudget;
	}

	public void setTotalBudget(BigDecimal totalBudget) {
		this.totalBudget = totalBudget;
	}

	public BigDecimal getRemainingBudget() {
		return remainingBudget;
	}

	public void setRemainingBudget(BigDecimal remainingBudget) {
		this.remainingBudget = remainingBudget;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Budget(Long id, String monthYear, BigDecimal totalBudget, BigDecimal remainingBudget,
			LocalDateTime createdAt) {
		super();
		this.id = id;
		this.monthYear = monthYear;
		this.totalBudget = totalBudget;
		this.remainingBudget = remainingBudget;
		this.createdAt = createdAt;
	}

	public Budget() {

	}

	public Boolean getArchived() {
		return archived;
	}

	public void setArchived(Boolean archived) {
		this.archived = archived;
	}

	public LocalDateTime getArchivedAt() {
		return archivedAt;
	}

	public void setArchivedAt(LocalDateTime archivedAt) {
		this.archivedAt = archivedAt;
	}

	public Budget(Long id, String monthYear, BigDecimal totalBudget, BigDecimal remainingBudget,
			LocalDateTime createdAt, Boolean archived, LocalDateTime archivedAt, String deleted,
			LocalDateTime deletedAt) {
		super();
		this.id = id;
		this.monthYear = monthYear;
		this.totalBudget = totalBudget;
		this.remainingBudget = remainingBudget;
		this.createdAt = createdAt;
		this.archived = archived;
		this.archivedAt = archivedAt;
		this.deleted = deleted;
		this.deletedAt = deletedAt;
	}

	public String getDeleted() {
		return deleted;
	}

	public void setDeleted(String deleted) {
		this.deleted = deleted;
	}

	public LocalDateTime getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(LocalDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}

}
