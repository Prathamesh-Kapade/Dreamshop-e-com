package com.demo.dreamshops.service.cart;

import com.demo.dreamshops.exceptions.ResourceNotFoundException;
import com.demo.dreamshops.model.Cart;
import com.demo.dreamshops.model.User;
import com.demo.dreamshops.repository.CartItemRepository;
import com.demo.dreamshops.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService implements ICartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    public Cart getCart(Long id) {
        log.info("Fetching cart | cartId: {}", id);

        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        BigDecimal totalAmount = cart.getTotalAmount();
        cart.setTotalAmount(totalAmount);

        return cartRepository.save(cart);
    }

    @Transactional
    @Override
    public void clearCart(Long id) {
        log.info("Clearing cart | cartId: {}", id);

        Cart cart = getCart(id);

        cartItemRepository.deleteAllByCartId(id);
        cart.getItems().clear();

        cartRepository.deleteById(id);
    }

    @Override
    public BigDecimal getTotalPrice(Long id) {
        Cart cart = getCart(id);
        return cart.getTotalAmount();
    }

    @Override
    public Cart initializeNewCart(User user) {

        return Optional.ofNullable(getCartByUserId(user.getId()))
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    cart.setTotalAmount(BigDecimal.ZERO);
                    cart.setItems(new HashSet<>());

                    user.setCart(cart);

                    return cartRepository.save(cart);
                });
    }

    @Override
    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }
}