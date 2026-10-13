package com.expensetracker.service;

import com.expensetracker.dto.request.CategoryRequest;
import com.expensetracker.dto.response.CategoryResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.User;
import com.expensetracker.enums.Role;
import com.expensetracker.enums.TransactionType;
import com.expensetracker.exception.InvalidRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.mapper.CategoryMapper;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.RecurringTransactionRepository;
import com.expensetracker.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private RecurringTransactionRepository recurringTransactionRepository;

    @Mock
    private SecurityUtils securityUtils;

    @Spy
    private CategoryMapper categoryMapper = new CategoryMapper();

    @InjectMocks
    private CategoryService categoryService;

    private User testUser;
    private Category customCategory;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("user@example.com").role(Role.USER).build();
        customCategory = Category.builder()
                .id(20L)
                .name("Hobbies")
                .type(TransactionType.EXPENSE)
                .icon("palette")
                .user(testUser)
                .build();
    }

    @Test
    @DisplayName("deleteCategory - Successfully deletes when no transactions, budgets, or recurring templates reference it")
    void testDeleteCategorySuccess() {
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(categoryRepository.findByIdAndUser(20L, testUser)).thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategory(customCategory)).thenReturn(false);
        when(budgetRepository.existsByCategory(customCategory)).thenReturn(false);
        when(recurringTransactionRepository.existsByCategory(customCategory)).thenReturn(false);

        categoryService.deleteCategory(20L);

        verify(categoryRepository, times(1)).delete(customCategory);
    }

    @Test
    @DisplayName("deleteCategory - Throws InvalidRequestException when transactions reference it")
    void testDeleteCategoryWithReferencingTransactionsThrows() {
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(categoryRepository.findByIdAndUser(20L, testUser)).thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategory(customCategory)).thenReturn(true);

        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> categoryService.deleteCategory(20L));

        assertTrue(ex.getMessage().contains("Cannot delete category as it has associated transactions"));
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    @DisplayName("deleteCategory - Throws InvalidRequestException when budgets reference it")
    void testDeleteCategoryWithReferencingBudgetsThrows() {
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(categoryRepository.findByIdAndUser(20L, testUser)).thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategory(customCategory)).thenReturn(false);
        when(budgetRepository.existsByCategory(customCategory)).thenReturn(true);

        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> categoryService.deleteCategory(20L));

        assertTrue(ex.getMessage().contains("Cannot delete category as it has associated budgets"));
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    @DisplayName("deleteCategory - Throws InvalidRequestException when recurring transactions reference it")
    void testDeleteCategoryWithReferencingRecurringTransactionsThrows() {
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(categoryRepository.findByIdAndUser(20L, testUser)).thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategory(customCategory)).thenReturn(false);
        when(budgetRepository.existsByCategory(customCategory)).thenReturn(false);
        when(recurringTransactionRepository.existsByCategory(customCategory)).thenReturn(true);

        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> categoryService.deleteCategory(20L));

        assertTrue(ex.getMessage().contains("Cannot delete category as it has associated recurring transaction templates"));
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    @DisplayName("deleteCategory - Throws ResourceNotFoundException for default category or non-existent")
    void testDeleteCategoryNotFoundThrows() {
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(categoryRepository.findByIdAndUser(99L, testUser)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(99L));
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    @DisplayName("createCategory - Creates and returns category response")
    void testCreateCategorySuccess() {
        CategoryRequest request = CategoryRequest.builder()
                .name("Hobbies")
                .type(TransactionType.EXPENSE)
                .icon("palette")
                .build();

        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(categoryRepository.save(any(Category.class))).thenReturn(customCategory);

        CategoryResponse response = categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals("Hobbies", response.getName());
        assertEquals(TransactionType.EXPENSE, response.getType());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }
}
