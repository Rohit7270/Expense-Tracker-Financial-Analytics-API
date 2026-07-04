package com.enterprise.expensetracker.repository;

import com.enterprise.expensetracker.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserId(Long userId);

    List<Budget> findByUserIdAndMonthAndYear(Long userId, Integer month, Integer year);

    Optional<Budget> findByUserIdAndCategoryIdAndMonthAndYear(Long userId, Long categoryId, Integer month, Integer year);

    Optional<Budget> findByUserIdAndMonthAndYearAndCategoryIsNull(Long userId, Integer month, Integer year);

    Optional<Budget> findByIdAndUserId(Long id, Long userId);
}
