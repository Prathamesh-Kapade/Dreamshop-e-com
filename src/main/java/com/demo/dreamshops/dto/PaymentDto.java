package com.demo.dreamshops.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentDto {

    private String paymentIntentId;
    private String clientSecret;
    private String status;
    private Double amount;
}