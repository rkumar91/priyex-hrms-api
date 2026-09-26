package com.priyex.hrms.common.controller;

import com.priyex.hrms.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Health", description = "Application health and readiness checks")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> data = Map.of(
                "status", "UP",
                "application", "Priyex HRMS API",
                "version", "1.0.0",
                "timestamp", Instant.now()
        );
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/ready")
    @Operation(summary = "Readiness check")
    public ResponseEntity<ApiResponse<Map<String, String>>> ready() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("status", "READY")));
    }
}
