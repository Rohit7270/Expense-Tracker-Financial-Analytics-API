package com.expensetracker.service;

import com.expensetracker.dto.request.CategoryRequest;
import com.expensetracker.dto.response.CategoryResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.User;
import com.expensetracker.exception.InvalidRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.mapper.CategoryMapper;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.RecurringTransactionRepository;
import com.expensetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final RecurringTransactionRepository recurringTransactionRepository;
    private final SecurityUtils securityUtils;
    private final CategoryMapper categoryMapper;

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        User user = securityUtils.getCurrentUser();
        Category category = categoryMapper.toEntity(request, user);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        User user = securityUtils.getCurrentUser();
        return categoryRepository.findByUserIsNullOrUser(user).stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        return categoryMapper.toResponse(getAccessibleCategory(id));
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        User user = securityUtils.getCurrentUser();
        Category category = categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found or is a default category that cannot be edited, id: " + id));
        category.setName(request.getName());
        category.setType(request.getType());
        category.setIcon(request.getIcon());
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        User user = securityUtils.getCurrentUser();
        Category category = categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found or is a default category that cannot be deleted, id: " + id));
        if (transactionRepository.existsByCategory(category)) {
            throw new InvalidRequestException("Cannot delete category as it has associated transactions");
        }
        if (budgetRepository.existsByCategory(category)) {
            throw new InvalidRequestException("Cannot delete category as it has associated budgets");
        }
        if (recurringTransactionRepository.existsByCategory(category)) {
            throw new InvalidRequestException("Cannot delete category as it has associated recurring transaction templates");
        }
        categoryRepository.delete(category);
    }

    protected Category getAccessibleCategory(Long id) {
        User user = securityUtils.getCurrentUser();
        return categoryRepository.findByIdAndUser(id, user)
                .or(() -> categoryRepository.findByIdAndUserIsNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }
}
