package com.farhan.quant.fx_triangulation_engine.config;

import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ClientConfig {

    private final String clientId;
    private final double defaultSpread;
    private final Map<CurrencyPair, Double> painSpreads;

    public ClientConfig(String clientId, double defaultSpread) {
        this.clientId = clientId;
        this.defaultSpread = defaultSpread;
        this.painSpreads = new HashMap<>();
    }
}
