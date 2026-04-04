package com.cosmicpulse.heliophysics_engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KIndexReading(
    @JsonProperty("time_tag") String timeTag,
    @JsonProperty("kp")       String kpRaw,
    @JsonProperty("source")   String source
) {
    public double kpIndex() {
        if (kpRaw == null) return 0.0;
        try {
            return Double.parseDouble(kpRaw.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public boolean isStormLevel() {
        return kpIndex() >= 5.0;
    }

    public StormCategory category() {
        double kp = kpIndex();
        if (kp >= 8) return StormCategory.EXTREME;
        if (kp >= 7) return StormCategory.SEVERE;
        if (kp >= 6) return StormCategory.STRONG;
        if (kp >= 5) return StormCategory.MODERATE;
        return StormCategory.QUIET;
    }
}
