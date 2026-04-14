package com.demo.dreamshops.dto;

import com.demo.dreamshops.model.Product;

import java.math.BigDecimal;

public class CartItemDto {
    private Long itemId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private ProductDto product;

}
