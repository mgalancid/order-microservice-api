package com.mindhub.order_service.dtos;

import com.mindhub.order_service.models.OrderEntity;
import com.mindhub.order_service.models.OrderStatus;
import com.mindhub.order_service.models.item.OrderItemEntity;

import java.util.List;

public class OrderEntityDTO {
    private final Long id;
    private final Long userId;
    private final OrderStatus status;
    private final List<OrderItemEntity> products;

    public OrderEntityDTO(OrderEntity order) {
        this.id = order.getId();
        this.userId = order.getUserId();
        this.status = order.getStatus();
        this.products = order.getProducts();
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

    public List<OrderItemEntity> getProducts() {
        return products;
    }
}



