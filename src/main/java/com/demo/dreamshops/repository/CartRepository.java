package com.demo.dreamshops.repository;

import com.demo.dreamshops.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart,Long> {
    Cart findByUserId(Long userId);

}
