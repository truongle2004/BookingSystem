package com.bookingsystem.platform.health.controller;

import com.bookingsystem.platform.health.service.HealthService;
import com.bookingsystem.platform.web.ApiErrorResponse;
import com.bookingsystem.platform.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Kubernetes-style process liveness and database readiness endpoints. */
@RestController
public class HealthController {

    private final HealthService healthService;

    public HealthController(final HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("/health/live")
    public Map<String, String> live() {
        return Map.of("status", "UP");
    }

    @GetMapping("/health/ready")
    public ResponseEntity<?> ready(final HttpServletRequest request) {
        if (healthService.isDatabaseReady()) {
            return ResponseEntity.ok(Map.of("status", "UP"));
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiErrorResponse.of(
                        "SERVICE_UNAVAILABLE",
                        "Database is not reachable.",
                        RequestIdFilter.currentRequestId(request)));
    }
}
