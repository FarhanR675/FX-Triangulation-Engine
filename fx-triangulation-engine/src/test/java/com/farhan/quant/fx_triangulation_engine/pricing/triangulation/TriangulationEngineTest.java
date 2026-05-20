package com.farhan.quant.fx_triangulation_engine.pricing.triangulation;

import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;
import com.farhan.quant.fx_triangulation_engine.domain.Price;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TriangulationEngineTest {

    @Test
    void shouldCompute_EurJpy_From_EurUsd_And_UsdJpy() {
        // Arrange
        TriangulationEngine triangulationEngine = new TriangulationEngine();

        CurrencyPair eurUsd = new CurrencyPair("EUR", "USD");
        CurrencyPair usdJpy = new CurrencyPair("USD", "JPY");
        CurrencyPair eurJpy = new CurrencyPair("EUR", "JPY");

        // Act
        Map<CurrencyPair, Double> prices = new HashMap<>();
        prices.put(eurUsd, 1.10);
        prices.put(usdJpy, 150.0);
        double result = triangulationEngine.computeCrossRate(prices, eurJpy);

        // Assert
        assertEquals(165.0, result, 0.0001);
    }

    @Test
    void shouldComputeExecutableCrossBidAndAsk() {
        TriangulationEngine triangulationEngine = new TriangulationEngine();

        Map<CurrencyPair, Price> prices = new HashMap<>();
        prices.put(new CurrencyPair("EUR", "USD"), new Price(
                new BigDecimal("1.100000"),
                new BigDecimal("1.099500"),
                new BigDecimal("1.100500"),
                false
        ));
        prices.put(new CurrencyPair("USD", "JPY"), new Price(
                new BigDecimal("150.000000"),
                new BigDecimal("149.999000"),
                new BigDecimal("150.001000"),
                false
        ));

        Price result = triangulationEngine.computeExecutablePrice(prices, new CurrencyPair("EUR", "JPY"));

        assertEquals(0, new BigDecimal("164.923901").compareTo(result.getBid()));
        assertEquals(0, new BigDecimal("165.076101").compareTo(result.getAsk()));
        assertFalse(result.isArbitrage());
    }
}
