package com.bookingsystem.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DisplayName("API documentation")
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
                + "org.springframework.modulith.events.jdbc.JdbcEventPublicationAutoConfiguration,"
                + "org.springframework.modulith.events.config.EventPublicationAutoConfiguration,"
                + "org.springframework.modulith.events.config.EventExternalizationAutoConfiguration",
        "management.otlp.metrics.export.enabled=false",
        "management.otlp.tracing.export.enabled=false",
        "app.environment=test",
        "spring.datasource.url=jdbc:postgresql://localhost:5432/bookingsystem",
        "spring.datasource.username=bookingsystem",
        "spring.datasource.password=bookingsystem",
        "app.clerk.issuer-uri=https://example.clerk.accounts.dev",
        "app.clerk.authorized-parties=https://booking.example.com"
})
@AutoConfigureMockMvc
class ApiDocumentationTest {

    private final MockMvc mockMvc;

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    ApiDocumentationTest(@Autowired final MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("serves the Scalar API reference")
    void scalarShouldReturnApiReferenceWhenRequestedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/scalar"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }

    @Test
    @DisplayName("serves the version-controlled OpenAPI document")
    void apiDocsShouldReturnOpenApiDocumentWhenRequestedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/openapi.yaml"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/yaml"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("openapi: 3.1.0")));
    }

    @Test
    @DisplayName("rejects unauthenticated requests to protected endpoints")
    void protectedEndpointShouldReturnUnauthorizedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/v1/protected-resource").header("X-Request-Id", "contract-test_1"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.error.request_id").value("contract-test_1"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("X-Request-Id", "contract-test_1"));
    }

    @Test
    @DisplayName("rejects malformed bearer tokens")
    void protectedEndpointShouldReturnUnauthorizedForMalformedBearerToken() throws Exception {
        mockMvc.perform(get("/v1/protected-resource")
                        .header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));
    }

    @Test
    @DisplayName("serves liveness without database access or authentication")
    void livenessShouldReturnUp() throws Exception {
        mockMvc.perform(get("/health/live"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().exists("X-Request-Id"));
    }

    @Test
    @DisplayName("uses the standard error envelope when readiness fails")
    void readinessShouldReturnStandardErrorWhenDatabaseIsUnavailable() throws Exception {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException("offline"));

        mockMvc.perform(get("/health/ready").header("X-Request-Id", "readiness_1"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error.code").value("SERVICE_UNAVAILABLE"))
                .andExpect(jsonPath("$.error.message").value("Database is not reachable."))
                .andExpect(jsonPath("$.error.request_id").value("readiness_1"));
    }
}
