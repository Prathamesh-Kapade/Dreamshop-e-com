package com.demo.dreamshops.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Entity
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
   private int quantity;
   private BigDecimal price;

   @ManyToOne
   @JsonIgnore
   @JoinColumn(name="order_id")
   private Order order;

   @ManyToOne
   @JsonIgnore
   @JoinColumn(name="product_id")
   private Product product;

    public OrderItem(Order order, Product product,int quantity, BigDecimal price ) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.price = price;

    }
}
