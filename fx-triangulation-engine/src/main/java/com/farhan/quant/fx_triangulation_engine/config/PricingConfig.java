package com.farhan.quant.fx_triangulation_engine.config;

import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;

import java.util.HashMap;
import java.util.Map;

public class PricingConfig {

    private final Map<CurrencyPair, Double> spreads = new HashMap<>();

    public PricingConfig() {
        spreads.put(new CurrencyPair("EUR", "USD"), 0.001);
        spreads.put(new CurrencyPair("USD", "JPY"), 0.002);
        spreads.put(new CurrencyPair("EUR", "JPY"), 0.003);
    }

    public double getSpread(CurrencyPair currencyPair) {
        return spreads.getOrDefault(currencyPair, 0.005); // default spread
    }
}
