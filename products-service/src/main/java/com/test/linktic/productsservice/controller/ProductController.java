package com.test.linktic.productsservice.controller;

import com.test.linktic.productsservice.model.Product;
import com.test.linktic.productsservice.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(@RequestBody Product product) {
        Product saved = service.save(product);

        Map<String, Object> response = Map.of(
                "data", Map.of(
                        "type", "product",
                        "id", saved.getId(),
                        "attributes", saved
                )
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProductById(@PathVariable Long id) {
        return service.findById(id)
                .map(product -> ResponseEntity.ok(Map.<String, Object>of(
                        "data", Map.of(
                                "type", "product",
                                "id", product.getId(),
                                "attributes", product
                        )
                )))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of(
                        "errors", List.of(Map.of(
                                "status", "404",
                                "title", "Product not found",
                                "detail", "The product with the id: " + id + " does not exist "
                        ))
                )));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProducts() {
        List<Map<String, Object>> products = service.findAll().stream()
                .map(p -> Map.of(
                        "type", "product",
                        "id", p.getId(),
                        "attributes", p
                ))
                .toList();

        return ResponseEntity.ok(Map.of("data", products));
    }
}
