package com.demo.dreamshops.stripe;

import com.demo.dreamshops.dto.PaymentDto;
import com.demo.dreamshops.model.*;
import com.demo.dreamshops.repository.OrderRepository;
import com.demo.dreamshops.repository.PaymentRepository;
import com.demo.dreamshops.service.cart.CartService;
import com.demo.dreamshops.service.stripe.PaymentService;
import com.demo.dreamshops.service.stripe.StripeService;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private StripeService stripeService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private PaymentService paymentService;

    // ===============================
    // ✅ TEST: createOrderAndPayment (cart exists)
    // ===============================
    @Test
    void testCreateOrderAndPayment_CartExists() throws Exception {

        User user = new User();
        user.setId(1L);

        Cart cart = new Cart();
        cart.setTotalAmount(new BigDecimal("1000"));

        PaymentIntent intent = mock(PaymentIntent.class);

        when(cartService.getCartByUserId(1L)).thenReturn(cart);
        when(stripeService.createPaymentIntent(1000.0)).thenReturn(intent);
        when(intent.getId()).thenReturn("pi_123");
        when(intent.getClientSecret()).thenReturn("secret_123");

        PaymentDto dto = paymentService.createOrderAndPayment(user);

        assertNotNull(dto);
        assertEquals("pi_123", dto.getPaymentIntentId());

        verify(orderRepository).save(any(Order.class));
        verify(paymentRepository).save(any(Payment.class));
    }

    // ===============================
    // ✅ TEST: createOrderAndPayment (cart NOT exists)
    // ===============================
    @Test
    void testCreateOrderAndPayment_NewCartCreated() throws Exception {

        User user = new User();
        user.setId(1L);

        Cart newCart = new Cart();
        newCart.setTotalAmount(new BigDecimal("500"));

        PaymentIntent intent = mock(PaymentIntent.class);

        when(cartService.getCartByUserId(1L)).thenReturn(null);
        when(cartService.initializeNewCart(user)).thenReturn(newCart);
        when(stripeService.createPaymentIntent(500.0)).thenReturn(intent);

        when(intent.getId()).thenReturn("pi_456");
        when(intent.getClientSecret()).thenReturn("secret_456");

        PaymentDto dto = paymentService.createOrderAndPayment(user);

        assertEquals("pi_456", dto.getPaymentIntentId());

        verify(cartService).initializeNewCart(user);
    }

    // ===============================
    // ✅ TEST: confirmPayment SUCCESS
    // ===============================
    @Test
    void testConfirmPayment_Success() throws Exception {

        String paymentIntentId = "pi_123";

        PaymentIntent intent = mock(PaymentIntent.class);
        when(intent.getStatus()).thenReturn("succeeded");

        Payment payment = new Payment();
        Order order = new Order();
        payment.setOrder(order);

        try (MockedStatic<PaymentIntent> mocked = mockStatic(PaymentIntent.class)) {
            mocked.when(() -> PaymentIntent.retrieve(paymentIntentId))
                    .thenReturn(intent);

            when(paymentRepository.findByPaymentIntentId(paymentIntentId))
                    .thenReturn(Optional.of(payment));

            ResponseEntity<?> response = paymentService.confirmPayment(paymentIntentId);

            assertEquals("Payment Successful", response.getBody());
            verify(orderRepository).save(order);
            verify(paymentRepository).save(payment);
        }
    }

    // ===============================
    // ❌ TEST: confirmPayment NOT COMPLETED
    // ===============================
    @Test
    void testConfirmPayment_NotCompleted() throws Exception {

        String paymentIntentId = "pi_123";

        PaymentIntent intent = mock(PaymentIntent.class);
        when(intent.getStatus()).thenReturn("pending");

        try (MockedStatic<PaymentIntent> mocked = mockStatic(PaymentIntent.class)) {
            mocked.when(() -> PaymentIntent.retrieve(paymentIntentId))
                    .thenReturn(intent);

            ResponseEntity<?> response = paymentService.confirmPayment(paymentIntentId);

            assertEquals(400, response.getStatusCodeValue());
        }
    }

    // ===============================
    // ❌ TEST: confirmPayment NOT FOUND
    // ===============================
    @Test
    void testConfirmPayment_PaymentNotFound() throws Exception {

        String paymentIntentId = "pi_123";

        PaymentIntent intent = mock(PaymentIntent.class);
        when(intent.getStatus()).thenReturn("succeeded");

        try (MockedStatic<PaymentIntent> mocked = mockStatic(PaymentIntent.class)) {
            mocked.when(() -> PaymentIntent.retrieve(paymentIntentId))
                    .thenReturn(intent);

            when(paymentRepository.findByPaymentIntentId(paymentIntentId))
                    .thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> {
                paymentService.confirmPayment(paymentIntentId);
            });
        }
    }
}