package com.expensetracker.service;

import com.expensetracker.dto.response.CategorySpendingResponse;
import com.expensetracker.dto.response.DashboardSummaryResponse;
import com.expensetracker.dto.response.MonthlyReportResponse;
import com.expensetracker.dto.response.TransactionResponse;
import com.expensetracker.entity.Account;
import com.expensetracker.entity.User;
import com.expensetracker.enums.TransactionType;
import com.expensetracker.mapper.TransactionMapper;
import com.expensetracker.repository.AccountRepository;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final BudgetRepository budgetRepository;
    private final SecurityUtils securityUtils;
    private final TransactionMapper transactionMapper;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        User user = securityUtils.getCurrentUser();
        YearMonth currentMonth = YearMonth.now();
        LocalDate start = currentMonth.atDay(1);
        LocalDate end = currentMonth.atEndOfMonth();

        BigDecimal income = transactionRepository.sumByUserAndTypeAndDateRange(user, TransactionType.INCOME, start, end);
        BigDecimal expense = transactionRepository.sumByUserAndTypeAndDateRange(user, TransactionType.EXPENSE, start, end);

        BigDecimal totalAccountsBalance = accountRepository.findByUser(user).stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long budgetAlerts = budgetRepository.findByUserAndMonthAndYear(user, currentMonth.getMonthValue(), currentMonth.getYear())
                .stream()
                .filter(b -> {
                    BigDecimal spent = transactionRepository.sumExpenseByCategoryAndDateRange(
                            user, b.getCategory().getId(), start, end);
                    return spent.compareTo(b.getMonthlyLimit()) > 0;
                })
                .count();

        List<TransactionResponse> recent = transactionRepository.findTop5ByUserOrderByTransactionDateDescIdDesc(user)
                .stream()
                .map(transactionMapper::toResponse)
                .toList();

        return DashboardSummaryResponse.builder()
                .totalIncomeThisMonth(income)
                .totalExpenseThisMonth(expense)
                .netBalanceThisMonth(income.subtract(expense))
                .totalAccountsBalance(totalAccountsBalance)
                .activeBudgetAlerts(budgetAlerts)
                .recentTransactions(recent)
                .build();
    }

    @Transactional(readOnly = true)
    public MonthlyReportResponse getMonthlyReport(Integer month, Integer year) {
        User user = securityUtils.getCurrentUser();
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        BigDecimal income = transactionRepository.sumByUserAndTypeAndDateRange(user, TransactionType.INCOME, start, end);
        BigDecimal expense = transactionRepository.sumByUserAndTypeAndDateRange(user, TransactionType.EXPENSE, start, end);

        List<CategorySpendingResponse> breakdown = getCategoryBreakdown(user, start, end, expense);

        return MonthlyReportResponse.builder()
                .month(month)
                .year(year)
                .totalIncome(income)
                .totalExpense(expense)
                .netSavings(income.subtract(expense))
                .categoryBreakdown(breakdown)
                .build();
    }

    @Transactional(readOnly = true)
    public List<CategorySpendingResponse> getSpendingByCategory(LocalDate startDate, LocalDate endDate) {
        User user = securityUtils.getCurrentUser();
        BigDecimal totalExpense = transactionRepository.sumByUserAndTypeAndDateRange(user, TransactionType.EXPENSE, startDate, endDate);
        return getCategoryBreakdown(user, startDate, endDate, totalExpense);
    }

    private List<CategorySpendingResponse> getCategoryBreakdown(User user, LocalDate start, LocalDate end, BigDecimal totalExpense) {
        List<Object[]> rows = transactionRepository.sumExpensesGroupedByCategory(user, start, end);
        return rows.stream().map(row -> {
            String categoryName = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            double percentage = totalExpense.compareTo(BigDecimal.ZERO) > 0
                    ? amount.divide(totalExpense, 4, RoundingMode.HALF_UP).doubleValue() * 100
                    : 0;
            return CategorySpendingResponse.builder()
                    .categoryName(categoryName)
                    .totalAmount(amount)
                    .percentage(Math.round(percentage * 100.0) / 100.0)
                    .build();
        }).toList();
    }
}
