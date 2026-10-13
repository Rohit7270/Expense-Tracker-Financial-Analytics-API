package com.expensetracker.service;

import com.expensetracker.entity.RecurringTransaction;
import com.expensetracker.repository.RecurringTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecurringTransactionScheduler {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final RecurringTransactionService recurringTransactionService;

    /**
     * Executes daily at 01:00 AM to process all due recurring transactions.
     * Note: Individual transaction executions are encapsulated in separate @Transactional boundaries
     * within RecurringTransactionService to ensure a failure in one transaction does not abort the batch.
     */
    @Scheduled(cron = "${app.scheduling.recurring-cron:0 0 1 * * ?}")
    public void processDueRecurringTransactions() {
        processDueRecurringTransactionsForDate(LocalDate.now());
    }

    public int processDueRecurringTransactionsForDate(LocalDate currentDate) {
        log.info("Starting automated execution of recurring transactions for date: {}", currentDate);
        List<RecurringTransaction> dueTransactions = recurringTransactionRepository
                .findByActiveTrueAndNextDueDateLessThanEqual(currentDate);

        int processedCount = 0;
        for (RecurringTransaction recurring : dueTransactions) {
            try {
                recurringTransactionService.executeRecurringTransaction(recurring.getId());
                processedCount++;
            } catch (Exception e) {
                log.error("Failed to process recurring transaction ID: " + recurring.getId(), e);
            }
        }

        log.info("Completed recurring transactions batch. Processed {} records.", processedCount);
        return processedCount;
    }
}
