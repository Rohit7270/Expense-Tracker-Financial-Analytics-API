package com.expensetracker.service;

import com.expensetracker.dto.request.RecurringTransactionRequest;
import com.expensetracker.dto.response.RecurringTransactionResponse;
import com.expensetracker.entity.*;
import com.expensetracker.exception.InvalidRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.enums.Frequency;
import com.expensetracker.mapper.RecurringTransactionMapper;
import com.expensetracker.repository.RecurringTransactionRepository;
import com.expensetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecurringTransactionService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final SecurityUtils securityUtils;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final RecurringTransactionMapper recurringTransactionMapper;

    @Transactional
    public RecurringTransactionResponse createRecurring(RecurringTransactionRequest request) {
        User user = securityUtils.getCurrentUser();
        Account account = accountService.getOwnedAccount(request.getAccountId());
        Category category = categoryService.getAccessibleCategory(request.getCategoryId());

        if (category.getType() != request.getType()) {
            throw new InvalidRequestException(
                    String.format("Category '%s' is of type '%s' and cannot be used for a '%s' recurring transaction",
                            category.getName(), category.getType(), request.getType()));
        }

        RecurringTransaction recurring = recurringTransactionMapper.toEntity(request, user, account, category);
        return recurringTransactionMapper.toResponse(recurringTransactionRepository.save(recurring));
    }

    @Transactional(readOnly = true)
    public List<RecurringTransactionResponse> getAllRecurring() {
        User user = securityUtils.getCurrentUser();
        return recurringTransactionRepository.findByUser(user).stream()
                .map(recurringTransactionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecurringTransactionResponse getRecurringById(Long id) {
        return recurringTransactionMapper.toResponse(getOwnedRecurring(id));
    }

    @Transactional
    public RecurringTransactionResponse updateRecurring(Long id, RecurringTransactionRequest request) {
        RecurringTransaction recurring = getOwnedRecurring(id);
        Account account = accountService.getOwnedAccount(request.getAccountId());
        Category category = categoryService.getAccessibleCategory(request.getCategoryId());

        if (category.getType() != request.getType()) {
            throw new InvalidRequestException(
                    String.format("Category '%s' is of type '%s' and cannot be used for a '%s' recurring transaction",
                            category.getName(), category.getType(), request.getType()));
        }

        recurring.setAccount(account);
        recurring.setCategory(category);
        recurring.setAmount(request.getAmount());
        recurring.setType(request.getType());
        recurring.setDescription(request.getDescription());
        recurring.setFrequency(request.getFrequency());
        if (!recurring.getStartDate().equals(request.getStartDate())) {
            recurring.setStartDate(request.getStartDate());
            if (recurring.getNextDueDate() == null || recurring.getNextDueDate().isBefore(request.getStartDate())) {
                recurring.setNextDueDate(request.getStartDate());
            }
        }

        return recurringTransactionMapper.toResponse(recurring);
    }

    @Transactional
    public void deleteRecurring(Long id) {
        recurringTransactionRepository.delete(getOwnedRecurring(id));
    }

    @Transactional
    public RecurringTransactionResponse toggleActive(Long id) {
        RecurringTransaction recurring = getOwnedRecurring(id);
        recurring.setActive(!recurring.getActive());
        return recurringTransactionMapper.toResponse(recurring);
    }

    private RecurringTransaction getOwnedRecurring(Long id) {
        User user = securityUtils.getCurrentUser();
        return recurringTransactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found with id: " + id));
    }

    @Transactional
    public void executeRecurringTransaction(Long recurringId) {
        RecurringTransaction recurring = recurringTransactionRepository.findById(recurringId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found with id: " + recurringId));

        if (!Boolean.TRUE.equals(recurring.getActive())) {
            return;
        }

        Account account = recurring.getAccount();
        LocalDate transactionDate = recurring.getNextDueDate();

        Transaction transaction = Transaction.builder()
                .amount(recurring.getAmount())
                .type(recurring.getType())
                .description("[Recurring] " + (recurring.getDescription() != null ? recurring.getDescription() : recurring.getCategory().getName()))
                .transactionDate(transactionDate)
                .category(recurring.getCategory())
                .account(account)
                .user(recurring.getUser())
                .build();

        transactionService.applyBalanceChange(account, recurring.getType(), recurring.getAmount());
        transactionRepository.save(transaction);

        transactionService.checkBudgetAlert(recurring.getUser(), recurring.getCategory(), recurring.getType(), transactionDate);

        LocalDate nextDate = computeNextDueDate(recurring.getNextDueDate(), recurring.getFrequency());
        recurring.setNextDueDate(nextDate);
        recurringTransactionRepository.save(recurring);

        log.info("Successfully executed recurring transaction ID {} for user {}. Next due date: {}",
                recurring.getId(), recurring.getUser().getEmail(), nextDate);
    }

    private LocalDate computeNextDueDate(LocalDate currentDueDate, Frequency frequency) {
        return switch (frequency) {
            case DAILY -> currentDueDate.plusDays(1);
            case WEEKLY -> currentDueDate.plusWeeks(1);
            case MONTHLY -> currentDueDate.plusMonths(1);
            case YEARLY -> currentDueDate.plusYears(1);
        };
    }
}
