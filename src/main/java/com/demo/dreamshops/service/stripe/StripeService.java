package com.demo.dreamshops.service.stripe;

import com.stripe.model.PaymentIntent;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService {

    public PaymentIntent createPaymentIntent(Double amount) throws Exception {

        Map<String, Object> params = new HashMap<>();
        params.put("amount", (long)(amount * 100)); // cents
        params.put("currency", "inr");

        return PaymentIntent.create(params);
    }
}