package com.farhan.quant.fx_triangulation_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FxTriangulationEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(FxTriangulationEngineApplication.class, args);

	}
}
