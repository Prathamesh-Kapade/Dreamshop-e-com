package com.demo.dreamshops.service.stripe;

import com.demo.dreamshops.dto.PaymentDto;
import com.demo.dreamshops.model.Cart;
import com.demo.dreamshops.model.Order;
import com.demo.dreamshops.model.Payment;
import com.demo.dreamshops.model.User;
import com.demo.dreamshops.repository.OrderRepository;
import com.demo.dreamshops.repository.PaymentRepository;
import com.demo.dreamshops.service.cart.CartService;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final StripeService stripeService;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CartService cartService;

    // STEP 1: CREATE ORDER + PAYMENT
    public PaymentDto createOrderAndPayment(User user) throws Exception {

        // 1. Get Cart
        Cart cart = cartService.getCartByUserId(user.getId());

        if (cart == null) {
            cart = cartService.initializeNewCart(user);
        }

        BigDecimal totalAmount = cart.getTotalAmount();

        // 2. Create Order
        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING"); // make sure field exists

        orderRepository.save(order);

        // 3. Create PaymentIntent (Stripe)
        PaymentIntent intent = stripeService.createPaymentIntent(totalAmount.doubleValue());

        // 4. Save Payment
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(totalAmount.doubleValue());
        payment.setPaymentIntentId(intent.getId());
        payment.setClientSecret(intent.getClientSecret());
        payment.setStatus("CREATED");

        paymentRepository.save(payment);

        // 5. Convert to DTO
        PaymentDto dto = new PaymentDto();
        dto.setPaymentIntentId(payment.getPaymentIntentId());
        dto.setClientSecret(payment.getClientSecret());
        dto.setStatus(payment.getStatus());
        dto.setAmount(payment.getAmount());

        return dto;
    }

    //  STEP 2: CONFIRM PAYMENT
    public ResponseEntity<?> confirmPayment(String paymentIntentId) throws Exception {

        PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);

        if ("succeeded".equals(intent.getStatus())) {

            Payment payment = paymentRepository
                    .findByPaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            payment.setStatus("SUCCESS");

            Order order = payment.getOrder();
            order.setStatus("CONFIRMED");

            orderRepository.save(order);
            paymentRepository.save(payment);

            return ResponseEntity.ok("Payment Successful");
        }

        return ResponseEntity.badRequest().body("Payment not completed");
    }
}