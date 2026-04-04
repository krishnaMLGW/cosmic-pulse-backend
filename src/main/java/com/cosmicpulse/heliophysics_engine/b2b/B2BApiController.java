package com.cosmicpulse.heliophysics_engine.b2b;

import com.cosmicpulse.heliophysics_engine.model.SolarEvent;
import com.cosmicpulse.heliophysics_engine.model.TechHealthScore;
import com.cosmicpulse.heliophysics_engine.service.SolarDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/b2b/v1")
@RequiredArgsConstructor
public class B2BApiController {

    private final SolarDataService solarDataService;

    /**
     * B2B Endpoint 1: Current space weather impact scores
     * Target customers: drone fleet operators, aviation companies, telecoms
     * GET /api/b2b/v1/impact
     */
    @GetMapping("/impact")
    public ResponseEntity<?> getImpactScores(
        @RequestHeader(value = "X-Client-Id", required = false) String clientId
    ) {
        log.info("B2B /impact called by client: {}", clientId);

        return solarDataService.getCurrentTechHealth()
            .map(score -> {
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("timestamp",      score.timestamp());
                response.put("kp_index",        score.kpIndex());
                response.put("storm_level",     score.stormCategory().name());
                response.put("alert_active",    score.hasActiveAlert());
                response.put("impact_scores", Map.of(
                    "gps_navigation",    Map.of("score", score.gpsReliabilityScore(),    "unit", "percent", "description", "GPS accuracy reliability"),
                    "satellite_internet",Map.of("score", score.satelliteInternetScore(), "unit", "percent", "description", "Satellite internet quality"),
                    "hf_radio",          Map.of("score", score.radioClarityScore(),      "unit", "percent", "description", "HF radio propagation quality")
                ));
                response.put("recommendation", score.alertMessage());
                response.put("next_update",    Instant.now().plusSeconds(180));
                response.put("api_version",    "1.0");
                response.put("provider",       "Cosmic Pulse Space Weather API");
                return ResponseEntity.ok(response);
            })
            .orElseGet(() -> ResponseEntity.ok(Map.of(
                "status", "warming_up",
                "message", "Data available within 30 seconds"
            )));
    }

    /**
     * B2B Endpoint 2: GPS-specific reliability for navigation companies
     * GET /api/b2b/v1/gps-reliability
     */
    @GetMapping("/gps-reliability")
    public ResponseEntity<?> getGpsReliability(
        @RequestParam(required = false) Double latitude,
        @RequestParam(required = false) Double longitude
    ) {
        return solarDataService.getCurrentTechHealth()
            .map(score -> {
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("timestamp",         score.timestamp());
                response.put("reliability_score", score.gpsReliabilityScore());
                response.put("kp_index",          score.kpIndex());
                response.put("storm_category",    score.stormCategory().name());
                response.put("safe_for_navigation", score.gpsReliabilityScore() >= 70);
                response.put("accuracy_degradation_percent", 100 - score.gpsReliabilityScore());

                if (latitude != null && longitude != null) {
                    response.put("location", Map.of("lat", latitude, "lon", longitude));
                    // Higher latitudes are more affected
                    double latFactor = Math.abs(latitude) > 60 ? 1.3 : Math.abs(latitude) > 45 ? 1.1 : 1.0;
                    int adjustedScore = (int) Math.max(0, score.gpsReliabilityScore() / latFactor);
                    response.put("location_adjusted_score", adjustedScore);
                }

                response.put("alert", score.alertMessage());
                return ResponseEntity.ok(response);
            })
            .orElse(ResponseEntity.ok(Map.of("status", "warming_up")));
    }

    /**
     * B2B Endpoint 3: Historical data for analytics
     * GET /api/b2b/v1/history?hours=24
     */
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(
        @RequestParam(defaultValue = "24") int hours
    ) {
        if (hours > 168) hours = 168; // Max 7 days

        List<SolarEvent> events = solarDataService.getLast24Hours();

        List<Map<String, Object>> data = events.stream().map(e -> {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("timestamp",       e.getTime());
            point.put("kp_index",        e.getKpIndex());
            point.put("storm_category",  e.getStormCategory());
            point.put("gps_score",       e.getGpsScore());
            point.put("satellite_score", e.getSatelliteScore());
            point.put("radio_score",     e.getRadioScore());
            return point;
        }).collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("period_hours",  hours);
        response.put("data_points",   data.size());
        response.put("resolution",    "3 minutes");
        response.put("data",          data);

        double avgKp  = data.stream().mapToDouble(d -> (double) d.get("kp_index")).average().orElse(0);
        double maxKp  = data.stream().mapToDouble(d -> (double) d.get("kp_index")).max().orElse(0);
        long stormPts = data.stream().filter(d -> (double) d.get("kp_index") >= 5).count();

        response.put("summary", Map.of(
            "avg_kp",        Math.round(avgKp * 100.0) / 100.0,
            "max_kp",        maxKp,
            "storm_periods", stormPts,
            "storm_hours",   Math.round(stormPts * 3.0 / 60.0 * 10) / 10.0
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * B2B Endpoint 4: Drone flight safety assessment
     * GET /api/b2b/v1/drone-safety
     */
    @GetMapping("/drone-safety")
    public ResponseEntity<?> getDroneSafety() {
        return solarDataService.getCurrentTechHealth()
            .map(score -> {
                int gps = score.gpsReliabilityScore();
                String safetyLevel = gps >= 90 ? "SAFE" :
                                     gps >= 70 ? "CAUTION" :
                                     gps >= 50 ? "WARNING" : "UNSAFE";
                String recommendation =
                    gps >= 90 ? "Normal flight operations. GPS fully reliable." :
                    gps >= 70 ? "Exercise caution. Verify GPS fix before flight." :
                    gps >= 50 ? "Delay non-critical flights. Manual override recommended." :
                                "Ground all GPS-dependent drones. Use manual control only.";

                return ResponseEntity.ok(Map.of(
                    "timestamp",       score.timestamp(),
                    "safety_level",    safetyLevel,
                    "gps_score",       gps,
                    "kp_index",        score.kpIndex(),
                    "recommendation",  recommendation,
                    "return_to_home_reliable", gps >= 70,
                    "autonomous_flight_safe",  gps >= 85
                ));
            })
            .orElse(ResponseEntity.ok(Map.of("status", "warming_up")));
    }

    /**
     * API documentation endpoint
     * GET /api/b2b/v1/docs
     */
    @GetMapping("/docs")
    public ResponseEntity<?> getDocs() {
        return ResponseEntity.ok(Map.of(
            "name",    "Cosmic Pulse Space Weather B2B API",
            "version", "1.0",
            "base_url","https://api.cosmicpulse.io/api/b2b/v1",
            "auth",    "Pass your API key in X-API-Key header",
            "endpoints", List.of(
                Map.of("GET /impact",         "Current impact scores for all systems"),
                Map.of("GET /gps-reliability","GPS reliability with optional lat/lon adjustment"),
                Map.of("GET /drone-safety",   "Drone flight safety assessment"),
                Map.of("GET /history",        "Historical data, ?hours=24 (max 168)")
            ),
            "plans", Map.of(
                "starter",    "$49/mo — 1,000 calls/day",
                "pro",        "$199/mo — 10,000 calls/day",
                "enterprise", "Custom pricing — unlimited calls + SLA"
            ),
            "contact", "api@cosmicpulse.io"
        ));
    }
}
