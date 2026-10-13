package com.expensetracker.dto.request;

import com.expensetracker.enums.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountRequest {

    @NotBlank(message = "Account name is required")
    @Size(min = 1, max = 100, message = "Account name must be between 1 and 100 characters")
    private String name;

    @NotNull(message = "Account type is required")
    private AccountType type;

    @DecimalMin(value = "0.00", message = "Initial balance cannot be negative")
    @Digits(integer = 13, fraction = 2, message = "Balance format invalid")
    private BigDecimal balance;
}
