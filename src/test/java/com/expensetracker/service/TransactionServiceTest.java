package com.expensetracker.service;

import com.expensetracker.dto.request.TransactionRequest;
import com.expensetracker.dto.response.TransactionResponse;
import com.expensetracker.entity.*;
import com.expensetracker.enums.AccountType;
import com.expensetracker.enums.NotificationType;
import com.expensetracker.enums.Role;
import com.expensetracker.enums.TransactionType;
import com.expensetracker.exception.InvalidRequestException;
import com.expensetracker.mapper.TransactionMapper;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.TagRepository;
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
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private AccountService accountService;

    @Mock
    private CategoryService categoryService;

    @Spy
    private TransactionMapper transactionMapper = new TransactionMapper();

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private Account testAccount;
    private Category expenseCategory;
    private Category incomeCategory;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("test@example.com").role(Role.USER).build();
        testAccount = Account.builder().id(10L).name("Checking").type(AccountType.BANK).balance(new BigDecimal("1000.00")).user(testUser).build();
        expenseCategory = Category.builder().id(20L).name("Groceries").type(TransactionType.EXPENSE).build();
        incomeCategory = Category.builder().id(21L).name("Salary").type(TransactionType.INCOME).build();
    }

    @Test
    @DisplayName("Create Expense Transaction - Deducts balance and saves")
    void testCreateExpenseTransactionSuccess() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("150.00"));
        request.setType(TransactionType.EXPENSE);
        request.setDescription("Supermarket");
        request.setTransactionDate(LocalDate.now());
        request.setCategoryId(20L);
        request.setAccountId(10L);

        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(accountService.getOwnedAccount(10L)).thenReturn(testAccount);
        when(categoryService.getAccessibleCategory(20L)).thenReturn(expenseCategory);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(100L);
            return t;
        });

        TransactionResponse response = transactionService.createTransaction(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("850.00"), testAccount.getBalance());
        assertEquals(new BigDecimal("150.00"), response.getAmount());
        assertEquals("Groceries", response.getCategoryName());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Create Income Transaction - Increases account balance")
    void testCreateIncomeTransactionSuccess() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("500.00"));
        request.setType(TransactionType.INCOME);
        request.setDescription("Freelance");
        request.setTransactionDate(LocalDate.now());
        request.setCategoryId(21L);
        request.setAccountId(10L);

        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(accountService.getOwnedAccount(10L)).thenReturn(testAccount);
        when(categoryService.getAccessibleCategory(21L)).thenReturn(incomeCategory);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(101L);
            return t;
        });

        TransactionResponse response = transactionService.createTransaction(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("1500.00"), testAccount.getBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Create Transaction with Mismatched Category Type throws InvalidRequestException")
    void testCreateTransactionCategoryTypeMismatch() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setType(TransactionType.EXPENSE);
        request.setCategoryId(21L); // Income category
        request.setAccountId(10L);
        request.setTransactionDate(LocalDate.now());

        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(accountService.getOwnedAccount(10L)).thenReturn(testAccount);
        when(categoryService.getAccessibleCategory(21L)).thenReturn(incomeCategory);

        assertThrows(InvalidRequestException.class, () -> transactionService.createTransaction(request));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("checkBudgetAlert - Creates notification when budget exceeded and no prior alert exists")
    void testCheckBudgetAlertCreatesNotification() {
        LocalDate date = LocalDate.of(2026, 10, 15);
        Budget budget = Budget.builder()
                .id(1L)
                .monthlyLimit(new BigDecimal("100.00"))
                .month(10)
                .year(2026)
                .category(expenseCategory)
                .user(testUser)
                .build();

        when(budgetRepository.findByUserAndCategoryIdAndMonthAndYear(testUser, 20L, 10, 2026))
                .thenReturn(Optional.of(budget));
        when(transactionRepository.sumExpenseByCategoryAndDateRange(eq(testUser), eq(20L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("150.00"));
        when(notificationService.hasNotification(eq(testUser), anyString(), eq(NotificationType.BUDGET_ALERT)))
                .thenReturn(false);

        transactionService.checkBudgetAlert(testUser, expenseCategory, TransactionType.EXPENSE, date);

        verify(notificationService, times(1)).createNotification(eq(testUser), anyString(), eq(NotificationType.BUDGET_ALERT));
    }

    @Test
    @DisplayName("checkBudgetAlert - Does NOT create notification when alert already exists for user, category, and month")
    void testCheckBudgetAlertDoesNotDuplicate() {
        LocalDate date = LocalDate.of(2026, 10, 15);
        Budget budget = Budget.builder()
                .id(1L)
                .monthlyLimit(new BigDecimal("100.00"))
                .month(10)
                .year(2026)
                .category(expenseCategory)
                .user(testUser)
                .build();

        when(budgetRepository.findByUserAndCategoryIdAndMonthAndYear(testUser, 20L, 10, 2026))
                .thenReturn(Optional.of(budget));
        when(transactionRepository.sumExpenseByCategoryAndDateRange(eq(testUser), eq(20L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("150.00"));
        when(notificationService.hasNotification(eq(testUser), anyString(), eq(NotificationType.BUDGET_ALERT)))
                .thenReturn(true);

        transactionService.checkBudgetAlert(testUser, expenseCategory, TransactionType.EXPENSE, date);

        verify(notificationService, never()).createNotification(any(User.class), anyString(), any(NotificationType.class));
    }

    @Test
    @DisplayName("updateTransaction - Reverses old balance and applies new balance")
    void testUpdateTransactionBalanceReversal() {
        Transaction existing = Transaction.builder()
                .id(100L)
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.EXPENSE)
                .description("Old Groceries")
                .transactionDate(LocalDate.now())
                .category(expenseCategory)
                .account(testAccount)
                .user(testUser)
                .build();
        testAccount.setBalance(new BigDecimal("900.00")); // Balance after existing transaction

        TransactionRequest updateRequest = new TransactionRequest();
        updateRequest.setAmount(new BigDecimal("150.00"));
        updateRequest.setType(TransactionType.EXPENSE);
        updateRequest.setDescription("Updated Groceries");
        updateRequest.setTransactionDate(LocalDate.now());
        updateRequest.setCategoryId(20L);
        updateRequest.setAccountId(10L);

        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(transactionRepository.findByIdAndUser(100L, testUser)).thenReturn(Optional.of(existing));
        when(categoryService.getAccessibleCategory(20L)).thenReturn(expenseCategory);
        when(accountService.getOwnedAccount(10L)).thenReturn(testAccount);

        TransactionResponse response = transactionService.updateTransaction(100L, updateRequest);

        assertNotNull(response);
        assertEquals(new BigDecimal("150.00"), response.getAmount());
        // 900.00 + 100.00 (reverse) - 150.00 (new) = 850.00
        assertEquals(new BigDecimal("850.00"), testAccount.getBalance());
    }

    @Test
    @DisplayName("deleteTransaction - Restores account balance and deletes entity")
    void testDeleteTransactionRestoresBalance() {
        Transaction existing = Transaction.builder()
                .id(100L)
                .amount(new BigDecimal("150.00"))
                .type(TransactionType.EXPENSE)
                .description("Supermarket")
                .transactionDate(LocalDate.now())
                .category(expenseCategory)
                .account(testAccount)
                .user(testUser)
                .build();
        testAccount.setBalance(new BigDecimal("850.00")); // After deducting 150.00

        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(transactionRepository.findByIdAndUser(100L, testUser)).thenReturn(Optional.of(existing));

        transactionService.deleteTransaction(100L);

        // 850.00 + 150.00 (restored) = 1000.00
        assertEquals(new BigDecimal("1000.00"), testAccount.getBalance());
        verify(transactionRepository, times(1)).delete(existing);
    }

    @Test
    @DisplayName("resolveTags - Deduplicates case-insensitive duplicate tag names in request")
    void testResolveTagsDeduplicatesCaseInsensitive() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("50.00"));
        request.setType(TransactionType.EXPENSE);
        request.setDescription("Dinner");
        request.setTransactionDate(LocalDate.now());
        request.setCategoryId(20L);
        request.setAccountId(10L);
        request.setTagNames(java.util.List.of("Food", "food", " FOOD "));

        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(accountService.getOwnedAccount(10L)).thenReturn(testAccount);
        when(categoryService.getAccessibleCategory(20L)).thenReturn(expenseCategory);
        when(tagRepository.findByNameIgnoreCaseAndUser("Food", testUser)).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(102L);
            return t;
        });

        TransactionResponse response = transactionService.createTransaction(request);

        assertNotNull(response);
        // Only 1 tag should be persisted despite 3 duplicate representations
        verify(tagRepository, times(1)).save(any(Tag.class));
    }
}
