package com.room.app.dto;

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
public class Budget{

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

}
