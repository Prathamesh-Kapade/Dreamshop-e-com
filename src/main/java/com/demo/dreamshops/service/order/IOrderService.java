package com.demo.dreamshops.service.order;

import com.demo.dreamshops.dto.OrderDto;
import com.demo.dreamshops.model.Order;

import java.util.List;

public interface IOrderService {
    Order placeOrder(Long userId);
    OrderDto getOrder(Long orderId);
    List<OrderDto> getUserOrders(Long userId);

    OrderDto getOrderById(Long orderId);

    OrderDto convertToDto(Order order);
}
