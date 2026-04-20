package com.farhan.quant.fx_triangulation_engine.pricing.service;

import com.farhan.quant.fx_triangulation_engine.config.PricingConfig;
import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;
import com.farhan.quant.fx_triangulation_engine.domain.Price;
import com.farhan.quant.fx_triangulation_engine.pricing.alpha.AlphaPriceGenerator;
import com.farhan.quant.fx_triangulation_engine.pricing.alpha.SimpleAlphaPriceGenerator;
import com.farhan.quant.fx_triangulation_engine.pricing.spread.FixedSpreadCalculator;
import com.farhan.quant.fx_triangulation_engine.pricing.spread.SpreadCalculator;
import com.farhan.quant.fx_triangulation_engine.pricing.triangulation.TriangulationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

class PricingServiceTest {

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        AlphaPriceGenerator alphaPriceGenerator = new SimpleAlphaPriceGenerator();
        SpreadCalculator spreadCalculator = new FixedSpreadCalculator();
        TriangulationEngine triangulationEngine = new TriangulationEngine();
        ClientConfigService clientConfigService = new ClientConfigService();
        pricingService = new PricingService(alphaPriceGenerator, spreadCalculator, triangulationEngine, clientConfigService);
    }

    @Test
    void shouldReturnDirectPrice_EurUsd() {
        Price price = pricingService.getPrice("CLIENT_A", new CurrencyPair("EUR", "USD"));

        assertEquals(0, new BigDecimal("1.1000").compareTo(price.getMid()));
        assertEquals(0, new BigDecimal("1.0995").compareTo(price.getBid()));
        assertEquals(0, new BigDecimal("1.1005").compareTo(price.getAsk()));


    }

    @Test
    void shouldTriangulate_EurJpy() {
        Price price = pricingService.getPrice("CLIENT_A", new CurrencyPair("EUR", "JPY"));

        assertEquals(0, new BigDecimal("165.0").compareTo(price.getMid()));
        assertEquals(0, new BigDecimal("164.9985").compareTo(price.getBid()));
        assertEquals(0, new BigDecimal("165.0015").compareTo(price.getAsk()));
    }

    @Test
    void shouldHandleInverse_JpyEur() {
        Price price = pricingService.getPrice("CLIENT_A", new CurrencyPair("JPY", "EUR"));

        BigDecimal expectedMid = BigDecimal.ONE.divide(new BigDecimal("165.0"), 6, RoundingMode.HALF_UP);
        assertEquals(0, expectedMid.compareTo(price.getMid()));
    }

    @Test
    void shouldThrow_WhenNoPathExists() {
        assertThrows(RuntimeException.class, () -> pricingService.getPrice("CLIENT_A", new CurrencyPair("GBP", "USD")));
    }

    @Test
    void shouldGeneratePriceForEurJpyUsingTriangulation() {
        // Arrange & Act
        Price price = pricingService.getPrice("CLIENT_A", new CurrencyPair("EUR", "JPY"));

        //Assert
        assertNotNull(price);

        assertEquals(0, new BigDecimal("165.0").compareTo(price.getMid()));
        assertEquals(0, new BigDecimal("164.9985").compareTo(price.getBid()));
        assertEquals(0, new BigDecimal("165.0015").compareTo(price.getAsk()));
    }
}