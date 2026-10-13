package com.expensetracker.repository;

import com.expensetracker.entity.Budget;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUser(User user);
    List<Budget> findByUserAndMonthAndYear(User user, Integer month, Integer year);
    Optional<Budget> findByIdAndUser(Long id, User user);
    Optional<Budget> findByUserAndCategoryIdAndMonthAndYear(User user, Long categoryId, Integer month, Integer year);
    boolean existsByCategory(Category category);
}
