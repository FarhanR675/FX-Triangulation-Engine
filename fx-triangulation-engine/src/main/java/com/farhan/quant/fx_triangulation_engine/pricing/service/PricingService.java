package com.farhan.quant.fx_triangulation_engine.pricing.service;

import com.farhan.quant.fx_triangulation_engine.config.ClientConfig;
import com.farhan.quant.fx_triangulation_engine.config.PricingConfig;
import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;
import com.farhan.quant.fx_triangulation_engine.domain.Price;
import com.farhan.quant.fx_triangulation_engine.pricing.alpha.AlphaPriceGenerator;
import com.farhan.quant.fx_triangulation_engine.pricing.alpha.SimpleAlphaPriceGenerator;
import com.farhan.quant.fx_triangulation_engine.pricing.spread.SpreadCalculator;
import com.farhan.quant.fx_triangulation_engine.pricing.triangulation.TriangulationEngine;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PricingService {

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

        double mid;

        try {
            mid = alphaPriceGenerator.generateMidPrice(currencyPair);
        } catch (Exception e) {

            Map<CurrencyPair, Double> availablePrices = ((SimpleAlphaPriceGenerator) alphaPriceGenerator).getAllPrices();

            boolean found = false;
            double result = 0;

            for (Map.Entry<CurrencyPair, Double> entry1 : availablePrices.entrySet()) {
                for (Map.Entry<CurrencyPair, Double> entry2 : availablePrices.entrySet()) {

                    if (entry1 == entry2) continue;

                    try {
                        result = triangulationEngine.computeCrossRate(
                                entry1.getKey(), entry1.getValue(),
                                entry2.getKey(), entry2.getValue(),
                                currencyPair);
                        found = true;
                        break;
                    } catch (Exception ignored) {}
                }

                if (found) break;
            }
            if (!found) {
                throw new RuntimeException("Cannot price " + currencyPair);
            }
            mid = result;
        }
        // Application of spread (bid/ask)
        return spreadCalculator.applySpread(mid, spread);
    }
}
