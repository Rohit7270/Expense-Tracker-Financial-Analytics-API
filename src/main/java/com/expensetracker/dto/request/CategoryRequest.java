package com.expensetracker.dto.request;

import com.expensetracker.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 1, max = 60, message = "Category name must be between 1 and 60 characters")
    private String name;

    @NotNull(message = "Category type is required")
    private TransactionType type;

    @Size(max = 50, message = "Icon cannot exceed 50 characters")
    private String icon;
}
