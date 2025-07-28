package com.example.order_service.service;

import com.example.order_service.controller.OrderController;
import com.example.order_service.model.Order;
import com.example.order_service.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    //@CircuitBreaker(name = "productApi", fallbackMethod = "getProductFallBack")
    //@Retry(name = "productApi", fallbackMethod = "getProductFallBack")
    //@RateLimiter(name = "productApi", fallbackMethod = "getRateLimiterFallback")
    @Transactional
    public Order placeOrder(Order OrderRequest) {//user sent productname and qty

        //logger.info("THIS IS AN INFO LOG - Should always be visible.");
        //logger.debug("THIS IS A DEBUG LOG - Should be visible if config is working.");

        BigDecimal totalOrderPrice = BigDecimal.ZERO;


            String productName = OrderRequest.getProductName();
            Integer quantityToOrder = OrderRequest.getQuantity();

            String ProductUrl = "http://localhost:8081/products/by_name/" + productName;
            Map<String, Object> ProductResponse = restTemplate.getForObject(ProductUrl, Map.class);
            Integer stockQuantity = (ProductResponse != null) ? (Integer) ProductResponse.get("quantity") : 0;

            if (stockQuantity < quantityToOrder) {
                throw new IllegalArgumentException("Insufficient quantity or product out of stock" + productName);
            }

            if (ProductResponse == null || ProductResponse.get("price") == null) {
                throw new RuntimeException("Internal server error" + productName);
            }
            BigDecimal pricePerUnit = new BigDecimal(ProductResponse.get("price").toString());

            totalOrderPrice = totalOrderPrice.add(pricePerUnit.multiply(BigDecimal.valueOf(quantityToOrder)));


        OrderRequest.setTotalPrice(totalOrderPrice);
        OrderRequest.setStatus("PENDING");

        return orderRepository.save(OrderRequest);
    }

    /*

     //@TimeLimiter(name = "productApi", fallbackMethod = "getProductFallBack")
    public CompletableFuture<Order> placeOrder(Order OrderRequest) {

        // Hum poore logic ko CompletableFuture.supplyAsync ke andar daal denge
        // Yeh poore kaam ko ek alag "worker thread" par chala dega
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Placing order for product: {}", OrderRequest.getProductName());

            BigDecimal totalOrderPrice = BigDecimal.ZERO;
            String productName = OrderRequest.getProductName();
            Integer quantityToOrder = OrderRequest.getQuantity();
            try {
                Thread.sleep(5000); // force method to run longer than TimeLimiter timeout
            } catch (InterruptedException e) {
            }
            // Network Call
            String ProductUrl = "http://localhost:8081/products/by_name/" + productName;
            Map<String, Object> ProductResponse = restTemplate.getForObject(ProductUrl, Map.class);

            // Business Logic
            Integer stockQuantity = (ProductResponse != null) ? (Integer) ProductResponse.get("quantity") : 0;

            if (stockQuantity < quantityToOrder) {
                throw new IllegalArgumentException("Insufficient quantity for product " + productName);
            }
            if (ProductResponse == null || ProductResponse.get("price") == null) {
                throw new RuntimeException("Could not fetch price for product " + productName);
            }

            BigDecimal pricePerUnit = new BigDecimal(ProductResponse.get("price").toString());
            totalOrderPrice = totalOrderPrice.add(pricePerUnit.multiply(BigDecimal.valueOf(quantityToOrder)));

            OrderRequest.setTotalPrice(totalOrderPrice);
            OrderRequest.setStatus("PENDING");

            // Database Call
            return orderRepository.save(OrderRequest);
        });
    }

     */


    private Order getProductFallBack(Order OrderRequest, Exception ex){
        logger.warn("Fallback executed for product: {}. Reason: {}", OrderRequest.getProductName(), ex.getMessage());
        throw new RuntimeException("Product service is currently unavailable. Please try again later.");
    }

    private Order getRateLimiterFallback(Order order, Exception ex) {
        logger.warn("RATE LIMITER APPLIED for product: {}. Too many requests. Reason: {}", order.getProductName(), ex.getMessage());
        throw new RuntimeException("Too many requests sent to the Product service. Please try again in a moment.");
    }

}