package com.test.linktic.inventoryservice.controller;

import com.test.linktic.inventoryservice.model.Inventory;
import com.test.linktic.inventoryservice.model.dto.PurchaseResponse;
import com.test.linktic.inventoryservice.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> getInventoryByProductId(@PathVariable Long productId) {
        return service.getByProductId(productId)
                .map(inventory -> {
                    Map<String, Object> attributes = new HashMap<>();
                    attributes.put("product", inventory.getProduct());
                    attributes.put("quantity", inventory.getQuantity());

                    return ResponseEntity.ok(Map.<String, Object>of(
                            "data", Map.of(
                                    "type", "inventory",
                                    "id", inventory.getId(),
                                    "attributes", attributes
                            )
                    ));
                })
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of(
                        "errors", List.of(Map.of(
                                "status", "404",
                                "title", "Inventory not found",
                                "detail", "No inventory record found for product ID: " + productId
                        ))
                )));
    }

    @PutMapping("")
    public ResponseEntity<Map<String, Object>> updateInventory(
            @RequestBody Inventory updatedInventory) {

        Inventory saved = service.updateQuantity(updatedInventory.getProductId(), updatedInventory.getQuantity());

        if (saved == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "errors", List.of(Map.of(
                            "status", "404",
                            "title", "Product not found",
                            "detail", "No product found for ID: " + updatedInventory.getProductId()
                    ))
            ));
        }

        return ResponseEntity.ok(Map.of(
                "data", Map.of(
                        "type", "inventory",
                        "id", saved.getId(),
                        "attributes", saved
                )
        ));
    }

    @PostMapping("/purchase")
    public ResponseEntity<Map<String, Object>> purchaseProduct(@RequestBody Map<String, Object> request) {
        try {
            Long productId = Long.valueOf(request.get("productId").toString());
            int quantity = Integer.parseInt(request.get("quantity").toString());

            PurchaseResponse purchase = service.purchaseProduct(productId, quantity);

            return ResponseEntity.ok(Map.of(
                    "data", Map.of(
                            "type", "purchase",
                            "attributes", purchase
                    )
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "errors", List.of(Map.of(
                            "status", "404",
                            "title", "Product or Inventory not found",
                            "detail", e.getMessage()
                    ))
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "errors", List.of(Map.of(
                            "status", "400",
                            "title", "Insufficient stock",
                            "detail", e.getMessage()
                    ))
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "errors", List.of(Map.of(
                            "status", "500",
                            "title", "Unexpected error",
                            "detail", e.getMessage()
                    ))
            ));
        }
    }
}
