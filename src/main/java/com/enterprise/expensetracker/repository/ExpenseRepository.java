package com.enterprise.expensetracker.repository;

import com.enterprise.expensetracker.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId " +
            "AND (:categoryId IS NULL OR e.category.id = :categoryId) " +
            "AND (:paymentMethodId IS NULL OR e.paymentMethod.id = :paymentMethodId) " +
            "AND (:startDate IS NULL OR e.date >= :startDate) " +
            "AND (:endDate IS NULL OR e.date <= :endDate) " +
            "AND (:search IS NULL OR LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Expense> searchExpenses(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("paymentMethodId") Long paymentMethodId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("search") String search,
            Pageable pageable
    );

    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId AND e.date >= :startDate AND e.date <= :endDate")
    BigDecimal getTotalExpenseForPeriod(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId AND e.category.id = :categoryId AND e.date >= :startDate AND e.date <= :endDate")
    BigDecimal getTotalExpenseByCategoryForPeriod(@Param("userId") Long userId, @Param("categoryId") Long categoryId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT e.category.id, e.category.name, SUM(e.amount) FROM Expense e " +
            "WHERE e.user.id = :userId AND e.date >= :startDate AND e.date <= :endDate " +
            "GROUP BY e.category.id, e.category.name")
    List<Object[]> getCategoryDistribution(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
