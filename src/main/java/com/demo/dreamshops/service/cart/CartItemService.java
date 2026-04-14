package com.demo.dreamshops.service.cart;

import com.demo.dreamshops.exceptions.ResourceNotFoundException;
import com.demo.dreamshops.model.Cart;
import com.demo.dreamshops.model.CartItem;
import com.demo.dreamshops.model.Product;
import com.demo.dreamshops.repository.CartItemRepository;
import com.demo.dreamshops.repository.CartRepository;
import com.demo.dreamshops.service.product.IProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartItemService implements ICartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final IProductService productService;
    private final ICartService cartService;

    @Override
    public void addItemToCart(Long cartId, Long productId, int quantity) {

        log.info("Adding item to cart | cartId: {}, productId: {}, quantity: {}", cartId, productId, quantity);

        // 1. Get cart
        Cart cart = cartService.getCart(cartId);

        // 2. Get product
        Product product = productService.getProductById(productId);

        // 3. Check existing item
        CartItem cartItem = cart.getItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(new CartItem());

        if (cartItem.getId() == null) {
            log.info("Product not in cart, creating new cart item | productId: {}", productId);

            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setUnitPrice(product.getPrice());

        } else {
            log.info("Product already in cart, updating quantity | productId: {}", productId);

            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        }

        cartItem.setTotalPrice();

        cart.addItem(cartItem);

        cartItemRepository.save(cartItem);
        cartRepository.save(cart);

        log.info("Item added/updated successfully | cartId: {}, productId: {}", cartId, productId);
    }

    @Override
    public void removeItemFromCart(Long cartId, Long productId) {

        log.info("Removing item from cart | cartId: {}, productId: {}", cartId, productId);

        Cart cart = cartService.getCart(cartId);

        CartItem itemToRemove = getCartItem(cartId, productId);

        cart.removeItem(itemToRemove);

        cartRepository.save(cart);

        log.info("Item removed successfully | cartId: {}, productId: {}", cartId, productId);
    }

    @Override
    public void updateItemQuantity(Long cartId, Long productId, int quantity) {

        log.info("Updating item quantity | cartId: {}, productId: {}, quantity: {}", cartId, productId, quantity);

        Cart cart = cartService.getCart(cartId);

        cart.getItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresentOrElse(item -> {
                    item.setQuantity(quantity);
                    item.setUnitPrice(item.getProduct().getPrice());
                    item.setTotalPrice();

                    log.info("Item quantity updated | productId: {}", productId);

                }, () -> {
                    log.warn("Item not found for update | cartId: {}, productId: {}", cartId, productId);
                    throw new ResourceNotFoundException("Item not found");
                });

        BigDecimal totalAmount = cart.getItems()
                .stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalAmount(totalAmount);

        cartRepository.save(cart);

        log.info("Cart total updated | cartId: {}, totalAmount: {}", cartId, totalAmount);
    }

    @Override
    public CartItem getCartItem(Long cartId, Long productId){

        log.info("Fetching cart item | cartId: {}, productId: {}", cartId, productId);

        Cart cart = cartService.getCart(cartId);

        return cart.getItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Item not found | cartId: {}, productId: {}", cartId, productId);
                    return new ResourceNotFoundException("Item not found");
                });
    }
}