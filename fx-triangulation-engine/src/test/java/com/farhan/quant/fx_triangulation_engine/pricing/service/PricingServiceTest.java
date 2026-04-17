package com.farhan.quant.fx_triangulation_engine.pricing.service;

import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;
import com.farhan.quant.fx_triangulation_engine.domain.Price;
import com.farhan.quant.fx_triangulation_engine.pricing.alpha.AlphaPriceGenerator;
import com.farhan.quant.fx_triangulation_engine.pricing.alpha.SimpleAlphaPriceGenerator;
import com.farhan.quant.fx_triangulation_engine.pricing.spread.FixedSpreadCalculator;
import com.farhan.quant.fx_triangulation_engine.pricing.spread.SpreadCalculator;
import com.farhan.quant.fx_triangulation_engine.pricing.triangulation.TriangulationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PricingServiceTest {

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        AlphaPriceGenerator alphaPriceGenerator = new SimpleAlphaPriceGenerator();
        SpreadCalculator spreadCalculator = new FixedSpreadCalculator();
        TriangulationEngine triangulationEngine = new TriangulationEngine();

        pricingService = new PricingService(alphaPriceGenerator, spreadCalculator, triangulationEngine);
    }

    @Test
    void shouldReturnDirectPrice_EurUsd() {
        Price price = pricingService.getPrice(new CurrencyPair("EUR", "USD"), 0.002);

        assertEquals(1.10, price.getMid(), 0.0001);
        assertEquals(1.099, price.getBid(), 0.001);
        assertEquals(1.101, price.getAsk(), 0.001);
    }

    @Test
    void shouldTriangulate_EurJpy() {
        Price price = pricingService.getPrice(new CurrencyPair("EUR", "JPY"), 0.002);

        assertEquals(165.0, price.getMid(), 0.0001);
        assertEquals(164.999, price.getBid(), 0.001);
        assertEquals(165.001, price.getAsk(), 0.001);
    }

    @Test
    void shouldHandleInverse_JpyEur() {
        Price price = pricingService.getPrice(new CurrencyPair("JPY", "EUR"), 0.002);

        double expectedMid = 1.0 / 165.0;

        assertEquals(expectedMid, price.getMid(), 0.0001);
    }

    @Test
    void shouldThrow_WhenNoPathExists() {
        assertThrows(RuntimeException.class, () -> pricingService.getPrice(new CurrencyPair("GBP", "USD"), 0.02));
    }

    @Test
    void shouldGeneratePriceForEurJpyUsingTriangulation() {
        // Arrange & Act
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