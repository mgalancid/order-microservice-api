package com.mindhub.order_service.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindhub.order_service.dtos.*;
import com.mindhub.order_service.dtos.product.ProductEntityDTO;
import com.mindhub.order_service.dtos.user.UserEntityDTO;
import com.mindhub.order_service.exceptions.OrderCreationException;
import com.mindhub.order_service.exceptions.OrderNotFoundException;
import com.mindhub.order_service.exceptions.products.InsufficientStockException;
import com.mindhub.order_service.exceptions.products.ProductNotFoundException;
import com.mindhub.order_service.exceptions.users.UnauthorizedUserException;
import com.mindhub.order_service.exceptions.users.UserNotFoundException;
import com.mindhub.order_service.exceptions.users.UserServiceException;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private RestTemplate restTemplate;

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private ObjectMapper objectMapper;

    private final String userServiceUrl;
    private final String productServiceUrl;

    public OrderServiceImpl(@Value("${user.service.url}") String userServiceUrl,
                            @Value("${product.service.url}") String productServiceUrl) {
        this.userServiceUrl = userServiceUrl;
        this.productServiceUrl = productServiceUrl;
    }

    @Transactional(rollbackFor = OrderCreationException.class)
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

    public void processOrderMessage(OrderEntityDTO orderEntityDTO) {
        log.info("Processing received order from RabbitMQ: {}", orderEntityDTO);
    }

    private UserEntityDTO fetchUserByEmail(String email) {
        String userServiceEmailUrl = UriComponentsBuilder.fromUriString(userServiceUrl)
                .path("/email")
                .queryParam("email", email)
                .toUriString();

        ResponseEntity<UserEntityDTO> response = restTemplate.getForEntity(userServiceEmailUrl, UserEntityDTO.class);
        return response.getBody();
    }

    private List<OrderItemEntity> validateAndMapProducts(List<NewOrderItemEntityDTO> productRequests, OrderEntity order) {
        List<Long> productIds = productRequests.stream()
                .map(NewOrderItemEntityDTO::getProductId)
                .toList();

        String productUrl = UriComponentsBuilder.fromUriString(productServiceUrl)
                .queryParam("ids", String.join(",",
                        productIds.stream()
                        .map(String::valueOf)
                        .toList()))
                .toUriString();

        ResponseEntity<ProductEntityDTO[]> response = restTemplate.getForEntity(productUrl, ProductEntityDTO[].class);
        ProductEntityDTO[] products = response.getBody();

        if (products == null || products.length == 0) {
            throw new ProductNotFoundException("No products found for IDs: " + productIds);
        }

        Map<Long, ProductEntityDTO> productMap = Arrays.stream(products)
                .collect(Collectors.toMap(ProductEntityDTO::id, Function.identity()));

        return productRequests.stream().map(request -> {
            ProductEntityDTO product = productMap.get(request.getProductId());
            if (product == null) {
                throw new ProductNotFoundException("Product with ID " + request.getProductId() + " not found.");
            }

            if (product.stock() < request.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product ID: " + product.id());
            }

            return new OrderItemEntity(order, product.id(), request.getQuantity());
        }).toList();
    }

    ///

    public List<OrderEntityDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .peek(order -> order.getProducts().size())
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private OrderEntityDTO mapToDTO(OrderEntity order) {
            return objectMapper.convertValue(order, OrderEntityDTO.class);
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
    public OrderConfirmationEmailDTO confirmOrder(Long orderId, Long userId) {
        Optional<OrderEntity> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) {
            throw new OrderNotFoundException("Order with ID " + orderId + " not found.");
        }
        OrderEntity order = optionalOrder.get();

        if (!order.getUserId().equals(userId)) {
            throw new UnauthorizedUserException("User " + userId + " is not authorized to confirm order " + orderId);
        }

        String userEmail = getUserEmail(userId); // Or fetchUserByEmail(userId) if you have a direct way

        List<OrderItemDTO> orderItems = order.getProducts().stream()
                .map(OrderItemDTO::new)
                .collect(Collectors.toList());

        deductStockFromInventory(orderItems, order);

        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        return new OrderConfirmationEmailDTO(
                order.getId(),
                userEmail,
                order.getStatus().toString(),
                orderItems
        );
    }

    /// confirmOrder Method

    private String getUserEmail(Long userId) {
        String userServiceUrlWithId = UriComponentsBuilder.fromUriString(userServiceUrl)
                .path("/" + userId)
                .build()
                .toUriString();

        try {
            ResponseEntity<UserEntityDTO> response = restTemplate
                    .getForEntity(userServiceUrlWithId, UserEntityDTO.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody().email();
            } else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new UserNotFoundException("User with ID " + userId + " not found.");
            }

        } catch (RestClientException e) {
            log.error("Error communicating with user service: {}", e.getMessage());
            throw new UserServiceException("Error communicating with user service.");
        }
        return userServiceUrlWithId;
    }

    private void deductStockFromInventory(List<OrderItemDTO> orderItems, OrderEntity order) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<List<OrderItemDTO>> requestEntity = new HttpEntity<>(orderItems, headers);

            restTemplate.exchange(
                    productServiceUrl + "/stock",
                    HttpMethod.PATCH,
                    requestEntity,
                    Void.class
            );
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

