package com.expensetracker.service;

import com.expensetracker.dto.request.AccountRequest;
import com.expensetracker.dto.response.AccountResponse;
import com.expensetracker.entity.Account;
import com.expensetracker.entity.User;
import com.expensetracker.enums.AccountType;
import com.expensetracker.enums.Role;
import com.expensetracker.exception.InvalidRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.mapper.AccountMapper;
import com.expensetracker.repository.AccountRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private RecurringTransactionRepository recurringTransactionRepository;

    @Mock
    private SecurityUtils securityUtils;

    @Spy
    private AccountMapper accountMapper = new AccountMapper();

    @InjectMocks
    private AccountService accountService;

    private User testUser;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("user@example.com").role(Role.USER).build();
        testAccount = Account.builder()
                .id(10L)
                .name("Checking Account")
                .type(AccountType.BANK)
                .balance(new BigDecimal("1000.00"))
                .user(testUser)
                .build();
    }

    @Test
    @DisplayName("deleteAccount - Successfully deletes when no transactions or recurring transactions reference it")
    void testDeleteAccountSuccess() {
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(accountRepository.findByIdAndUser(10L, testUser)).thenReturn(Optional.of(testAccount));
        when(transactionRepository.existsByAccount(testAccount)).thenReturn(false);
        when(recurringTransactionRepository.existsByAccount(testAccount)).thenReturn(false);

        accountService.deleteAccount(10L);

        verify(accountRepository, times(1)).delete(testAccount);
    }

    @Test
    @DisplayName("deleteAccount - Throws InvalidRequestException when transactions reference it")
    void testDeleteAccountWithReferencingTransactionsThrows() {
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(accountRepository.findByIdAndUser(10L, testUser)).thenReturn(Optional.of(testAccount));
        when(transactionRepository.existsByAccount(testAccount)).thenReturn(true);

        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> accountService.deleteAccount(10L));

        assertTrue(ex.getMessage().contains("Cannot delete account as it has associated transactions"));
        verify(accountRepository, never()).delete(any(Account.class));
    }

    @Test
    @DisplayName("deleteAccount - Throws ResourceNotFoundException when account does not exist")
    void testDeleteAccountNotFoundThrows() {
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(accountRepository.findByIdAndUser(99L, testUser)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.deleteAccount(99L));
        verify(accountRepository, never()).delete(any(Account.class));
    }

    @Test
    @DisplayName("createAccount - Creates and returns account response")
    void testCreateAccountSuccess() {
        AccountRequest request = AccountRequest.builder()
                .name("Checking Account")
                .type(AccountType.BANK)
                .balance(new BigDecimal("1000.00"))
                .build();

        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        AccountResponse response = accountService.createAccount(request);

        assertNotNull(response);
        assertEquals("Checking Account", response.getName());
        assertEquals(AccountType.BANK, response.getType());
        assertEquals(new BigDecimal("1000.00"), response.getBalance());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("deleteAccount - Throws InvalidRequestException when recurring transactions reference it")
    void testDeleteAccountWithReferencingRecurringTransactionsThrows() {
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(accountRepository.findByIdAndUser(10L, testUser)).thenReturn(Optional.of(testAccount));
        when(transactionRepository.existsByAccount(testAccount)).thenReturn(false);
        when(recurringTransactionRepository.existsByAccount(testAccount)).thenReturn(true);

        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> accountService.deleteAccount(10L));

        assertTrue(ex.getMessage().contains("Cannot delete account as it has associated recurring transaction templates"));
        verify(accountRepository, never()).delete(any(Account.class));
    }

    @Test
    @DisplayName("updateAccount - Preserves existing balance when balance is omitted in update request")
    void testUpdateAccountPreservesBalanceWhenOmitted() {
        AccountRequest request = AccountRequest.builder()
                .name("Updated Checking")
                .type(AccountType.BANK)
                .balance(null)
                .build();

        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(accountRepository.findByIdAndUser(10L, testUser)).thenReturn(Optional.of(testAccount));

        AccountResponse response = accountService.updateAccount(10L, request);

        assertNotNull(response);
        assertEquals("Updated Checking", response.getName());
        assertEquals(new BigDecimal("1000.00"), response.getBalance());
        assertEquals(new BigDecimal("1000.00"), testAccount.getBalance());
    }

    @Test
    @DisplayName("updateAccount - Updates balance when explicitly provided in update request")
    void testUpdateAccountUpdatesBalanceWhenProvided() {
        AccountRequest request = AccountRequest.builder()
                .name("Adjusted Checking")
                .type(AccountType.BANK)
                .balance(new BigDecimal("2500.00"))
                .build();

        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(accountRepository.findByIdAndUser(10L, testUser)).thenReturn(Optional.of(testAccount));

        AccountResponse response = accountService.updateAccount(10L, request);

        assertNotNull(response);
        assertEquals("Adjusted Checking", response.getName());
        assertEquals(new BigDecimal("2500.00"), response.getBalance());
        assertEquals(new BigDecimal("2500.00"), testAccount.getBalance());
    }
}
