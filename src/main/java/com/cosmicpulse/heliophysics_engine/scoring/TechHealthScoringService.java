package com.cosmicpulse.heliophysics_engine.scoring;

import com.cosmicpulse.heliophysics_engine.model.KIndexReading;
import com.cosmicpulse.heliophysics_engine.model.StormCategory;
import com.cosmicpulse.heliophysics_engine.model.TechHealthScore;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Core proprietary algorithm: maps raw Kp-Index readings to
 * consumer-friendly "Tech Health" scores for GPS, satellite internet,
 * and radio communications.
 *
 * Score scale: 100 = perfect operation, 0 = severe disruption.
 */
@Service
public class TechHealthScoringService {

    public TechHealthScore score(KIndexReading reading) {
        double kp = reading.kpIndex();
        StormCategory category = reading.category();

        int gpsScore           = calculateGpsScore(kp);
        int satelliteScore     = calculateSatelliteScore(kp);
        int radioScore         = calculateRadioScore(kp);
        String alertMessage    = buildAlertMessage(category, gpsScore, satelliteScore, radioScore);

        return new TechHealthScore(
            Instant.now(),
            kp,
            category,
            gpsScore,
            satelliteScore,
            radioScore,
            alertMessage
        );
    }

    /**
     * GPS is impacted by ionospheric scintillation.
     * Degradation starts at Kp 4, severe above Kp 7.
     */
    private int calculateGpsScore(double kp) {
        if (kp <= 3) return 100;
        if (kp <= 4) return 90;
        if (kp <= 5) return 75;   // G1 — minor degradation
        if (kp <= 6) return 55;   // G2 — moderate, hikers/pilots notice
        if (kp <= 7) return 30;   // G3 — significant, avoid GPS navigation
        if (kp <= 8) return 12;   // G4 — severe
        return 0;                  // G5 — GPS unreliable
    }

    /**
     * Satellite internet (Starlink) impacted by increased ionospheric drag
     * and atmospheric expansion causing orbit adjustments.
     */
    private int calculateSatelliteScore(double kp) {
        if (kp <= 3) return 100;
        if (kp <= 4) return 95;
        if (kp <= 5) return 80;   // G1 — slight latency spikes
        if (kp <= 6) return 60;   // G2 — noticeable ping increases
        if (kp <= 7) return 35;   // G3 — degraded throughput
        if (kp <= 8) return 15;   // G4 — intermittent outages
        return 5;                  // G5 — major outages likely
    }

    /**
     * HF Radio (ham radio, aviation comms) most severely impacted.
     * Polar routes especially affected above Kp 5.
     */
    private int calculateRadioScore(double kp) {
        if (kp <= 3) return 100;
        if (kp <= 4) return 85;
        if (kp <= 5) return 60;   // G1 — HF degraded at high latitudes
        if (kp <= 6) return 35;   // G2 — widespread HF issues
        if (kp <= 7) return 15;   // G3 — HF blackouts possible
        if (kp <= 8) return 5;    // G4 — major radio blackouts
        return 0;                  // G5 — complete HF blackout
    }

    private String buildAlertMessage(StormCategory category, int gps, int sat, int radio) {
        return switch (category) {
            case QUIET    -> "Solar conditions calm. All systems operating normally.";
            case MODERATE -> "Minor geomagnetic storm (G1). GPS may show slight inaccuracies at high latitudes.";
            case STRONG   -> "Moderate geomagnetic storm (G2). GPS degraded, expect Starlink latency spikes. Aurora possible at high latitudes.";
            case SEVERE   -> "Strong geomagnetic storm (G3). Avoid GPS-critical navigation. HF radio blackouts likely. Aurora visible to mid-latitudes.";
            case EXTREME  -> "SEVERE GEOMAGNETIC STORM (G4-G5). GPS unreliable. Major Starlink disruptions. Complete HF radio blackout. Aurora visible at low latitudes.";
        };
    }
}
