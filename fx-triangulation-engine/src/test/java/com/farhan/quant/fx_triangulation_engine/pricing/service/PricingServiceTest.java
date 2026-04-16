package com.farhan.quant.fx_triangulation_engine.pricing.service;

import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;
import com.farhan.quant.fx_triangulation_engine.domain.Price;
import com.farhan.quant.fx_triangulation_engine.pricing.alpha.AlphaPriceGenerator;
import com.farhan.quant.fx_triangulation_engine.pricing.alpha.SimpleAlphaPriceGenerator;
import com.farhan.quant.fx_triangulation_engine.pricing.spread.FixedSpreadCalculator;
import com.farhan.quant.fx_triangulation_engine.pricing.spread.SpreadCalculator;
import com.farhan.quant.fx_triangulation_engine.pricing.triangulation.TriangulationEngine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PricingServiceTest {

    @Test
    void shouldGeneratePriceForEurJpyUsingTriangulation() {
        // Arrange
        AlphaPriceGenerator alphaPriceGenerator = new SimpleAlphaPriceGenerator();
        SpreadCalculator spreadCalculator = new FixedSpreadCalculator();
        TriangulationEngine triangulationEngine = new TriangulationEngine();

        PricingService pricingService = new PricingService(alphaPriceGenerator, spreadCalculator, triangulationEngine);

        //Act
        //Price price = pricingService.getPrice(new CurrencyPair("EUR", "JPY"), 0.002);
        Price price = pricingService.getPrice(new CurrencyPair("EUR", "JPY"), 0.002);

        //Assert
        assertNotNull(price);

        /* Expected Results:
        *  EUR/USD = 1.10
        *  USD/JPY = 150
        *  EUR/JPY = 165  */
        assertEquals(165.0, price.getMid(), 0.0001);

        // Spread = 0.002 = Half = 0.001
        assertEquals(164.999, price.getBid(), 0.0001);
        assertEquals(165.001, price.getAsk(), 0.0001);
    }
}