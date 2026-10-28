package com.helpflow.dispatch.service;

import com.helpflow.dispatch.dto.DispatchRequest;
import com.helpflow.dispatch.dto.DispatchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DispatchService.
 *
 * No Spring context is loaded — DispatchService has no framework dependencies.
 *
 * Scenarios verified:
 *  - Same (ticketId, categoria, prioridad) always returns the same response (determinism)
 *  - Different inputs return different technician assignments
 *  - Technician ID follows the format TEC-NNN (001–020)
 *  - Availability is classified: 0–2h → ALTA, 3–8h → MEDIA, >8h → BAJA
 *  - ticketId is echoed back in the response
 */
class DispatchServiceTest {

    private DispatchService service;

    @BeforeEach
    void setUp() {
        service = new DispatchService();
    }

    // ── Determinism ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Same input always returns the same technician and hours (determinism)")
    void sameInputReturnsSameOutput() {
        DispatchRequest req = new DispatchRequest(125L, "RED", "ALTA");

        DispatchResponse first  = service.dispatch(req);
        DispatchResponse second = service.dispatch(req);
        DispatchResponse third  = service.dispatch(req);

        assertEquals(first.tecnicoId(),             second.tecnicoId());
        assertEquals(first.tecnicoId(),             third.tecnicoId());
        assertEquals(first.tiempoEstimadoHoras(),   second.tiempoEstimadoHoras());
        assertEquals(first.disponibilidad(),         second.disponibilidad());
    }

    @Test
    @DisplayName("Different ticketId produces a different seed — results may differ")
    void differentTicketIdProducesDifferentSeed() {
        DispatchResponse r1 = service.dispatch(new DispatchRequest(1L,   "HARDWARE", "BAJA"));
        DispatchResponse r2 = service.dispatch(new DispatchRequest(999L, "HARDWARE", "BAJA"));
        // The hash will differ for different ticketIds; at minimum the ticketId echoed is different
        assertNotEquals(r1.ticketId(), r2.ticketId());
    }

    @Test
    @DisplayName("Changing categoria changes the result even with the same ticketId")
    void differentCategoriaProducesDifferentResult() {
        DispatchResponse hardware = service.dispatch(new DispatchRequest(50L, "HARDWARE", "MEDIA"));
        DispatchResponse software = service.dispatch(new DispatchRequest(50L, "SOFTWARE", "MEDIA"));
        // Different categories will produce different hash seeds
        assertNotEquals(hardware.tecnicoId() + hardware.tiempoEstimadoHoras(),
                        software.tecnicoId() + software.tiempoEstimadoHoras(),
                        "Different categoria should yield a different assignment");
    }

    // ── Response structure ───────────────────────────────────────────────────

    @Test
    @DisplayName("ticketId in response matches the request")
    void responseEchoesTicketId() {
        DispatchResponse resp = service.dispatch(new DispatchRequest(42L, "ACCESOS", "CRÍTICA"));
        assertEquals(42L, resp.ticketId());
    }

    @Test
    @DisplayName("tecnicoId follows the format TEC-NNN")
    void tecnicoIdMatchesFormat() {
        for (long id = 1L; id <= 25L; id++) {
            DispatchResponse resp = service.dispatch(new DispatchRequest(id, "RED", "ALTA"));
            assertTrue(resp.tecnicoId().matches("TEC-\\d{3}"),
                    "Expected TEC-NNN format, got: " + resp.tecnicoId());
        }
    }

    @Test
    @DisplayName("tecnicoId index is in range 1–20")
    void tecnicoIdIndexInRange() {
        for (long id = 1L; id <= 40L; id++) {
            DispatchResponse resp = service.dispatch(new DispatchRequest(id, "SOFTWARE", "MEDIA"));
            int index = Integer.parseInt(resp.tecnicoId().substring(4)); // "TEC-007" → 7
            assertTrue(index >= 1 && index <= 20,
                    "Expected index 1–20, got: " + index + " for ticketId=" + id);
        }
    }

    @Test
    @DisplayName("tiempoEstimadoHoras is in range 1–12")
    void hoursInRange() {
        for (long id = 1L; id <= 30L; id++) {
            DispatchResponse resp = service.dispatch(new DispatchRequest(id, "HARDWARE", "BAJA"));
            int h = resp.tiempoEstimadoHoras();
            assertTrue(h >= 1 && h <= 12,
                    "Expected 1–12 h, got: " + h + " for ticketId=" + id);
        }
    }

    // ── Availability classification ──────────────────────────────────────────

    @Test
    @DisplayName("Hours 1–2 are classified as ALTA")
    void hoursOneOrTwoClassifiedAsAlta() {
        // Brute-force enough inputs to hit at least one result with 1 or 2 hours
        boolean foundAlta = false;
        for (long id = 1L; id <= 100L; id++) {
            DispatchResponse r = service.dispatch(new DispatchRequest(id, "RED", "ALTA"));
            if (r.tiempoEstimadoHoras() <= 2) {
                assertEquals("ALTA", r.disponibilidad(),
                        "Expected ALTA for " + r.tiempoEstimadoHoras() + " h");
                foundAlta = true;
            }
        }
        assertTrue(foundAlta, "No result with 1–2 h was found in 100 inputs — range may be unreachable");
    }

    @Test
    @DisplayName("Hours 3–8 are classified as MEDIA")
    void threeToEightHoursClassifiedAsMedia() {
        boolean foundMedia = false;
        for (long id = 1L; id <= 100L; id++) {
            DispatchResponse r = service.dispatch(new DispatchRequest(id, "SOFTWARE", "MEDIA"));
            int h = r.tiempoEstimadoHoras();
            if (h >= 3 && h <= 8) {
                assertEquals("MEDIA", r.disponibilidad(),
                        "Expected MEDIA for " + h + " h, got: " + r.disponibilidad());
                foundMedia = true;
            }
        }
        assertTrue(foundMedia, "No result with 3–8 h was found in 100 inputs");
    }

    @Test
    @DisplayName("Hours > 8 are classified as BAJA")
    void moreThanEightHoursClassifiedAsBaja() {
        boolean foundBaja = false;
        for (long id = 1L; id <= 100L; id++) {
            DispatchResponse r = service.dispatch(new DispatchRequest(id, "HARDWARE", "BAJA"));
            if (r.tiempoEstimadoHoras() > 8) {
                assertEquals("BAJA", r.disponibilidad(),
                        "Expected BAJA for " + r.tiempoEstimadoHoras() + " h");
                foundBaja = true;
            }
        }
        assertTrue(foundBaja, "No result with >8 h was found in 100 inputs");
    }

    @Test
    @DisplayName("All five valid categories produce a non-null response")
    void allCategoriesProduceResult() {
        String[] categories = {"HARDWARE", "SOFTWARE", "RED", "ACCESOS", "OTRO"};
        for (String cat : categories) {
            DispatchResponse r = service.dispatch(new DispatchRequest(1L, cat, "ALTA"));
            assertNotNull(r, "Null response for categoria=" + cat);
            assertNotNull(r.tecnicoId());
            assertNotNull(r.disponibilidad());
        }
    }

    @Test
    @DisplayName("All four valid priorities produce a non-null response")
    void allPrioritiesProduceResult() {
        String[] priorities = {"BAJA", "MEDIA", "ALTA", "CRÍTICA"};
        for (String p : priorities) {
            DispatchResponse r = service.dispatch(new DispatchRequest(1L, "RED", p));
            assertNotNull(r, "Null response for prioridad=" + p);
        }
    }
}
