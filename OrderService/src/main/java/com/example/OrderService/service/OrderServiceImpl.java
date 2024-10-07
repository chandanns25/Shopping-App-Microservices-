package com.example.OrderService.service;

import com.example.OrderService.entity.Order;
import com.example.OrderService.exception.CustomException;
import com.example.OrderService.external.client.PaymentService;
import com.example.OrderService.external.client.ProductService;
import com.example.OrderService.external.request.PaymentRequest;
import com.example.OrderService.external.response.PaymentResponse;
import com.example.OrderService.model.OrderRequest;
import com.example.OrderService.model.OrderResponse;
import com.example.OrderService.repository.OrderRepository;
import com.example.ProductService.ProductService.model.ProductResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Instant;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService{
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public long placeOrder(OrderRequest orderRequest) {

        log.info("Placing order request:{},",orderRequest);

        productService.reduceQuantity(orderRequest.getProductId(),orderRequest.getQuantity());


        log.info("creating order with status CREATED");
        Order order = Order.builder()
                .productId(orderRequest.getProductId())
                .orderDate(Instant.now())
                .amount(orderRequest.getTotalAmount())
                .orderStatus("CREATED")
                .quantity(orderRequest.getQuantity()).build();

        order = orderRepository.save(order);

        log.info("calling the payment service to make payment");

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(order.getId())
                .paymentMode(orderRequest.getPaymentMode())
                .amount(orderRequest.getTotalAmount())
                .build();

        String orderStatus = null;
        try{
            paymentService.doPayment(paymentRequest);
            log.info("payment done successfully");
            orderStatus ="SUCCESS";
        }
        catch(Exception e){
            log.info("error in making payment ");
            orderStatus ="PAYMENT_FAILED";

        }
        order.setOrderStatus(orderStatus);
        orderRepository.save(order);

        log.info("Order has been created with order id:{}",order.getId());
        return order.getId();
    }

    @Override
    public OrderResponse getOrderDetails(long orderId) {
        log.info("get order details with id:{}",orderId);
        Order order = orderRepository.findById(orderId).orElseThrow(()-> new CustomException("Order not found for the orderId:"+orderId,"NOT_FOUND",404));

        log.info("Invoking Product service to fetch the product details for id:{}",order.getId());
        ProductResponse productResponse = restTemplate.getForObject("http://PRODUCT-SERVICE/product/"+order.getProductId(),
                ProductResponse.class);

        log.info("getting payment information from the payment service");

        PaymentResponse paymentResponse = restTemplate.getForObject("http://PAYMENT-SERVICE/payment/order/"+order.getId(), PaymentResponse.class);

        OrderResponse.ProductDetails productDetails =
        OrderResponse.ProductDetails
                .builder()
                .productName(productResponse.getProductName())
                .productId(productResponse.getProductId())
                .build();

        OrderResponse.PaymentDetails paymentDetails = OrderResponse.PaymentDetails.builder()
                .paymentId(paymentResponse.getPaymentId())
                .paymentStatus(paymentResponse.getStatus())
                .paymentDate(paymentResponse.getPaymentDate())
                .paymentMode(paymentResponse.getPaymentMode())
                .build();


        OrderResponse orderResponse = OrderResponse.builder()
                .orderId(order.getId())
                .amount(order.getAmount())
                .orderDate(Instant.now())
                .orderStatus(order.getOrderStatus())
                .productDetails(productDetails)
                .paymentDetails(paymentDetails)
                .build();

        return orderResponse;
    }
}
