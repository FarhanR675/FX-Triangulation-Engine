package com.farhan.quant.fx_triangulation_engine.pricing.service;

import com.farhan.quant.fx_triangulation_engine.config.ClientConfig;
import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;

import java.util.HashMap;
import java.util.Map;

public class ClientConfigService {

    private final Map<String, ClientConfig>  clients = new HashMap<>();

    public ClientConfigService() {
        ClientConfig clientA = new ClientConfig("CLIENT_A", 0.002);
        clientA.addPairSpread(new CurrencyPair("EUR", "USD"), 0.001);

        ClientConfig clientB = new ClientConfig("CLIENT_B", 0.005);

        clients.put("CLIENT_A", clientA);
        clients.put("CLIENT_B", clientB);
    }
}
