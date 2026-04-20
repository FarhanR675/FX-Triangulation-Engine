package com.farhan.quant.fx_triangulation_engine.pricing.triangulation;

import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;
import org.springframework.stereotype.Component;

import java.util.*;

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

    private Double dfs(
            String current,
            String target,
            Map<String, Map<String, Double>> graph,
            Set<String> visited,
            double accumulatedRate
    ) {
        if (current.equals(target)) {
            return accumulatedRate;
        }
        visited.add(current);

        Map<String, Double> neighbours = graph.getOrDefault(current, Collections.emptyMap());

        for (Map.Entry<String, Double> entry : neighbours.entrySet()) {
            String next = entry.getKey();
            double rate = entry.getValue();

            if (!visited.contains(next)) {
                Double result = dfs(
                        next,
                        target,
                        graph,
                        visited,
                        accumulatedRate = rate
                );
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public double computeCrossRate(Map<CurrencyPair, Double> prices,
                                   CurrencyPair target
    ) {
        Map<String, Map<String, Double>> graph = buildGraph(prices);

        Double result = dfs(
                target.getBase(),
                target.getQuote(),
                graph,
                new HashSet<>(),
                1.0
        );

        if (result == null) {
            throw new RuntimeException("No path found for: " + target);
        }
        return result;
    }
}
