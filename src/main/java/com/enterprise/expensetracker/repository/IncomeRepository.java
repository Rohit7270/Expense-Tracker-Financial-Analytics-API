package com.enterprise.expensetracker.repository;

import com.enterprise.expensetracker.entity.Income;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {

    @Query("SELECT i FROM Income i WHERE i.user.id = :userId " +
            "AND (:categoryId IS NULL OR i.category.id = :categoryId) " +
            "AND (:startDate IS NULL OR i.date >= :startDate) " +
            "AND (:endDate IS NULL OR i.date <= :endDate) " +
            "AND (:search IS NULL OR LOWER(i.source) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Income> searchIncome(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("search") String search,
            Pageable pageable
    );

    Optional<Income> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.user.id = :userId AND i.date >= :startDate AND i.date <= :endDate")
    BigDecimal getTotalIncomeForPeriod(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
