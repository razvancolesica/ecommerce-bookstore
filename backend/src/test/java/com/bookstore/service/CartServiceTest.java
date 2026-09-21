package com.bookstore.service;

import com.bookstore.dto.CartDto;
import com.bookstore.entity.*;
import com.bookstore.exception.ApiException;
import com.bookstore.repository.CartRepository;
import com.bookstore.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Tests")
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ProductService productService;
    @InjectMocks private CartService cartService;

    private User user;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).firstName("Jane").lastName("Doe")
                .email("jane@example.com").password("secret").giftPoints(100).role("USER").build();
        product = Product.builder().id(10L).title("Clean Code").author("Robert C. Martin")
                .price(new BigDecimal("39.99")).estimatedDeliveryDays(3).stockQuantity(20).build();
        cart = Cart.builder().id(1L).user(user).items(new ArrayList<>()).build();
    }

    @Test
    @DisplayName("getCart - should create cart if none exists")
    void getCart_createsIfAbsent() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartDto.CartResponse response = cartService.getCart(user);

        assertThat(response.getItems()).isEmpty();
        assertThat(response.getTotalItems()).isEqualTo(0);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("getCart - should return existing cart")
    void getCart_existingCart() {
        CartItem item = CartItem.builder().id(1L).cart(cart).product(product)
                .quantity(2).unitPrice(product.getPrice()).build();
        cart.getItems().add(item);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productService.toSummary(any())).thenCallRealMethod();

        CartDto.CartResponse response = cartService.getCart(user);

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotalItems()).isEqualTo(2);
        assertThat(response.getSubtotal()).isEqualByComparingTo("79.98");
    }

    @Test
    @DisplayName("addItem - should add new product to cart")
    void addItem_newProduct() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(productService.toSummary(any())).thenCallRealMethod();

        CartDto.AddItemRequest req = new CartDto.AddItemRequest();
        req.setProductId(10L);
        req.setQuantity(1);

        CartDto.CartResponse response = cartService.addItem(user, req);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("addItem - should increase quantity if product already in cart")
    void addItem_existingProduct_increasesQty() {
        CartItem existing = CartItem.builder().id(1L).cart(cart).product(product)
                .quantity(1).unitPrice(product.getPrice()).build();
        cart.getItems().add(existing);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(productService.toSummary(any())).thenCallRealMethod();

        CartDto.AddItemRequest req = new CartDto.AddItemRequest();
        req.setProductId(10L);
        req.setQuantity(2);

        cartService.addItem(user, req);

        assertThat(existing.getQuantity()).isEqualTo(3);
        assertThat(cart.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("addItem - should throw NOT_FOUND for unknown product")
    void addItem_productNotFound() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        CartDto.AddItemRequest req = new CartDto.AddItemRequest();
        req.setProductId(99L);
        req.setQuantity(1);

        assertThatThrownBy(() -> cartService.addItem(user, req))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    @DisplayName("removeItem - should remove item from cart")
    void removeItem_success() {
        CartItem item = CartItem.builder().id(5L).cart(cart).product(product)
                .quantity(1).unitPrice(product.getPrice()).build();
        cart.getItems().add(item);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.removeItem(user, 5L);

        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    @DisplayName("clearCart - should empty all items")
    void clearCart_success() {
        CartItem item = CartItem.builder().id(1L).cart(cart).product(product)
                .quantity(2).unitPrice(product.getPrice()).build();
        cart.getItems().add(item);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.clearCart(user);

        assertThat(cart.getItems()).isEmpty();
    }
}
