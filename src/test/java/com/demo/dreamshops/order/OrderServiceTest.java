package com.demo.dreamshops.order;

import com.demo.dreamshops.dto.OrderDto;
import com.demo.dreamshops.enums.OrderStatus;
import com.demo.dreamshops.exceptions.ResourceNotFoundException;
import com.demo.dreamshops.model.*;
import com.demo.dreamshops.repository.OrderRepository;
import com.demo.dreamshops.repository.ProductRepository;
import com.demo.dreamshops.service.cart.CartService;
import com.demo.dreamshops.service.order.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartService cartService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private OrderService orderService;

    // ✅ Helper method to create mock cart
    private Cart createMockCart() {
        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(101L);
        product.setInventory(10);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setUnitPrice(new BigDecimal("500"));

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        cart.setItems(Set.of(cartItem));

        return cart;
    }

    // ===============================
    // ✅ TEST: placeOrder SUCCESS
    // ===============================
    @Test
    void testPlaceOrder_Success() {

        Cart cart = createMockCart();

        when(cartService.getCartByUserId(1L)).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        Order order = orderService.placeOrder(1L);

        assertNotNull(order);
        assertEquals(OrderStatus.PENDING, order.getOrderStatus());
        assertEquals(1, order.getOrderItems().size());

        // total = 500 * 2 = 1000
        assertEquals(new BigDecimal("1000"), order.getTotalAmount());

        verify(productRepository, times(1)).save(any(Product.class));
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(cartService, times(1)).clearCart(1L);
    }

    // ===============================
    // ❌ TEST: placeOrder CART NOT FOUND
    // ===============================
    @Test
    void testPlaceOrder_CartNotFound() {

        when(cartService.getCartByUserId(1L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.placeOrder(1L);
        });

        verify(orderRepository, never()).save(any());
    }

    // ===============================
    // ✅ TEST: getOrder SUCCESS
    // ===============================
    @Test
    void testGetOrder_Success() {

        Order order = new Order();
        order.setId(1L);

        OrderDto dto = new OrderDto();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(modelMapper.map(order, OrderDto.class)).thenReturn(dto);

        OrderDto result = orderService.getOrder(1L);

        assertNotNull(result);
        verify(orderRepository).findById(1L);
    }

    // ===============================
    // ❌ TEST: getOrder NOT FOUND
    // ===============================
    @Test
    void testGetOrder_NotFound() {

        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.getOrder(1L);
        });
    }

    // ===============================
    // ✅ TEST: getUserOrders
    // ===============================
    @Test
    void testGetUserOrders() {

        Order order1 = new Order();
        Order order2 = new Order();

        OrderDto dto1 = new OrderDto();
        OrderDto dto2 = new OrderDto();

        when(orderRepository.findByUserId(1L))
                .thenReturn(List.of(order1, order2));

        when(modelMapper.map(order1, OrderDto.class)).thenReturn(dto1);
        when(modelMapper.map(order2, OrderDto.class)).thenReturn(dto2);

        List<OrderDto> result = orderService.getUserOrders(1L);

        assertEquals(2, result.size());
        verify(orderRepository).findByUserId(1L);
    }
}