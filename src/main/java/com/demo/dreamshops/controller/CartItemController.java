package com.demo.dreamshops.controller;

import com.demo.dreamshops.exceptions.ResourceNotFoundException;
import com.demo.dreamshops.model.Cart;
import com.demo.dreamshops.model.User;
import com.demo.dreamshops.response.ApiResponse;
import com.demo.dreamshops.service.cart.ICartItemService;
import com.demo.dreamshops.service.cart.ICartService;
import com.demo.dreamshops.service.user.IUserService;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/cartItems")
@Slf4j
@Tag(name="CartItem APIS")
public class CartItemController {

    private final ICartItemService cartItemService;
    private final ICartService cartService;
    private final IUserService userService;

    @PostMapping("/item/add")
    public ResponseEntity<ApiResponse> addItemToCart(
            @RequestParam Long productId,
            @RequestParam Integer quantity){

        log.info("Request to add item to cart | productId: {}, quantity: {}", productId, quantity);

        try {
            User user = userService.getAuthenticatedUser();
            log.info("Authenticated user ID: {}", user.getId());

            Cart cart = cartService.initializeNewCart(user);
            log.info("Cart initialized/fetched with ID: {}", cart.getId());

            cartItemService.addItemToCart(cart.getId(), productId, quantity);

            log.info("Item added successfully to cartId: {}", cart.getId());
            return ResponseEntity.ok(new ApiResponse("Add Item Success", null));

        } catch (ResourceNotFoundException e) {
            log.warn("Resource not found while adding item | productId: {}", productId);
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));

        } catch (JwtException e) {
            log.warn("Unauthorized access while adding item to cart");
            return ResponseEntity.status(UNAUTHORIZED)
                    .body(new ApiResponse(e.getMessage(), null));

        } catch (Exception e) {
            log.error("Unexpected error while adding item to cart", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse("Something went wrong", null));
        }
    }

    @DeleteMapping("/cart/{cartId}/item/{itemId}/remove")
    public ResponseEntity<ApiResponse> removeItemFromCart(@PathVariable Long cartId,
                                                          @PathVariable Long itemId){

        log.info("Request to remove item | cartId: {}, itemId: {}", cartId, itemId);

        try {
            cartItemService.removeItemFromCart(cartId, itemId);

            log.info("Item removed successfully | cartId: {}, itemId: {}", cartId, itemId);
            return ResponseEntity.ok(new ApiResponse("Remove Item Success", null));

        } catch (ResourceNotFoundException e) {
            log.warn("Item not found for removal | cartId: {}, itemId: {}", cartId, itemId);
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));

        } catch (Exception e) {
            log.error("Unexpected error while removing item", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse("Something went wrong", null));
        }
    }

    @PutMapping("/cart/{cartId}/item/{itemId}/update")
    public ResponseEntity<ApiResponse> updateItemQuantity(@PathVariable Long cartId,
                                                          @PathVariable Long itemId,
                                                          @RequestParam Integer quantity) {

        log.info("Request to update item quantity | cartId: {}, itemId: {}, quantity: {}",
                cartId, itemId, quantity);

        try {
            cartItemService.updateItemQuantity(cartId, itemId, quantity);

            log.info("Item quantity updated successfully | cartId: {}, itemId: {}", cartId, itemId);
            return ResponseEntity.ok(new ApiResponse("Update the item success", null));

        } catch (ResourceNotFoundException e) {
            log.warn("Item not found for update | cartId: {}, itemId: {}", cartId, itemId);
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));

        } catch (Exception e) {
            log.error("Unexpected error while updating item quantity", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse("Something went wrong", null));
        }
    }
}