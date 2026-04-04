package com.cosmicpulse.heliophysics_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HeliophysicsEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(HeliophysicsEngineApplication.class, args);
	}

}
