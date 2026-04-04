package com.cosmicpulse.heliophysics_engine.controller;

import com.cosmicpulse.heliophysics_engine.model.TechHealthScore;
import com.cosmicpulse.heliophysics_engine.service.SolarDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TechHealthController {

    private final SolarDataService solarDataService;

    /**
     * Primary endpoint for the React Native app.
     * Returns the current Tech Health scores from Redis cache.
     * GET /api/v1/tech-health
     */
    @GetMapping("/tech-health")
    public ResponseEntity<?> getTechHealth() {
        return solarDataService.getCurrentTechHealth()
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.ok(Map.of(
                "status", "warming_up",
                "message", "First data poll in progress — check back in 30 seconds"
            )));
    }

    /**
     * Quick status check — useful for the app's home screen header.
     * GET /api/v1/status
     */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        return solarDataService.getCurrentTechHealth()
            .map(score -> ResponseEntity.ok(Map.of(
                "stormCategory",  score.stormCategory().name(),
                "kpIndex",        score.kpIndex(),
                "hasAlert",       score.hasActiveAlert(),
                "alertMessage",   score.alertMessage()
            )))
            .orElseGet(() -> ResponseEntity.ok(Map.of(
                "stormCategory", "UNKNOWN",
                "hasAlert",       false,
                "alertMessage",   "Awaiting first data poll"
            )));
    }
}
