package com.room.app.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.room.app.dto.ExpenseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.room.app.dto.MemberBalanceSummary;
import com.room.app.entity.Expense;
import com.room.app.entity.User;

import jakarta.transaction.Transactional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @Query("SELECT e FROM Expense e WHERE e.isDeleted = 'N'")
    List<Expense> findAllActive();

    @Query("""
                SELECT new com.room.app.dto.ExpenseDTO(
                    e.id,
                    e.description,
                    e.date,
                    e.amount,
                    e.clearedAmount,
                    e.remainingAmount,
                    CASE WHEN e.clearedAmount >= e.amount THEN true ELSE false END,
                    m.name,
                    m.id,
                    e.lastClearedAt,
                    e.lastClearedAmount,
                    lcb.name
                )
                FROM Expense e
                LEFT JOIN e.member m
                LEFT JOIN e.lastClearedBy lcb
                WHERE e.isDeleted = 'N'
            """)
    List<ExpenseDTO> findAllActiveUi();


    Optional<Expense> findById(Long id);

    @Query("SELECT e FROM Expense e WHERE e.isDeleted = 'N' AND e.id = :id")
    Optional<Expense> findActiveById(@Param("id") Long id);

    List<Expense> findByIsDeleted(String isDeleted);

    @Query("SELECT e.member, SUM(e.amount) FROM Expense e GROUP BY e.member")
    List<Object[]> getTotalExpensesByMember();

    @Query("SELECT e.member, SUM(e.amount) as total FROM Expense e GROUP BY e.member ORDER BY total DESC")
    List<Object[]> getTopSpenderWithAmount();

    List<Expense> findByMemberId(Long memberId);

    List<Expense> findByMemberIsNull();

    Optional<Expense> findByMessageId(Integer messageId);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.member.name = :member AND e.cleared = true")
    Optional<BigDecimal> findClearedAmountByMember(@Param("member") String member);

    List<Expense> findByClearedTrue();

    @Query("SELECT e FROM Expense e WHERE e.date BETWEEN :startDate AND :endDate")
    List<Expense> findByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT e FROM Expense e WHERE e.member.name ILIKE %:name% AND e.isDeleted = 'N'")
    List<Expense> findByMemberNameContainingIgnoreCase(@Param("name") String name);

    @Query("SELECT e FROM Expense e WHERE e.active = true AND (e.isDeleted IS NULL OR e.isDeleted != 'Y')")
    List<Expense> findByActiveTrueAndIsDeletedNot(String deletedFlag);

    List<Expense> findByActiveTrue();

    @Modifying
    @Query("UPDATE Expense e SET e.isDeleted = 'Y', e.deletedAt = :now, e.deletedBy.id = :userId WHERE e.isDeleted = 'N'")
    void softDeleteAllExpenses(@Param("now") LocalDateTime now, @Param("userId") Long userId);

    @Query("SELECT e.member.name, SUM(e.clearedAmount) FROM Expense e WHERE e.clearedAmount IS NOT NULL " + "GROUP BY e.member.name")
    List<Object[]> getClearedSummaryByMember();

    @Query("SELECT e.member.name, SUM(e.amount) FROM Expense e WHERE e.member IS NOT NULL GROUP BY e.member.name")
    List<Object[]> getExpenseSummaryByMember();

    @Query("SELECT e.member.name as memberName, SUM(e.amount) as totalAmount," + "COALESCE(SUM(e.clearedAmount), 0) as clearedAmount FROM Expense e WHERE e.member IS NOT NULL " + "GROUP BY e.member.name")
    List<MemberBalanceSummary> getMemberBalanceSummary();

}
