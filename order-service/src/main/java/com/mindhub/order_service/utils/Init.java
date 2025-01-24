package com.mindhub.order_service.utils;

import com.mindhub.order_service.models.OrderEntity;
import com.mindhub.order_service.models.OrderStatus;
import com.mindhub.order_service.models.item.OrderItemEntity;
import com.mindhub.order_service.repositories.OrderItemRepository;
import com.mindhub.order_service.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Init implements CommandLineRunner {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Override
    public void run(String... args) throws Exception {
        OrderEntity order1 = new OrderEntity(1L, OrderStatus.PENDING);
        orderRepository.save(order1);

        OrderItemEntity orderItem1 = new OrderItemEntity(order1,1L, 5);
        orderItemRepository.save(orderItem1);

        OrderEntity order2 = new OrderEntity(1L, OrderStatus.COMPLETED);
        orderRepository.save(order2);

        OrderItemEntity orderItem2 = new OrderItemEntity(order2,1L, 10);
        orderItemRepository.save(orderItem2);
    }
}
