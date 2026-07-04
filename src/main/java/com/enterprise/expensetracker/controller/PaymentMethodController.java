package com.enterprise.expensetracker.controller;

import com.enterprise.expensetracker.dto.PaymentMethodDto;
import com.enterprise.expensetracker.security.UserDetailsImpl;
import com.enterprise.expensetracker.service.PaymentMethodService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment-methods")
public class PaymentMethodController {

    @Autowired
    private PaymentMethodService paymentMethodService;

    @GetMapping
    public ResponseEntity<List<PaymentMethodDto>> getPaymentMethods(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<PaymentMethodDto> methods = paymentMethodService.getPaymentMethodsForUser(userDetails.getId());
        return ResponseEntity.ok(methods);
    }

    @PostMapping
    public ResponseEntity<PaymentMethodDto> createPaymentMethod(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody PaymentMethodDto paymentMethodDto
    ) {
        PaymentMethodDto created = paymentMethodService.createPaymentMethod(userDetails.getId(), paymentMethodDto);
        return ResponseEntity.ok(created);
    }
}
