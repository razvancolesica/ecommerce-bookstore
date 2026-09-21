package com.bookstore.service;

import com.bookstore.dto.CartDto;
import com.bookstore.dto.ProductDto;
import com.bookstore.entity.*;
import com.bookstore.exception.ApiException;
import com.bookstore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    @Transactional
    public CartDto.CartResponse getCart(User user) {
        Cart cart = getOrCreateCart(user);
        return toResponse(cart);
    }

    @Transactional
    public CartDto.CartResponse addItem(User user, CartDto.AddItemRequest req) {
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));

        cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst()
                .ifPresentOrElse(
                    i -> i.setQuantity(i.getQuantity() + req.getQuantity()),
                    () -> cart.getItems().add(CartItem.builder()
                            .cart(cart).product(product)
                            .quantity(req.getQuantity())
                            .unitPrice(product.getPrice())
                            .build())
                );
        cartRepository.save(cart);
        return toResponse(cart);
    }

    @Transactional
    public CartDto.CartResponse updateItem(User user, Long itemId, CartDto.UpdateItemRequest req) {
        Cart cart = getOrCreateCart(user);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cart item not found"));
        item.setQuantity(req.getQuantity());
        cartRepository.save(cart);
        return toResponse(cart);
    }

    @Transactional
    public CartDto.CartResponse removeItem(User user, Long itemId) {
        Cart cart = getOrCreateCart(user);
        cart.getItems().removeIf(i -> i.getId().equals(itemId));
        cartRepository.save(cart);
        return toResponse(cart);
    }

    @Transactional
    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    public CartDto.CartResponse toResponse(Cart cart) {
        List<CartDto.CartItemResponse> items = cart.getItems().stream().map(i -> {
            CartDto.CartItemResponse r = new CartDto.CartItemResponse();
            r.setId(i.getId());
            r.setProduct(productService.toSummary(i.getProduct()));
            r.setQuantity(i.getQuantity());
            r.setUnitPrice(i.getUnitPrice());
            r.setSubtotal(i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())));
            return r;
        }).toList();

        BigDecimal subtotal = items.stream()
                .map(CartDto.CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CartDto.CartResponse resp = new CartDto.CartResponse();
        resp.setId(cart.getId());
        resp.setItems(items);
        resp.setTotalItems(items.stream().mapToInt(CartDto.CartItemResponse::getQuantity).sum());
        resp.setSubtotal(subtotal);
        resp.setDiscountFromPoints(BigDecimal.ZERO);
        resp.setTotal(subtotal);
        return resp;
    }
}
