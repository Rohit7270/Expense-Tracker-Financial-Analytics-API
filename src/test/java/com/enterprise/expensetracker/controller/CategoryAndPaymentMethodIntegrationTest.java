package com.enterprise.expensetracker.controller;

import com.enterprise.expensetracker.dto.CategoryDto;
import com.enterprise.expensetracker.dto.LoginRequest;
import com.enterprise.expensetracker.dto.PaymentMethodDto;
import com.enterprise.expensetracker.dto.RegisterRequest;
import com.enterprise.expensetracker.entity.CategoryType;
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

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
public class CategoryAndPaymentMethodIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    public void setup() throws Exception {
        // Register and login user to get token
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("catuser");
        registerRequest.setEmail("catuser@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("Category User");
        registerRequest.setCurrency("USD");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("catuser");
        loginRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Map<?, ?> responseMap = objectMapper.readValue(responseBody, Map.class);
        token = (String) responseMap.get("token");
    }

    @Test
    public void testCategoriesWorkflow() throws Exception {
        // 1. Get default categories (should contain Food & Dining, Rent & Housing etc. from data-h2.sql)
        mockMvc.perform(get("/api/categories")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$[0].name").value("Food & Dining"));

        // 2. Add custom category
        CategoryDto customCategory = CategoryDto.builder()
                .name("Custom Hobby")
                .type(CategoryType.EXPENSE)
                .build();

        mockMvc.perform(post("/api/categories")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Custom Hobby"));

        // 3. Verify total categories is now 11
        mockMvc.perform(get("/api/categories")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(11));
    }

    @Test
    public void testPaymentMethodsWorkflow() throws Exception {
        // 1. Get default payment methods (should contain Cash, Credit Card etc.)
        mockMvc.perform(get("/api/payment-methods")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].name").value("Cash"));

        // 2. Add custom payment method
        PaymentMethodDto customMethod = PaymentMethodDto.builder()
                .name("Crypto Wallet")
                .build();

        mockMvc.perform(post("/api/payment-methods")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customMethod)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Crypto Wallet"));

        // 3. Verify total payment methods is now 6
        mockMvc.perform(get("/api/payment-methods")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6));
    }
}
