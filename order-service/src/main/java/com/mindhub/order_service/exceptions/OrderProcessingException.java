package com.mindhub.order_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class OrderProcessingException extends RuntimeException {
  public OrderProcessingException() {
    super("Error during processing order");
  }
    public OrderProcessingException(String message) {
        super(message);
    }
}
