package com.bookstore.service;

import com.bookstore.dto.AddressDto;
import com.bookstore.dto.OrderDto;
import com.bookstore.entity.*;
import com.bookstore.exception.ApiException;
import com.bookstore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final int POINTS_PER_ORDER = 50;
    private static final BigDecimal POINT_VALUE = new BigDecimal("0.01");
    private static final int CANCEL_HOURS = 48;

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderDto.OrderResponse placeOrder(User user, OrderDto.PlaceOrderRequest req) {
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Cart is empty"));
        if (cart.getItems().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        Address address = addressRepository.findById(req.getAddressId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Address not found"));

        BigDecimal subtotal = cart.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = BigDecimal.ZERO;
        if (req.isUseGiftPoints() && user.getGiftPoints() > 0) {
            discount = POINT_VALUE.multiply(BigDecimal.valueOf(user.getGiftPoints()));
            if (discount.compareTo(subtotal) > 0) discount = subtotal;
            int pointsUsed = discount.divide(POINT_VALUE).intValue();
            user.setGiftPoints(user.getGiftPoints() - pointsUsed);
        }

        BigDecimal total = subtotal.subtract(discount);

        List<OrderItem> orderItems = cart.getItems().stream().map(ci ->
            OrderItem.builder()
                .productTitle(ci.getProduct().getTitle())
                .product(ci.getProduct())
                .quantity(ci.getQuantity())
                .unitPrice(ci.getUnitPrice())
                .build()
        ).toList();

        Order order = Order.builder()
                .user(user)
                .status(Order.Status.CONFIRMED)
                .placedAt(LocalDateTime.now())
                .address(address)
                .paymentMethod(req.getPaymentMethod())
                .subtotal(subtotal)
                .discountFromPoints(discount)
                .total(total)
                .build();
        orderItems.forEach(oi -> { oi.setOrder(order); order.getItems().add(oi); });

        // Award points for the purchase
        user.setGiftPoints(user.getGiftPoints() + POINTS_PER_ORDER);
        userRepository.save(user);

        // Clear cart
        cart.getItems().clear();
        cartRepository.save(cart);

        return toResponse(orderRepository.save(order));
    }

    public OrderDto.OrderPage getHistory(User user, int page, int size) {
        Page<Order> result = orderRepository.findByUserIdOrderByPlacedAtDesc(
                user.getId(), PageRequest.of(page, size));
        OrderDto.OrderPage dto = new OrderDto.OrderPage();
        dto.setContent(result.getContent().stream().map(this::toResponse).toList());
        dto.setTotalElements(result.getTotalElements());
        dto.setTotalPages(result.getTotalPages());
        dto.setPage(result.getNumber());
        dto.setSize(result.getSize());
        return dto;
    }

    public OrderDto.OrderResponse getById(User user, Long id) {
        Order order = findOrderForUser(user, id);
        return toResponse(order);
    }

    @Transactional
    public OrderDto.OrderResponse cancelOrder(User user, Long id) {
        Order order = findOrderForUser(user, id);
        if (order.getStatus() == Order.Status.CANCELLED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Order already cancelled");
        }
        long hoursSince = ChronoUnit.HOURS.between(order.getPlacedAt(), LocalDateTime.now());
        if (hoursSince > CANCEL_HOURS) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Order cannot be cancelled after " + CANCEL_HOURS + " hours");
        }
        order.setStatus(Order.Status.CANCELLED);
        return toResponse(orderRepository.save(order));
    }

    private Order findOrderForUser(User user, Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return order;
    }

    private OrderDto.OrderResponse toResponse(Order order) {
        OrderDto.OrderResponse r = new OrderDto.OrderResponse();
        r.setId(order.getId());
        r.setStatus(order.getStatus().name());
        r.setPlacedAt(order.getPlacedAt());
        r.setPaymentMethod(order.getPaymentMethod());
        r.setSubtotal(order.getSubtotal());
        r.setDiscountFromPoints(order.getDiscountFromPoints());
        r.setTotal(order.getTotal());

        long hours = ChronoUnit.HOURS.between(order.getPlacedAt(), LocalDateTime.now());
        r.setCancellable(order.getStatus() != Order.Status.CANCELLED && hours <= CANCEL_HOURS);

        AddressDto.AddressResponse addr = new AddressDto.AddressResponse();
        addr.setId(order.getAddress().getId());
        addr.setFullName(order.getAddress().getFullName());
        addr.setLine1(order.getAddress().getLine1());
        addr.setLine2(order.getAddress().getLine2());
        addr.setCity(order.getAddress().getCity());
        addr.setPostcode(order.getAddress().getPostcode());
        addr.setCountry(order.getAddress().getCountry());
        r.setAddress(addr);

        r.setItems(order.getItems().stream().map(i -> {
            OrderDto.OrderItemResponse oi = new OrderDto.OrderItemResponse();
            oi.setId(i.getId());
            oi.setProductId(i.getProduct() != null ? i.getProduct().getId() : null);
            oi.setProductTitle(i.getProductTitle());
            oi.setQuantity(i.getQuantity());
            oi.setUnitPrice(i.getUnitPrice());
            oi.setSubtotal(i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())));
            return oi;
        }).toList());
        return r;
    }
}
