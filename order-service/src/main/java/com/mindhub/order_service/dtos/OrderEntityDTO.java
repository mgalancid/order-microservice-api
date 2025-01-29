package com.mindhub.order_service.dtos;

import com.mindhub.order_service.models.OrderEntity;
import com.mindhub.order_service.models.OrderStatus;

import java.util.List;

public class OrderEntityDTO {
    private Long id;
    private Long userId;
    private OrderStatus status;
    private List<OrderItemDTO> products;

    public OrderEntityDTO() {

    }

    public OrderEntityDTO(OrderEntity order) {
        this.id = order.getId();
        this.userId = order.getUserId();
        this.status = order.getStatus();
        this.products = order.getProducts().stream().map(
                OrderItemDTO:: new
        ).toList();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<OrderItemDTO> getProducts() {
        return products;
    }

}



