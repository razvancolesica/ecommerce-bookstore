package com.bookstore.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class CartDto {

    @Data
    public static class AddItemRequest {
        @NotNull private Long productId;
        @Min(1) private Integer quantity;
    }

    @Data
    public static class UpdateItemRequest {
        @Min(1) private Integer quantity;
    }

    @Data
    public static class CartItemResponse {
        private Long id;
        private ProductDto.Summary product;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }

    @Data
    public static class CartResponse {
        private Long id;
        private List<CartItemResponse> items;
        private Integer totalItems;
        private BigDecimal subtotal;
        private BigDecimal discountFromPoints;
        private BigDecimal total;
    }
}
