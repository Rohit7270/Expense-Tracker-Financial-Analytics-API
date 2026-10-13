package com.expensetracker.controller;

import com.expensetracker.dto.response.CategorySpendingResponse;
import com.expensetracker.dto.response.DashboardSummaryResponse;
import com.expensetracker.dto.response.MonthlyReportResponse;
import com.expensetracker.exception.InvalidRequestException;
import com.expensetracker.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardSummaryResponse> getDashboard() {
        return ResponseEntity.ok(analyticsService.getDashboardSummary());
    }

    @GetMapping("/monthly-report")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        if (month != null && (month < 1 || month > 12)) {
            throw new InvalidRequestException("Month must be between 1 and 12");
        }
        if (year != null && (year < 1900 || year > 2100)) {
            throw new InvalidRequestException("Year must be between 1900 and 2100");
        }
        YearMonth now = YearMonth.now();
        int m = (month != null) ? month : now.getMonthValue();
        int y = (year != null) ? year : now.getYear();
        return ResponseEntity.ok(analyticsService.getMonthlyReport(m, y));
    }

    @GetMapping("/spending-by-category")
    public ResponseEntity<List<CategorySpendingResponse>> getSpendingByCategory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new InvalidRequestException("startDate cannot be after endDate");
        }
        return ResponseEntity.ok(analyticsService.getSpendingByCategory(startDate, endDate));
    }
}
