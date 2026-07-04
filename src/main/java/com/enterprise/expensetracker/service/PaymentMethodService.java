package com.enterprise.expensetracker.service;

import com.enterprise.expensetracker.dto.PaymentMethodDto;
import com.enterprise.expensetracker.entity.PaymentMethod;
import com.enterprise.expensetracker.entity.User;
import com.enterprise.expensetracker.exception.BadRequestException;
import com.enterprise.expensetracker.exception.ResourceNotFoundException;
import com.enterprise.expensetracker.repository.PaymentMethodRepository;
import com.enterprise.expensetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentMethodService {

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityLogService activityLogService;

    public List<PaymentMethodDto> getPaymentMethodsForUser(Long userId) {
        List<PaymentMethod> methods = paymentMethodRepository.findByUserIdOrUserIsNull(userId);
        return methods.stream().map(m -> PaymentMethodDto.builder()
                .id(m.getId())
                .name(m.getName())
                .build()
        ).collect(Collectors.toList());
    }

    @Transactional
    public PaymentMethodDto createPaymentMethod(Long userId, PaymentMethodDto paymentMethodDto) {
        if (paymentMethodRepository.existsByNameIgnoreCaseAndUserIsNull(paymentMethodDto.getName()) ||
                paymentMethodRepository.existsByNameIgnoreCaseAndUserId(paymentMethodDto.getName(), userId)) {
            throw new BadRequestException("Payment method with this name already exists!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PaymentMethod paymentMethod = PaymentMethod.builder()
                .name(paymentMethodDto.getName())
                .user(user)
                .build();

        paymentMethodRepository.save(paymentMethod);

        activityLogService.log("PAYMENT_METHOD_CREATED", "Created custom payment method: " + paymentMethod.getName(), user);

        return PaymentMethodDto.builder()
                .id(paymentMethod.getId())
                .name(paymentMethod.getName())
                .build();
    }
}
