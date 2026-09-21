package com.bookstore.repository;

import com.bookstore.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
        SELECT p FROM Product p
        WHERE (:categoryId IS NULL OR p.category.id = :categoryId)
          AND (:brandId    IS NULL OR p.brand.id    = :brandId)
          AND (:search     IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%',:search,'%'))
                                   OR LOWER(p.author) LIKE LOWER(CONCAT('%',:search,'%')))
          AND (:minPrice   IS NULL OR p.price >= :minPrice)
          AND (:maxPrice   IS NULL OR p.price <= :maxPrice)
        """)
    Page<Product> findWithFilters(
        @Param("categoryId") Long categoryId,
        @Param("brandId")    Long brandId,
        @Param("search")     String search,
        @Param("minPrice")   BigDecimal minPrice,
        @Param("maxPrice")   BigDecimal maxPrice,
        Pageable pageable
    );

    List<Product> findByCategoryIdAndIdNot(Long categoryId, Long excludeId, Pageable pageable);

    @Query("""
        SELECT p FROM Product p
        WHERE p.id IN (
            SELECT DISTINCT oi.product.id FROM OrderItem oi
            WHERE oi.order.user.id = :userId
        )
        ORDER BY p.id DESC
        """)
    List<Product> findOrderedProductsByUser(@Param("userId") Long userId, Pageable pageable);
}
