package com.farhan.quant.fx_triangulation_engine.pricing.alpha;

import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;
import com.farhan.quant.fx_triangulation_engine.exception.PriceNotAvailableException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
public class SimpleAlphaPriceGenerator implements AlphaPriceGenerator {

    private final Map<CurrencyPair, Double> prices = new HashMap<>();
    private final Random random = new Random();

    public SimpleAlphaPriceGenerator() {
        prices.put(new CurrencyPair("EUR", "USD"), 1.10);
        prices.put(new CurrencyPair("USD", "JPY"), 150.0);
    }

    @Override
    public double generateMidPrice(CurrencyPair currencyPair) {
        Double currentPrice = prices.get(currencyPair);

        if (currentPrice == null) {
            throw new PriceNotAvailableException("No price available for " + currencyPair);
        }
        double change = currentPrice * (random.nextDouble() - 0.5) * 0.001;
        double newPrice = currentPrice + change;

        prices.put(currencyPair, newPrice);

        return newPrice;
    }

    public Map<CurrencyPair, Double> getAllPrices() {
        return prices;
    }
}
