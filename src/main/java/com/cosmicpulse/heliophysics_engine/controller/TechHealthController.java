package com.cosmicpulse.heliophysics_engine.controller;

import com.cosmicpulse.heliophysics_engine.model.SolarEvent;
import com.cosmicpulse.heliophysics_engine.model.TechHealthScore;
import com.cosmicpulse.heliophysics_engine.service.SolarDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TechHealthController {

    private final SolarDataService solarDataService;

    @GetMapping("/tech-health")
    public ResponseEntity<?> getTechHealth() {
        return solarDataService.getCurrentTechHealth()
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.ok(Map.of(
                "status", "warming_up",
                "message", "First data poll in progress — check back in 30 seconds"
            )));
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        return solarDataService.getCurrentTechHealth()
            .map(score -> ResponseEntity.ok(Map.of(
                "stormCategory", score.stormCategory().name(),
                "kpIndex",       score.kpIndex(),
                "hasAlert",      score.hasActiveAlert(),
                "alertMessage",  score.alertMessage()
            )))
            .orElseGet(() -> ResponseEntity.ok(Map.of(
                "stormCategory", "UNKNOWN",
                "hasAlert",      false,
                "alertMessage",  "Awaiting first data poll"
            )));
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory() {
        List<SolarEvent> events = solarDataService.getLast24Hours();

        if (events.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<Map<String, Object>> result = events.stream().map(e -> {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("time",  e.getTime().toString());
            point.put("kp",    e.getKpIndex());
            point.put("label", e.getTime().toString().substring(11, 16));
            point.put("category", e.getStormCategory());
            return point;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/cme-events")
    public ResponseEntity<?> getCmeEvents() {
        return ResponseEntity.ok(solarDataService.getRecentCmeEvents());
    }
}
