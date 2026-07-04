package com.enterprise.expensetracker.controller;

import com.enterprise.expensetracker.dto.BudgetRequest;
import com.enterprise.expensetracker.dto.BudgetResponse;
import com.enterprise.expensetracker.dto.BudgetStatusResponse;
import com.enterprise.expensetracker.dto.MessageResponse;
import com.enterprise.expensetracker.security.UserDetailsImpl;
import com.enterprise.expensetracker.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getBudgets(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<BudgetResponse> budgets = budgetService.getBudgetsForUser(userDetails.getId());
        return ResponseEntity.ok(budgets);
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody BudgetRequest request
    ) {
        BudgetResponse created = budgetService.createBudget(userDetails.getId(), request);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request
    ) {
        BudgetResponse updated = budgetService.updateBudget(id, userDetails.getId(), request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBudget(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id
    ) {
        budgetService.deleteBudget(id, userDetails.getId());
        return ResponseEntity.ok(new MessageResponse("Budget deleted successfully!"));
    }

    @GetMapping("/status")
    public ResponseEntity<List<BudgetStatusResponse>> getBudgetStatus(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        int targetMonth = month != null ? month : LocalDate.now().getMonthValue();
        int targetYear = year != null ? year : LocalDate.now().getYear();

        List<BudgetStatusResponse> status = budgetService.getBudgetStatus(userDetails.getId(), targetMonth, targetYear);
        return ResponseEntity.ok(status);
    }
}
