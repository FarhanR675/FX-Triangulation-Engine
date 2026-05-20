package com.farhan.quant.fx_triangulation_engine.pricing.service;

import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;
import com.farhan.quant.fx_triangulation_engine.domain.Price;
import com.farhan.quant.fx_triangulation_engine.pricing.alpha.AlphaPriceGenerator;
import com.farhan.quant.fx_triangulation_engine.pricing.spread.FixedSpreadCalculator;
import com.farhan.quant.fx_triangulation_engine.pricing.spread.SpreadCalculator;
import com.farhan.quant.fx_triangulation_engine.pricing.triangulation.TriangulationEngine;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PricingServiceTest {

    @Test
    void shouldReturnDirectPrice_EurUsd() {
        PricingService pricingService = createPricingService(Map.of(
                new CurrencyPair("EUR", "USD"), 1.1000,
                new CurrencyPair("USD", "JPY"), 150.0000
        ));

        Price price = pricingService.getPrice("CLIENT_A", new CurrencyPair("EUR", "USD"));

        assertEquals(0, new BigDecimal("1.100000").compareTo(price.getMid()));
        assertEquals(0, new BigDecimal("1.099500").compareTo(price.getBid()));
        assertEquals(0, new BigDecimal("1.100500").compareTo(price.getAsk()));
        assertFalse(price.isArbitrage());
    }

    @Test
    void shouldTriangulate_EurJpy_FromExecutableLegs() {
        PricingService pricingService = createPricingService(Map.of(
                new CurrencyPair("EUR", "USD"), 1.1000,
                new CurrencyPair("USD", "JPY"), 150.0000
        ));

        Price price = pricingService.getPrice("CLIENT_A", new CurrencyPair("EUR", "JPY"));

        assertEquals(0, new BigDecimal("165.000001").compareTo(price.getMid()));
        assertEquals(0, new BigDecimal("164.923901").compareTo(price.getBid()));
        assertEquals(0, new BigDecimal("165.076101").compareTo(price.getAsk()));
        assertFalse(price.isArbitrage());
    }

    @Test
    void shouldHandleInverse_JpyEur() {
        PricingService pricingService = createPricingService(Map.of(
                new CurrencyPair("EUR", "USD"), 1.1000,
                new CurrencyPair("USD", "JPY"), 150.0000
        ));

        Price price = pricingService.getPrice("CLIENT_A", new CurrencyPair("JPY", "EUR"));

        assertEquals(0, new BigDecimal("0.006061").compareTo(price.getMid()));
        assertEquals(0, new BigDecimal("0.006058").compareTo(price.getBid()));
        assertEquals(0, new BigDecimal("0.006063").compareTo(price.getAsk()));
    }

    @Test
    void shouldClampDirectQuoteIntoNoArbBand() {
        PricingService pricingService = createPricingService(Map.of(
                new CurrencyPair("EUR", "USD"), 1.1000,
                new CurrencyPair("USD", "JPY"), 150.0000,
                new CurrencyPair("EUR", "JPY"), 166.0000
        ));

        Price price = pricingService.getPrice("CLIENT_A", new CurrencyPair("EUR", "JPY"));

        assertEquals(0, new BigDecimal("165.538801").compareTo(price.getMid()));
        assertEquals(0, new BigDecimal("165.076101").compareTo(price.getBid()));
        assertEquals(0, new BigDecimal("166.001500").compareTo(price.getAsk()));
        assertFalse(price.isArbitrage());
    }

    @Test
    void shouldThrow_WhenNoPathExists() {
        PricingService pricingService = createPricingService(Map.of(
                new CurrencyPair("EUR", "USD"), 1.1000
        ));

        assertThrows(RuntimeException.class, () -> pricingService.getPrice("CLIENT_A", new CurrencyPair("GBP", "USD")));
    }

    private PricingService createPricingService(Map<CurrencyPair, Double> prices) {
        AlphaPriceGenerator alphaPriceGenerator = new StubAlphaPriceGenerator(prices);
        SpreadCalculator spreadCalculator = new FixedSpreadCalculator();
        TriangulationEngine triangulationEngine = new TriangulationEngine();
        ClientConfigService clientConfigService = new ClientConfigService();
        return new PricingService(alphaPriceGenerator, spreadCalculator, triangulationEngine, clientConfigService);
    }

    private static class StubAlphaPriceGenerator implements AlphaPriceGenerator {
        private final Map<CurrencyPair, Double> prices;

        private StubAlphaPriceGenerator(Map<CurrencyPair, Double> prices) {
            this.prices = Map.copyOf(prices);
        }

        @Override
        public double generateMidPrice(CurrencyPair currencyPair) {
            Double price = prices.get(currencyPair);
            if (price == null) {
                throw new RuntimeException("No price available for " + currencyPair);
            }
            return price;
        }

        @Override
        public Map<CurrencyPair, Double> getAllPrices() {
            return prices;
        }
    }
}
