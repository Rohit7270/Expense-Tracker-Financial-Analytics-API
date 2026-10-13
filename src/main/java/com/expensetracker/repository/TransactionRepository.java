package com.expensetracker.repository;

import com.expensetracker.entity.Account;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.User;
import com.expensetracker.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdAndUser(Long id, User user);

    boolean existsByAccount(Account account);

    boolean existsByCategory(Category category);

    @Query("SELECT t FROM Transaction t WHERE t.user = :user " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
           "AND (:accountId IS NULL OR t.account.id = :accountId) " +
           "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR t.transactionDate <= :endDate) " +
           "ORDER BY t.transactionDate DESC, t.id DESC")
    List<Transaction> search(@Param("user") User user,
                              @Param("type") TransactionType type,
                              @Param("categoryId") Long categoryId,
                              @Param("accountId") Long accountId,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate);

    List<Transaction> findTop5ByUserOrderByTransactionDateDescIdDesc(User user);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.type = :type " +
           "AND t.transactionDate BETWEEN :start AND :end")
    BigDecimal sumByUserAndTypeAndDateRange(@Param("user") User user,
                                             @Param("type") TransactionType type,
                                             @Param("start") LocalDate start,
                                             @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.category.id = :categoryId " +
           "AND t.type = 'EXPENSE' AND t.transactionDate BETWEEN :start AND :end")
    BigDecimal sumExpenseByCategoryAndDateRange(@Param("user") User user,
                                                 @Param("categoryId") Long categoryId,
                                                 @Param("start") LocalDate start,
                                                 @Param("end") LocalDate end);

    @Query("SELECT t.category.name AS categoryName, COALESCE(SUM(t.amount), 0) AS total " +
           "FROM Transaction t WHERE t.user = :user AND t.type = 'EXPENSE' " +
           "AND t.transactionDate BETWEEN :start AND :end GROUP BY t.category.name ORDER BY total DESC")
    List<Object[]> sumExpensesGroupedByCategory(@Param("user") User user,
                                                 @Param("start") LocalDate start,
                                                 @Param("end") LocalDate end);
}
