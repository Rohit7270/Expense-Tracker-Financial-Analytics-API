package com.enterprise.expensetracker.service;

import com.enterprise.expensetracker.dto.DashboardResponse;
import com.enterprise.expensetracker.dto.TransactionDto;
import com.enterprise.expensetracker.entity.Budget;
import com.enterprise.expensetracker.entity.Transaction;
import com.enterprise.expensetracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public DashboardResponse getDashboardStats(Long userId) {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();

        LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());

        // 1. Current Month Total Income & Expenses
        BigDecimal monthlyIncomeRaw = incomeRepository.getTotalIncomeForPeriod(userId, startOfMonth, endOfMonth);
        BigDecimal monthlyExpenseRaw = expenseRepository.getTotalExpenseForPeriod(userId, startOfMonth, endOfMonth);

        BigDecimal monthlyIncome = monthlyIncomeRaw != null ? monthlyIncomeRaw : BigDecimal.ZERO;
        BigDecimal monthlyExpense = monthlyExpenseRaw != null ? monthlyExpenseRaw : BigDecimal.ZERO;

        // 2. All-Time Balance
        LocalDate minDate = LocalDate.of(2000, 1, 1);
        LocalDate maxDate = LocalDate.of(2100, 12, 31);
        BigDecimal allTimeIncomeRaw = incomeRepository.getTotalIncomeForPeriod(userId, minDate, maxDate);
        BigDecimal allTimeExpenseRaw = expenseRepository.getTotalExpenseForPeriod(userId, minDate, maxDate);

        BigDecimal allTimeIncome = allTimeIncomeRaw != null ? allTimeIncomeRaw : BigDecimal.ZERO;
        BigDecimal allTimeExpense = allTimeExpenseRaw != null ? allTimeExpenseRaw : BigDecimal.ZERO;
        BigDecimal currentBalance = allTimeIncome.subtract(allTimeExpense);

        // 3. Monthly Budget & Utilization
        Optional<Budget> budgetOpt = budgetRepository.findByUserIdAndMonthAndYearAndCategoryIsNull(userId, month, year);
        BigDecimal budgetLimit = BigDecimal.ZERO;
        double utilization = 0.0;

        if (budgetOpt.isPresent()) {
            budgetLimit = budgetOpt.get().getAmount();
            if (budgetLimit.compareTo(BigDecimal.ZERO) > 0) {
                utilization = monthlyExpense.multiply(new BigDecimal("100"))
                        .divide(budgetLimit, 2, RoundingMode.HALF_UP).doubleValue();
            }
        }

        // 4. 5 Recent Transactions
        List<Transaction> transactions = transactionRepository.findByUserIdOrderByDateDesc(userId, PageRequest.of(0, 5));
        List<TransactionDto> recentTx = transactions.stream().map(t -> TransactionDto.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .type(t.getType())
                .description(t.getDescription())
                .date(t.getDate())
                .build()
        ).collect(Collectors.toList());

        return DashboardResponse.builder()
                .totalIncome(monthlyIncome)
                .totalExpenses(monthlyExpense)
                .currentBalance(currentBalance)
                .monthlyBudget(budgetLimit)
                .budgetUtilization(utilization)
                .recentTransactions(recentTx)
                .build();
    }
}
