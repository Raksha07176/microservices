package com.example.order_service.controller;

import com.example.order_service.model.Order;
import com.example.order_service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody Order order) {
        try {
            Order createdOrder = orderService.placeOrder(order);
            return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Bad request :" + e.getMessage(), HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            return new ResponseEntity<>("Unable to load order details " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}