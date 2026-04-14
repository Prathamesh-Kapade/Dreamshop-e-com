package com.demo.dreamshops.controller;

import com.demo.dreamshops.dto.PaymentDto;
import com.demo.dreamshops.model.User;
import com.demo.dreamshops.response.ApiResponse;
import com.demo.dreamshops.service.stripe.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@AuthenticationPrincipal User user) throws Exception {

        // ✅ check user
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("User not authenticated", null));
        }

        PaymentDto payment = paymentService.createOrderAndPayment(user);

        return ResponseEntity.ok(payment);
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestParam String paymentIntentId) throws Exception {

        return paymentService.confirmPayment(paymentIntentId);
    }
}