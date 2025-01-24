package com.mindhub.order_service.models;

import jakarta.persistence.*;
import com.mindhub.order_service.models.item.OrderItemEntity;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Order status cannot be null")
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    @OneToMany(mappedBy = "order", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @NotEmpty(message = "Order must contain at least one product")
    private List<OrderItemEntity> products = List.of();

    public OrderEntity(){

    }

    public OrderEntity(Long userId, OrderStatus status) {
        this.userId = userId;
        this.status = status;
    }

    public void addProducts(OrderItemEntity orderItem) {
        orderItem.setOrder(this);
        products.add(orderItem);
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public List<OrderItemEntity> getProducts() {
        return products;
    }

    public void setProducts(List<OrderItemEntity> products) {
        this.products = products;
    }
}
