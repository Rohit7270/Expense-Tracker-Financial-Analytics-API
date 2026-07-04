package com.enterprise.expensetracker.service;

import com.enterprise.expensetracker.dto.CategoryDistributionDto;
import com.enterprise.expensetracker.dto.MonthlySummaryDto;
import com.enterprise.expensetracker.repository.ExpenseRepository;
import com.enterprise.expensetracker.repository.IncomeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    public List<CategoryDistributionDto> getCategoryDistribution(Long userId, Integer month, Integer year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        List<Object[]> rawData = expenseRepository.getCategoryDistribution(userId, startDate, endDate);
        BigDecimal totalSpent = expenseRepository.getTotalExpenseForPeriod(userId, startDate, endDate);

        if (totalSpent == null || totalSpent.compareTo(BigDecimal.ZERO) == 0) {
            return new ArrayList<>();
        }

        final BigDecimal finalTotalSpent = totalSpent;
        return rawData.stream().map(row -> {
            Long catId = ((Number) row[0]).longValue();
            String catName = (String) row[1];
            BigDecimal amount = (BigDecimal) row[2];
            
            double percentage = amount.multiply(new BigDecimal("100"))
                    .divide(finalTotalSpent, 2, RoundingMode.HALF_UP).doubleValue();

            return CategoryDistributionDto.builder()
                    .categoryId(catId)
                    .categoryName(catName)
                    .amount(amount)
                    .percentage(percentage)
                    .build();
        }).collect(Collectors.toList());
    }

    public List<CategoryDistributionDto> getHighestSpendingCategories(Long userId, Integer month, Integer year) {
        return getCategoryDistribution(userId, month, year).stream()
                .sorted((c1, c2) -> c2.getAmount().compareTo(c1.getAmount()))
                .collect(Collectors.toList());
    }

    public MonthlySummaryDto getMonthlySavingsReport(Long userId, Integer month, Integer year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        BigDecimal income = incomeRepository.getTotalIncomeForPeriod(userId, startDate, endDate);
        BigDecimal expense = expenseRepository.getTotalExpenseForPeriod(userId, startDate, endDate);

        BigDecimal totalIncome = income != null ? income : BigDecimal.ZERO;
        BigDecimal totalExpense = expense != null ? expense : BigDecimal.ZERO;
        BigDecimal savings = totalIncome.subtract(totalExpense);

        return MonthlySummaryDto.builder()
                .month(month)
                .year(year)
                .income(totalIncome)
                .expense(totalExpense)
                .savings(savings)
                .build();
    }

    public List<MonthlySummaryDto> getIncomeVsExpenseSummary(Long userId, int limitMonths) {
        List<MonthlySummaryDto> summaryList = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (int i = limitMonths - 1; i >= 0; i--) {
            LocalDate targetDate = now.minusMonths(i);
            int month = targetDate.getMonthValue();
            int year = targetDate.getYear();

            MonthlySummaryDto monthSummary = getMonthlySavingsReport(userId, month, year);
            summaryList.add(monthSummary);
        }

        return summaryList;
    }
}
