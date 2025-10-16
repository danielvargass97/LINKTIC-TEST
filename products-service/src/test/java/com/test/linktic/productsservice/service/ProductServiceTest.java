package com.test.linktic.productsservice.service;

import com.test.linktic.productsservice.model.Product;
import com.test.linktic.productsservice.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;

    private Product product;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        product = new Product(1L, "Laptop", 2500.0, "Laptop Gamer");
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void shouldCreateProduct() {
        when(repository.save(product)).thenReturn(product);

        Product saved = service.save(product);

        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("Laptop");
        verify(repository, times(1)).save(product);
    }

    @Test
    void shouldFindById() {
        when(repository.findById(1L)).thenReturn(Optional.of(product));

        Optional<Product> found = service.findById(1L);

        assertThat(found).isPresent();
        assertThat(found.get().getPrice()).isEqualTo(2500.0);
    }

    @Test
    void shouldReturnEmptyIfNotFound() {
        when(repository.findById(2L)).thenReturn(Optional.empty());

        Optional<Product> found = service.findById(2L);

        assertThat(found).isEmpty();
    }

    @Test
    void shouldListAllProducts() {
        when(repository.findAll()).thenReturn(List.of(product));

        List<Product> products = service.findAll();

        assertThat(products).hasSize(1);
        verify(repository, times(1)).findAll();
    }
}
