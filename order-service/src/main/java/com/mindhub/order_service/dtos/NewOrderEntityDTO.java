package com.mindhub.order_service.dtos;

import com.mindhub.order_service.models.OrderEntity;
import com.mindhub.order_service.models.OrderStatus;
import com.mindhub.order_service.models.item.OrderItemEntity;

import java.util.List;

public class NewOrderEntityDTO {
    private final Long userId;
    private final OrderStatus status;
    private final List<OrderItemEntity> products;

    public NewOrderEntityDTO(OrderEntity order) {
        this.userId = order.getUserId();
        this.status = order.getStatus();
        this.products = order.getProducts();
    }

    public Long getUserId() {
        return userId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<OrderItemEntity> getProducts() {
        return products;
    }
}
