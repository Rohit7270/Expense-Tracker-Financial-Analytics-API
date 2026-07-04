package com.enterprise.expensetracker.controller;

import com.enterprise.expensetracker.dto.ExpenseRequest;
import com.enterprise.expensetracker.dto.ExpenseResponse;
import com.enterprise.expensetracker.dto.MessageResponse;
import com.enterprise.expensetracker.security.UserDetailsImpl;
import com.enterprise.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<Page<ExpenseResponse>> getExpenses(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long paymentMethodId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ExpenseResponse> response = expenseService.searchExpenses(
                userDetails.getId(), categoryId, paymentMethodId, startDate, endDate, search, pageable);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id
    ) {
        ExpenseResponse expense = expenseService.getExpenseById(id, userDetails.getId());
        return ResponseEntity.ok(expense);
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ExpenseRequest request
    ) {
        ExpenseResponse created = expenseService.createExpense(userDetails.getId(), request);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request
    ) {
        ExpenseResponse updated = expenseService.updateExpense(id, userDetails.getId(), request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExpense(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id
    ) {
        expenseService.deleteExpense(id, userDetails.getId());
        return ResponseEntity.ok(new MessageResponse("Expense deleted successfully!"));
    }
}
