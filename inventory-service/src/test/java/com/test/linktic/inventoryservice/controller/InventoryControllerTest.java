package com.test.linktic.inventoryservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.linktic.inventoryservice.model.Inventory;
import com.test.linktic.inventoryservice.model.Product;
import com.test.linktic.inventoryservice.model.dto.PurchaseResponse;
import com.test.linktic.inventoryservice.security.ApiKeyAuthFilter;
import com.test.linktic.inventoryservice.service.InventoryService;
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

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InventoryController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ApiKeyAuthFilter.class)
})
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InventoryService service;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class MockConfig {
        @Bean
        InventoryService inventoryService() {
            return Mockito.mock(InventoryService.class);
        }
    }

    @Test
    void testGetInventoryByProductId_Found() throws Exception {
        Product product = new Product(1L, "Gansito", 2000.0, "Description Gansito");
        Inventory inventory = new Inventory();
        inventory.setId(1L);
        inventory.setProductId(1L);
        inventory.setQuantity(5);
        inventory.setProduct(product);

        Mockito.when(service.getByProductId(1L)).thenReturn(Optional.of(inventory));

        mockMvc.perform(get("/api/inventory/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.attributes.quantity").value(5))
                .andExpect(jsonPath("$.data.attributes.product.name").value("Gansito"));
    }

    @Test
    void testGetInventoryByProductId_NotFound() throws Exception {
        Mockito.when(service.getByProductId(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/inventory/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].title").value("Inventory not found"));
    }

    @Test
    void testUpdateInventory_Success() throws Exception {
        Inventory requestInventory = new Inventory();
        requestInventory.setProductId(1L);
        requestInventory.setQuantity(10);

        Product product = new Product(1L, "Gansito", 2000.0, "Description Gansito");
        Inventory savedInventory = new Inventory();
        savedInventory.setId(1L);
        savedInventory.setProductId(1L);
        savedInventory.setQuantity(10);
        savedInventory.setProduct(product);

        Mockito.when(service.updateQuantity(eq(1L), eq(10))).thenReturn(savedInventory);

        mockMvc.perform(put("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInventory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attributes.quantity").value(10))
                .andExpect(jsonPath("$.data.attributes.product.name").value("Gansito"));
    }

    @Test
    void testUpdateInventory_NotFound() throws Exception {
        Inventory requestInventory = new Inventory();
        requestInventory.setProductId(99L);
        requestInventory.setQuantity(10);

        Mockito.when(service.updateQuantity(eq(99L), eq(10))).thenReturn(null);

        mockMvc.perform(put("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInventory)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].title").value("Product not found"));
    }

    @Test
    void testPurchaseProduct_Success() throws Exception {
        PurchaseResponse response = new PurchaseResponse(1L, "Gansito", 2, 8);

        Mockito.when(service.purchaseProduct(Mockito.anyLong(), Mockito.anyInt())).thenReturn(response);

        mockMvc.perform(post("/api/inventory/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("productId", 1, "quantity", 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("purchase"))
                .andExpect(jsonPath("$.data.attributes.quantityPurchased").value(2));
    }

    @Test
    void testPurchaseProduct_ProductNotFound() throws Exception {
        Mockito.when(service.purchaseProduct(1L, 2))
                .thenThrow(new IllegalArgumentException("Product with ID 1 not found"));

        mockMvc.perform(post("/api/inventory/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("productId", 1, "quantity", 2))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].title").value("Product or Inventory not found"));
    }

    @Test
    void testPurchaseProduct_InsufficientStock() throws Exception {
        Mockito.when(service.purchaseProduct(1L, 5))
                .thenThrow(new IllegalStateException("Insufficient inventory for product ID: 1"));

        mockMvc.perform(post("/api/inventory/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("productId", 1, "quantity", 5))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].title").value("Insufficient stock"));
    }
}
