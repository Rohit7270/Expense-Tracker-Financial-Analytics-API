package com.expensetracker.controller;

import com.expensetracker.dto.request.RecurringTransactionRequest;
import com.expensetracker.dto.response.RecurringTransactionResponse;
import com.expensetracker.service.RecurringTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recurring-transactions")
@RequiredArgsConstructor
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    @PostMapping
    public ResponseEntity<RecurringTransactionResponse> createRecurring(@Valid @RequestBody RecurringTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recurringTransactionService.createRecurring(request));
    }

    @GetMapping
    public ResponseEntity<List<RecurringTransactionResponse>> getAllRecurring() {
        return ResponseEntity.ok(recurringTransactionService.getAllRecurring());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecurringTransactionResponse> getRecurring(@PathVariable Long id) {
        return ResponseEntity.ok(recurringTransactionService.getRecurringById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecurringTransactionResponse> updateRecurring(@PathVariable Long id, @Valid @RequestBody RecurringTransactionRequest request) {
        return ResponseEntity.ok(recurringTransactionService.updateRecurring(id, request));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<RecurringTransactionResponse> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(recurringTransactionService.toggleActive(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecurring(@PathVariable Long id) {
        recurringTransactionService.deleteRecurring(id);
        return ResponseEntity.noContent().build();
    }
}
