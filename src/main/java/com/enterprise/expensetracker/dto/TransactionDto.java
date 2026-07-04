package com.enterprise.expensetracker.dto;

import com.enterprise.expensetracker.entity.TransactionType;
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
public class TransactionDto {
    private Long id;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private LocalDate date;
}
