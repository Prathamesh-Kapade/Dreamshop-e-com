package com.demo.dreamshops.repository;

import com.demo.dreamshops.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;



public interface CartItemRepository extends JpaRepository<CartItem, Long> {

     void deleteAllByCartId(Long id);
}
