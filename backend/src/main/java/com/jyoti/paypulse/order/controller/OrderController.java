package com.jyoti.paypulse.order.controller;

import com.jyoti.paypulse.order.dto.CreateOrderRequest;
import com.jyoti.paypulse.order.dto.OrderResponse;
import com.jyoti.paypulse.order.model.Product;
import com.jyoti.paypulse.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Orders & Catalog", description = "Order lifecycle management and product catalog operations")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/products")
    @Operation(summary = "Get list of available products in the catalog")
    public ResponseEntity<List<Product>> getProducts() {
        return ResponseEntity.ok(orderService.getAllProducts());
    }

    @PostMapping("/orders")
    @Operation(summary = "Place a new order with automated event-driven payment processing")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest req) {
        return ResponseEntity.ok(orderService.createOrder(req));
    }

    @GetMapping("/orders")
    @Operation(summary = "Get all orders sorted by creation time")
    public ResponseEntity<List<OrderResponse>> getOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/orders/{id}")
    @Operation(summary = "Get specific order details by ID")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
