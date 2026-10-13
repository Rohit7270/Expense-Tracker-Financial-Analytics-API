package com.expensetracker.controller;

import com.expensetracker.dto.response.DashboardSummaryResponse;
import com.expensetracker.service.AnalyticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("GET /api/analytics/monthly-report - Invalid month (> 12) returns 400 Bad Request")
    void testMonthlyReportInvalidMonth() throws Exception {
        mockMvc.perform(get("/api/analytics/monthly-report")
                        .param("month", "13")
                        .param("year", "2026")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Month must be between 1 and 12"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("GET /api/analytics/monthly-report - Invalid year (< 1900) returns 400 Bad Request")
    void testMonthlyReportInvalidYear() throws Exception {
        mockMvc.perform(get("/api/analytics/monthly-report")
                        .param("month", "5")
                        .param("year", "1800")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Year must be between 1900 and 2100"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("GET /api/analytics/spending-by-category - startDate after endDate returns 400 Bad Request")
    void testSpendingByCategoryInvalidDateRange() throws Exception {
        mockMvc.perform(get("/api/analytics/spending-by-category")
                        .param("startDate", "2026-10-20")
                        .param("endDate", "2026-10-10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("startDate cannot be after endDate"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("GET /api/analytics/spending-by-category - Missing required parameters returns 400 Bad Request")
    void testSpendingByCategoryMissingParam() throws Exception {
        mockMvc.perform(get("/api/analytics/spending-by-category")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("POST /api/analytics/dashboard - Unsupported method returns 405 Method Not Allowed")
    void testUnsupportedMethodReturns405() throws Exception {
        mockMvc.perform(post("/api/analytics/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("GET /api/unknown-endpoint - Unknown route returns 404 Not Found")
    void testUnknownRouteReturns404() throws Exception {
        mockMvc.perform(get("/api/unknown-endpoint")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/analytics/dashboard - Unauthenticated request returns 401 Unauthorized")
    void testUnauthenticatedReturns401() throws Exception {
        mockMvc.perform(get("/api/analytics/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Unauthorized: valid JWT token required"));
    }
}
