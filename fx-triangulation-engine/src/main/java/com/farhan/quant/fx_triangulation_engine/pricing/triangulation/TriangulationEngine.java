package com.farhan.quant.fx_triangulation_engine.pricing.triangulation;

import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TriangulationEngine {

    private Map<String, Map<String, Double>> buildGraph(Map<CurrencyPair, Double> prices) {
        Map<String, Map<String, Double>> graph = new HashMap<>();

        for (Map.Entry<CurrencyPair, Double> entry : prices.entrySet()) {
            String base = entry.getKey().getBase();
            String quote = entry.getKey().getQuote();
            double rate = entry.getValue();

            // Forward edge
            graph.computeIfAbsent(base, k -> new HashMap<>()).put(quote, rate);

            // Reverse edge
            graph.computeIfAbsent(quote, k -> new HashMap<>()).put(base, 1 / rate);
        }
        return graph;
    }



    public double computeCrossRate(CurrencyPair currencyPair1, double price1,
                                   CurrencyPair currencyPair2, double price2,
                                   CurrencyPair target
    ) { // normalising currencyPair1 so ut flows into currencyPair2
        if (!currencyPair1.getQuote().equals(currencyPair2.getBase())) {
            // inverting currencyPair1
            if (currencyPair1.getBase().equals(currencyPair2.getBase())) {
                currencyPair1 = currencyPair1.invert();
                price1 = 1 / price1;
            } else if (currencyPair1.getQuote().equals(currencyPair2.getQuote())) {
                currencyPair2 = currencyPair2.invert(); // 5000
                price2 = 1 / price2;
            } else {
                throw new RuntimeException("Pairs cannot be aligned");
            }
        }

        String base = currencyPair1.getBase();
        String quote = currencyPair2.getQuote();

        CurrencyPair derived = new CurrencyPair(base, quote);

        double result = price1 * price2;

        if (!derived.equals(target)) {
            if (derived.invert().equals(target)) {
                return 1 / result;
            } else {
                throw new RuntimeException("Target mismatch");
            }
        }
        return result;
    }
}
