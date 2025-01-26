package com.mindhub.order_service.dtos;

import com.mindhub.order_service.models.OrderStatus;

public class UpdateOrderStatusDTO {
    private OrderStatus status;

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}

