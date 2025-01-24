package com.mindhub.order_service.dtos.order;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record UserEntityDTO(Long id, @NotNull String username, @Email String email) {
}