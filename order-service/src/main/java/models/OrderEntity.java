package models;

import jakarta.persistence.*;
import models.item.OrderItemEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private OrderStatus status = OrderStatus.PENDING;

    @OneToMany(mappedBy = "order", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<OrderItemEntity> products = List.of();

    public OrderEntity(){

    }

    public OrderEntity(OrderEntity order) {
        this.userId = order.userId;
        this.status = order.status;
        this.products = order.products.stream()
                .map(
                        orderItem -> new OrderItemEntity(orderItem)
                ).toList();
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
