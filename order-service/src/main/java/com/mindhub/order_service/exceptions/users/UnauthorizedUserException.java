package com.mindhub.order_service.exceptions.users;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNAUTHORIZED)
public class UnauthorizedUserException extends RuntimeException {
  public UnauthorizedUserException() {
    super("Error during creation of an order");
  }
  public UnauthorizedUserException(String message) {
        super(message);
    }
}
