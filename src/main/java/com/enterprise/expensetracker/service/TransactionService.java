package com.enterprise.expensetracker.service;

import com.enterprise.expensetracker.entity.Expense;
import com.enterprise.expensetracker.entity.Income;
import com.enterprise.expensetracker.entity.Transaction;
import com.enterprise.expensetracker.entity.TransactionType;
import com.enterprise.expensetracker.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional
    public void syncExpense(Expense expense) {
        Transaction transaction = transactionRepository.findByTypeAndReferenceId(TransactionType.EXPENSE, expense.getId())
                .orElse(Transaction.builder()
                        .type(TransactionType.EXPENSE)
                        .referenceId(expense.getId())
                        .user(expense.getUser())
                        .build());

        transaction.setAmount(expense.getAmount());
        transaction.setDescription(expense.getDescription());
        transaction.setDate(expense.getDate());
        transactionRepository.save(transaction);
    }

    @Transactional
    public void syncIncome(Income income) {
        Transaction transaction = transactionRepository.findByTypeAndReferenceId(TransactionType.INCOME, income.getId())
                .orElse(Transaction.builder()
                        .type(TransactionType.INCOME)
                        .referenceId(income.getId())
                        .user(income.getUser())
                        .build());

        transaction.setAmount(income.getAmount());
        transaction.setDescription(income.getSource());
        transaction.setDate(income.getDate());
        transactionRepository.save(transaction);
    }

    @Transactional
    public void deleteExpenseTransaction(Long expenseId) {
        transactionRepository.deleteByTypeAndReferenceId(TransactionType.EXPENSE, expenseId);
    }

    @Transactional
    public void deleteIncomeTransaction(Long incomeId) {
        transactionRepository.deleteByTypeAndReferenceId(TransactionType.INCOME, incomeId);
    }
}
