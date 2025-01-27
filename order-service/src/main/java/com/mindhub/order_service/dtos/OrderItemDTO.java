package com.mindhub.order_service.dtos;

import com.mindhub.order_service.models.item.OrderItemEntity;

public class OrderItemDTO {
    private Long id;
    private Long productId;
    private Integer quantity;

    public OrderItemDTO() {

    }

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
