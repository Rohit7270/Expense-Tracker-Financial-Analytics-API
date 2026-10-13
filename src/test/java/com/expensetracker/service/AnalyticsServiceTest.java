package com.expensetracker.service;

import com.expensetracker.dto.response.DashboardSummaryResponse;
import com.expensetracker.dto.response.MonthlyReportResponse;
import com.expensetracker.entity.Account;
import com.expensetracker.entity.User;
import com.expensetracker.enums.TransactionType;
import com.expensetracker.mapper.TransactionMapper;
import com.expensetracker.repository.AccountRepository;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private SecurityUtils securityUtils;

    @Spy
    private TransactionMapper transactionMapper = new TransactionMapper();

    @InjectMocks
    private AnalyticsService analyticsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("user@example.com").build();
    }

    @Test
    @DisplayName("Get Dashboard Summary - Computes income, expenses, net savings, and account totals")
    void testGetDashboardSummary() {
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(transactionRepository.sumByUserAndTypeAndDateRange(eq(testUser), eq(TransactionType.INCOME), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("5000.00"));
        when(transactionRepository.sumByUserAndTypeAndDateRange(eq(testUser), eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("2000.00"));

        Account acc1 = Account.builder().id(1L).balance(new BigDecimal("3000.00")).build();
        Account acc2 = Account.builder().id(2L).balance(new BigDecimal("1500.00")).build();
        when(accountRepository.findByUser(testUser)).thenReturn(List.of(acc1, acc2));

        when(budgetRepository.findByUserAndMonthAndYear(eq(testUser), any(Integer.class), any(Integer.class)))
                .thenReturn(Collections.emptyList());
        when(transactionRepository.findTop5ByUserOrderByTransactionDateDescIdDesc(testUser))
                .thenReturn(Collections.emptyList());

        DashboardSummaryResponse summary = analyticsService.getDashboardSummary();

        assertNotNull(summary);
        assertEquals(new BigDecimal("5000.00"), summary.getTotalIncomeThisMonth());
        assertEquals(new BigDecimal("2000.00"), summary.getTotalExpenseThisMonth());
        assertEquals(new BigDecimal("3000.00"), summary.getNetBalanceThisMonth());
        assertEquals(new BigDecimal("4500.00"), summary.getTotalAccountsBalance());
        assertEquals(0, summary.getActiveBudgetAlerts());
    }

    @Test
    @DisplayName("Get Monthly Report - Aggregates category breakdown accurately")
    void testGetMonthlyReport() {
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(transactionRepository.sumByUserAndTypeAndDateRange(eq(testUser), eq(TransactionType.INCOME), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("4000.00"));
        when(transactionRepository.sumByUserAndTypeAndDateRange(eq(testUser), eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("1000.00"));

        List<Object[]> rows = List.of(
                new Object[]{"Groceries", new BigDecimal("600.00")},
                new Object[]{"Utilities", new BigDecimal("400.00")}
        );
        when(transactionRepository.sumExpensesGroupedByCategory(eq(testUser), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(rows);

        MonthlyReportResponse report = analyticsService.getMonthlyReport(9, 2026);

        assertNotNull(report);
        assertEquals(9, report.getMonth());
        assertEquals(2026, report.getYear());
        assertEquals(new BigDecimal("4000.00"), report.getTotalIncome());
        assertEquals(new BigDecimal("1000.00"), report.getTotalExpense());
        assertEquals(new BigDecimal("3000.00"), report.getNetSavings());
        assertEquals(2, report.getCategoryBreakdown().size());
        assertEquals("Groceries", report.getCategoryBreakdown().get(0).getCategoryName());
        assertEquals(60.0, report.getCategoryBreakdown().get(0).getPercentage());
        assertEquals("Utilities", report.getCategoryBreakdown().get(1).getCategoryName());
        assertEquals(40.0, report.getCategoryBreakdown().get(1).getPercentage());
    }
}
