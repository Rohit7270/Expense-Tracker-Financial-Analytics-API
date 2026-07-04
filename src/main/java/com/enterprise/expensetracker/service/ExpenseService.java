package com.enterprise.expensetracker.service;

import com.enterprise.expensetracker.dto.CategoryDto;
import com.enterprise.expensetracker.dto.ExpenseRequest;
import com.enterprise.expensetracker.dto.ExpenseResponse;
import com.enterprise.expensetracker.dto.PaymentMethodDto;
import com.enterprise.expensetracker.entity.Category;
import com.enterprise.expensetracker.entity.Expense;
import com.enterprise.expensetracker.entity.PaymentMethod;
import com.enterprise.expensetracker.entity.User;
import com.enterprise.expensetracker.exception.ResourceNotFoundException;
import com.enterprise.expensetracker.repository.CategoryRepository;
import com.enterprise.expensetracker.repository.ExpenseRepository;
import com.enterprise.expensetracker.repository.PaymentMethodRepository;
import com.enterprise.expensetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    @Lazy
    private BudgetService budgetService; // Lazy loaded to prevent circular dependency later

    public Page<ExpenseResponse> searchExpenses(
            Long userId,
            Long categoryId,
            Long paymentMethodId,
            LocalDate startDate,
            LocalDate endDate,
            String search,
            Pageable pageable
    ) {
        Page<Expense> expenses = expenseRepository.searchExpenses(
                userId, categoryId, paymentMethodId, startDate, endDate, search, pageable);
        
        return expenses.map(this::mapToResponse);
    }

    public ExpenseResponse getExpenseById(Long id, Long userId) {
        Expense expense = expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
        return mapToResponse(expense);
    }

    @Transactional
    public ExpenseResponse createExpense(Long userId, ExpenseRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment Method not found"));

        Expense expense = Expense.builder()
                .description(request.getDescription())
                .amount(request.getAmount())
                .date(request.getDate())
                .category(category)
                .paymentMethod(paymentMethod)
                .user(user)
                .build();

        Expense savedExpense = expenseRepository.save(expense);
        
        // Sync with transactions consolidated table
        transactionService.syncExpense(savedExpense);
        
        activityLogService.log("EXPENSE_ADDED", "Added expense: " + expense.getDescription() + " (" + expense.getAmount() + ")", user);

        // Budget check warning trigger (Phase 6 hook)
        try {
            budgetService.checkBudgetAndNotify(user, request.getCategoryId(), request.getDate().getMonthValue(), request.getDate().getYear());
        } catch (Exception e) {
            // Log warning but don't fail transaction
        }

        return mapToResponse(savedExpense);
    }

    @Transactional
    public ExpenseResponse updateExpense(Long id, Long userId, ExpenseRequest request) {
        Expense expense = expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment Method not found"));

        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setDate(request.getDate());
        expense.setCategory(category);
        expense.setPaymentMethod(paymentMethod);

        Expense savedExpense = expenseRepository.save(expense);

        transactionService.syncExpense(savedExpense);

        activityLogService.log("EXPENSE_UPDATED", "Updated expense: " + expense.getDescription() + " (" + expense.getAmount() + ")", expense.getUser());

        try {
            budgetService.checkBudgetAndNotify(expense.getUser(), request.getCategoryId(), request.getDate().getMonthValue(), request.getDate().getYear());
        } catch (Exception e) {
            // Log warning but don't fail transaction
        }

        return mapToResponse(savedExpense);
    }

    @Transactional
    public void deleteExpense(Long id, Long userId) {
        Expense expense = expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        expenseRepository.delete(expense);
        transactionService.deleteExpenseTransaction(id);

        activityLogService.log("EXPENSE_DELETED", "Deleted expense: " + expense.getDescription(), expense.getUser());
    }

    private ExpenseResponse mapToResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .date(expense.getDate())
                .category(CategoryDto.builder()
                        .id(expense.getCategory().getId())
                        .name(expense.getCategory().getName())
                        .type(expense.getCategory().getType())
                        .build())
                .paymentMethod(PaymentMethodDto.builder()
                        .id(expense.getPaymentMethod().getId())
                        .name(expense.getPaymentMethod().getName())
                        .build())
                .build();
    }
}
