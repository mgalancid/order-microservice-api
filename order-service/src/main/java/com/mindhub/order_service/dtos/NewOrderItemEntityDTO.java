package com.mindhub.order_service.dtos;

import com.mindhub.order_service.models.item.OrderItemEntity;

public class NewOrderItemEntityDTO {
    private final Long productId;
    private final Integer quantity;

    public NewOrderItemEntityDTO(OrderItemEntity order) {
        this.productId = order.getProductId();
        this.quantity = order.getQuantity();
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }
}
