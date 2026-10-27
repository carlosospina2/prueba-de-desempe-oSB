package com.helpflow.dispatch.service;

import com.helpflow.dispatch.dto.DispatchRequest;
import com.helpflow.dispatch.dto.DispatchResponse;
import org.springframework.stereotype.Service;

@Service
public class DispatchService {

    private static final int TECHNICIAN_COUNT = 20;
    private static final int MAX_HOURS = 12;

    public DispatchResponse dispatch(DispatchRequest request) {
        long seed = Math.abs((long) (request.ticketId() + ":" + request.categoria() + ":" + request.prioridad()).hashCode());
        int techIndex = (int) (seed % TECHNICIAN_COUNT) + 1;
        int hours = (int) (seed % MAX_HOURS) + 1;
        String tecnicoId = String.format("TEC-%03d", techIndex);
        String disponibilidad = hours <= 2 ? "ALTA" : hours <= 8 ? "MEDIA" : "BAJA";
        return new DispatchResponse(request.ticketId(), tecnicoId, disponibilidad, hours);
    }
}
