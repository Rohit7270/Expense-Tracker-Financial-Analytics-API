package com.enterprise.expensetracker.controller;

import com.enterprise.expensetracker.dto.DashboardResponse;
import com.enterprise.expensetracker.security.UserDetailsImpl;
import com.enterprise.expensetracker.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardResponse> getDashboardStats(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        DashboardResponse stats = dashboardService.getDashboardStats(userDetails.getId());
        return ResponseEntity.ok(stats);
    }
}
