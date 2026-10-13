package com.expensetracker.repository;

import com.expensetracker.entity.Account;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.RecurringTransaction;
import com.expensetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {
    List<RecurringTransaction> findByUser(User user);
    Optional<RecurringTransaction> findByIdAndUser(Long id, User user);
    List<RecurringTransaction> findByActiveTrueAndNextDueDateLessThanEqual(LocalDate date);
    boolean existsByAccount(Account account);
    boolean existsByCategory(Category category);
}
