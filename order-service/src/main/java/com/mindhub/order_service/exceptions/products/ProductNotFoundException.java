package com.mindhub.order_service.exceptions.products;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException() { super("Product was not found"); }
    public ProductNotFoundException(String message) {
        super(message);
    }
}

