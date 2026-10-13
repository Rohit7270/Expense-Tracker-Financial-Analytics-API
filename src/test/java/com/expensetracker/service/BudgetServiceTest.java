package com.expensetracker.service;

import com.expensetracker.dto.request.BudgetRequest;
import com.expensetracker.dto.response.BudgetResponse;
import com.expensetracker.entity.Budget;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.User;
import com.expensetracker.enums.TransactionType;
import com.expensetracker.exception.DuplicateResourceException;
import com.expensetracker.exception.InvalidRequestException;
import com.expensetracker.mapper.BudgetMapper;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private CategoryService categoryService;

    @Spy
    private BudgetMapper budgetMapper = new BudgetMapper();

    @InjectMocks
    private BudgetService budgetService;

    private User testUser;
    private Category expenseCategory;
    private Category incomeCategory;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("user@example.com").build();
        expenseCategory = Category.builder().id(10L).name("Dining").type(TransactionType.EXPENSE).build();
        incomeCategory = Category.builder().id(11L).name("Dividends").type(TransactionType.INCOME).build();
    }

    @Test
    @DisplayName("Create Budget - Success with spent and percentage calculations")
    void testCreateBudgetSuccess() {
        BudgetRequest request = new BudgetRequest();
        request.setCategoryId(10L);
        request.setMonthlyLimit(new BigDecimal("500.00"));
        request.setMonth(9);
        request.setYear(2026);

        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(categoryService.getAccessibleCategory(10L)).thenReturn(expenseCategory);
        when(budgetRepository.findByUserAndCategoryIdAndMonthAndYear(testUser, 10L, 9, 2026)).thenReturn(Optional.empty());

        Budget savedBudget = Budget.builder()
                .id(1L)
                .user(testUser)
                .category(expenseCategory)
                .monthlyLimit(new BigDecimal("500.00"))
                .month(9)
                .year(2026)
                .build();

        when(budgetRepository.save(any(Budget.class))).thenReturn(savedBudget);
        when(transactionRepository.sumExpenseByCategoryAndDateRange(eq(testUser), eq(10L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("200.00"));

        BudgetResponse response = budgetService.createBudget(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("500.00"), response.getMonthlyLimit());
        assertEquals(new BigDecimal("200.00"), response.getSpentAmount());
        assertEquals(new BigDecimal("300.00"), response.getRemainingAmount());
        assertEquals(40.0, response.getPercentageUsed());
        assertFalse(response.isExceeded());
    }

    @Test
    @DisplayName("Create Budget for INCOME category throws InvalidRequestException")
    void testCreateBudgetForIncomeCategoryThrowsException() {
        BudgetRequest request = new BudgetRequest();
        request.setCategoryId(11L);
        request.setMonthlyLimit(new BigDecimal("500.00"));
        request.setMonth(9);
        request.setYear(2026);

        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(categoryService.getAccessibleCategory(11L)).thenReturn(incomeCategory);

        assertThrows(InvalidRequestException.class, () -> budgetService.createBudget(request));
        verify(budgetRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create Budget when duplicate exists throws DuplicateResourceException")
    void testCreateBudgetDuplicateThrowsException() {
        BudgetRequest request = new BudgetRequest();
        request.setCategoryId(10L);
        request.setMonthlyLimit(new BigDecimal("500.00"));
        request.setMonth(9);
        request.setYear(2026);

        Budget existing = Budget.builder().id(99L).build();

        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(categoryService.getAccessibleCategory(10L)).thenReturn(expenseCategory);
        when(budgetRepository.findByUserAndCategoryIdAndMonthAndYear(testUser, 10L, 9, 2026))
                .thenReturn(Optional.of(existing));

        assertThrows(DuplicateResourceException.class, () -> budgetService.createBudget(request));
    }
}
