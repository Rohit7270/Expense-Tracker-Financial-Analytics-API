package com.enterprise.expensetracker.controller;

import com.enterprise.expensetracker.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
public class AnalyticsAndDashboardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private Long foodCategoryId;
    private Long utilitiesCategoryId;
    private Long cashPaymentMethodId;
    private int currentMonth;
    private int currentYear;

    @BeforeEach
    public void setup() throws Exception {
        currentMonth = LocalDate.now().getMonthValue();
        currentYear = LocalDate.now().getYear();

        // 1. Register & Login
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("analyticsuser");
        registerRequest.setEmail("analytics@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("Analytics User");
        registerRequest.setCurrency("USD");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("analyticsuser");
        loginRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Map<?, ?> responseMap = objectMapper.readValue(responseBody, Map.class);
        token = (String) responseMap.get("token");

        // 2. Fetch categories and payment methods
        MvcResult catResult = mockMvc.perform(get("/api/categories")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        List<?> categories = objectMapper.readValue(catResult.getResponse().getContentAsString(), List.class);
        
        Map<?, ?> foodCat = (Map<?, ?>) categories.stream()
                .filter(c -> ((Map<?, ?>) c).get("name").equals("Food & Dining"))
                .findFirst().orElseThrow();
        foodCategoryId = ((Number) foodCat.get("id")).longValue();

        Map<?, ?> utilCat = (Map<?, ?>) categories.stream()
                .filter(c -> ((Map<?, ?>) c).get("name").equals("Utilities"))
                .findFirst().orElseThrow();
        utilitiesCategoryId = ((Number) utilCat.get("id")).longValue();

        MvcResult pmResult = mockMvc.perform(get("/api/payment-methods")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        List<?> methods = objectMapper.readValue(pmResult.getResponse().getContentAsString(), List.class);
        Map<?, ?> cashMethod = (Map<?, ?>) methods.stream()
                .filter(m -> ((Map<?, ?>) m).get("name").equals("Cash"))
                .findFirst().orElseThrow();
        cashPaymentMethodId = ((Number) cashMethod.get("id")).longValue();
    }

    @Test
    public void testDashboardAndAnalyticsWorkflow() throws Exception {
        // 1. Configure overall budget ($1200)
        BudgetRequest budget = new BudgetRequest();
        budget.setAmount(new BigDecimal("1200.00"));
        budget.setMonth(currentMonth);
        budget.setYear(currentYear);
        budget.setCategoryId(null);

        mockMvc.perform(post("/api/budgets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(budget)))
                .andExpect(status().isOk());

        // 2. Add Expenses ($100 Food, $50 Utilities)
        ExpenseRequest expense1 = new ExpenseRequest();
        expense1.setDescription("Grocery");
        expense1.setAmount(new BigDecimal("100.00"));
        expense1.setDate(LocalDate.now());
        expense1.setCategoryId(foodCategoryId);
        expense1.setPaymentMethodId(cashPaymentMethodId);

        mockMvc.perform(post("/api/expenses")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expense1)))
                .andExpect(status().isOk());

        ExpenseRequest expense2 = new ExpenseRequest();
        expense2.setDescription("Electric bill");
        expense2.setAmount(new BigDecimal("50.00"));
        expense2.setDate(LocalDate.now());
        expense2.setCategoryId(utilitiesCategoryId);
        expense2.setPaymentMethodId(cashPaymentMethodId);

        mockMvc.perform(post("/api/expenses")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expense2)))
                .andExpect(status().isOk());

        // 3. Add Income ($3000 Salary)
        IncomeRequest income = new IncomeRequest();
        income.setSource("Salary");
        income.setAmount(new BigDecimal("3000.00"));
        income.setDate(LocalDate.now());
        income.setCategoryId(null);

        mockMvc.perform(post("/api/income")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(income)))
                .andExpect(status().isOk());

        // 4. Verify Dashboard Statistics
        mockMvc.perform(get("/api/dashboard/stats")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(3000.00))
                .andExpect(jsonPath("$.totalExpenses").value(150.00))
                .andExpect(jsonPath("$.currentBalance").value(2850.00))
                .andExpect(jsonPath("$.monthlyBudget").value(1200.00))
                .andExpect(jsonPath("$.budgetUtilization").value(12.50))
                .andExpect(jsonPath("$.recentTransactions", hasSize(3)));

        // 5. Verify Category Distribution (Food 66.67%, Utilities 33.33%)
        mockMvc.perform(get("/api/analytics/category-distribution")
                .header("Authorization", "Bearer " + token)
                .param("month", String.valueOf(currentMonth))
                .param("year", String.valueOf(currentYear)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.categoryName=='Food & Dining')].percentage").value(66.67))
                .andExpect(jsonPath("$[?(@.categoryName=='Utilities')].percentage").value(33.33));

        // 6. Verify Savings Summary
        mockMvc.perform(get("/api/analytics/monthly-savings")
                .header("Authorization", "Bearer " + token)
                .param("month", String.valueOf(currentMonth))
                .param("year", String.valueOf(currentYear)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.income").value(3000.00))
                .andExpect(jsonPath("$.expense").value(150.00))
                .andExpect(jsonPath("$.savings").value(2850.00));
    }
}
