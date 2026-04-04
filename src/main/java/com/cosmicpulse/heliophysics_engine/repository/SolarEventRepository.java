package com.cosmicpulse.heliophysics_engine.repository;

import com.cosmicpulse.heliophysics_engine.model.SolarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SolarEventRepository extends JpaRepository<SolarEvent, Long> {

    List<SolarEvent> findByTimeAfterOrderByTimeAsc(Instant after);

    @Query("SELECT s FROM SolarEvent s WHERE s.time >= :after ORDER BY s.time ASC")
    List<SolarEvent> findLast24Hours(Instant after);

    @Query("SELECT s FROM SolarEvent s ORDER BY s.time DESC LIMIT 1")
    SolarEvent findLatest();
}
