package com.enterprise.expensetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncomeResponse {
    private Long id;
    private String source;
    private BigDecimal amount;
    private LocalDate date;
    private CategoryDto category;
}
