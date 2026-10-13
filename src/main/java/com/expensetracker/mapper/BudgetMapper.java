package com.expensetracker.mapper;

import com.expensetracker.dto.request.BudgetRequest;
import com.expensetracker.dto.response.BudgetResponse;
import com.expensetracker.entity.Budget;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Mapper for Budget Entity <-> DTO conversions.
 */
@Component
public class BudgetMapper {

    public BudgetResponse toResponse(Budget budget, BigDecimal spentAmount) {
        if (budget == null) {
            return null;
        }
        BigDecimal spent = spentAmount != null ? spentAmount : BigDecimal.ZERO;
        BigDecimal remaining = budget.getMonthlyLimit().subtract(spent);
        double percentage = budget.getMonthlyLimit().compareTo(BigDecimal.ZERO) > 0
                ? spent.divide(budget.getMonthlyLimit(), 4, RoundingMode.HALF_UP).doubleValue() * 100
                : 0;
        boolean exceeded = spent.compareTo(budget.getMonthlyLimit()) > 0;

        return BudgetResponse.builder()
                .id(budget.getId())
                .categoryId(budget.getCategory() != null ? budget.getCategory().getId() : null)
                .categoryName(budget.getCategory() != null ? budget.getCategory().getName() : null)
                .monthlyLimit(budget.getMonthlyLimit())
                .month(budget.getMonth())
                .year(budget.getYear())
                .spentAmount(spent)
                .remainingAmount(remaining)
                .percentageUsed(Math.round(percentage * 100.0) / 100.0)
                .exceeded(exceeded)
                .build();
    }

    public Budget toEntity(BudgetRequest request, User user, Category category) {
        if (request == null) {
            return null;
        }
        return Budget.builder()
                .user(user)
                .category(category)
                .monthlyLimit(request.getMonthlyLimit())
                .month(request.getMonth())
                .year(request.getYear())
                .build();
    }
}
