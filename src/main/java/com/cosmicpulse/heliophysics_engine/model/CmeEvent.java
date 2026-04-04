package com.cosmicpulse.heliophysics_engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CmeEvent(
    @JsonProperty("activityID")  String activityId,
    @JsonProperty("startTime")   String startTime,
    @JsonProperty("note")        String note,
    @JsonProperty("instruments") List<Instrument> instruments,
    @JsonProperty("cmeAnalyses") List<CmeAnalysis> analyses
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Instrument(
        @JsonProperty("displayName") String name
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CmeAnalysis(
        @JsonProperty("speed")          Double speed,
        @JsonProperty("type")           String type,
        @JsonProperty("isMostAccurate") boolean mostAccurate
    ) {}

    public Double estimatedSpeed() {
        if (analyses == null) return null;
        return analyses.stream()
            .filter(CmeAnalysis::mostAccurate)
            .map(CmeAnalysis::speed)
            .findFirst()
            .orElse(null);
    }
}
