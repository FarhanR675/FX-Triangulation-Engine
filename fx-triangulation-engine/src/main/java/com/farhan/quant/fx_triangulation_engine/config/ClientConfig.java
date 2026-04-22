package com.farhan.quant.fx_triangulation_engine.config;

import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;

import java.util.HashMap;
import java.util.Map;

public class ClientConfig {

    private final String clientId;
    private final double defaultSpread;
    private final Map<CurrencyPair, Double> pairSpreads;

    public ClientConfig(String clientId, double defaultSpread) {
        this.clientId = clientId;
        this.defaultSpread = defaultSpread;
        this.pairSpreads = new HashMap<>();
    }

    public double getSpread(CurrencyPair currencyPair) {
        return pairSpreads.getOrDefault(currencyPair, defaultSpread);
    }
    public void addPairSpread(CurrencyPair currencyPair, double spread) {
        pairSpreads.put(currencyPair, spread);
    }
    public String getClientId() {
        return clientId;
    }
}
