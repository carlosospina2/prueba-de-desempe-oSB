package com.helpflow.dispatch.controller;

import com.helpflow.dispatch.dto.DispatchRequest;
import com.helpflow.dispatch.dto.DispatchResponse;
import com.helpflow.dispatch.service.DispatchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for TechnicianDispatchController.
 *
 * Uses @WebMvcTest — only the controller and its MVC configuration are loaded.
 * DispatchService is mocked so these tests are completely independent of the
 * business logic (which is covered by DispatchServiceTest).
 *
 * Scenarios verified:
 *  - Valid request → 200 with correct JSON response fields
 *  - Controller delegates to DispatchService exactly once
 *  - Missing ticketId (null) → 400 Bad Request
 *  - Missing categoria (blank) → 400 Bad Request
 *  - Missing prioridad (blank) → 400 Bad Request
 *  - GET on the POST endpoint → 405 Method Not Allowed
 */
@WebMvcTest(TechnicianDispatchController.class)
class TechnicianDispatchControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean DispatchService dispatchService;

    // ── Happy path ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Valid request returns 200 with correct response body")
    void validRequestReturns200() throws Exception {
        DispatchResponse stubResponse =
                new DispatchResponse(125L, "TEC-007", "ALTA", 2);

        when(dispatchService.dispatch(any(DispatchRequest.class)))
                .thenReturn(stubResponse);

        mockMvc.perform(post("/technician-dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ticketId":125,"categoria":"RED","prioridad":"ALTA"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ticketId").value(125))
                .andExpect(jsonPath("$.tecnicoId").value("TEC-007"))
                .andExpect(jsonPath("$.disponibilidad").value("ALTA"))
                .andExpect(jsonPath("$.tiempoEstimadoHoras").value(2));

        verify(dispatchService).dispatch(any(DispatchRequest.class));
    }

    @Test
    @DisplayName("Second call with same input returns the same response (controller is stateless)")
    void repeatedCallReturnsSameResponse() throws Exception {
        DispatchResponse stubResponse =
                new DispatchResponse(125L, "TEC-007", "ALTA", 2);

        when(dispatchService.dispatch(any(DispatchRequest.class)))
                .thenReturn(stubResponse);

        String body = """
                {"ticketId":125,"categoria":"RED","prioridad":"ALTA"}
                """;

        mockMvc.perform(post("/technician-dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tecnicoId").value("TEC-007"));

        mockMvc.perform(post("/technician-dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tecnicoId").value("TEC-007"));
    }

    // ── Bean Validation ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Missing ticketId (null) → 400 Bad Request")
    void missingTicketIdReturns400() throws Exception {
        mockMvc.perform(post("/technician-dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoria":"RED","prioridad":"ALTA"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Blank categoria → 400 Bad Request")
    void blankCategoriaReturns400() throws Exception {
        mockMvc.perform(post("/technician-dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ticketId":1,"categoria":"","prioridad":"ALTA"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Blank prioridad → 400 Bad Request")
    void blankPrioridadReturns400() throws Exception {
        mockMvc.perform(post("/technician-dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ticketId":1,"categoria":"RED","prioridad":""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Completely empty body → 400 Bad Request")
    void emptyBodyReturns400() throws Exception {
        mockMvc.perform(post("/technician-dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ── HTTP method ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET on POST-only endpoint → 405 Method Not Allowed")
    void getMethodReturns405() throws Exception {
        mockMvc.perform(post("/technician-dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ticketId":1,"categoria":"RED","prioridad":"ALTA"}
                                """))
                .andExpect(status().isOk()); // confirm the endpoint exists

        // Now try GET
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/technician-dispatch"))
                .andExpect(status().isMethodNotAllowed());
    }
}
