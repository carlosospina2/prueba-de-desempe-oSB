package com.helpflow.support.infrastructure.adapter.out.dispatch;

import com.helpflow.support.application.port.out.TechnicianDispatchPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Component
public class TechnicianDispatchAdapter implements TechnicianDispatchPort {

    private static final Logger log = LoggerFactory.getLogger(TechnicianDispatchAdapter.class);

    private final RestTemplate restTemplate;
    private final String dispatchUrl;

    public TechnicianDispatchAdapter(RestTemplate restTemplate,
                                     @Value("${helpflow.dispatch.url:http://localhost:8081}") String dispatchUrl) {
        this.restTemplate = restTemplate;
        this.dispatchUrl = dispatchUrl;
    }

    @Override
    public DispatchResult dispatch(Long ticketId, String categoria, String prioridad) {
        try {
            var body = Map.of("ticketId", ticketId, "categoria", categoria, "prioridad", prioridad);
            @SuppressWarnings("unchecked")
            var response = restTemplate.postForObject(
                dispatchUrl + "/technician-dispatch", body, Map.class);
            if (response == null) throw new RuntimeException("Empty dispatch response");
            return new DispatchResult(
                ticketId,
                (String) response.get("tecnicoId"),
                (String) response.get("disponibilidad"),
                ((Number) response.get("tiempoEstimadoHoras")).intValue()
            );
        } catch (Exception e) {
            log.warn("Dispatch service unavailable: {}", e.getMessage());
            throw new RuntimeException("Dispatch service unavailable", e);
        }
    }
}
