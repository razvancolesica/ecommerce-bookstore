package com.bookstore.repository;

import com.bookstore.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
public interface BrandRepository extends JpaRepository<Brand, Long> {}
