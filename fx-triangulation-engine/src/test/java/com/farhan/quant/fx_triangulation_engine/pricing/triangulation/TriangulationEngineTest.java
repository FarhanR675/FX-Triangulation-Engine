package com.farhan.quant.fx_triangulation_engine.pricing.triangulation;

import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;
import org.junit.jupiter.api.Test;

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
}