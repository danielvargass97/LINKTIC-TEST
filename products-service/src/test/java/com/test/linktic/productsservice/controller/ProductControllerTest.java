package com.test.linktic.productsservice.controller;

import com.test.linktic.productsservice.model.Product;
import com.test.linktic.productsservice.security.ApiKeyAuthFilter;
import com.test.linktic.productsservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ApiKeyAuthFilter.class)
})
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductService service;

    @TestConfiguration
    static class MockConfig {
        @Bean
        ProductService productService() {
            return Mockito.mock(ProductService.class);
        }
    }

    @Test
    void shouldReturnProductById() throws Exception {
        Product product = new Product(1L, "Laptop", 2500.0, "Gaming");
        Mockito.when(service.findById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/api/products/1")
                        .header("X-API-KEY", "SECRET123")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.type").value("product"))
                .andExpect(jsonPath("$.data.attributes.name").value("Laptop"))
                .andExpect(jsonPath("$.data.attributes.price").value(2500.0))
                .andExpect(jsonPath("$.data.attributes.description").value("Gaming"));
    }

    @Test
    void shouldReturn404IfNotFound() throws Exception {
        Mockito.when(service.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/1")
                        .header("X-API-KEY", "SECRET123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].title").value("Product not found"));
    }

    @Test
    void shouldCreateProduct() throws Exception {
        Product product = new Product(1L, "Mouse", 50.0, "Mouse gamer");
        Mockito.when(service.save(any(Product.class))).thenReturn(product);

        String json = """
                {
                    "name": "Mouse",
                    "price": 50.0,
                    "description": "Mouse gamer"
                }
                """;

        mockMvc.perform(post("/api/products")
                        .header("X-API-KEY", "SECRET123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attributes.name").value("Mouse"));
    }
}
