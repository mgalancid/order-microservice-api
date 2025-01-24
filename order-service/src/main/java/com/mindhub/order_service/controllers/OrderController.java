package com.mindhub.order_service.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.mindhub.order_service.dtos.NewOrderEntityDTO;
import com.mindhub.order_service.dtos.OrderEntityDTO;
import com.mindhub.order_service.exceptions.OrderNotFoundException;
import com.mindhub.order_service.models.OrderStatus;
import com.mindhub.order_service.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderEntityDTO> createOrder(@RequestBody NewOrderEntityDTO newOrderEntityDTO) {
        OrderEntityDTO createdOrder = orderService.createOrder(newOrderEntityDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @GetMapping
    public ResponseEntity<List<OrderEntityDTO>> getAllOrders() {
        List<OrderEntityDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderEntityDTO> updateOrderStatus(@PathVariable Long id, @RequestBody OrderStatus status) {
        OrderEntityDTO updatedOrder = orderService.updateOrderStatus(id, status);

        if (updatedOrder == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(updatedOrder);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<OrderEntityDTO> confirmOrder(@PathVariable Long id,
                                                       @RequestParam Long userId) throws OrderNotFoundException {
        OrderEntityDTO confirmedOrder = orderService.confirmOrder(id, userId);
        return ResponseEntity.status(HttpStatus.OK).body(confirmedOrder);
    }
}

