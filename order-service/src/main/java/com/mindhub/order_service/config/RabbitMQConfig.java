package com.mindhub.order_service.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue createOrderQueue() {
        return new Queue("createOrder", false);
    }

    @Bean
    public Queue orderConfirmationQueue() {
        return new Queue("order_confirmation", true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange("exchange");
    }

    @Bean
    public Binding createOrderBinding(@Qualifier("createOrderQueue") Queue createOrderQueue,
                                      TopicExchange exchange) {
        return BindingBuilder.bind(createOrderQueue).to(exchange).with("order_confirmation");
    }

    @Bean
    public Binding orderConfirmationBinding(@Qualifier("orderConfirmationQueue") Queue orderConfirmationQueue,
                                            TopicExchange exchange) {
        return BindingBuilder.bind(orderConfirmationQueue).to(exchange).with("order_confirmation");
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        return rabbitTemplate(connectionFactory, jsonMessageConverter());
    }
}
