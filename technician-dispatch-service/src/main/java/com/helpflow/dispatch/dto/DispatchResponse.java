package com.helpflow.dispatch.dto;

public record DispatchResponse(
    Long ticketId,
    String tecnicoId,
    String disponibilidad,
    int tiempoEstimadoHoras
) {}
