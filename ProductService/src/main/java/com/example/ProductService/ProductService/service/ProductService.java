package com.example.ProductService.ProductService.service;

import com.example.ProductService.ProductService.model.ProductRequest;
import com.example.ProductService.ProductService.model.ProductResponse;

public interface ProductService {
    long addProduct(ProductRequest productRequest);
    ProductResponse getproductById(long productId);

    void reduceQuantity(long productId, long quantity);
}
