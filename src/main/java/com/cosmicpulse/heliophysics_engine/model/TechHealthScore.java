package com.cosmicpulse.heliophysics_engine.model;

import java.time.Instant;

public record TechHealthScore(
    Instant timestamp,
    double kpIndex,
    StormCategory stormCategory,

    // Scores: 100 = perfect, 0 = severely impacted
    int gpsReliabilityScore,
    int satelliteInternetScore,
    int radioClarityScore,

    String alertMessage
) {
    public boolean hasActiveAlert() {
        return stormCategory != StormCategory.QUIET;
    }
}
