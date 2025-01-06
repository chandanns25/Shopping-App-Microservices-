package com.dailycodebuffer.ProductService.service;

import com.dailycodebuffer.ProductService.entity.Product;
import com.dailycodebuffer.ProductService.exception.ProductServiceCustomException;
import com.dailycodebuffer.ProductService.model.ProductRequest;
import com.dailycodebuffer.ProductService.model.ProductResponse;
import com.dailycodebuffer.ProductService.repository.ProductRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class ProductServiceImpl implements ProductService{

    @Autowired
    private ProductRepository productRepository;

    @Override
    public long addProduct(ProductRequest productRequest) {
        log.info("adding Product..");

        Product product = Product.builder()
                .productName(productRequest.getName())
                .quantity(productRequest.getQuantity())
                .price(productRequest.getPrice()).build();

        productRepository.save(product);

        log.info(":Product created");
        return product.getProductId();
    }

    @Override
    public ProductResponse getProductById(long productId) {
        log.info("get the product for productId :{} ",productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(()->new ProductServiceCustomException("Product with given id not found","Product_NOT_FOUND"));

        ProductResponse productResponse = new ProductResponse();

        BeanUtils.copyProperties(product,productResponse);
        log.info("got the product");
        return productResponse;


    }

    @Override
    public void reduceQuantity(long productId, long quantity) {
        log.info("Reduce Quantity {} for {}",quantity,productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ProductServiceCustomException("Product with given Id Not Found","PRODUCT_NOT_FOUND"));

        if(product.getQuantity() < quantity){
            throw  new ProductServiceCustomException("Product does not have sufficient Quantity","INSUFFICIENT_QUANTITY");
        }

        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);
        log.info("Product Quantity updated Sucessfully");
    }
}
