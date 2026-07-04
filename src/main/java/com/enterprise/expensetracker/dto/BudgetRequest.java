package com.enterprise.expensetracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetRequest {
    @NotNull
    @DecimalMin(value = "0.01", message = "Budget limit must be greater than zero")
    private BigDecimal amount;

    @NotNull
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;

    @NotNull
    @Min(value = 2000, message = "Year must be valid")
    private Integer year;

    private Long categoryId; // Nullable for overall monthly budget
}
