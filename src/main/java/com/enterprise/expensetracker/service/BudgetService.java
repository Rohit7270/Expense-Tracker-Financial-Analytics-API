package com.enterprise.expensetracker.service;

import com.enterprise.expensetracker.dto.CategoryDto;
import com.enterprise.expensetracker.dto.BudgetRequest;
import com.enterprise.expensetracker.dto.BudgetResponse;
import com.enterprise.expensetracker.dto.BudgetStatusResponse;
import com.enterprise.expensetracker.entity.Budget;
import com.enterprise.expensetracker.entity.Category;
import com.enterprise.expensetracker.entity.User;
import com.enterprise.expensetracker.exception.BadRequestException;
import com.enterprise.expensetracker.exception.ResourceNotFoundException;
import com.enterprise.expensetracker.repository.BudgetRepository;
import com.enterprise.expensetracker.repository.CategoryRepository;
import com.enterprise.expensetracker.repository.ExpenseRepository;
import com.enterprise.expensetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ActivityLogService activityLogService;

    public List<BudgetResponse> getBudgetsForUser(Long userId) {
        return budgetRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BudgetResponse createBudget(Long userId, BudgetRequest request) {
        // Validate if budget already exists for the category or overall
        Optional<Budget> existing;
        if (request.getCategoryId() != null) {
            existing = budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                    userId, request.getCategoryId(), request.getMonth(), request.getYear());
        } else {
            existing = budgetRepository.findByUserIdAndMonthAndYearAndCategoryIsNull(
                    userId, request.getMonth(), request.getYear());
        }

        if (existing.isPresent()) {
            throw new BadRequestException("Budget already configured for this timeframe and category!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }

        Budget budget = Budget.builder()
                .amount(request.getAmount())
                .month(request.getMonth())
                .year(request.getYear())
                .category(category)
                .user(user)
                .build();

        Budget saved = budgetRepository.save(budget);
        
        String desc = category != null ? "Category " + category.getName() : "Overall";
        activityLogService.log("BUDGET_CREATED", "Set " + desc + " monthly budget of: " + budget.getAmount(), user);

        return mapToResponse(saved);
    }

    @Transactional
    public BudgetResponse updateBudget(Long id, Long userId, BudgetRequest request) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }

        budget.setAmount(request.getAmount());
        budget.setMonth(request.getMonth());
        budget.setYear(request.getYear());
        budget.setCategory(category);

        Budget saved = budgetRepository.save(budget);
        activityLogService.log("BUDGET_UPDATED", "Updated budget limit to: " + budget.getAmount(), budget.getUser());

        return mapToResponse(saved);
    }

    @Transactional
    public void deleteBudget(Long id, Long userId) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        budgetRepository.delete(budget);
        activityLogService.log("BUDGET_DELETED", "Removed budget for month: " + budget.getMonth() + "/" + budget.getYear(), budget.getUser());
    }

    public List<BudgetStatusResponse> getBudgetStatus(Long userId, Integer month, Integer year) {
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);
        List<BudgetStatusResponse> statusList = new ArrayList<>();

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        for (Budget budget : budgets) {
            BigDecimal spent;
            if (budget.getCategory() != null) {
                spent = expenseRepository.getTotalExpenseByCategoryForPeriod(userId, budget.getCategory().getId(), startDate, endDate);
            } else {
                spent = expenseRepository.getTotalExpenseForPeriod(userId, startDate, endDate);
            }

            if (spent == null) {
                spent = BigDecimal.ZERO;
            }

            BigDecimal remaining = budget.getAmount().subtract(spent);
            double utilization = 0.0;
            if (budget.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                utilization = spent.multiply(new BigDecimal("100"))
                        .divide(budget.getAmount(), 2, RoundingMode.HALF_UP).doubleValue();
            }

            String alert = null;
            if (utilization >= 100.0) {
                alert = "Critical: Budget Exceeded!";
            } else if (utilization >= 90.0) {
                alert = "Warning: Budget utilization is above 90%!";
            }

            statusList.add(BudgetStatusResponse.builder()
                    .budgetId(budget.getId())
                    .amount(budget.getAmount())
                    .spent(spent)
                    .remaining(remaining)
                    .utilizationPercentage(utilization)
                    .category(budget.getCategory() != null ? CategoryDto.builder()
                            .id(budget.getCategory().getId())
                            .name(budget.getCategory().getName())
                            .type(budget.getCategory().getType())
                            .build() : null)
                    .alertMessage(alert)
                    .build());
        }

        return statusList;
    }

    @Transactional
    public void checkBudgetAndNotify(User user, Long categoryId, int month, int year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        // 1. Check Category Specific Budget
        Optional<Budget> catBudgetOpt = budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(user.getId(), categoryId, month, year);
        if (catBudgetOpt.isPresent()) {
            Budget budget = catBudgetOpt.get();
            BigDecimal spent = expenseRepository.getTotalExpenseByCategoryForPeriod(user.getId(), categoryId, startDate, endDate);
            if (spent != null) {
                double utilization = spent.multiply(new BigDecimal("100"))
                        .divide(budget.getAmount(), 2, RoundingMode.HALF_UP).doubleValue();

                if (utilization >= 100.0) {
                    activityLogService.log("BUDGET_EXCEEDED", "Category '" + budget.getCategory().getName() + "' budget of " + budget.getAmount() + " exceeded! Spent: " + spent, user);
                } else if (utilization >= 90.0) {
                    activityLogService.log("BUDGET_WARNING", "Category '" + budget.getCategory().getName() + "' budget utilized over 90%! Spent: " + spent, user);
                }
            }
        }

        // 2. Check Overall Budget
        Optional<Budget> overallBudgetOpt = budgetRepository.findByUserIdAndMonthAndYearAndCategoryIsNull(user.getId(), month, year);
        if (overallBudgetOpt.isPresent()) {
            Budget budget = overallBudgetOpt.get();
            BigDecimal spent = expenseRepository.getTotalExpenseForPeriod(user.getId(), startDate, endDate);
            if (spent != null) {
                double utilization = spent.multiply(new BigDecimal("100"))
                        .divide(budget.getAmount(), 2, RoundingMode.HALF_UP).doubleValue();

                if (utilization >= 100.0) {
                    activityLogService.log("BUDGET_EXCEEDED", "Overall monthly budget of " + budget.getAmount() + " exceeded! Spent: " + spent, user);
                } else if (utilization >= 90.0) {
                    activityLogService.log("BUDGET_WARNING", "Overall monthly budget utilized over 90%! Spent: " + spent, user);
                }
            }
        }
    }

    private BudgetResponse mapToResponse(Budget budget) {
        return BudgetResponse.builder()
                .id(budget.getId())
                .amount(budget.getAmount())
                .month(budget.getMonth())
                .year(budget.getYear())
                .category(budget.getCategory() != null ? CategoryDto.builder()
                        .id(budget.getCategory().getId())
                        .name(budget.getCategory().getName())
                        .type(budget.getCategory().getType())
                        .build() : null)
                .build();
    }
}
