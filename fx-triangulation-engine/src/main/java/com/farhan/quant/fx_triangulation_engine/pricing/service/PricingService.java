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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
        Map<CurrencyPair, Double> marketMidPrices = alphaPriceGenerator.getAllPrices();
        Map<CurrencyPair, Price> directClientPrices = buildDirectClientPrices(client, marketMidPrices);
        Map<CurrencyPair, Price> normalizedDirectPrices = normalizeDirectPrices(directClientPrices);

        boolean isArbitrage = triangulationEngine.detectArbitrageOnExecutablePrices(normalizedDirectPrices);

        if (isArbitrage) {
            log.warn("Arbitrage opportunity detected for {} for client {}", currencyPair, clientId);
        }

        Price directPrice = normalizedDirectPrices.get(currencyPair);
        if (directPrice != null) {
            return withArbitrageFlag(directPrice, isArbitrage);
        }

        Price inversePrice = normalizedDirectPrices.get(currencyPair.invert());
        if (inversePrice != null) {
            return withArbitrageFlag(invert(inversePrice), isArbitrage);
        }

        Price syntheticPrice = triangulationEngine.computeExecutablePrice(normalizedDirectPrices, currencyPair);
        return withArbitrageFlag(syntheticPrice, isArbitrage);
    }

    private Map<CurrencyPair, Price> buildDirectClientPrices(ClientConfig client,
                                                             Map<CurrencyPair, Double> marketMidPrices) {
        Map<CurrencyPair, Price> directPrices = new HashMap<>();

        for (Map.Entry<CurrencyPair, Double> entry : marketMidPrices.entrySet()) {
            CurrencyPair pair = entry.getKey();
            double spread = client.getSpread(pair);
            directPrices.put(pair, spreadCalculator.applySpread(entry.getValue(), spread, false));
        }

        return directPrices;
    }

    private Map<CurrencyPair, Price> normalizeDirectPrices(Map<CurrencyPair, Price> directPrices) {
        Map<CurrencyPair, Price> normalized = new HashMap<>(directPrices);

        for (Map.Entry<CurrencyPair, Price> entry : directPrices.entrySet()) {
            CurrencyPair pair = entry.getKey();
            Price direct = entry.getValue();

            try {
                Price synthetic = triangulationEngine.computeExecutablePrice(directPrices, pair, Set.of(pair));
                normalized.put(pair, clampToNoArbBand(direct, synthetic));
            } catch (RuntimeException ignored) {
                normalized.put(pair, direct);
            }
        }

        return normalized;
    }

    private Price clampToNoArbBand(Price direct,
                                   Price synthetic) {
        BigDecimal bid = direct.getBid().min(synthetic.getAsk());
        BigDecimal ask = direct.getAsk().max(synthetic.getBid());
        BigDecimal mid = bid.add(ask)
                .divide(BigDecimal.valueOf(2), 6, RoundingMode.HALF_UP);

        return new Price(mid, bid, ask, false);
    }

    private Price invert(Price price) {
        BigDecimal ask = BigDecimal.ONE.divide(price.getBid(), 6, RoundingMode.HALF_UP);
        BigDecimal bid = BigDecimal.ONE.divide(price.getAsk(), 6, RoundingMode.HALF_UP);
        BigDecimal mid = bid.add(ask)
                .divide(BigDecimal.valueOf(2), 6, RoundingMode.HALF_UP);

        return new Price(mid, bid, ask, price.isArbitrage());
    }

    private Price withArbitrageFlag(Price price,
                                    boolean arbitrage) {
        return new Price(price.getMid(), price.getBid(), price.getAsk(), arbitrage);
    }
}
