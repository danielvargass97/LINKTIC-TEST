package com.test.linktic.inventoryservice.service;

import com.test.linktic.inventoryservice.model.Inventory;
import com.test.linktic.inventoryservice.model.Product;
import com.test.linktic.inventoryservice.model.dto.PurchaseResponse;
import com.test.linktic.inventoryservice.repository.InventoryRepository;
import com.test.linktic.inventoryservice.service.client.ProductClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryServiceTest {

    @Mock
    private InventoryRepository repository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private InventoryService service;

    private Product mockProduct;
    private Inventory mockInventory;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        mockProduct = new Product(1L, "Laptop", 1500.0, "Gaming laptop");
        mockInventory = new Inventory();
        mockInventory.setId(1L);
        mockInventory.setProductId(1L);
        mockInventory.setQuantity(10);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testGetByProductId_ProductExists_InventoryFound() {
        when(productClient.getProductById(1L)).thenReturn(mockProduct);
        when(repository.findByProductId(1L)).thenReturn(Optional.of(mockInventory));

        Optional<Inventory> result = service.getByProductId(1L);

        assertTrue(result.isPresent());
        assertEquals(10, result.get().getQuantity());
        assertEquals("Laptop", result.get().getProduct().getName());
        verify(repository, never()).save(any());
    }

    @Test
    void testGetByProductId_ProductExists_NoInventory_CreatesNew() {
        when(productClient.getProductById(1L)).thenReturn(mockProduct);
        when(repository.findByProductId(1L)).thenReturn(Optional.empty());
        when(repository.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Inventory> result = service.getByProductId(1L);

        assertTrue(result.isPresent());
        assertEquals(0, result.get().getQuantity());
        verify(repository).save(any(Inventory.class));
    }

    @Test
    void testGetByProductId_ProductNotFound() {
        when(productClient.getProductById(1L)).thenReturn(null);

        Optional<Inventory> result = service.getByProductId(1L);

        assertTrue(result.isEmpty());
        verify(repository, never()).save(any());
    }

    @Test
    void testUpdateQuantity_Valid() {
        when(productClient.getProductById(1L)).thenReturn(mockProduct);
        when(repository.findByProductId(1L)).thenReturn(Optional.of(mockInventory));
        when(repository.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));

        Inventory updated = service.updateQuantity(1L, 5);

        assertNotNull(updated);
        assertEquals(5, updated.getQuantity());
        assertEquals("Laptop", updated.getProduct().getName());
    }

    @Test
    void testUpdateQuantity_ProductNotFound() {
        when(productClient.getProductById(1L)).thenReturn(null);

        Inventory updated = service.updateQuantity(1L, 5);
        assertNull(updated);
    }

    @Test
    void testUpdateQuantity_NegativeQuantity() {
        Inventory updated = service.updateQuantity(1L, -3);
        assertNull(updated);
    }

    @Test
    void testPurchaseProduct_Successful() {
        when(productClient.getProductById(1L)).thenReturn(mockProduct);
        when(repository.findByProductId(1L)).thenReturn(Optional.of(mockInventory));
        when(repository.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseResponse response = service.purchaseProduct(1L, 3);

        assertNotNull(response);
        assertEquals(1L, response.getProductId());
        assertEquals(3, response.getQuantityPurchased());
        assertEquals(7, response.getRemainingStock());
    }

    @Test
    void testPurchaseProduct_InsufficientInventory() {
        when(productClient.getProductById(1L)).thenReturn(mockProduct);
        mockInventory.setQuantity(2);
        when(repository.findByProductId(1L)).thenReturn(Optional.of(mockInventory));

        assertThrows(IllegalStateException.class, () -> service.purchaseProduct(1L, 5));
    }

    @Test
    void testPurchaseProduct_ProductNotFound() {
        when(productClient.getProductById(1L)).thenThrow(HttpClientErrorException.NotFound.class);

        assertThrows(IllegalArgumentException.class, () -> service.purchaseProduct(1L, 2));
    }

    @Test
    void testPurchaseProduct_InvalidQuantity() {
        assertThrows(IllegalArgumentException.class, () -> service.purchaseProduct(1L, 0));
    }
}
