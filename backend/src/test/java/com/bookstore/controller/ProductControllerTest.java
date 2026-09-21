package com.bookstore.controller;

import com.bookstore.dto.ProductDto;
import com.bookstore.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@DisplayName("ProductController Tests")
class ProductControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private ProductService productService;
    @MockBean private com.bookstore.repository.UserRepository userRepository;

    private ProductDto.Summary buildSummary(Long id, String title, String author, double price) {
        ProductDto.Summary s = new ProductDto.Summary();
        s.setId(id); s.setTitle(title); s.setAuthor(author);
        s.setPrice(BigDecimal.valueOf(price)); s.setEstimatedDeliveryDays(4);
        s.setCategoryId(1L); s.setCategoryName("Fiction");
        return s;
    }

    @Test
    @DisplayName("GET /api/products - should return 200 with product list")
    void listProducts_returns200() throws Exception {
        ProductDto.Page page = new ProductDto.Page();
        page.setContent(List.of(buildSummary(1L, "The Great Gatsby", "F. Scott Fitzgerald", 9.99)));
        page.setTotalElements(1); page.setTotalPages(1); page.setPage(0); page.setSize(20);

        when(productService.findAll(any(), any(), any(), any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/products").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("The Great Gatsby"))
                .andExpect(jsonPath("$.content[0].author").value("F. Scott Fitzgerald"))
                .andExpect(jsonPath("$.content[0].price").value(9.99));
    }

    @Test
    @DisplayName("GET /api/products?search=gatsby - should pass search param to service")
    void listProducts_withSearch() throws Exception {
        ProductDto.Page page = new ProductDto.Page();
        page.setContent(List.of(buildSummary(1L, "The Great Gatsby", "F. Scott Fitzgerald", 9.99)));
        page.setTotalElements(1); page.setTotalPages(1); page.setPage(0); page.setSize(20);

        when(productService.findAll(isNull(), isNull(), eq("gatsby"), isNull(), isNull(),
                eq(0), eq(20), eq("title_asc"))).thenReturn(page);

        mockMvc.perform(get("/api/products")
                        .param("search", "gatsby")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("The Great Gatsby"));
    }

    @Test
    @DisplayName("GET /api/products/{id} - should return 200 with product detail")
    void getProductById_returns200() throws Exception {
        ProductDto.Detail detail = new ProductDto.Detail();
        detail.setId(1L); detail.setTitle("The Great Gatsby");
        detail.setAuthor("F. Scott Fitzgerald"); detail.setPrice(new BigDecimal("9.99"));
        detail.setDescription("A tale of wealth."); detail.setIsbn("978-0743273565");
        detail.setCategoryName("Fiction"); detail.setEstimatedDeliveryDays(4);

        when(productService.findById(1L)).thenReturn(detail);

        mockMvc.perform(get("/api/products/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("The Great Gatsby"))
                .andExpect(jsonPath("$.isbn").value("978-0743273565"))
                .andExpect(jsonPath("$.categoryName").value("Fiction"));
    }

    @Test
    @DisplayName("GET /api/products/{id}/related - should return related products")
    void getRelatedProducts_returns200() throws Exception {
        List<ProductDto.Summary> related = List.of(
                buildSummary(2L, "1984", "George Orwell", 10.99),
                buildSummary(3L, "Brave New World", "Aldous Huxley", 11.49)
        );
        when(productService.findRelated(1L, 5)).thenReturn(related);

        mockMvc.perform(get("/api/products/1/related").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("1984"))
                .andExpect(jsonPath("$[1].title").value("Brave New World"));
    }

    @Test
    @DisplayName("GET /api/recommendations - should return empty list when unauthenticated")
    void recommendations_unauthenticated_returnsEmpty() throws Exception {
        mockMvc.perform(get("/api/recommendations").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
