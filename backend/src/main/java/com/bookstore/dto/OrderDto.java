package com.bookstore.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDto {

    @Data
    public static class PlaceOrderRequest {
        @NotNull private Long addressId;
        @NotBlank private String paymentMethod;
        private boolean useGiftPoints = false;
        private String cardNumber;
        private String cardHolderName;
    }

    @Data
    public static class OrderItemResponse {
        private Long id;
        private Long productId;
        private String productTitle;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }

    @Data
    public static class OrderResponse {
        private Long id;
        private String status;
        private LocalDateTime placedAt;
        private AddressDto.AddressResponse address;
        private List<OrderItemResponse> items;
        private String paymentMethod;
        private BigDecimal subtotal;
        private BigDecimal discountFromPoints;
        private BigDecimal total;
        private boolean cancellable;
    }

    @Data
    public static class OrderPage {
        private List<OrderResponse> content;
        private long totalElements;
        private int totalPages;
        private int page;
        private int size;
    }
}
