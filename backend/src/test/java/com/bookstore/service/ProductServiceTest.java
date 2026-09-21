package com.bookstore.service;

import com.bookstore.dto.ProductDto;
import com.bookstore.entity.Brand;
import com.bookstore.entity.Category;
import com.bookstore.entity.Product;
import com.bookstore.exception.ApiException;
import com.bookstore.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests")
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @InjectMocks private ProductService productService;

    private Product sampleProduct;
    private Category fiction;
    private Brand penguin;

    @BeforeEach
    void setUp() {
        fiction = Category.builder().id(1L).name("Fiction").build();
        penguin = Brand.builder().id(1L).name("Penguin Books").build();
        sampleProduct = Product.builder()
                .id(1L).title("The Great Gatsby").author("F. Scott Fitzgerald")
                .price(new BigDecimal("9.99")).estimatedDeliveryDays(4)
                .coverImageUrl("https://picsum.photos/seed/gatsby/300/450")
                .category(fiction).brand(penguin).stockQuantity(50)
                .description("A tale of wealth and the American Dream.")
                .isbn("978-0743273565").pages(180)
                .build();
    }

    @Test
    @DisplayName("findAll - should return paginated product list")
    void findAll_returnsPage() {
        Page<Product> page = new PageImpl<>(List.of(sampleProduct), PageRequest.of(0, 20), 1);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ProductDto.Page result = productService.findAll(null, null, null, null, null, 0, 20, "title_asc");

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("The Great Gatsby");
    }

    @Test
    @DisplayName("findAll - should filter by search term")
    void findAll_withSearch() {
        Page<Product> page = new PageImpl<>(List.of(sampleProduct), PageRequest.of(0, 20), 1);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ProductDto.Page result = productService.findAll(null, null, "gatsby", null, null, 0, 20, "title_asc");

        assertThat(result.getContent().get(0).getTitle()).isEqualTo("The Great Gatsby");
    }

    @Test
    @DisplayName("findById - should return product detail")
    void findById_success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        ProductDto.Detail detail = productService.findById(1L);

        assertThat(detail.getTitle()).isEqualTo("The Great Gatsby");
        assertThat(detail.getAuthor()).isEqualTo("F. Scott Fitzgerald");
        assertThat(detail.getCategoryName()).isEqualTo("Fiction");
        assertThat(detail.getBrand().getName()).isEqualTo("Penguin Books");
        assertThat(detail.getIsbn()).isEqualTo("978-0743273565");
    }

    @Test
    @DisplayName("findById - should throw NOT_FOUND for missing product")
    void findById_notFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    @DisplayName("findRelated - should return products from same category")
    void findRelated_success() {
        Product related = Product.builder().id(2L).title("1984").author("George Orwell")
                .price(new BigDecimal("10.99")).estimatedDeliveryDays(4)
                .category(fiction).build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.findByCategoryIdAndIdNot(eq(1L), eq(1L), any(Pageable.class)))
                .thenReturn(List.of(related));

        List<ProductDto.Summary> related1 = productService.findRelated(1L, 5);

        assertThat(related1).hasSize(1);
        assertThat(related1.get(0).getTitle()).isEqualTo("1984");
    }

    @Test
    @DisplayName("findAll - should sort by price ascending")
    void findAll_sortByPriceAsc() {
        Page<Product> page = new PageImpl<>(List.of(sampleProduct));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ProductDto.Page result = productService.findAll(null, null, null, null, null, 0, 20, "price_asc");

        assertThat(result).isNotNull();
        verify(productRepository).findAll(
                any(Specification.class),
                (Pageable) argThat(p -> ((Pageable) p).getSort().getOrderFor("price") != null
                        && ((Pageable) p).getSort().getOrderFor("price").isAscending()));
    }
}
