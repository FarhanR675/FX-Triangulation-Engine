package com.farhan.quant.fx_triangulation_engine.pricing.service;

import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;
import com.farhan.quant.fx_triangulation_engine.domain.Price;
import com.farhan.quant.fx_triangulation_engine.pricing.alpha.AlphaPriceGenerator;
import com.farhan.quant.fx_triangulation_engine.pricing.spread.SpreadCalculator;
import com.farhan.quant.fx_triangulation_engine.pricing.triangulation.TriangulationEngine;

public class PricingService {

    private final AlphaPriceGenerator alphaPriceGenerator;
    private final SpreadCalculator spreadCalculator;
    private final TriangulationEngine triangulationEngine;

    public PricingService(AlphaPriceGenerator alphaPriceGenerator,
                          SpreadCalculator spreadCalculator,
                          TriangulationEngine triangulationEngine) {
        this.alphaPriceGenerator = alphaPriceGenerator;
        this.spreadCalculator = spreadCalculator;
        this.triangulationEngine = triangulationEngine;
    }

    public Price getPrice(CurrencyPair currencyPair, double spread) {

        double mid;

        try {
            mid = alphaPriceGenerator.generateMidPrice(currencyPair);
        } catch (Exception e) {

            // If not available then this will triangulate
            CurrencyPair currencyPair1 = new CurrencyPair("EUR", "USD");
            CurrencyPair currencyPair2 = new CurrencyPair("USD", "JPY");

            double price1 = alphaPriceGenerator.generateMidPrice(currencyPair1);
            double price2 = alphaPriceGenerator.generateMidPrice(currencyPair2);

            mid = triangulationEngine.computeCrossRate(
                    currencyPair1, price1,
                    currencyPair2, price2,
                    currencyPair);
        }
        // Application of spread (bid/ask)
        return spreadCalculator.applySpread(mid, spread);
    }
}
