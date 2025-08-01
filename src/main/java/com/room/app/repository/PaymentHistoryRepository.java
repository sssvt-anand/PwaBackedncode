package com.room.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.room.app.entity.PaymentHistory;

@Repository
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
    List<PaymentHistory> findByExpenseIdOrderByTimestampDesc(Long expenseId);

	List<PaymentHistory> findByExpenseId(Long expenseId);
}
