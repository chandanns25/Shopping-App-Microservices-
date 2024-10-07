package com.example.ProductService.ProductService.service;

import com.example.ProductService.ProductService.entity.Product;
import com.example.ProductService.ProductService.exception.ProductServiceCustomException;
import com.example.ProductService.ProductService.model.ProductRequest;
import com.example.ProductService.ProductService.model.ProductResponse;
import com.example.ProductService.ProductService.repository.ProductRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.springframework.beans.BeanUtils.*;

@Service
@Log4j2
public class ProductServiceImpl implements ProductService{

    @Autowired
    public ProductRepository productRepository;

    @Override
    public long addProduct(ProductRequest productRequest) {
        log.info("Adding product");

        Product product = Product.builder()
                .productName(productRequest.getName())
                .price(productRequest.getPrice())
                .quantity(productRequest.getQuantity())
                .build();

        productRepository.save(product);

        log.info("product created");
        return product.getProductId();
    }

    @Override
    public ProductResponse getproductById(long productId) {

        log.info("Get the product for productId:{}",productId);

        Product product = productRepository.findById(productId).orElseThrow(()-> new ProductServiceCustomException("the given productId is not found","PRODUCT_NOT_FOUND"));

        ProductResponse productResponse = new ProductResponse();

        copyProperties(product,productResponse);
        return productResponse;
    }

    @Override
    public void reduceQuantity(long productId, long quantity) {

        log.info("Reduce Quantity {} to Id:{}",quantity,productId);

        Product product = productRepository.findById(productId).orElseThrow(()-> new ProductServiceCustomException("product with the given id not found","PRODUCT_NOT_FOUND"));

        if(product.getQuantity()<quantity){
            throw new ProductServiceCustomException("product does not have sufficient quantity","INSUFFICIENT_QUANTITY");

        }
        product.setQuantity(product.getQuantity()-quantity);
        productRepository.save(product);
        log.info("product quantity updated successfully");



    }
}
