package com.helpflow.dispatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DispatchRequest(
    @NotNull Long ticketId,
    @NotBlank String categoria,
    @NotBlank String prioridad
) {}
