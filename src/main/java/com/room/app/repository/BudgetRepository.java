package com.room.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.room.app.dto.Budget;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    
    Optional<Budget> findTopByOrderByCreatedAtDesc();
    Optional<Budget> findByMonthYear(String monthYear);
}