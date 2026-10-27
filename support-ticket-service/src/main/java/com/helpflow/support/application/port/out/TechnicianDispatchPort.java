package com.helpflow.support.application.port.out;

public interface TechnicianDispatchPort {
    DispatchResult dispatch(Long ticketId, String categoria, String prioridad);

    record DispatchResult(
        Long ticketId,
        String tecnicoId,
        String disponibilidad,
        int tiempoEstimadoHoras
    ) {}
}
