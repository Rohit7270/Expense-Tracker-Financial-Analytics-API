package com.enterprise.expensetracker.controller;

import com.enterprise.expensetracker.dto.*;
import com.enterprise.expensetracker.entity.CategoryType;
import com.enterprise.expensetracker.entity.TransactionType;
import com.enterprise.expensetracker.repository.TransactionRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
public class IncomeAndExpenseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    private String token;
    private Long foodCategoryId;
    private Long cashPaymentMethodId;

    @BeforeEach
    public void setup() throws Exception {
        // 1. Register & Login
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("ledgeruser");
        registerRequest.setEmail("ledgeruser@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("Ledger User");
        registerRequest.setCurrency("USD");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("ledgeruser");
        loginRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Map<?, ?> responseMap = objectMapper.readValue(responseBody, Map.class);
        token = (String) responseMap.get("token");

        // 2. Fetch categories to get Food & Dining category ID
        MvcResult catResult = mockMvc.perform(get("/api/categories")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String catBody = catResult.getResponse().getContentAsString();
        List<?> categories = objectMapper.readValue(catBody, List.class);
        Map<?, ?> foodCat = (Map<?, ?>) categories.stream()
                .filter(c -> ((Map<?, ?>) c).get("name").equals("Food & Dining"))
                .findFirst().orElseThrow();
        foodCategoryId = ((Number) foodCat.get("id")).longValue();

        // 3. Fetch payment methods to get Cash ID
        MvcResult pmResult = mockMvc.perform(get("/api/payment-methods")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String pmBody = pmResult.getResponse().getContentAsString();
        List<?> methods = objectMapper.readValue(pmBody, List.class);
        Map<?, ?> cashMethod = (Map<?, ?>) methods.stream()
                .filter(m -> ((Map<?, ?>) m).get("name").equals("Cash"))
                .findFirst().orElseThrow();
        cashPaymentMethodId = ((Number) cashMethod.get("id")).longValue();
    }

    @Test
    public void testExpenseAndIncomeWorkflow() throws Exception {
        // 1. Add Expense
        ExpenseRequest expenseRequest = new ExpenseRequest();
        expenseRequest.setDescription("Dinner Buffet");
        expenseRequest.setAmount(new BigDecimal("45.50"));
        expenseRequest.setDate(LocalDate.now());
        expenseRequest.setCategoryId(foodCategoryId);
        expenseRequest.setPaymentMethodId(cashPaymentMethodId);

        MvcResult expenseResult = mockMvc.perform(post("/api/expenses")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expenseRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.description").value("Dinner Buffet"))
                .andExpect(jsonPath("$.amount").value(45.50))
                .andReturn();

        Map<?, ?> expenseResponse = objectMapper.readValue(expenseResult.getResponse().getContentAsString(), Map.class);
        Long expenseId = ((Number) expenseResponse.get("id")).longValue();

        // Verify consolidated transaction was logged automatically
        var expTx = transactionRepository.findByTypeAndReferenceId(TransactionType.EXPENSE, expenseId);
        assertTrue(expTx.isPresent());
        assertEquals(new BigDecimal("45.50"), expTx.get().getAmount());
        assertEquals("Dinner Buffet", expTx.get().getDescription());

        // 2. Add Income
        IncomeRequest incomeRequest = new IncomeRequest();
        incomeRequest.setSource("Salary Credit");
        incomeRequest.setAmount(new BigDecimal("2500.00"));
        incomeRequest.setDate(LocalDate.now());
        incomeRequest.setCategoryId(null); // Optional category

        MvcResult incomeResult = mockMvc.perform(post("/api/income")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incomeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.source").value("Salary Credit"))
                .andExpect(jsonPath("$.amount").value(2500.00))
                .andReturn();

        Map<?, ?> incomeResponse = objectMapper.readValue(incomeResult.getResponse().getContentAsString(), Map.class);
        Long incomeId = ((Number) incomeResponse.get("id")).longValue();

        // Verify consolidated transaction was logged automatically
        var incTx = transactionRepository.findByTypeAndReferenceId(TransactionType.INCOME, incomeId);
        assertTrue(incTx.isPresent());
        assertEquals(new BigDecimal("2500.00"), incTx.get().getAmount());

        // 3. Search Expenses with Filter
        mockMvc.perform(get("/api/expenses")
                .header("Authorization", "Bearer " + token)
                .param("categoryId", foodCategoryId.toString())
                .param("search", "Dinner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(expenseId));

        // 4. Update Expense
        expenseRequest.setDescription("Premium Dinner");
        expenseRequest.setAmount(new BigDecimal("60.00"));

        mockMvc.perform(put("/api/expenses/" + expenseId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expenseRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Premium Dinner"))
                .andExpect(jsonPath("$.amount").value(60.00));

        // Verify consolidated transaction updated
        expTx = transactionRepository.findByTypeAndReferenceId(TransactionType.EXPENSE, expenseId);
        assertEquals(new BigDecimal("60.00"), expTx.get().getAmount());
        assertEquals("Premium Dinner", expTx.get().getDescription());

        // 5. Delete Expense
        mockMvc.perform(delete("/api/expenses/" + expenseId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Verify expense is deleted and corresponding consolidated transaction deleted too
        mockMvc.perform(get("/api/expenses/" + expenseId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        var expTxDeleted = transactionRepository.findByTypeAndReferenceId(TransactionType.EXPENSE, expenseId);
        assertFalse(expTxDeleted.isPresent());
    }
}
