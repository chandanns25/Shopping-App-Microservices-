package com.example.OrderService.service;


import com.example.OrderService.entity.Order;
import com.example.OrderService.exception.CustomException;
import com.example.OrderService.external.client.PaymentService;
import com.example.OrderService.external.client.ProductService;
import com.example.OrderService.external.request.PaymentRequest;
import com.example.OrderService.external.response.PaymentResponse;
import com.example.OrderService.model.OrderRequest;
import com.example.OrderService.model.OrderResponse;
import com.example.OrderService.model.PaymentMode;
import com.example.OrderService.repository.OrderRepository;
import com.example.ProductService.ProductService.model.ProductResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class OrderServiceImplTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductService productService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    OrderService orderService = new OrderServiceImpl();


    @DisplayName("get order success scenario")
    @Test
    void test_When_Order_Success(){

        //Mocking
        Order order = getMockOrder();
        when(orderRepository.findById(anyLong()))
                .thenReturn(Optional.of(order));
        when(restTemplate.getForObject("http://PRODUCT-SERVICE/product/"+order.getProductId(),
                        ProductResponse.class)).thenReturn(getMockProductResponse());
        when(restTemplate.getForObject("http://PAYMENT-SERVICE/payment/order/"+order.getId(), PaymentResponse.class))
                .thenReturn(getMockPaymentresponse());

        //Actual

        OrderResponse orderResponse = orderService.getOrderDetails(1);
        //Verification
        verify(orderRepository,times(1)).findById(anyLong());
        verify(restTemplate,times(1)).getForObject("http://PRODUCT-SERVICE/product/"+order.getProductId(),
                ProductResponse.class);
        verify(restTemplate,times(1)).getForObject("http://PAYMENT-SERVICE/payment/order/"+order.getId(), PaymentResponse.class);

        //Assert
        assertNotNull(orderResponse);
        assertEquals(order.getId(),orderResponse.getOrderId());
    }
    @DisplayName("get order failure scenario")
    @Test
    void test_When_Order_NOTFOUND_get_NOTFOUND(){

        when(orderRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(null));

//        OrderResponse orderResponse = orderService.getOrderDetails(1);

        CustomException exception = assertThrows(CustomException.class,() ->orderService.getOrderDetails(1));

        assertEquals("NOT_FOUND",exception.getErrorCode());
        assertEquals(404,exception.getStatus());

        verify(orderRepository,times(1)).findById(anyLong());

    }
    @DisplayName("place order success scenario")
    @Test
    void test_When_place_order_success(){

        Order order = getMockOrder();

        OrderRequest orderRequest = getMockOrderRequest();

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);

        when(productService.reduceQuantity(anyLong(),anyLong()))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.OK));

        when(paymentService.doPayment(any(PaymentRequest.class)))
                .thenReturn(new ResponseEntity<Long>(1L,HttpStatus.OK));

        long orderId = orderService.placeOrder(orderRequest);

        verify(orderRepository,times(2))
                .save(any());

        verify(productService,times(1))
                .reduceQuantity(anyLong(),anyLong());
        verify(paymentService,times(1))
                .doPayment(any(PaymentRequest.class));

        assertEquals(order.getId(),orderId);

    }

    @DisplayName("placed order but payment failed")
    @Test
    void test_when_place_order_payment_fails(){

        Order order = getMockOrder();

        OrderRequest orderRequest = getMockOrderRequest();

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);

        when(productService.reduceQuantity(anyLong(),anyLong()))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.OK));

        when(paymentService.doPayment(any(PaymentRequest.class)))
                .thenThrow(new RuntimeException());

        long orderId = orderService.placeOrder(orderRequest);

        verify(orderRepository,times(2))
                .save(any());

        verify(productService,times(1))
                .reduceQuantity(anyLong(),anyLong());
        verify(paymentService,times(1))
                .doPayment(any(PaymentRequest.class));

        assertEquals(order.getId(),orderId);


    }

    private OrderRequest getMockOrderRequest() {
        return OrderRequest.builder()
                .productId(1)
                .paymentMode(PaymentMode.CASH)
                .quantity(100)
                .totalAmount(200)
                .build();
    }


    private PaymentResponse getMockPaymentresponse() {
        return PaymentResponse.builder()
                .paymentDate(Instant.now())
                .orderId(1)
                .paymentMode(PaymentMode.APPLE_PAY)
                .amount(100)
                .status("Success")
                .paymentId(1)
                .build();
    }

    private ProductResponse getMockProductResponse() {
        return ProductResponse.builder()
                .productId(2)
                .quantity(200)
                .price(100)
                .productName("Iphone")
                .build();
    }

    private Order getMockOrder() {
        return Order.builder()
                .orderDate(Instant.now())
                .orderStatus("Placed")
                .id(1)
                .quantity(200)
                .amount(100)
                .productId(2)
                .build();
    }


}