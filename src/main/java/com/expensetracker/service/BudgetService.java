package com.expensetracker.service;

import com.expensetracker.dto.request.BudgetRequest;
import com.expensetracker.dto.response.BudgetResponse;
import com.expensetracker.entity.Budget;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.User;
import com.expensetracker.enums.TransactionType;
import com.expensetracker.exception.DuplicateResourceException;
import com.expensetracker.exception.InvalidRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.mapper.BudgetMapper;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final SecurityUtils securityUtils;
    private final CategoryService categoryService;
    private final BudgetMapper budgetMapper;

    @Transactional
    public BudgetResponse createBudget(BudgetRequest request) {
        User user = securityUtils.getCurrentUser();
        Category category = categoryService.getAccessibleCategory(request.getCategoryId());

        if (category.getType() != TransactionType.EXPENSE) {
            throw new InvalidRequestException("Budgets can only be created for EXPENSE categories");
        }

        budgetRepository.findByUserAndCategoryIdAndMonthAndYear(user, category.getId(), request.getMonth(), request.getYear())
                .ifPresent(b -> {
                    throw new DuplicateResourceException("A budget for this category and month already exists");
                });

        Budget budget = budgetMapper.toEntity(request, user, category);
        Budget saved = budgetRepository.save(budget);
        return mapToResponseWithSpent(saved);
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> getAllBudgets() {
        User user = securityUtils.getCurrentUser();
        return budgetRepository.findByUser(user).stream()
                .map(this::mapToResponseWithSpent)
                .toList();
    }

    @Transactional(readOnly = true)
    public BudgetResponse getBudgetById(Long id) {
        return mapToResponseWithSpent(getOwnedBudget(id));
    }

    @Transactional
    public BudgetResponse updateBudget(Long id, BudgetRequest request) {
        Budget budget = getOwnedBudget(id);
        Category category = categoryService.getAccessibleCategory(request.getCategoryId());

        if (category.getType() != TransactionType.EXPENSE) {
            throw new InvalidRequestException("Budgets can only be created for EXPENSE categories");
        }

        User user = securityUtils.getCurrentUser();
        budgetRepository.findByUserAndCategoryIdAndMonthAndYear(user, category.getId(), request.getMonth(), request.getYear())
                .filter(existing -> !existing.getId().equals(budget.getId()))
                .ifPresent(b -> {
                    throw new DuplicateResourceException("A budget for this category and month already exists");
                });

        budget.setCategory(category);
        budget.setMonthlyLimit(request.getMonthlyLimit());
        budget.setMonth(request.getMonth());
        budget.setYear(request.getYear());
        return mapToResponseWithSpent(budget);
    }

    @Transactional
    public void deleteBudget(Long id) {
        budgetRepository.delete(getOwnedBudget(id));
    }

    private Budget getOwnedBudget(Long id) {
        User user = securityUtils.getCurrentUser();
        return budgetRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
    }

    private BudgetResponse mapToResponseWithSpent(Budget budget) {
        User user = budget.getUser();
        YearMonth ym = YearMonth.of(budget.getYear(), budget.getMonth());
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        BigDecimal spent = transactionRepository.sumExpenseByCategoryAndDateRange(
                user, budget.getCategory().getId(), start, end);

        return budgetMapper.toResponse(budget, spent);
    }
}
