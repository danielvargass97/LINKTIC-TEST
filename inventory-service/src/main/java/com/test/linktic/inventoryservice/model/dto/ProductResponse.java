package com.test.linktic.inventoryservice.model.dto;

import com.test.linktic.inventoryservice.model.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {

    private ProductData data;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductData {
        private String type;
        private Long id;
        private Product attributes;
    }

}

