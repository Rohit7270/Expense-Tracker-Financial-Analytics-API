package com.expensetracker.dto.response;

import com.expensetracker.enums.Frequency;
import com.expensetracker.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringTransactionResponse {
    private Long id;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private Frequency frequency;
    private LocalDate startDate;
    private LocalDate nextDueDate;
    private boolean active;
    private String categoryName;
    private String accountName;
}
