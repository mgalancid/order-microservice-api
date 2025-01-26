package com.mindhub.order_service.controllers;

import com.mindhub.order_service.dtos.NewOrderEntityDTO;
import com.mindhub.order_service.dtos.OrderEntityDTO;
import com.mindhub.order_service.dtos.UpdateOrderStatusDTO;
import com.mindhub.order_service.exceptions.OrderNotFoundException;
import com.mindhub.order_service.services.impl.OrderServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/orders")
public class OrderController {

    @Autowired
    private OrderServiceImpl orderService;

    @PostMapping
    public ResponseEntity<OrderEntityDTO> createOrder(@Valid @RequestBody NewOrderEntityDTO newOrderEntityDTO) {
        OrderEntityDTO createdOrder = orderService.createOrder(newOrderEntityDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @GetMapping
    public ResponseEntity<List<OrderEntityDTO>> getAllOrders() {
        List<OrderEntityDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderEntityDTO> updateOrderStatus(@PathVariable Long id,
                                                            @RequestBody UpdateOrderStatusDTO status) {
        OrderEntityDTO updatedOrder = orderService.updateOrderStatus(id, status.getStatus());

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

