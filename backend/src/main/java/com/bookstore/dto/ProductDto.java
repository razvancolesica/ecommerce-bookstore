package com.bookstore.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ProductDto {

    @Data
    public static class Summary {
        private Long id;
        private String title;
        private String author;
        private BigDecimal price;
        private String coverImageUrl;
        private Long categoryId;
        private String categoryName;
        private Integer estimatedDeliveryDays;
    }

    @Data
    public static class Detail extends Summary {
        private String description;
        private String isbn;
        private String publisher;
        private LocalDate publishedDate;
        private Integer pages;
        private Integer stockQuantity;
        private BrandDto brand;
    }

    @Data
    public static class BrandDto {
        private Long id;
        private String name;
        private String logoUrl;
    }

    @Data
    public static class Page {
        private java.util.List<Summary> content;
        private long totalElements;
        private int totalPages;
        private int page;
        private int size;
    }
}
