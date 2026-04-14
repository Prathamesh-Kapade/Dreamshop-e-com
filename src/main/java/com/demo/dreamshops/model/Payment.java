package com.demo.dreamshops.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String paymentIntentId;
    private String clientSecret;

    private String status; // CREATED, SUCCESS, FAILED

    private Double amount;

    @OneToOne
    private Order order;
}