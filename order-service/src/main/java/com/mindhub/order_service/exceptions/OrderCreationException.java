package com.mindhub.order_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class OrderCreationException extends RuntimeException {
    public OrderCreationException() {
        super("Error during creation of an order");
    }
    public OrderCreationException(String message) {
        super(message);
    }
}
