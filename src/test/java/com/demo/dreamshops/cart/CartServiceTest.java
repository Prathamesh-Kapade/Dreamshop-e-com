package com.demo.dreamshops.cart;

import com.demo.dreamshops.exceptions.ResourceNotFoundException;
import com.demo.dreamshops.model.Cart;
import com.demo.dreamshops.model.User;
import com.demo.dreamshops.repository.CartItemRepository;
import com.demo.dreamshops.repository.CartRepository;
import com.demo.dreamshops.service.cart.CartService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartService cartService;


    @Test
    void testGetCart_Success() {

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setTotalAmount(new BigDecimal("1000"));

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);

        Cart result = cartService.getCart(1L);

        assertNotNull(result);
        assertEquals(new BigDecimal("1000"), result.getTotalAmount());

        verify(cartRepository).save(cart);
    }


    @Test
    void testGetCart_NotFound() {

        when(cartRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.getCart(1L);
        });
    }


    @Test
    void testClearCart_Success() {

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setItems(new HashSet<>());

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);

        cartService.clearCart(1L);

        verify(cartItemRepository).deleteAllByCartId(1L);
        verify(cartRepository).deleteById(1L);
    }

    @Test
    void testClearCart_NotFound() {

        when(cartRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.clearCart(1L);
        });
    }


    @Test
    void testGetTotalPrice() {

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setTotalAmount(new BigDecimal("2000"));

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);

        BigDecimal total = cartService.getTotalPrice(1L);

        assertEquals(new BigDecimal("2000"), total);
    }


    @Test
    void testInitializeNewCart_NewCart() {

        User user = new User();
        user.setId(1L);

        when(cartRepository.findByUserId(1L)).thenReturn(null);
        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        Cart cart = cartService.initializeNewCart(user);

        assertNotNull(cart);
        assertEquals(BigDecimal.ZERO, cart.getTotalAmount());
        assertEquals(user, cart.getUser());

        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void testInitializeNewCart_ExistingCart() {

        User user = new User();
        user.setId(1L);

        Cart existingCart = new Cart();
        existingCart.setId(10L);

        when(cartRepository.findByUserId(1L)).thenReturn(existingCart);

        Cart result = cartService.initializeNewCart(user);

        assertEquals(10L, result.getId());

        verify(cartRepository, never()).save(any());
    }
}