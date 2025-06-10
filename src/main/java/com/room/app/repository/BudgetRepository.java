package com.room.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.room.app.dto.Budget;

import jakarta.persistence.LockModeType;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

	Optional<Budget> findTopByOrderByCreatedAtDesc();

	@Query("SELECT b FROM Budget b ORDER BY b.createdAt DESC LIMIT 1")
	Budget findCurrentBudget();

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT b FROM Budget b WHERE b.id = :id")

	Optional<Budget> findByIdWithLock(Long id);

	@Query("SELECT b FROM Budget b WHERE b.monthYear = :monthYear")
	Optional<Budget> findByMonthYear(String monthYear);
}