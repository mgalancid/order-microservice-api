package com.mindhub.order_service.dtos;

import com.mindhub.order_service.models.item.OrderItemEntity;

public class OrderItemDTO {
    private final Long id;
    private final Long productId;
    private final Integer quantity;

    public OrderItemDTO(OrderItemEntity order) {
        this.id = order.getId();
        this.productId = order.getProductId();
        this.quantity = order.getQuantity();
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }
}
