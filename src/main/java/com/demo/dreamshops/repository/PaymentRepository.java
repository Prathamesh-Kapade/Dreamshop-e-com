package com.demo.dreamshops.repository;

import com.demo.dreamshops.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentIntentId(String paymentIntentId);
}