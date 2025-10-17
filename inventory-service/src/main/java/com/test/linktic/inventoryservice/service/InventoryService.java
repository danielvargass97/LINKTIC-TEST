package com.test.linktic.inventoryservice.service;

import com.test.linktic.inventoryservice.model.Inventory;
import com.test.linktic.inventoryservice.model.Product;
import com.test.linktic.inventoryservice.model.dto.PurchaseResponse;
import com.test.linktic.inventoryservice.repository.InventoryRepository;
import com.test.linktic.inventoryservice.service.client.ProductClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;

@Service
public class InventoryService {

    private final InventoryRepository repository;
    private final ProductClient productClient;

    public InventoryService(InventoryRepository repository, ProductClient productClient) {
        this.repository = repository;
        this.productClient = productClient;
    }

    public Optional<Inventory> getByProductId(Long productId) {

        Product product = productClient.getProductById(productId);

        if (product == null) {
            return Optional.empty();
        }

        Optional<Inventory> inventoryOpt = repository.findByProductId(productId);
        Inventory inventory;

        if (inventoryOpt.isPresent()) {
            inventory = inventoryOpt.get();
        } else {
            inventory = new Inventory();
            inventory.setProductId(productId);
            inventory.setQuantity(0);
            repository.save(inventory);
        }

        inventory.setProduct(new Product(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription()
        ));

        return Optional.of(inventory);
    }

    public Inventory updateQuantity(Long productId, Integer quantity) {
        if (quantity < 0) {
            return null;
        }

        Product product = productClient.getProductById(productId);
        if (product == null) {
            return null;
        }

        Optional<Inventory> inventoryOpt = repository.findByProductId(productId);
        Inventory inventory;

        if (inventoryOpt.isPresent()) {
            inventory = inventoryOpt.get();
        } else {
            inventory = new Inventory();
            inventory.setProductId(productId);
        }

        inventory.setQuantity(quantity);
        inventory.setProduct(product);

        return repository.save(inventory);
    }

    public PurchaseResponse purchaseProduct(Long productId, int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        try {
            Product product = productClient.getProductById(productId);

            Inventory inventory = repository.findByProductId(productId)
                    .orElseThrow(() -> new IllegalArgumentException("No inventory found for product ID: " + productId));

            if (inventory.getQuantity() < quantity) {
                throw new IllegalStateException("Insufficient inventory for product ID: " + productId);
            }

            inventory.setQuantity(inventory.getQuantity() - quantity);
            repository.save(inventory);

            return new PurchaseResponse(
                    product.getId(),
                    product.getName(),
                    quantity,
                    inventory.getQuantity()
            );

        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Product with ID " + productId + " not found");
        }
    }
}
