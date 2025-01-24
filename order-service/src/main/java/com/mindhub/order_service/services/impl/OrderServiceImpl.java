package com.mindhub.order_service.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindhub.order_service.dtos.NewOrderEntityDTO;
import com.mindhub.order_service.dtos.OrderEntityDTO;
import com.mindhub.order_service.dtos.order.ProductEntityDTO;
import com.mindhub.order_service.dtos.order.UserEntityDTO;
import com.mindhub.order_service.exceptions.OrderCreationException;
import com.mindhub.order_service.exceptions.OrderNotFoundException;
import com.mindhub.order_service.models.OrderEntity;
import com.mindhub.order_service.models.OrderStatus;
import com.mindhub.order_service.models.item.OrderItemEntity;
import com.mindhub.order_service.repositories.OrderItemRepository;
import com.mindhub.order_service.repositories.OrderRepository;
import com.mindhub.order_service.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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

    public OrderServiceImpl(@Value("${user.service.url") String userServiceUrl,
                            @Value("${product.service.url")String productServiceUrl) {
        this.userServiceUrl = userServiceUrl;
        this.productServiceUrl = productServiceUrl;
    }


    @Override
    public OrderEntityDTO createOrder(NewOrderEntityDTO newOrderEntityDTO) {
        OrderEntity order = new OrderEntity();
        order.setStatus(newOrderEntityDTO.getStatus());

        try {
            String userServiceEmailUrl = UriComponentsBuilder.fromPath(userServiceUrl)
                    .path("/users")
                    .queryParam("email", newOrderEntityDTO.getUserEmail())
                    .toUriString();

            ResponseEntity<UserEntityDTO> userResponse = restTemplate.getForEntity(userServiceEmailUrl,
                    UserEntityDTO.class);

            UserEntityDTO user = userResponse.getBody();
            if (user == null || user.id() == null) {
                throw new RuntimeException("User not found for email: " + newOrderEntityDTO.getUserEmail());
            }
            order.setUserId(user.id());

            List<ProductEntityDTO> products = newOrderEntityDTO.getProducts()
                    .stream()
                    .map(product -> {
                String productUrl = UriComponentsBuilder.fromPath(productServiceUrl)
                        .path("/" + product.getProductId())
                        .toUriString();

                ResponseEntity<ProductEntityDTO> productResponse = restTemplate.getForEntity(productUrl,
                                                                                            ProductEntityDTO.class);

                ProductEntityDTO productDetails = productResponse.getBody();
                if (productDetails == null || productDetails.id() == null) {
                    throw new RuntimeException("Product not found for ID: " + product.getProductId());
                }
                return productDetails;
            }).toList();

            order.setProducts(products.stream().map(
                    productEntityDTO -> new OrderItemEntity(order,
                            productEntityDTO.id(),
                            1)
            ).toList());

        } catch (Exception e) {
            throw new OrderCreationException("Order was not able to be processed");
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
}

