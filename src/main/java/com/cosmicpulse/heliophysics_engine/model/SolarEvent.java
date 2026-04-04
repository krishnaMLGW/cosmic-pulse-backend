package com.cosmicpulse.heliophysics_engine.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "solar_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SolarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "time", nullable = false)
    private Instant time;

    @Column(name = "kp_index", nullable = false)
    private double kpIndex;

    @Column(name = "storm_category", nullable = false)
    private String stormCategory;

    @Column(name = "gps_score", nullable = false)
    private int gpsScore;

    @Column(name = "satellite_score", nullable = false)
    private int satelliteScore;

    @Column(name = "radio_score", nullable = false)
    private int radioScore;

    @Column(name = "source")
    private String source;
}
