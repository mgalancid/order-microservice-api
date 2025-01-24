package com.mindhub.order_service.dtos.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductEntityDTO(Long id, @NotNull String name, @NotNull Double price, @Positive Integer stock) {
}
