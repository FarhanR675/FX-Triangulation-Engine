package com.farhan.quant.fx_triangulation_engine;

import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;
import com.farhan.quant.fx_triangulation_engine.pricing.alpha.AlphaPriceGenerator;
import com.farhan.quant.fx_triangulation_engine.pricing.alpha.SimpleAlphaPriceGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FxTriangulationEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(FxTriangulationEngineApplication.class, args);

        AlphaPriceGenerator generator = new SimpleAlphaPriceGenerator();

        double price = generator.generateMidPrice(new CurrencyPair("EUR", "USD"));

        System.out.println(price);
	}

}
