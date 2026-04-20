package com.farhan.quant.fx_triangulation_engine.pricing.service;

import com.farhan.quant.fx_triangulation_engine.config.ClientConfig;
import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;
import com.farhan.quant.fx_triangulation_engine.domain.Price;
import com.farhan.quant.fx_triangulation_engine.pricing.alpha.AlphaPriceGenerator;
import com.farhan.quant.fx_triangulation_engine.pricing.alpha.SimpleAlphaPriceGenerator;
import com.farhan.quant.fx_triangulation_engine.pricing.spread.SpreadCalculator;
import com.farhan.quant.fx_triangulation_engine.pricing.triangulation.TriangulationEngine;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PricingService {

    private final AlphaPriceGenerator alphaPriceGenerator;
    private final SpreadCalculator spreadCalculator;
    private final TriangulationEngine triangulationEngine;
    private final ClientConfigService clientConfigService;
    private final Map<String, Price> cache = new HashMap<>();

    public PricingService(AlphaPriceGenerator alphaPriceGenerator,
                          SpreadCalculator spreadCalculator,
                          TriangulationEngine triangulationEngine,
                          ClientConfigService clientConfigService) {
        this.alphaPriceGenerator = alphaPriceGenerator;
        this.spreadCalculator = spreadCalculator;
        this.triangulationEngine = triangulationEngine;
        this.clientConfigService = clientConfigService;
    }

    public Price getPrice(String clientId, CurrencyPair currencyPair) {

        String cacheKey = clientId + "|" + currencyPair.toString();

        Price cached = cache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        ClientConfig client = clientConfigService.getClient(clientId);
        double spread = client.getSpread(currencyPair);

        double mid;

        try {
            mid = alphaPriceGenerator.generateMidPrice(currencyPair);
        } catch (Exception e) {

            Map<CurrencyPair, Double> availablePrices = ((SimpleAlphaPriceGenerator) alphaPriceGenerator).getAllPrices();

            mid = triangulationEngine.computeCrossRate(availablePrices, currencyPair);
        }
        Price price = spreadCalculator.applySpread(mid,spread);

        cache.put(cacheKey, price);

        return price;
    }
}
