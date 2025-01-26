package com.mindhub.order_service.dtos;

import com.mindhub.order_service.models.item.OrderItemEntity;

public class NewOrderItemEntityDTO {
    private Long productId;
    private Integer quantity;

    public NewOrderItemEntityDTO() {
    }

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
