package com.newscp.backend.health;

import java.time.OffsetDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public HealthPayload health() {
        return new HealthPayload("UP", "NEWSCP Backend is ready", OffsetDateTime.now().toString());
    }

    public record HealthPayload(String status, String service, String timestamp) {
    }
}
