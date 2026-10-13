package com.expensetracker.service;

import com.expensetracker.dto.request.TransactionRequest;
import com.expensetracker.dto.response.TransactionResponse;
import com.expensetracker.entity.*;
import com.expensetracker.enums.NotificationType;
import com.expensetracker.enums.TransactionType;
import com.expensetracker.exception.InvalidRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.mapper.TransactionMapper;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.TagRepository;
import com.expensetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TagRepository tagRepository;
    private final BudgetRepository budgetRepository;
    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final TransactionMapper transactionMapper;

    /**
     * Creates a new financial transaction.
     * 
     * Why @Transactional?
     * This method performs multiple database mutations atomically:
     * 1. Inserts a new Transaction record.
     * 2. Updates the associated Account balance.
     * 3. Optionally creates a Budget Notification Alert if monthly limit is exceeded.
     * If any operation fails (e.g. database error), all changes are rolled back automatically,
     * ensuring account balance and transaction ledger remain synchronized.
     */
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        User user = securityUtils.getCurrentUser();
        Account account = accountService.getOwnedAccount(request.getAccountId());
        Category category = categoryService.getAccessibleCategory(request.getCategoryId());

        validateCategoryType(category, request.getType());
        Set<Tag> tags = resolveTags(request.getTagNames(), user);

        Transaction transaction = transactionMapper.toEntity(request, user, category, account, tags);
        applyBalanceChange(account, request.getType(), request.getAmount());

        Transaction saved = transactionRepository.save(transaction);
        checkBudgetAlert(user, category, request.getType(), request.getTransactionDate());
        
        log.info("Transaction created successfully. ID: {}, User: {}, Amount: {}", saved.getId(), user.getEmail(), saved.getAmount());
        return transactionMapper.toResponse(saved);
    }

    public void checkBudgetAlert(User user, Category category, TransactionType type, LocalDate transactionDate) {
        if (type != TransactionType.EXPENSE) {
            return;
        }
        YearMonth ym = YearMonth.from(transactionDate);
        budgetRepository.findByUserAndCategoryIdAndMonthAndYear(user, category.getId(), ym.getMonthValue(), ym.getYear())
                .ifPresent(budget -> {
                    BigDecimal spent = transactionRepository.sumExpenseByCategoryAndDateRange(
                            user, category.getId(), ym.atDay(1), ym.atEndOfMonth());
                    if (spent.compareTo(budget.getMonthlyLimit()) > 0) {
                        String alertMessage = String.format("You've exceeded your %s budget for %d/%d",
                                category.getName(), ym.getMonthValue(), ym.getYear());
                        if (!notificationService.hasNotification(user, alertMessage, NotificationType.BUDGET_ALERT)) {
                            notificationService.createNotification(user, alertMessage, NotificationType.BUDGET_ALERT);
                        }
                    }
                });
    }

    /**
     * Searches transactions with optional filtering parameters.
     * 
     * Why @Transactional(readOnly = true)?
     * Read-only transactions optimize performance by disabling Hibernate dirty-checking
     * and allowing the database driver to optimize read queries.
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> searchTransactions(TransactionType type, Long categoryId, Long accountId,
                                                          LocalDate startDate, LocalDate endDate) {
        User user = securityUtils.getCurrentUser();
        return transactionRepository.search(user, type, categoryId, accountId, startDate, endDate)
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id) {
        return transactionMapper.toResponse(getOwnedTransaction(id));
    }

    /**
     * Updates an existing transaction.
     * 
     * Why @Transactional?
     * Atomically reverses the old balance effect on the previous account, updates transaction fields,
     * applies the new balance effect on the new account, and re-checks budget alerts.
     */
    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        User user = securityUtils.getCurrentUser();
        Transaction transaction = getOwnedTransaction(id);

        Category category = categoryService.getAccessibleCategory(request.getCategoryId());
        validateCategoryType(category, request.getType());

        // Reverse the old balance effect on old account
        reverseBalanceChange(transaction.getAccount(), transaction.getType(), transaction.getAmount());

        // Get potentially updated account
        Account account = accountService.getOwnedAccount(request.getAccountId());

        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setCategory(category);
        transaction.setAccount(account);
        transaction.setTags(resolveTags(request.getTagNames(), user));

        // Apply new balance effect on current account
        applyBalanceChange(account, request.getType(), request.getAmount());

        checkBudgetAlert(user, category, request.getType(), request.getTransactionDate());

        log.info("Transaction updated successfully. ID: {}, User: {}", id, user.getEmail());
        return transactionMapper.toResponse(transaction);
    }

    /**
     * Deletes a transaction and restores account balance.
     * 
     * Why @Transactional?
     * Reversing account balance and deleting the transaction entity must occur within one transaction boundary.
     */
    @Transactional
    public void deleteTransaction(Long id) {
        Transaction transaction = getOwnedTransaction(id);
        reverseBalanceChange(transaction.getAccount(), transaction.getType(), transaction.getAmount());
        transactionRepository.delete(transaction);
        log.info("Transaction deleted successfully. ID: {}", id);
    }

    protected Transaction getOwnedTransaction(Long id) {
        User user = securityUtils.getCurrentUser();
        return transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
    }

    public void applyBalanceChange(Account account, TransactionType type, BigDecimal amount) {
        if (type == TransactionType.INCOME) {
            account.setBalance(account.getBalance().add(amount));
        } else {
            account.setBalance(account.getBalance().subtract(amount));
        }
    }

    public void reverseBalanceChange(Account account, TransactionType type, BigDecimal amount) {
        if (type == TransactionType.INCOME) {
            account.setBalance(account.getBalance().subtract(amount));
        } else {
            account.setBalance(account.getBalance().add(amount));
        }
    }

    private void validateCategoryType(Category category, TransactionType type) {
        if (category.getType() != type) {
            throw new InvalidRequestException(
                    String.format("Category '%s' is of type '%s' and cannot be used for a '%s' transaction",
                            category.getName(), category.getType(), type));
        }
    }

    private Set<Tag> resolveTags(List<String> tagNames, User user) {
        if (tagNames == null || tagNames.isEmpty()) {
            return Collections.emptySet();
        }
        // Deduplicate incoming tag names case-insensitively while preserving order
        Map<String, String> distinctTags = new LinkedHashMap<>();
        for (String name : tagNames) {
            if (name != null && !name.isBlank()) {
                distinctTags.putIfAbsent(name.trim().toLowerCase(), name.trim());
            }
        }
        Set<Tag> tags = new HashSet<>();
        for (String tagName : distinctTags.values()) {
            Tag tag = tagRepository.findByNameIgnoreCaseAndUser(tagName, user)
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).user(user).build()));
            tags.add(tag);
        }
        return tags;
    }
}
