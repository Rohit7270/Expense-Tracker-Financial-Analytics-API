package com.expensetracker.service;

import com.expensetracker.dto.request.AccountRequest;
import com.expensetracker.dto.response.AccountResponse;
import com.expensetracker.entity.Account;
import com.expensetracker.entity.User;
import com.expensetracker.exception.InvalidRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.mapper.AccountMapper;
import com.expensetracker.repository.AccountRepository;
import com.expensetracker.repository.RecurringTransactionRepository;
import com.expensetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final RecurringTransactionRepository recurringTransactionRepository;
    private final SecurityUtils securityUtils;
    private final AccountMapper accountMapper;

    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        User user = securityUtils.getCurrentUser();
        Account account = accountMapper.toEntity(request, user);
        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        User user = securityUtils.getCurrentUser();
        return accountRepository.findByUser(user).stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id) {
        return accountMapper.toResponse(getOwnedAccount(id));
    }

    @Transactional
    public AccountResponse updateAccount(Long id, AccountRequest request) {
        Account account = getOwnedAccount(id);
        account.setName(request.getName());
        account.setType(request.getType());
        if (request.getBalance() != null) {
            account.setBalance(request.getBalance());
        }
        return accountMapper.toResponse(account);
    }

    @Transactional
    public void deleteAccount(Long id) {
        Account account = getOwnedAccount(id);
        if (transactionRepository.existsByAccount(account)) {
            throw new InvalidRequestException("Cannot delete account as it has associated transactions");
        }
        if (recurringTransactionRepository.existsByAccount(account)) {
            throw new InvalidRequestException("Cannot delete account as it has associated recurring transaction templates");
        }
        accountRepository.delete(account);
    }

    protected Account getOwnedAccount(Long id) {
        User user = securityUtils.getCurrentUser();
        return accountRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
    }
}
