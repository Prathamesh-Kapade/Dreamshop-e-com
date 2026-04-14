package com.demo.dreamshops.service.order;

import com.demo.dreamshops.dto.OrderDto;
import com.demo.dreamshops.enums.OrderStatus;
import com.demo.dreamshops.exceptions.ResourceNotFoundException;
import com.demo.dreamshops.model.Cart;
import com.demo.dreamshops.model.Order;
import com.demo.dreamshops.model.OrderItem;
import com.demo.dreamshops.model.Product;
import com.demo.dreamshops.repository.OrderRepository;
import com.demo.dreamshops.repository.ProductRepository;
import com.demo.dreamshops.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final ModelMapper modelMapper;

    @Transactional
    @Override
    public Order placeOrder(Long userId) {

        log.info("Placing order for userId: {}", userId);

        Cart cart = cartService.getCartByUserId(userId);

        if (cart == null) {
            log.warn("Cart not found for userId: {}", userId);
            throw new ResourceNotFoundException("Cart not found");
        }

        Order order = createOrder(cart);
        log.info("Order object created for userId: {}", userId);

        List<OrderItem> orderItemList = createOrderItems(order, cart);
        order.setOrderItems(new HashSet<>(orderItemList));

        BigDecimal totalAmount = calculateTotalAmount(orderItemList);
        order.setTotalAmount(totalAmount);

        log.info("Total order amount calculated: {}", totalAmount);

        Order savedOrder = orderRepository.save(order);
        log.info("Order saved successfully | orderId: {}", savedOrder);

        cartService.clearCart(cart.getId());
        log.info("Cart cleared after order placement | cartId: {}", cart.getId());

        return savedOrder;
    }

    private Order createOrder(Cart cart) {
        log.info("Creating order for userId: {}", cart.getUser().getId());

        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDate.now());

        return order;
    }

    private List<OrderItem> createOrderItems(Order order, Cart cart) {

        log.info("Creating order items for order");

        return cart.getItems().stream().map(cartItem -> {

            Product product = cartItem.getProduct();

            log.info("Processing product | productId: {}, quantity: {}",
                    product.getId(), cartItem.getQuantity());

            // Update inventory
            int updatedInventory = product.getInventory() - cartItem.getQuantity();
            product.setInventory(updatedInventory);
            productRepository.save(product);

            log.info("Inventory updated | productId: {}, remaining: {}",
                    product.getId(), updatedInventory);

            return new OrderItem(
                    order,
                    product,
                    cartItem.getQuantity(),
                    cartItem.getUnitPrice()
            );

        }).toList();
    }

    private BigDecimal calculateTotalAmount(List<OrderItem> orderItemList) {

        BigDecimal total = orderItemList.stream()
                .map(item -> item.getPrice()
                        .multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("Calculated total order amount: {}", total);
        return total;
    }

    @Override
    public OrderDto getOrder(Long orderId) {

        log.info("Fetching order by ID: {}", orderId);

        return orderRepository.findById(orderId)
                .map(order -> {
                    log.info("Order found | orderId: {}", orderId);
                    return convertToDto(order);
                })
                .orElseThrow(() -> {
                    log.warn("Order not found | orderId: {}", orderId);
                    return new ResourceNotFoundException("Order not found");
                });
    }

    @Override
    public List<OrderDto> getUserOrders(Long userId) {

        log.info("Fetching orders for userId: {}", userId);

        List<Order> orders = orderRepository.findByUserId(userId);

        log.info("Total orders found: {} for userId: {}", orders.size(), userId);

        return orders.stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public OrderDto getOrderById(Long orderId) {
        log.warn("getOrderById method not implemented yet | orderId: {}", orderId);
        return null;
    }

    @Override
    public OrderDto convertToDto(Order order) {

        log.info("Converting Order to DTO | orderId: {}", order);

        return modelMapper.map(order, OrderDto.class);
    }
}