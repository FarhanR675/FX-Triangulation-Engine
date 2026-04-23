package com.farhan.quant.fx_triangulation_engine.pricing.service;

import com.farhan.quant.fx_triangulation_engine.config.ClientConfig;
import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;
import com.farhan.quant.fx_triangulation_engine.domain.Price;
import com.farhan.quant.fx_triangulation_engine.pricing.alpha.AlphaPriceGenerator;
import com.farhan.quant.fx_triangulation_engine.pricing.spread.SpreadCalculator;
import com.farhan.quant.fx_triangulation_engine.pricing.triangulation.TriangulationEngine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PricingService {

    private static final Logger log = LogManager.getLogger(PricingService.class);
    private final AlphaPriceGenerator alphaPriceGenerator;
    private final SpreadCalculator spreadCalculator;
    private final TriangulationEngine triangulationEngine;
    private final ClientConfigService clientConfigService;

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

        ClientConfig client = clientConfigService.getClient(clientId);
        double spread = client.getSpread(currencyPair);

        Map<CurrencyPair, Double> availablePrices = alphaPriceGenerator.getAllPrices();
        boolean isArbitrage = triangulationEngine.detectArbitrage(availablePrices);

        if (isArbitrage) {
            log.warn("Arbitrage opportunity detected for {} for client {}", currencyPair, clientId);
        }

        double mid;

        try {
            mid = alphaPriceGenerator.generateMidPrice(currencyPair);
        } catch (Exception e) {
            mid = triangulationEngine.computeCrossRate(availablePrices, currencyPair);
        }
        return spreadCalculator.applySpread(mid, spread, isArbitrage);
    }
}
