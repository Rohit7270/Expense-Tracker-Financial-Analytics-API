package com.enterprise.expensetracker.controller;

import com.enterprise.expensetracker.dto.*;
import com.enterprise.expensetracker.repository.ActivityLogRepository;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
public class BudgetIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    private String token;
    private Long foodCategoryId;
    private Long cashPaymentMethodId;
    private int currentMonth;
    private int currentYear;

    @BeforeEach
    public void setup() throws Exception {
        currentMonth = LocalDate.now().getMonthValue();
        currentYear = LocalDate.now().getYear();

        // 1. Register and login
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("budgetuser");
        registerRequest.setEmail("budgetuser@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("Budget User");
        registerRequest.setCurrency("USD");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("budgetuser");
        loginRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Map<?, ?> responseMap = objectMapper.readValue(responseBody, Map.class);
        token = (String) responseMap.get("token");

        // 2. Load food category and payment method ids
        MvcResult catResult = mockMvc.perform(get("/api/categories")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        List<?> categories = objectMapper.readValue(catResult.getResponse().getContentAsString(), List.class);
        Map<?, ?> foodCat = (Map<?, ?>) categories.stream()
                .filter(c -> ((Map<?, ?>) c).get("name").equals("Food & Dining"))
                .findFirst().orElseThrow();
        foodCategoryId = ((Number) foodCat.get("id")).longValue();

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
    public void testBudgetFlowAndAlerts() throws Exception {
        // 1. Configure Category Specific Budget for Food ($200)
        BudgetRequest catBudget = new BudgetRequest();
        catBudget.setAmount(new BigDecimal("200.00"));
        catBudget.setMonth(currentMonth);
        catBudget.setYear(currentYear);
        catBudget.setCategoryId(foodCategoryId);

        mockMvc.perform(post("/api/budgets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(catBudget)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(200.00));

        // 2. Configure Overall Budget ($1000)
        BudgetRequest overallBudget = new BudgetRequest();
        overallBudget.setAmount(new BigDecimal("1000.00"));
        overallBudget.setMonth(currentMonth);
        overallBudget.setYear(currentYear);
        overallBudget.setCategoryId(null);

        mockMvc.perform(post("/api/budgets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(overallBudget)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(1000.00));

        // 3. Verify status lists 0% utilization
        mockMvc.perform(get("/api/budgets/status")
                .header("Authorization", "Bearer " + token)
                .param("month", String.valueOf(currentMonth))
                .param("year", String.valueOf(currentYear)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].spent").value(0.0))
                .andExpect(jsonPath("$[1].spent").value(0.0));

        // 4. Record expense of $150 (utilization 75% for category, 15% overall)
        ExpenseRequest expense = new ExpenseRequest();
        expense.setDescription("Dinner 1");
        expense.setAmount(new BigDecimal("150.00"));
        expense.setDate(LocalDate.now());
        expense.setCategoryId(foodCategoryId);
        expense.setPaymentMethodId(cashPaymentMethodId);

        mockMvc.perform(post("/api/expenses")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isOk());

        // 5. Record expense of $60 (total food spent = $210, utilization 105%, over threshold!)
        expense.setDescription("Dinner 2");
        expense.setAmount(new BigDecimal("60.00"));

        mockMvc.perform(post("/api/expenses")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isOk());

        // 6. Check budget status and assert alerts
        mockMvc.perform(get("/api/budgets/status")
                .header("Authorization", "Bearer " + token)
                .param("month", String.valueOf(currentMonth))
                .param("year", String.valueOf(currentYear)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.amount==200.0)].utilizationPercentage").value(105.0))
                .andExpect(jsonPath("$[?(@.amount==200.0)].alertMessage").value("Critical: Budget Exceeded!"))
                .andExpect(jsonPath("$[?(@.amount==1000.0)].spent").value(210.0))
                .andExpect(jsonPath("$[?(@.amount==1000.0)].alertMessage").value(org.hamcrest.Matchers.hasItem((Object) null)));

        // 7. Verify Activity Logs recorded the BUDGET_EXCEEDED action
        var logs = activityLogRepository.findAll();
        boolean foundExceeded = logs.stream().anyMatch(l -> l.getAction().equals("BUDGET_EXCEEDED"));
        assertTrue(foundExceeded);
    }
}
