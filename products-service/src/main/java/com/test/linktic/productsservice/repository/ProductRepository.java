package com.test.linktic.productsservice.repository;

import com.test.linktic.productsservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
