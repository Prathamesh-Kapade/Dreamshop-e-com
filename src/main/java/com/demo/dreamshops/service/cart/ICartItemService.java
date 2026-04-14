package com.demo.dreamshops.service.cart;

import com.demo.dreamshops.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICartItemService {

    void addItemToCart(Long cartId,Long productId,int quantity);
    void removeItemFromCart(Long cartId,Long productId);
    void updateItemQuantity(Long cartId, Long productId,int quantity);

    CartItem getCartItem(Long cartId, Long productId);
}
