package com.demo.dreamshops.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter

public class CartDto {
    private Long cartId;
    private BigDecimal totalAmount;
    private Set<CartItemDto> items;


}
