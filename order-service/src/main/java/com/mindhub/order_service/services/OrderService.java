package com.mindhub.order_service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.mindhub.order_service.dtos.NewOrderEntityDTO;
import com.mindhub.order_service.dtos.OrderEntityDTO;
import com.mindhub.order_service.models.OrderStatus;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface OrderService {
    OrderEntityDTO createOrder(NewOrderEntityDTO newOrderEntityDTO);
    List<OrderEntityDTO> getAllOrders();
    OrderEntityDTO updateOrderStatus(Long orderId, OrderStatus status);
    OrderEntityDTO confirmOrder(Long orderId, Long userId);
}
