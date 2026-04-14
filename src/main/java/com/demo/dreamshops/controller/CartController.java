package com.demo.dreamshops.controller;

import com.demo.dreamshops.exceptions.ResourceNotFoundException;
import com.demo.dreamshops.model.Cart;
import com.demo.dreamshops.response.ApiResponse;
import com.demo.dreamshops.service.cart.ICartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/carts")
@Slf4j
@Tag(name="Cart APIs")
public class CartController {

    private final ICartService cartService;

    @GetMapping("/{cartId}/my-cart")
    public ResponseEntity<ApiResponse> getCart(@PathVariable Long cartId){

        log.info("Fetching cart details | cartId: {}", cartId);

        try{
            Cart cart = cartService.getCart(cartId);

            log.info("Cart fetched successfully | cartId: {}", cartId);
            return ResponseEntity.ok(new ApiResponse("Success",cart));

        }catch(ResourceNotFoundException e){
            log.warn("Cart not found | cartId: {}", cartId);
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(),null));

        } catch (Exception e) {
            log.error("Unexpected error while fetching cart | cartId: {}", cartId, e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse("Something went wrong", null));
        }
    }

    @GetMapping("/{cartId}/cart/total-price")
    public ResponseEntity<ApiResponse> getTotalAmount(@PathVariable Long cartId){

        log.info("Fetching total price for cart | cartId: {}", cartId);

        try {
            BigDecimal totalPrice = cartService.getTotalPrice(cartId);

            log.info("Total price calculated | cartId: {}, amount: {}", cartId, totalPrice);
            return ResponseEntity.ok(new ApiResponse("Total price", totalPrice));

        } catch (ResourceNotFoundException e) {
            log.warn("Cart not found while calculating total | cartId: {}", cartId);
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(),null));

        } catch (Exception e) {
            log.error("Unexpected error while calculating total price | cartId: {}", cartId, e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse("Something went wrong", null));
        }
    }

    @DeleteMapping("/{cartId}/clear")
    public ResponseEntity<ApiResponse> clearCart(@PathVariable Long cartId){

        log.info("Clearing cart | cartId: {}", cartId);

        try {
            cartService.clearCart(cartId);

            log.info("Cart cleared successfully | cartId: {}", cartId);
            return ResponseEntity.ok(new ApiResponse("Clear cart success", null));

        } catch (ResourceNotFoundException e) {
            log.warn("Cart not found while clearing | cartId: {}", cartId);
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(),null));

        } catch (Exception e) {
            log.error("Unexpected error while clearing cart | cartId: {}", cartId, e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse("Something went wrong", null));
        }
    }
}