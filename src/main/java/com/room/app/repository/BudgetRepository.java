package com.room.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.room.app.entity.Budget;

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

	@Query("SELECT b FROM Budget b WHERE b.archived = false ORDER BY b.createdAt DESC")
	Optional<Budget> findCurrentActiveBudget();

	@Query("SELECT b FROM Budget b WHERE b.archived = true ORDER BY b.archivedAt DESC")
	List<Budget> findAllArchivedBudgets();

	@Query("SELECT b FROM Budget b WHERE b.deleted = 'N'")
	List<Budget> findAllActive();

	boolean existsByMonthYearAndDeleted(String currentMonthYear, String string);
}