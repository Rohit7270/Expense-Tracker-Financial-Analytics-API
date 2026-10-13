package com.expensetracker.mapper;

import com.expensetracker.dto.request.TransactionRequest;
import com.expensetracker.dto.response.TransactionResponse;
import com.expensetracker.entity.Account;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Tag;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

/**
 * Mapper for Transaction Entity <-> DTO conversions.
 */
@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .categoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null)
                .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                .accountId(transaction.getAccount() != null ? transaction.getAccount().getId() : null)
                .accountName(transaction.getAccount() != null ? transaction.getAccount().getName() : null)
                .tags(transaction.getTags() != null 
                        ? transaction.getTags().stream().map(Tag::getName).toList() 
                        : Collections.emptyList())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    public Transaction toEntity(TransactionRequest request, User user, Category category, Account account, Set<Tag> tags) {
        if (request == null) {
            return null;
        }
        return Transaction.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate())
                .category(category)
                .account(account)
                .user(user)
                .tags(tags != null ? tags : Collections.emptySet())
                .build();
    }
}
