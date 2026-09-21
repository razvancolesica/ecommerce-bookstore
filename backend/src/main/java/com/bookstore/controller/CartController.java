package com.bookstore.controller;

import com.bookstore.dto.CartDto;
import com.bookstore.entity.User;
import com.bookstore.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final AuthController authController;

    @GetMapping
    public ResponseEntity<CartDto.CartResponse> get(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        User user = authController.resolveUser(auth);
        return ResponseEntity.ok(cartService.getCart(user));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto.CartResponse> addItem(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @Valid @RequestBody CartDto.AddItemRequest req) {
        User user = authController.resolveUser(auth);
        return ResponseEntity.ok(cartService.addItem(user, req));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDto.CartResponse> updateItem(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long itemId,
            @Valid @RequestBody CartDto.UpdateItemRequest req) {
        User user = authController.resolveUser(auth);
        return ResponseEntity.ok(cartService.updateItem(user, itemId, req));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDto.CartResponse> removeItem(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long itemId) {
        User user = authController.resolveUser(auth);
        return ResponseEntity.ok(cartService.removeItem(user, itemId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        User user = authController.resolveUser(auth);
        cartService.clearCart(user);
        return ResponseEntity.noContent().build();
    }
}
