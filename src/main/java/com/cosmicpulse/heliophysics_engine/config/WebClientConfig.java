package com.cosmicpulse.heliophysics_engine.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${noaa.api.base-url}")
    private String noaaBaseUrl;

    @Value("${nasa.api.base-url}")
    private String nasaBaseUrl;

    @Bean("noaaWebClient")
    public WebClient noaaWebClient(WebClient.Builder builder) {
        return builder
            .baseUrl(noaaBaseUrl)
            .defaultHeader("Accept", "application/json")
            .build();
    }

    @Bean("nasaWebClient")
    public WebClient nasaWebClient(WebClient.Builder builder) {
        return builder
            .baseUrl(nasaBaseUrl)
            .defaultHeader("Accept", "application/json")
            .build();
    }
}
