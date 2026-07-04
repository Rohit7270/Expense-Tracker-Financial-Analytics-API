package com.enterprise.expensetracker.service;

import com.enterprise.expensetracker.dto.CategoryDto;
import com.enterprise.expensetracker.dto.IncomeRequest;
import com.enterprise.expensetracker.dto.IncomeResponse;
import com.enterprise.expensetracker.entity.Category;
import com.enterprise.expensetracker.entity.Income;
import com.enterprise.expensetracker.entity.User;
import com.enterprise.expensetracker.exception.ResourceNotFoundException;
import com.enterprise.expensetracker.repository.CategoryRepository;
import com.enterprise.expensetracker.repository.IncomeRepository;
import com.enterprise.expensetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class IncomeService {

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ActivityLogService activityLogService;

    public Page<IncomeResponse> searchIncome(
            Long userId,
            Long categoryId,
            LocalDate startDate,
            LocalDate endDate,
            String search,
            Pageable pageable
    ) {
        Page<Income> incomes = incomeRepository.searchIncome(
                userId, categoryId, startDate, endDate, search, pageable);
        return incomes.map(this::mapToResponse);
    }

    public IncomeResponse getIncomeById(Long id, Long userId) {
        Income income = incomeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Income not found with id: " + id));
        return mapToResponse(income);
    }

    @Transactional
    public IncomeResponse createIncome(Long userId, IncomeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }

        Income income = Income.builder()
                .source(request.getSource())
                .amount(request.getAmount())
                .date(request.getDate())
                .category(category)
                .user(user)
                .build();

        Income savedIncome = incomeRepository.save(income);

        transactionService.syncIncome(savedIncome);

        activityLogService.log("INCOME_ADDED", "Added income: " + income.getSource() + " (" + income.getAmount() + ")", user);

        return mapToResponse(savedIncome);
    }

    @Transactional
    public IncomeResponse updateIncome(Long id, Long userId, IncomeRequest request) {
        Income income = incomeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Income not found"));

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }

        income.setSource(request.getSource());
        income.setAmount(request.getAmount());
        income.setDate(request.getDate());
        income.setCategory(category);

        Income savedIncome = incomeRepository.save(income);

        transactionService.syncIncome(savedIncome);

        activityLogService.log("INCOME_UPDATED", "Updated income: " + income.getSource() + " (" + income.getAmount() + ")", income.getUser());

        return mapToResponse(savedIncome);
    }

    @Transactional
    public void deleteIncome(Long id, Long userId) {
        Income income = incomeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Income not found"));

        incomeRepository.delete(income);
        transactionService.deleteIncomeTransaction(id);

        activityLogService.log("INCOME_DELETED", "Deleted income: " + income.getSource(), income.getUser());
    }

    private IncomeResponse mapToResponse(Income income) {
        return IncomeResponse.builder()
                .id(income.getId())
                .source(income.getSource())
                .amount(income.getAmount())
                .date(income.getDate())
                .category(income.getCategory() != null ? CategoryDto.builder()
                        .id(income.getCategory().getId())
                        .name(income.getCategory().getName())
                        .type(income.getCategory().getType())
                        .build() : null)
                .build();
    }
}
