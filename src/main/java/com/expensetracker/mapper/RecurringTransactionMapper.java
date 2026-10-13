package com.expensetracker.mapper;

import com.expensetracker.dto.request.RecurringTransactionRequest;
import com.expensetracker.dto.response.RecurringTransactionResponse;
import com.expensetracker.entity.Account;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.RecurringTransaction;
import com.expensetracker.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper for RecurringTransaction Entity <-> DTO conversions.
 */
@Component
public class RecurringTransactionMapper {

    public RecurringTransactionResponse toResponse(RecurringTransaction recurring) {
        if (recurring == null) {
            return null;
        }
        return RecurringTransactionResponse.builder()
                .id(recurring.getId())
                .amount(recurring.getAmount())
                .type(recurring.getType())
                .description(recurring.getDescription())
                .frequency(recurring.getFrequency())
                .startDate(recurring.getStartDate())
                .nextDueDate(recurring.getNextDueDate())
                .active(recurring.getActive())
                .categoryName(recurring.getCategory() != null ? recurring.getCategory().getName() : null)
                .accountName(recurring.getAccount() != null ? recurring.getAccount().getName() : null)
                .build();
    }

    public RecurringTransaction toEntity(RecurringTransactionRequest request, User user, Account account, Category category) {
        if (request == null) {
            return null;
        }
        return RecurringTransaction.builder()
                .user(user)
                .account(account)
                .category(category)
                .amount(request.getAmount())
                .type(request.getType())
                .description(request.getDescription())
                .frequency(request.getFrequency())
                .startDate(request.getStartDate())
                .nextDueDate(request.getStartDate())
                .active(true)
                .build();
    }
}
