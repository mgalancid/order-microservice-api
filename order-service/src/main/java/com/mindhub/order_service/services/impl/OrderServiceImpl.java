package com.mindhub.order_service.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindhub.order_service.dtos.NewOrderEntityDTO;
import com.mindhub.order_service.dtos.NewOrderItemEntityDTO;
import com.mindhub.order_service.dtos.OrderEntityDTO;
import com.mindhub.order_service.dtos.OrderItemDTO;
import com.mindhub.order_service.dtos.product.ProductEntityDTO;
import com.mindhub.order_service.dtos.user.UserEntityDTO;
import com.mindhub.order_service.exceptions.OrderNotFoundException;
import com.mindhub.order_service.exceptions.products.InsufficientStockException;
import com.mindhub.order_service.exceptions.products.ProductNotFoundException;
import com.mindhub.order_service.models.OrderEntity;
import com.mindhub.order_service.models.OrderStatus;
import com.mindhub.order_service.models.item.OrderItemEntity;
import com.mindhub.order_service.repositories.OrderItemRepository;
import com.mindhub.order_service.repositories.OrderRepository;
import com.mindhub.order_service.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
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

    private final String userServiceUrl;
    private final String productServiceUrl;

    public OrderServiceImpl(@Value("${user.service.url}") String userServiceUrl,
                            @Value("${product.service.url}") String productServiceUrl) {
        this.userServiceUrl = userServiceUrl;
        this.productServiceUrl = productServiceUrl;
    }

    @Override
    public OrderEntityDTO createOrder(NewOrderEntityDTO newOrderEntityDTO) {
        OrderEntity order = new OrderEntity();
        order.setStatus(OrderStatus.PENDING);

        UserEntityDTO user = fetchUserByEmail(newOrderEntityDTO.getUserEmail());
        order.setUserId(user.id());

        List<OrderItemEntity> orderItems = validateAndMapProducts(newOrderEntityDTO.getProducts(), order);
        order.setProducts(orderItems);

        order = orderRepository.save(order);
        return new OrderEntityDTO(order);
    }

    /// createOrder Methods

    private UserEntityDTO fetchUserByEmail(String email) {
        String userServiceEmailUrl = UriComponentsBuilder.fromUriString(userServiceUrl)
                .path("/email")
                .queryParam("email", email)
                .toUriString();

        ResponseEntity<UserEntityDTO> response = restTemplate.getForEntity(userServiceEmailUrl, UserEntityDTO.class);
        UserEntityDTO user = response.getBody();

        return user;
    }

    private List<OrderItemEntity> validateAndMapProducts(List<NewOrderItemEntityDTO> productRequests, OrderEntity order) {
        return productRequests.stream().map(request -> {
            try {
                String productUrl = UriComponentsBuilder.fromUriString(productServiceUrl)
                        .pathSegment(request.getProductId().toString())
                        .toUriString();

                ResponseEntity<ProductEntityDTO> response = restTemplate.getForEntity(productUrl, ProductEntityDTO.class);
                ProductEntityDTO product = response.getBody();

                if (product == null || product.id() == null) {
                    throw new ProductNotFoundException("Product with ID " + request.getProductId() + " not found.");
                }

                if (product.stock() < request.getQuantity()) {
                    throw new InsufficientStockException("Insufficient stock for product ID: " + product.id());
                }

                return new OrderItemEntity(order, product.id(), request.getQuantity());
            } catch (RestClientResponseException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    throw new ProductNotFoundException("Product with ID " + request.getProductId() + " not found.");
                } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                    throw new InsufficientStockException("Insufficient stock for product ID: " + request.getProductId());
                }
                throw new RuntimeException("Failed to fetch product: " + e.getMessage());
            }
        }).toList();
    }

    ///

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
    public void removeOrderItem(Long orderItemId) {
        OrderItemEntity orderItem = orderItemRepository.findById(orderItemId).orElseThrow(OrderNotFoundException::new);
        orderItemRepository.delete(orderItem);
    }

    @Override
    public OrderEntityDTO confirmOrder(Long orderId, Long userId) {
        Optional<OrderEntity> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) {
            throw new OrderNotFoundException("Order with ID " + orderId + " not found.");
        }
        OrderEntity order = optionalOrder.get();

        List<OrderItemDTO> orderItems = order.getProducts().stream()
                .map(OrderItemDTO::new)
                .collect(Collectors.toList());

        deductStockFromInventory(orderItems, order);

        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        return new OrderEntityDTO(order);
    }

    /// confirmOrder Method

    private void deductStockFromInventory(List<OrderItemDTO> orderItems, OrderEntity order) {
        try {
            restTemplate.patchForObject(productServiceUrl + "/stock", orderItems, Void.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                orderRepository.deleteById(order.getId());
                throw new RuntimeException("Order failed due to insufficient stock.");
            } else {
                throw new RuntimeException("Failed to deduct stock from product service: " + e.getMessage());
            }
        }
    }
}

