package com.room.app.entity;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity

@Table(name = "member", indexes = { @Index(name = "idx_member_name", columnList = "name"),
		@Index(name = "idx_member_active", columnList = "active") })
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String name;

	@Column(name = "monthly_budget", precision = 10, scale = 2)
	private BigDecimal monthlyBudget;
	
	@Column(name = "active")
	private boolean active;

	@OneToMany(mappedBy = "member")
	@JsonIgnore
	private List<Expense> expenses;
	@Column(unique = true)
	private String mobileNumber;
	@Column(name = "user_id", unique = true)
	private Long userId;

	@Column(name = "chat_id", unique = true)
	private Long chatId;

	@Column(name = "is_admin", nullable = false)
	private boolean admin = true;

	public Member() {
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getChatId() {
		return chatId;
	}

	public void setChatId(Long chatId) {
		this.chatId = chatId;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public BigDecimal getMonthlyBudget() {
		return monthlyBudget;
	}

	public void setMonthlyBudget(BigDecimal monthlyBudget) {
		this.monthlyBudget = monthlyBudget;
	}

	public BigDecimal getCurrentSpending() {
		// TODO Auto-generated method stubs
		return null;
	}

}
