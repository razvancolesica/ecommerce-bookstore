package com.bookstore.controller;

import com.bookstore.entity.Brand;
import com.bookstore.entity.Category;
import com.bookstore.exception.ApiException;
import com.bookstore.repository.BrandRepository;
import com.bookstore.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CatalogueController {

    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> listCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<Category> getCategory(@PathVariable Long id) {
        return ResponseEntity.ok(categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category not found")));
    }

    @GetMapping("/brands")
    public ResponseEntity<List<Brand>> listBrands() {
        return ResponseEntity.ok(brandRepository.findAll());
    }
}
