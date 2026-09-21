package com.bookstore.controller;

import com.bookstore.dto.ProductDto;
import com.bookstore.entity.User;
import com.bookstore.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/products")
    public ResponseEntity<ProductDto.Page> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title_asc") String sort) {
        return ResponseEntity.ok(
                productService.findAll(categoryId, brandId, search, minPrice, maxPrice, page, size, sort));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDto.Detail> get(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @GetMapping("/products/{id}/related")
    public ResponseEntity<List<ProductDto.Summary>> related(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(productService.findRelated(id, limit));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<ProductDto.Summary>> recommendations(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "8") int limit) {
        if (user == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(productService.findRecommendations(user.getId(), limit));
    }
}
