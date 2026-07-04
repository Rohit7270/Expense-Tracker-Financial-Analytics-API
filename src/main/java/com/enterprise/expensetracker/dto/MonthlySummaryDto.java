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
public class MonthlySummaryDto {
    private Integer month;
    private Integer year;
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal savings;
}
