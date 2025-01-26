package com.mindhub.order_service.exceptions.products;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT)
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException() {
        super("Insufficient stock on product");
    }
    public InsufficientStockException(String message) {
        super(message);
    }
}
