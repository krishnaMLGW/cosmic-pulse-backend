package com.cosmicpulse.heliophysics_engine.service;

import com.cosmicpulse.heliophysics_engine.model.CmeEvent;
import com.cosmicpulse.heliophysics_engine.model.KIndexReading;
import com.cosmicpulse.heliophysics_engine.model.TechHealthScore;
import com.cosmicpulse.heliophysics_engine.scoring.TechHealthScoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
public class SolarDataService {

    private static final String REDIS_KEY_TECH_HEALTH = "cosmicpulse:tech-health:current";
    private static final Duration TECH_HEALTH_TTL     = Duration.ofMinutes(5);

    private final WebClient noaaWebClient;
    private final TechHealthScoringService scoringService;
    private final ReactiveRedisTemplate<String, TechHealthScore> redisTemplate;

    @Value("${nasa.api.key}")
    private String nasaApiKey;

    public SolarDataService(
        @Qualifier("noaaWebClient") WebClient noaaWebClient,
        TechHealthScoringService scoringService,
        ReactiveRedisTemplate<String, TechHealthScore> redisTemplate
    ) {
        this.noaaWebClient  = noaaWebClient;
        this.scoringService = scoringService;
        this.redisTemplate  = redisTemplate;
    }

    @Scheduled(fixedDelay = 180_000, initialDelay = 5_000)
    public void pollKIndex() {
        log.info("Polling NOAA K-Index...");
        noaaWebClient.get()
            .uri("/json/planetary_k_index_1m.json")
            .retrieve()
            .bodyToFlux(KIndexReading.class)
            .takeLast(1)
            .next()
            .doOnNext(reading -> log.info("K-Index: {} ({})", reading.kpIndex(), reading.category()))
            .map(scoringService::score)
            .flatMap(score ->
                redisTemplate.opsForValue()
                    .set(REDIS_KEY_TECH_HEALTH, score, TECH_HEALTH_TTL)
                    .thenReturn(score)
            )
            .doOnNext(score -> log.info(
                "Tech Health cached — GPS: {}%, Satellite: {}%, Radio: {}%",
                score.gpsReliabilityScore(),
                score.satelliteInternetScore(),
                score.radioClarityScore()
            ))
            .doOnError(e -> log.error("NOAA poll failed: {}", e.getMessage()))
            .onErrorComplete()
            .subscribe();
    }

    @Scheduled(fixedDelay = 900_000, initialDelay = 15_000)
    public void pollCmeEvents() {
        log.info("Polling NASA DONKI for CME events...");
        String startDate = LocalDate.now().minusDays(7).toString();
        WebClient donkiClient = WebClient.builder()
            .baseUrl("https://api.nasa.gov")
            .defaultHeader("Accept", "application/json")
            .build();
        donkiClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/DONKI/CME")
                .queryParam("startDate", startDate)
                .queryParam("api_key", nasaApiKey)
                .build()
            )
            .retrieve()
            .bodyToFlux(CmeEvent.class)
            .collectList()
            .doOnNext(events -> log.info("DONKI returned {} CME events in last 7 days", events.size()))
            .doOnError(e -> log.error("DONKI poll failed: {}", e.getMessage()))
            .onErrorComplete()
            .subscribe();
    }

    public Optional<TechHealthScore> getCurrentTechHealth() {
        return Optional.ofNullable(
            redisTemplate.opsForValue()
                .get(REDIS_KEY_TECH_HEALTH)
                .block(Duration.ofSeconds(2))
        );
    }
}
