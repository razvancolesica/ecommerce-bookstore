package com.bookstore.service;

import com.bookstore.dto.OrderDto;
import com.bookstore.entity.*;
import com.bookstore.exception.ApiException;
import com.bookstore.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private OrderService orderService;

    private User user;
    private Cart cart;
    private Product product;
    private Address address;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).firstName("Jane").lastName("Doe")
                .email("jane@example.com").password("secret").giftPoints(200).role("USER").build();

        product = Product.builder().id(10L).title("Clean Code").author("Robert C. Martin")
                .price(new BigDecimal("39.99")).estimatedDeliveryDays(3).build();

        CartItem cartItem = CartItem.builder().id(1L).product(product)
                .quantity(2).unitPrice(new BigDecimal("39.99")).build();

        cart = Cart.builder().id(1L).user(user).items(new ArrayList<>(List.of(cartItem))).build();
        cartItem.setCart(cart);

        address = Address.builder().id(1L).user(user).fullName("Jane Doe")
                .line1("123 Main St").city("London").postcode("SW1A 1AA").country("UK").build();
    }

    @Test
    @DisplayName("placeOrder - should create confirmed order and clear cart")
    void placeOrder_success() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.getItems().forEach(i -> i.setId(1L));
            return o;
        });

        OrderDto.PlaceOrderRequest req = new OrderDto.PlaceOrderRequest();
        req.setAddressId(1L);
        req.setPaymentMethod("CREDIT_CARD");
        req.setUseGiftPoints(false);

        OrderDto.OrderResponse response = orderService.placeOrder(user, req);

        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
        assertThat(response.getTotal()).isEqualByComparingTo("79.98");
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getProductTitle()).isEqualTo("Clean Code");
        assertThat(cart.getItems()).isEmpty(); // cart was cleared
        assertThat(user.getGiftPoints()).isEqualTo(250); // 200 + 50 earned
    }

    @Test
    @DisplayName("placeOrder - should apply gift point discount")
    void placeOrder_withGiftPoints() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderDto.PlaceOrderRequest req = new OrderDto.PlaceOrderRequest();
        req.setAddressId(1L);
        req.setPaymentMethod("CREDIT_CARD");
        req.setUseGiftPoints(true); // 200 points = $2.00 discount

        OrderDto.OrderResponse response = orderService.placeOrder(user, req);

        assertThat(response.getDiscountFromPoints()).isEqualByComparingTo("2.00");
        assertThat(response.getTotal()).isEqualByComparingTo("77.98");
    }

    @Test
    @DisplayName("placeOrder - should throw BAD_REQUEST for empty cart")
    void placeOrder_emptyCart_throwsBadRequest() {
        cart.getItems().clear();
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        OrderDto.PlaceOrderRequest req = new OrderDto.PlaceOrderRequest();
        req.setAddressId(1L);
        req.setPaymentMethod("CREDIT_CARD");

        assertThatThrownBy(() -> orderService.placeOrder(user, req))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Cart is empty");
    }

    @Test
    @DisplayName("cancelOrder - should cancel order within 48 hours")
    void cancelOrder_withinWindow() {
        Order order = Order.builder().id(1L).user(user).status(Order.Status.CONFIRMED)
                .placedAt(LocalDateTime.now().minusHours(10))
                .address(address).items(new ArrayList<>())
                .subtotal(new BigDecimal("79.98")).discountFromPoints(BigDecimal.ZERO)
                .total(new BigDecimal("79.98")).paymentMethod("CREDIT_CARD").build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDto.OrderResponse response = orderService.cancelOrder(user, 1L);

        assertThat(response.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("cancelOrder - should throw BAD_REQUEST after 48 hour window")
    void cancelOrder_afterWindow_throwsBadRequest() {
        Order order = Order.builder().id(1L).user(user).status(Order.Status.CONFIRMED)
                .placedAt(LocalDateTime.now().minusHours(50))
                .address(address).items(new ArrayList<>())
                .subtotal(new BigDecimal("79.98")).discountFromPoints(BigDecimal.ZERO)
                .total(new BigDecimal("79.98")).paymentMethod("CREDIT_CARD").build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(user, 1L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("48 hours");
    }

    @Test
    @DisplayName("cancelOrder - should throw NOT_FOUND for unknown order")
    void cancelOrder_notFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.cancelOrder(user, 99L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    @DisplayName("getHistory - should return paginated order history")
    void getHistory_returnsPaginatedOrders() {
        Order order = Order.builder().id(1L).user(user).status(Order.Status.DELIVERED)
                .placedAt(LocalDateTime.now().minusDays(5))
                .address(address).items(new ArrayList<>())
                .subtotal(new BigDecimal("39.99")).discountFromPoints(BigDecimal.ZERO)
                .total(new BigDecimal("39.99")).paymentMethod("DEBIT_CARD").build();

        Page<Order> page = new PageImpl<>(List.of(order));
        when(orderRepository.findByUserIdOrderByPlacedAtDesc(eq(1L), any(Pageable.class))).thenReturn(page);

        OrderDto.OrderPage result = orderService.getHistory(user, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo("DELIVERED");
    }
}
