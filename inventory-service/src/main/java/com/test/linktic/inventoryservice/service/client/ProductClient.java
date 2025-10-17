package com.test.linktic.inventoryservice.service.client;

import com.test.linktic.inventoryservice.model.Product;
import com.test.linktic.inventoryservice.model.dto.ProductResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class ProductClient {

    private final RestTemplate restTemplate;
    private final String productServiceUrl;
    private final String apiKey;

    public ProductClient(RestTemplate restTemplate,
                         @Value("${product.service.url}") String productServiceUrl,
                         @Value("${product.service.api-key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.productServiceUrl = productServiceUrl;
        this.apiKey = apiKey;
    }

    public Product getProductById(Long productId) {
        String url = productServiceUrl + "/" + productId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", apiKey);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ProductResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    ProductResponse.class
            );

            ProductResponse.ProductData productData = response.getBody().getData();
            return productData.getAttributes();
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }
}
