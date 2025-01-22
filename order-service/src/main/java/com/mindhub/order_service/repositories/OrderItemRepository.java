package com.mindhub.order_service.repositories;

import com.mindhub.order_service.models.item.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
}
