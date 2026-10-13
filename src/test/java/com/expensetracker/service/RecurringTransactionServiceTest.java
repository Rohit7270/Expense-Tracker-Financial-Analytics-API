package com.expensetracker.service;

import com.expensetracker.dto.request.RecurringTransactionRequest;
import com.expensetracker.dto.response.RecurringTransactionResponse;
import com.expensetracker.entity.Account;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.RecurringTransaction;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.User;
import com.expensetracker.enums.AccountType;
import com.expensetracker.enums.Frequency;
import com.expensetracker.enums.Role;
import com.expensetracker.enums.TransactionType;
import com.expensetracker.exception.InvalidRequestException;
import com.expensetracker.mapper.RecurringTransactionMapper;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecurringTransactionServiceTest {

    @Mock
    private RecurringTransactionRepository recurringTransactionRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private AccountService accountService;

    @Mock
    private CategoryService categoryService;

    @Spy
    private RecurringTransactionMapper recurringTransactionMapper = new RecurringTransactionMapper();

    @InjectMocks
    private RecurringTransactionService recurringTransactionService;

    private User testUser;
    private Account testAccount;
    private Category expenseCategory;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("user@example.com").role(Role.USER).build();
        testAccount = Account.builder().id(10L).name("Main Bank").type(AccountType.BANK).balance(new BigDecimal("1000.00")).user(testUser).build();
        expenseCategory = Category.builder().id(20L).name("Subscriptions").type(TransactionType.EXPENSE).build();
    }

    @Test
    @DisplayName("createRecurring - Successfully creates recurring transaction")
    void testCreateRecurringSuccess() {
        RecurringTransactionRequest request = new RecurringTransactionRequest();
        request.setAmount(new BigDecimal("15.99"));
        request.setType(TransactionType.EXPENSE);
        request.setDescription("Netflix");
        request.setFrequency(Frequency.MONTHLY);
        request.setStartDate(LocalDate.of(2026, 10, 1));
        request.setAccountId(10L);
        request.setCategoryId(20L);

        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(accountService.getOwnedAccount(10L)).thenReturn(testAccount);
        when(categoryService.getAccessibleCategory(20L)).thenReturn(expenseCategory);
        when(recurringTransactionRepository.save(any(RecurringTransaction.class))).thenAnswer(invocation -> {
            RecurringTransaction r = invocation.getArgument(0);
            r.setId(100L);
            return r;
        });

        RecurringTransactionResponse response = recurringTransactionService.createRecurring(request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(new BigDecimal("15.99"), response.getAmount());
        assertEquals(LocalDate.of(2026, 10, 1), response.getNextDueDate());
        assertTrue(response.isActive());
    }

    @Test
    @DisplayName("createRecurring - Type mismatch with category throws InvalidRequestException")
    void testCreateRecurringTypeMismatch() {
        RecurringTransactionRequest request = new RecurringTransactionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setType(TransactionType.INCOME); // Mismatch: category is EXPENSE
        request.setAccountId(10L);
        request.setCategoryId(20L);

        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(accountService.getOwnedAccount(10L)).thenReturn(testAccount);
        when(categoryService.getAccessibleCategory(20L)).thenReturn(expenseCategory);

        assertThrows(InvalidRequestException.class, () -> recurringTransactionService.createRecurring(request));
        verify(recurringTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateRecurring - Updating start date into future realigns nextDueDate")
    void testUpdateRecurringRealignsNextDueDate() {
        RecurringTransaction recurring = RecurringTransaction.builder()
                .id(100L)
                .amount(new BigDecimal("15.99"))
                .type(TransactionType.EXPENSE)
                .frequency(Frequency.MONTHLY)
                .startDate(LocalDate.of(2026, 9, 1))
                .nextDueDate(LocalDate.of(2026, 9, 1))
                .active(true)
                .account(testAccount)
                .category(expenseCategory)
                .user(testUser)
                .build();

        LocalDate newStartDate = LocalDate.of(2026, 11, 1);
        RecurringTransactionRequest request = new RecurringTransactionRequest();
        request.setAmount(new BigDecimal("19.99"));
        request.setType(TransactionType.EXPENSE);
        request.setFrequency(Frequency.MONTHLY);
        request.setStartDate(newStartDate);
        request.setAccountId(10L);
        request.setCategoryId(20L);

        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(recurringTransactionRepository.findByIdAndUser(100L, testUser)).thenReturn(Optional.of(recurring));
        when(accountService.getOwnedAccount(10L)).thenReturn(testAccount);
        when(categoryService.getAccessibleCategory(20L)).thenReturn(expenseCategory);

        RecurringTransactionResponse response = recurringTransactionService.updateRecurring(100L, request);

        assertNotNull(response);
        assertEquals(newStartDate, response.getStartDate());
        assertEquals(newStartDate, response.getNextDueDate());
    }

    @Test
    @DisplayName("toggleActive - Flips active status from true to false and back")
    void testToggleActive() {
        RecurringTransaction recurring = RecurringTransaction.builder()
                .id(100L)
                .active(true)
                .amount(new BigDecimal("10.00"))
                .user(testUser)
                .build();

        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(recurringTransactionRepository.findByIdAndUser(100L, testUser)).thenReturn(Optional.of(recurring));

        RecurringTransactionResponse response = recurringTransactionService.toggleActive(100L);

        assertFalse(response.isActive());
        assertFalse(recurring.getActive());
    }

    @Test
    @DisplayName("executeRecurringTransaction - Deducts balance, persists transaction, and advances nextDueDate")
    void testExecuteRecurringTransaction() {
        LocalDate currentDue = LocalDate.of(2026, 10, 1);
        RecurringTransaction recurring = RecurringTransaction.builder()
                .id(100L)
                .amount(new BigDecimal("50.00"))
                .type(TransactionType.EXPENSE)
                .description("Gym")
                .frequency(Frequency.MONTHLY)
                .startDate(LocalDate.of(2026, 9, 1))
                .nextDueDate(currentDue)
                .active(true)
                .account(testAccount)
                .category(expenseCategory)
                .user(testUser)
                .build();

        when(recurringTransactionRepository.findById(100L)).thenReturn(Optional.of(recurring));

        recurringTransactionService.executeRecurringTransaction(100L);

        verify(transactionService, times(1)).applyBalanceChange(testAccount, TransactionType.EXPENSE, new BigDecimal("50.00"));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(transactionService, times(1)).checkBudgetAlert(testUser, expenseCategory, TransactionType.EXPENSE, currentDue);
        assertEquals(LocalDate.of(2026, 11, 1), recurring.getNextDueDate());
        verify(recurringTransactionRepository, times(1)).save(recurring);
    }

    @Test
    @DisplayName("executeRecurringTransaction - Inactive template does not generate transactions")
    void testExecuteRecurringTransactionWhenInactive() {
        RecurringTransaction recurring = RecurringTransaction.builder()
                .id(100L)
                .active(false)
                .build();

        when(recurringTransactionRepository.findById(100L)).thenReturn(Optional.of(recurring));

        recurringTransactionService.executeRecurringTransaction(100L);

        verify(transactionService, never()).applyBalanceChange(any(), any(), any());
        verify(transactionRepository, never()).save(any());
        verify(recurringTransactionRepository, never()).save(any());
    }
}
