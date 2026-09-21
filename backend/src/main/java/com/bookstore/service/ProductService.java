package com.bookstore.service;

import com.bookstore.dto.ProductDto;
import com.bookstore.entity.Product;
import com.bookstore.exception.ApiException;
import com.bookstore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductDto.Page findAll(Long categoryId, Long brandId, String search,
                                   BigDecimal minPrice, BigDecimal maxPrice,
                                   int page, int size, String sort) {
        Sort s = switch (sort == null ? "title_asc" : sort) {
            case "price_asc"  -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "newest"     -> Sort.by("publishedDate").descending();
            default           -> Sort.by("title").ascending();
        };
        Pageable pageable = PageRequest.of(page, size, s);
        Page<Product> result = productRepository.findWithFilters(
                categoryId, brandId, search, minPrice, maxPrice, pageable);

        ProductDto.Page dto = new ProductDto.Page();
        dto.setContent(result.getContent().stream().map(this::toSummary).toList());
        dto.setTotalElements(result.getTotalElements());
        dto.setTotalPages(result.getTotalPages());
        dto.setPage(result.getNumber());
        dto.setSize(result.getSize());
        return dto;
    }

    public ProductDto.Detail findById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
        return toDetail(p);
    }

    public List<ProductDto.Summary> findRelated(Long id, int limit) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
        if (p.getCategory() == null) return List.of();
        return productRepository
                .findByCategoryIdAndIdNot(p.getCategory().getId(), id, PageRequest.of(0, limit))
                .stream().map(this::toSummary).toList();
    }

    public List<ProductDto.Summary> findRecommendations(Long userId, int limit) {
        return productRepository
                .findOrderedProductsByUser(userId, PageRequest.of(0, limit))
                .stream().map(this::toSummary).toList();
    }

    public ProductDto.Summary toSummary(Product p) {
        ProductDto.Summary dto = new ProductDto.Summary();
        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setAuthor(p.getAuthor());
        dto.setPrice(p.getPrice());
        dto.setCoverImageUrl(p.getCoverImageUrl());
        dto.setEstimatedDeliveryDays(p.getEstimatedDeliveryDays());
        if (p.getCategory() != null) {
            dto.setCategoryId(p.getCategory().getId());
            dto.setCategoryName(p.getCategory().getName());
        }
        return dto;
    }

    private ProductDto.Detail toDetail(Product p) {
        ProductDto.Detail dto = new ProductDto.Detail();
        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setAuthor(p.getAuthor());
        dto.setPrice(p.getPrice());
        dto.setCoverImageUrl(p.getCoverImageUrl());
        dto.setEstimatedDeliveryDays(p.getEstimatedDeliveryDays());
        dto.setDescription(p.getDescription());
        dto.setIsbn(p.getIsbn());
        dto.setPages(p.getPages());
        dto.setPublishedDate(p.getPublishedDate());
        dto.setStockQuantity(p.getStockQuantity());
        if (p.getCategory() != null) {
            dto.setCategoryId(p.getCategory().getId());
            dto.setCategoryName(p.getCategory().getName());
        }
        if (p.getBrand() != null) {
            ProductDto.BrandDto b = new ProductDto.BrandDto();
            b.setId(p.getBrand().getId());
            b.setName(p.getBrand().getName());
            b.setLogoUrl(p.getBrand().getLogoUrl());
            dto.setBrand(b);
            dto.setPublisher(p.getBrand().getName());
        }
        return dto;
    }
}
