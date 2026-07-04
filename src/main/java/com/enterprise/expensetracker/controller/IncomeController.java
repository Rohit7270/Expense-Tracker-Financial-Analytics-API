package com.enterprise.expensetracker.controller;

import com.enterprise.expensetracker.dto.IncomeRequest;
import com.enterprise.expensetracker.dto.IncomeResponse;
import com.enterprise.expensetracker.dto.MessageResponse;
import com.enterprise.expensetracker.security.UserDetailsImpl;
import com.enterprise.expensetracker.service.IncomeService;
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
@RequestMapping("/api/income")
public class IncomeController {

    @Autowired
    private IncomeService incomeService;

    @GetMapping
    public ResponseEntity<Page<IncomeResponse>> getIncome(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) Long categoryId,
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

        Page<IncomeResponse> response = incomeService.searchIncome(
                userDetails.getId(), categoryId, startDate, endDate, search, pageable);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncomeResponse> getIncomeById(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id
    ) {
        IncomeResponse income = incomeService.getIncomeById(id, userDetails.getId());
        return ResponseEntity.ok(income);
    }

    @PostMapping
    public ResponseEntity<IncomeResponse> createIncome(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody IncomeRequest request
    ) {
        IncomeResponse created = incomeService.createIncome(userDetails.getId(), request);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncomeResponse> updateIncome(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id,
            @Valid @RequestBody IncomeRequest request
    ) {
        IncomeResponse updated = incomeService.updateIncome(id, userDetails.getId(), request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteIncome(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id
    ) {
        incomeService.deleteIncome(id, userDetails.getId());
        return ResponseEntity.ok(new MessageResponse("Income deleted successfully!"));
    }
}
