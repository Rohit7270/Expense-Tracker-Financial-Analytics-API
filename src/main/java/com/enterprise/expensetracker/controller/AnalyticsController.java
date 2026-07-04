package com.enterprise.expensetracker.controller;

import com.enterprise.expensetracker.dto.CategoryDistributionDto;
import com.enterprise.expensetracker.dto.MonthlySummaryDto;
import com.enterprise.expensetracker.security.UserDetailsImpl;
import com.enterprise.expensetracker.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/monthly-report")
    public ResponseEntity<List<MonthlySummaryDto>> getMonthlyReport(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "6") int limit
    ) {
        List<MonthlySummaryDto> report = analyticsService.getIncomeVsExpenseSummary(userDetails.getId(), limit);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/income-vs-expense")
    public ResponseEntity<List<MonthlySummaryDto>> getIncomeVsExpense(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "6") int limit
    ) {
        List<MonthlySummaryDto> report = analyticsService.getIncomeVsExpenseSummary(userDetails.getId(), limit);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/category-distribution")
    public ResponseEntity<List<CategoryDistributionDto>> getCategoryDistribution(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        int targetMonth = month != null ? month : LocalDate.now().getMonthValue();
        int targetYear = year != null ? year : LocalDate.now().getYear();

        List<CategoryDistributionDto> distribution = analyticsService.getCategoryDistribution(
                userDetails.getId(), targetMonth, targetYear);
        return ResponseEntity.ok(distribution);
    }

    @GetMapping("/highest-spending")
    public ResponseEntity<List<CategoryDistributionDto>> getHighestSpending(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        int targetMonth = month != null ? month : LocalDate.now().getMonthValue();
        int targetYear = year != null ? year : LocalDate.now().getYear();

        List<CategoryDistributionDto> list = analyticsService.getHighestSpendingCategories(
                userDetails.getId(), targetMonth, targetYear);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/monthly-savings")
    public ResponseEntity<MonthlySummaryDto> getMonthlySavings(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        int targetMonth = month != null ? month : LocalDate.now().getMonthValue();
        int targetYear = year != null ? year : LocalDate.now().getYear();

        MonthlySummaryDto savings = analyticsService.getMonthlySavingsReport(
                userDetails.getId(), targetMonth, targetYear);
        return ResponseEntity.ok(savings);
    }
}
