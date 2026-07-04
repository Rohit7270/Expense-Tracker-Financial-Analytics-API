package com.enterprise.expensetracker.repository;

import com.enterprise.expensetracker.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    @Query("SELECT p FROM PaymentMethod p WHERE p.user IS NULL OR p.user.id = :userId")
    List<PaymentMethod> findByUserIdOrUserIsNull(@Param("userId") Long userId);

    Boolean existsByNameIgnoreCaseAndUserIsNull(String name);
    Boolean existsByNameIgnoreCaseAndUserId(String name, Long userId);
}
