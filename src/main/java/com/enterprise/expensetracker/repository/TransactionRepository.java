package com.enterprise.expensetracker.repository;

import com.enterprise.expensetracker.entity.Transaction;
import com.enterprise.expensetracker.entity.TransactionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdOrderByDateDesc(Long userId, Pageable pageable);
    void deleteByTypeAndReferenceId(TransactionType type, Long referenceId);
    Optional<Transaction> findByTypeAndReferenceId(TransactionType type, Long referenceId);
}
