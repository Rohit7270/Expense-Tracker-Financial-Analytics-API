package com.expensetracker.service;

import com.expensetracker.entity.*;
import com.expensetracker.enums.Frequency;
import com.expensetracker.enums.TransactionType;
import com.expensetracker.repository.RecurringTransactionRepository;
import com.expensetracker.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecurringTransactionSchedulerTest {

    @Mock
    private RecurringTransactionRepository recurringTransactionRepository;

    @Mock
    private RecurringTransactionService recurringTransactionService;

    @InjectMocks
    private RecurringTransactionScheduler scheduler;

    @Test
    @DisplayName("Process Due Recurring Transactions - Delegates each execution to RecurringTransactionService")
    void testProcessDueRecurringTransactionsDelegation() {
        LocalDate today = LocalDate.of(2026, 9, 19);

        RecurringTransaction monthlyRecurring = RecurringTransaction.builder()
                .id(50L)
                .build();

        when(recurringTransactionRepository.findByActiveTrueAndNextDueDateLessThanEqual(today))
                .thenReturn(List.of(monthlyRecurring));

        int processed = scheduler.processDueRecurringTransactionsForDate(today);

        assertEquals(1, processed);
        verify(recurringTransactionService, times(1)).executeRecurringTransaction(50L);
    }

    @Test
    @DisplayName("Process Due Recurring Transactions - Failure in one transaction does not abort others in the batch")
    void testProcessDueRecurringTransactionsFailureIsolation() {
        LocalDate today = LocalDate.of(2026, 9, 19);

        RecurringTransaction tx1 = RecurringTransaction.builder().id(101L).build();
        RecurringTransaction tx2 = RecurringTransaction.builder().id(102L).build();

        when(recurringTransactionRepository.findByActiveTrueAndNextDueDateLessThanEqual(today))
                .thenReturn(List.of(tx1, tx2));

        // tx1 throws exception, tx2 succeeds
        doThrow(new RuntimeException("Simulated processing failure"))
                .when(recurringTransactionService).executeRecurringTransaction(101L);
        doNothing()
                .when(recurringTransactionService).executeRecurringTransaction(102L);

        int processed = scheduler.processDueRecurringTransactionsForDate(today);

        assertEquals(1, processed);
        verify(recurringTransactionService, times(1)).executeRecurringTransaction(101L);
        verify(recurringTransactionService, times(1)).executeRecurringTransaction(102L);
    }
}
