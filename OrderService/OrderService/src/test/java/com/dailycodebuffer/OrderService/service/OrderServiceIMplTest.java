package com.dailycodebuffer.OrderService.service;


import com.dailycodebuffer.OrderService.entity.Order;
import com.dailycodebuffer.OrderService.exception.CustomException;
import com.dailycodebuffer.OrderService.external.client.PaymentService;
import com.dailycodebuffer.OrderService.external.client.ProductService;
import com.dailycodebuffer.OrderService.external.request.PaymentRequest;
import com.dailycodebuffer.OrderService.external.response.PaymentResponse;
import com.dailycodebuffer.OrderService.model.OrderRequest;
import com.dailycodebuffer.OrderService.model.OrderResponse;
import com.dailycodebuffer.OrderService.model.PaymentMode;
import com.dailycodebuffer.OrderService.repository.OrderRepository;
import com.dailycodebuffer.ProductService.model.ProductResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito.*;
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
public class OrderServiceIMplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    OrderService orderService = new OrderServiceIMpl();

    @DisplayName("Get Order - Success Scenario")
    @Test
    void test_When_Order_Success(){
        //Mocking
        Order order = getMockOrder();

        when(orderRepository.findById(anyLong()))
                .thenReturn(Optional.of(order));

        when(restTemplate.getForObject("http://PRODUCT-SERVICE/product/"+order.getProductId(), ProductResponse.class))
                .thenReturn(getMockProductResponse());

        when(restTemplate.getForObject("http://PAYMENT-SERVICE/payment/order/"+order.getId(), PaymentResponse.class))
                .thenReturn(getMockPaymentResponse());


        //Actual
        OrderResponse orderResponse = orderService.getOrderDetails(1);


        //Verification
        verify(orderRepository,times(1)).findById(anyLong());
        verify(restTemplate,times(1)).getForObject("http://PRODUCT-SERVICE/product/"+order.getProductId(), ProductResponse.class);
        verify(restTemplate,times(1)).getForObject("http://PAYMENT-SERVICE/payment/order/"+order.getId(), PaymentResponse.class);

        //Assertion
        assertNotNull(orderResponse);
        assertEquals(order.getId(),orderResponse.getOrderId());



        }

        @DisplayName("Get Orders - Failure Scenario")
        @Test
        void test_When_Get_Order_NOT_FOUND_then_Not_Found(){

        when(orderRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(null));

            //Actual
           // OrderResponse orderResponse = orderService.getOrderDetails(1);

        //assertions
            CustomException exception = assertThrows(CustomException.class,()->orderService.getOrderDetails(1));
            assertEquals("NOT_FOUND",exception.getErrorCode());
            assertEquals(404,exception.getStatus());

            verify(orderRepository,times(1)).findById(anyLong());

        }

        @DisplayName("Place Order Success Scenario")
        @Test
        void test_When_Place_Order_Success(){

            Order order = getMockOrder();
            OrderRequest orderRequest = getMockOrderRequest();

            when(orderRepository.save(any(Order.class)))
                    .thenReturn(order);
            when(productService.reduceQuantity(anyLong(),anyLong()))
                    .thenReturn(new ResponseEntity<Void>(HttpStatus.OK));
            when(paymentService.doPayment(any(PaymentRequest.class)))
                    .thenReturn(new ResponseEntity<Long>(1L,HttpStatus.OK));

            long orderId = orderService.placeOrder(orderRequest);

            verify(orderRepository,times(2)).save(any());
            verify(productService,times(1)).reduceQuantity(anyLong(),anyLong());
            verify(paymentService,times(1)).doPayment(any(PaymentRequest.class));

            assertEquals(order.getId(),orderId);
        }

        @DisplayName("Place Order - Payment failed Scenario")
        @Test
        void test_When_Place_Order_Payment_Fails_then_Order_Placed(){

            Order order = getMockOrder();
            OrderRequest orderRequest = getMockOrderRequest();

            when(orderRepository.save(any(Order.class)))
                    .thenReturn(order);
            when(productService.reduceQuantity(anyLong(),anyLong()))
                    .thenReturn(new ResponseEntity<Void>(HttpStatus.OK));
            when(paymentService.doPayment(any(PaymentRequest.class)))
                    .thenThrow(new RuntimeException());

            long orderId = orderService.placeOrder(orderRequest);

            verify(orderRepository,times(2)).save(any());
            verify(productService,times(1)).reduceQuantity(anyLong(),anyLong());
            verify(paymentService,times(1)).doPayment(any(PaymentRequest.class));

            assertEquals(order.getId(),orderId);

        }

    private OrderRequest getMockOrderRequest() {

        return OrderRequest.builder()
                .productId(1)
                .quantity(10)
                .paymentMode(PaymentMode.CASH)
                .totalAmount(2000)
                .build();

    }

    private PaymentResponse getMockPaymentResponse() {

        return PaymentResponse.builder()
                .paymentId(1)
                .paymentDate(Instant.now())
                .paymentMode(PaymentMode.CASH)
                .amount(2000)
                .orderid(1)
                .status("ACCEPTED")
                .build();
    }

    private ProductResponse getMockProductResponse() {

        return ProductResponse.builder()
                .productId(Long.valueOf(1))
                .productName("Iphone")
                .price(2000)
                .quantity(10)
                .build();
    }

    private Order getMockOrder() {

        return Order.builder()
                .orderStatus("PLACED")
                .orderDate(Instant.now())
                .id(1)
                .amount(2000)
                .quantity(10)
                .productId(1)
                .build();
    }









    }
