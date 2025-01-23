package com.mindhub.order_service.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindhub.order_service.dtos.NewOrderEntityDTO;
import com.mindhub.order_service.dtos.OrderEntityDTO;
import com.mindhub.order_service.exceptions.OrderNotFoundException;
import com.mindhub.order_service.models.OrderEntity;
import com.mindhub.order_service.models.OrderStatus;
import com.mindhub.order_service.repositories.OrderItemRepository;
import com.mindhub.order_service.repositories.OrderRepository;
import com.mindhub.order_service.services.OrderService;
import com.mindhub.order_service.utils.RestTemplateConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public OrderEntityDTO createOrder(NewOrderEntityDTO newOrderEntityDTO) {
        OrderEntity order = new OrderEntity();
        order.setStatus(newOrderEntityDTO.getStatus());


        try {
            String userServiceUrl = "http://localhost:8081/api/users?email=" + newOrderEntityDTO.getUserEmail();
            JsonNode userDetails = getJsonFromUrl(userServiceUrl);
            order.setUserId(userDetails.path("id").asLong());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to fetch user details", e);
        }

        OrderEntity savedOrder = orderRepository.save(order);
        return new OrderEntityDTO(savedOrder);
    }

    @Override
    public List<OrderEntityDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderEntityDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public OrderEntityDTO updateOrderStatus(Long orderId, OrderStatus status) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);

        order.setStatus(status);
        OrderEntity updatedOrder = orderRepository.save(order);

        return new OrderEntityDTO(updatedOrder);
    }

    @Override
    public OrderEntityDTO confirmOrder(Long orderId, Long userId) {
        Optional<OrderEntity> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) {
            throw new OrderNotFoundException("Order with ID " + orderId + " not found.");
        }
        OrderEntity order = optionalOrder.get();

        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        return new OrderEntityDTO(order);
    }

    @Override
    public JsonNode getJsonFromUrl(String url) throws JsonProcessingException {
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return objectMapper.readTree(response.getBody());
    }
}

