package com.demo.dreamshops.controller;

import com.demo.dreamshops.dto.OrderDto;
import com.demo.dreamshops.exceptions.ResourceNotFoundException;
import com.demo.dreamshops.model.Order;
import com.demo.dreamshops.response.ApiResponse;
import com.demo.dreamshops.service.order.IOrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Generated;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@Tag(name="Order APIs")
@RequestMapping("${api.prefix}/orders")
public class OrderController {
 private final IOrderService orderService;

 @PostMapping("/order")
 public ResponseEntity<ApiResponse> createOrder(@RequestParam Long userId){
     log.info("API CALL: Create order for userId={}", userId);
     try {
         Order order = orderService.placeOrder(userId);
         OrderDto orderDto = orderService.convertToDto(order);

         log.info("Order created successfully");

         return ResponseEntity.ok(new ApiResponse("Item Order Success!", orderDto));
     } catch (Exception e) {
         log.error("Error while creating order for userId={}", userId, e);

         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("Error Occured",e.getMessage()));
     }
 }

 @GetMapping("/order/{orderId}")
 public ResponseEntity<ApiResponse> getOrderById(@PathVariable Long orderId){
     log.info("API CALL: Get order by orderId={}", orderId);

     try {
         OrderDto order = orderService.getOrderById(orderId);

         log.info("Order fetched successfully for orderId={}", orderId);

         return ResponseEntity.ok(new ApiResponse("Item order success!", order));
     } catch (ResourceNotFoundException e) {
         log.warn("Order not found for orderId={}", orderId);

         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                 .body(new ApiResponse("Oops!",e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/orders")
 public ResponseEntity<ApiResponse> getUserOrders(@PathVariable Long userId){
     log.info("API CALL: Get all orders for userId={}", userId);

     try {
        List<OrderDto> order = orderService.getUserOrders(userId);

         log.info("Fetched orders for userId={}", userId);

         return ResponseEntity.ok(new ApiResponse("Item order success!", order));
     } catch (ResourceNotFoundException e) {
         log.warn("No orders found for userId={}", userId);

         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("Oops!",e.getMessage()));
     }
  }
}
