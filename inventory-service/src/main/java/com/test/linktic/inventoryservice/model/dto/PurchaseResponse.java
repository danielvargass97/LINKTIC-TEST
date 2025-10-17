package com.test.linktic.inventoryservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseResponse {
    private Long productId;
    private String productName;
    private int quantityPurchased;
    private int remainingStock;
}
