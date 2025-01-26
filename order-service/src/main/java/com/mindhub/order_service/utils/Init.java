package com.mindhub.order_service.utils;

import com.mindhub.order_service.dtos.product.ProductEntityDTO;
import com.mindhub.order_service.dtos.user.UserEntityDTO;
import com.mindhub.order_service.models.OrderEntity;
import com.mindhub.order_service.models.OrderStatus;
import com.mindhub.order_service.models.item.OrderItemEntity;
import com.mindhub.order_service.repositories.OrderItemRepository;
import com.mindhub.order_service.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component
public class Init implements CommandLineRunner {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${user.service.url}")
    private String userServiceUrl;

    @Value("${product.service.url}")
    private String productServiceUrl;

    @Override
    public void run(String... args) throws Exception {
        String userEmail = "john.doe@example.com";
        UserEntityDTO user = fetchUserByEmail(userEmail);

        if (user == null || user.id() == null) {
            throw new RuntimeException("User not found for email: " + userEmail);
        }

        OrderEntity order1 = new OrderEntity();
        order1.setStatus(OrderStatus.PENDING);
        order1.setUserId(user.id());

        OrderItemEntity item1 = new OrderItemEntity();
        item1.setOrder(order1);
        item1.setProductId(1L);
        item1.setQuantity(5);
        order1.addProducts(item1);

        ProductEntityDTO product = fetchProductById(item1.getProductId());
        if (product == null || product.id() == null) {
            throw new RuntimeException("Product not found with ID: " + item1.getProductId());
        }

        order1.addProducts(item1);

        orderRepository.save(order1);
    }

    private UserEntityDTO fetchUserByEmail(String email) {
        String userServiceEmailUrl = UriComponentsBuilder.fromUriString(userServiceUrl)
                .path("/email")
                .queryParam("email", email)
                .toUriString();

        ResponseEntity<UserEntityDTO> response = restTemplate.getForEntity(userServiceEmailUrl, UserEntityDTO.class);
        return response.getBody();
    }

    private ProductEntityDTO fetchProductById(Long productId) {
        String productServiceProductUrl = UriComponentsBuilder.fromUriString(productServiceUrl)
                .path("/products")
                .queryParam("ids", productId)
                .toUriString();

        ResponseEntity<List<ProductEntityDTO>> response = restTemplate.exchange(
                productServiceProductUrl, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<ProductEntityDTO>>() {}
        );

        List<ProductEntityDTO> products = response.getBody();
        if (products != null && !products.isEmpty()) {
            return products.get(0);
        } else {
            return null;
        }
    }

}


