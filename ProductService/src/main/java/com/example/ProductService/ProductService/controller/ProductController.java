package com.example.ProductService.ProductService.controller;

import com.example.ProductService.ProductService.model.ProductRequest;
import com.example.ProductService.ProductService.model.ProductResponse;
import com.example.ProductService.ProductService.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private ProductService productService;

    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping
    public ResponseEntity<Long>addproduct(@RequestBody ProductRequest productRequest){
        long productId = productService.addProduct(productRequest);
        return new ResponseEntity<>(productId, HttpStatus.CREATED);

        }
    @PreAuthorize("hasAuthority('Admin') || hasAuthority('Customer') || hasAuthority('SCOPE_internal')")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse>getproductById(@PathVariable("id") long productId){

        ProductResponse productResponse = productService.getproductById(productId);
        return new ResponseEntity<>(productResponse,HttpStatus.OK);

    }

    @PutMapping("/reducequantity/{id}")
    public ResponseEntity<Void> reduceQuantity(@PathVariable("id")long productId,@RequestParam long quantity){

        productService.reduceQuantity(productId,quantity);
        return new ResponseEntity<>(HttpStatus.OK);

    }
}
