package com.farhan.quant.fx_triangulation_engine.pricing.service;

import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;
import com.farhan.quant.fx_triangulation_engine.domain.Price;
import com.farhan.quant.fx_triangulation_engine.pricing.alpha.AlphaPriceGenerator;
import com.farhan.quant.fx_triangulation_engine.pricing.spread.SpreadCalculator;

public class PricingService {

    private final AlphaPriceGenerator alphaPriceGenerator;
    private final SpreadCalculator spreadCalculator;

    public PricingService(AlphaPriceGenerator alphaPriceGenerator, SpreadCalculator spreadCalculator) {
        this.alphaPriceGenerator = alphaPriceGenerator;
        this.spreadCalculator = spreadCalculator;
    }

    public Price getPrice(CurrencyPair currencyPair, double spread) {
        // Get fair price using mid
        double mid = alphaPriceGenerator.generateMidPrice(currencyPair);

        // Application of spread (bid/ask)
        return spreadCalculator.applySpread(mid, spread);
    }
}
