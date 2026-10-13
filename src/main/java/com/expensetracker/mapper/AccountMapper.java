package com.expensetracker.mapper;

import com.expensetracker.dto.request.AccountRequest;
import com.expensetracker.dto.response.AccountResponse;
import com.expensetracker.entity.Account;
import com.expensetracker.entity.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Mapper for Account Entity <-> DTO conversions.
 */
@Component
public class AccountMapper {

    public AccountResponse toResponse(Account account) {
        if (account == null) {
            return null;
        }
        return AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .type(account.getType())
                .balance(account.getBalance())
                .createdAt(account.getCreatedAt())
                .build();
    }

    public Account toEntity(AccountRequest request, User user) {
        if (request == null) {
            return null;
        }
        return Account.builder()
                .name(request.getName())
                .type(request.getType())
                .balance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO)
                .user(user)
                .build();
    }
}
