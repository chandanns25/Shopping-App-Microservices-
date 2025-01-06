package com.dailycodebuffer.ProductService.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private String productName;
    private Long productId;
    private long quantity;
    private long price;
}