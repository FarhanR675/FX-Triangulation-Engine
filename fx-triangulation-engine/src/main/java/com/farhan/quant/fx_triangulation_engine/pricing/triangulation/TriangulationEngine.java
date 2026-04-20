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

        for (Map.Entry<String, Double> entry : graph.getOrDefault(current, Collections.emptyMap()).entrySet()) {
            String next = entry.getKey();
            double rate = entry.getValue();

            if (!visited.contains(next)) {
                Double result = dfs(
                        next,
                        target,
                        graph,
                        visited,
                        accumulatedRate * rate
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

    public boolean detectArbitrage(Map<CurrencyPair, Double> prices) {

        Map<String, Map<String, Double>> graph = buildGraph(prices);

        List<Edge> edges = new ArrayList<>();

        for (String from : graph.keySet()) {
            for (Map.Entry<String, Double> entry : graph.get(from).entrySet()) {

                String to = entry.getKey();
                double rate = entry.getValue();

                double weight = -Math.log(rate);

                edges.add(new Edge(from, to, weight));
            }
        }
        Set<String> currencies = graph.keySet();

        Map<String, Double> dist = new HashMap<>();
        for (String c : currencies) {
            dist.put(c, Double.MAX_VALUE);
        }

        String start = currencies.iterator().next();
        dist.put(start, 0.0);

        int n = currencies.size();

        for (int i = 0; i < n - 1; i++) {
            for (Edge edge : edges) {
                if (dist.get(edge.from) + edge.weight < dist.get(edge.to)) {
                    dist.put(edge.to, dist.get(edge.from) + edge.weight);
                }
            }
        }
        for (Edge edge : edges) {
            if (dist.get(edge.from) + edge.weight < dist.get(edge.to)) {
                return true; // Means arbitrage found
            }
        }
        return false;
    }

    private static class Edge {
        String from;
        String to;
        double weight;

        Edge(String from, String to, double weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
    }
}
