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
        // Get fair price using mid
        double mid = alphaPriceGenerator.generateMidPrice(currencyPair);

        // Application of spread (bid/ask)
        return spreadCalculator.applySpread(mid, spread);
    }
}
