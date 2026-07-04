package com.enterprise.expensetracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileRequest {
    @NotBlank
    @Size(max = 100)
    private String fullName;

    @NotBlank
    @Size(max = 10)
    private String currency;
}
