package com.mindhub.order_service.exceptions.users;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.SERVICE_UNAVAILABLE)

public class UserServiceException extends RuntimeException {
    public UserServiceException() {
        super("User service was not found");
    }
    public UserServiceException(String message) {
        super(message);
    }
}
