package com.mindhub.order_service.repositories;

import com.mindhub.order_service.models.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
}
