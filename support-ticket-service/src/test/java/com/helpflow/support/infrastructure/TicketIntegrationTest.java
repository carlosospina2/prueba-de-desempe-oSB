package com.helpflow.support.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full integration test: Spring Boot + PostgreSQL + Flyway + Spring Security.
 *
 * Uses Testcontainers to start an isolated, ephemeral PostgreSQL 16 container
 * for each test class run. The container is destroyed after all tests in the
 * class complete. Each test run starts with a clean schema (Flyway re-applies
 * all 4 migrations on the fresh container).
 *
 * PREREQUISITE — Docker Desktop 29.2.1+ requires docker-java to negotiate
 * API version 1.44 instead of the default 1.30. Create the file:
 *
 *   %USERPROFILE%\.docker-java.properties   (Windows)
 *   ~/.docker-java.properties               (Linux/macOS)
 *
 * with the single line:
 *   api.version=1.44
 *
 * Without that file, Testcontainers receives HTTP 400 Bad Request from the
 * daemon and throws:
 *   IllegalStateException: Could not find a valid Docker environment
 *
 * To run from command line:
 *   mvn test -Dtest=TicketIntegrationTest
 *   (or: mvn clean verify)
 *
 * Scenarios verified:
 *  1. POST /auth/register → 200, JWT token in response
 *  2. GET /api/tickets/my with ROLE_USUARIO JWT → 200  (verifies 403 fix)
 *  3. GET /api/tickets    with ROLE_USUARIO JWT → 403  (admin-only enforced)
 *  4. GET /api/tickets/my without token       → 401
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class TicketIntegrationTest {

    /**
     * Shared, class-scoped PostgreSQL 16 container.
     * @Container + static = started once before all tests, stopped after the last one.
     * Flyway runs all migrations on the clean, empty database.
     */
    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("helpflow_test")
                    .withUsername("helpflow")
                    .withPassword("helpflow_test");

    /**
     * Inject the ephemeral container's JDBC URL into Spring's datasource.
     * Flyway is enabled so migrations run automatically on the clean DB.
     */
    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled",      () -> "true");
        registry.add("app.jwt.secret",
                () -> "integration-test-secret-at-least-32-chars-long");
        registry.add("app.jwt.expiration-minutes", () -> "120");
        registry.add("helpflow.dispatch.url",      () -> "http://localhost:8081");
    }

    @Autowired MockMvc     mockMvc;
    @Autowired ObjectMapper objectMapper;

    // ── Tests ────────────────────────────────────────────────────────────────

    @Test
    void shouldRegisterLoginAndAccessOwnTickets() throws Exception {
        String unique = "IT" + System.currentTimeMillis();
        String registerBody = """
                {"document":"%s","name":"Integration User","email":"%s@helpflow.com",
                 "password":"Test-Integration-2026!","sede":"Bogota","area":"TI",
                 "role":"ROLE_USUARIO"}
                """.formatted(unique, unique);

        // 1. Register → HTTP 200, token in body
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        String token = body.get("token").asText();

        // 2. GET /api/tickets/my with valid ROLE_USUARIO JWT → 200 (403 fix)
        mockMvc.perform(get("/api/tickets/my")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 3. GET /api/tickets (admin-only) with ROLE_USUARIO JWT → 403
        mockMvc.perform(get("/api/tickets")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn401ForAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/tickets/my"))
                .andExpect(status().isUnauthorized());
    }
}
