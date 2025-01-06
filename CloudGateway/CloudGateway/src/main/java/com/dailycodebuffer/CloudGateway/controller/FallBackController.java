package com.dailycodebuffer.CloudGateway.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallBackController {

    @GetMapping("/orderServiceFallback")
    @CircuitBreaker(name="CircuitBreaker" , fallbackMethod = "/orderServiceFallback")
    public String orderServiceFallBack(){
        return "Order Service is Down!";
    }

    @GetMapping("/paymentServiceFallBack")
    @CircuitBreaker(name="CircuitBreaker" , fallbackMethod = "/paymentServiceFallBack")
    public String paymentServiceFallBack(){
        return "Payment Service is Down!";
    }

    @GetMapping("/productServiceFallBack")
    @CircuitBreaker(name="CircuitBreaker" , fallbackMethod = "/productServiceFallBack")
    public String productServiceFallBack(){
        return "Product Service is Down!";
    }
}
