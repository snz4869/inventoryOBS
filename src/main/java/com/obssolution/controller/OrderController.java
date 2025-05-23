package com.obssolution.controller;

import com.obssolution.dto.order.OrderRequestDTO;
import com.obssolution.dto.order.OrderResponseDTO;
import com.obssolution.dto.order.OrderUpdateRequestDTO;
import com.obssolution.dto.PageResponseDTO;
import com.obssolution.exceptions.ResourceNotFoundException;
import com.obssolution.service.impl.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<?> getAllOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Fetching paginated orders (page: {}, size: {})", page, size);
            PageResponseDTO<OrderResponseDTO> response = orderService.getAllOrdersPaginated(page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving paginated orders: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving orders: " + e.getMessage());
        }
    }

    @GetMapping("/{orderNo}")
    public ResponseEntity<?> getOrderByOrderNo(@PathVariable String orderNo) {
        try {
            log.info("Fetching order with orderNo: {}", orderNo);
            OrderResponseDTO order = orderService.getOrderByOrderNo(orderNo);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid orderNo request: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid orderNo: " + e.getMessage());
        } catch (ResourceNotFoundException e) {
            log.warn("Order not found with orderNo: {}", orderNo);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error fetching order with orderNo {}: {}", orderNo, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error retrieving order: " + e.getMessage());
        }
    }

    @PostMapping("/save")
    public ResponseEntity<?> createOrder(@RequestBody @Valid OrderRequestDTO requestDTO) {
        try {
            OrderResponseDTO createdOrder = orderService.createOrder(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        } catch (Exception e) {
            log.error("Error creating order: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create order: " + e.getMessage());
        }
    }

    @PutMapping("/edit")
    public ResponseEntity<?> updateOrder(@RequestBody @Valid OrderUpdateRequestDTO requestDTO) {
        try {
            OrderResponseDTO updatedOrder = orderService.updateOrder(requestDTO);
            return ResponseEntity.ok(updatedOrder);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid update request: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid data: " + e.getMessage());
        } catch (ResourceNotFoundException e) {
            log.warn("Order not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating order: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update order: " + e.getMessage());
        }
    }

    @PutMapping("/delete/{orderNo}")
    public ResponseEntity<?> deleteOrder(@PathVariable String orderNo) {
        try {
            orderService.deleteOrderByOrderNo(orderNo);
            return ResponseEntity.ok("Order successfully marked as deleted.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Delete failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            log.warn("Order not found during delete: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during delete: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error deleting order: " + e.getMessage());
        }
    }
}