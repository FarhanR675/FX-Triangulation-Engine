package com.farhan.quant.fx_triangulation_engine.pricing.alpha;

import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;
import com.farhan.quant.fx_triangulation_engine.exception.PriceNotAvailableException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SimpleAlphaPriceGenerator implements AlphaPriceGenerator {

    private final Map<CurrencyPair, Double> prices = new HashMap<>();

    public SimpleAlphaPriceGenerator() {
        prices.put(new CurrencyPair("EUR", "USD"), 1.10);
        prices.put(new CurrencyPair("USD", "JPY"), 150.0);
    }

    @Override
    public double generateMidPrice(CurrencyPair currencyPair) {
        Double price = prices.get(currencyPair);

        if (price == null) {
            throw new PriceNotAvailableException("No price available for " + currencyPair);
        }
        return price;
    }

    public Map<CurrencyPair, Double> getAllPrices() {
        return prices;
    }
}
