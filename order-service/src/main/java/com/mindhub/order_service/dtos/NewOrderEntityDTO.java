package com.mindhub.order_service.dtos;

import com.mindhub.order_service.models.OrderEntity;
import com.mindhub.order_service.models.OrderStatus;
import com.mindhub.order_service.models.item.OrderItemEntity;

import java.util.List;

public class NewOrderEntityDTO {
    private String userEmail;
    private OrderStatus status;
    private List<NewOrderItemEntityDTO> products;

    public NewOrderEntityDTO() {

    }

    public NewOrderEntityDTO(String userEmail, OrderStatus status, List<NewOrderItemEntityDTO> products) {
        this.userEmail = userEmail;
        this.status = status;
        this.products = products;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<NewOrderItemEntityDTO> getProducts() {
        return products;
    }
}
