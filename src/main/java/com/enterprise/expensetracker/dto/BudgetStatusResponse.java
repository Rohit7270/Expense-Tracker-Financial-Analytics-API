package com.enterprise.expensetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetStatusResponse {
    private Long budgetId;
    private BigDecimal amount; // Budget limit
    private BigDecimal spent;
    private BigDecimal remaining;
    private double utilizationPercentage;
    private CategoryDto category;
    private String alertMessage;
}
