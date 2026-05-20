package com.farhan.quant.fx_triangulation_engine.pricing.triangulation;

import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;
import com.farhan.quant.fx_triangulation_engine.domain.Price;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
public class TriangulationEngine {

    private static final double EPSILON = 1e-9;

    private Map<String, Map<String, Double>> buildMidGraph(Map<CurrencyPair, Double> prices) {
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

    public double computeCrossRate(Map<CurrencyPair, Double> prices,
                                   CurrencyPair target
    ) {
        Double result = bestConversionRate(
                buildMidGraph(prices),
                target.getBase(),
                target.getQuote()
        );

        if (result == null) {
            throw new RuntimeException("No path found for: " + target);
        }
        return result;
    }

    public Price computeExecutablePrice(Map<CurrencyPair, Price> prices,
                                        CurrencyPair target) {
        return computeExecutablePrice(prices, target, Set.of());
    }

    public Price computeExecutablePrice(Map<CurrencyPair, Price> prices,
                                        CurrencyPair target,
                                        Set<CurrencyPair> excludedPairs) {
        Map<String, Map<String, Double>> graph = buildExecutableGraph(prices, excludedPairs);
        Double bid = bestConversionRate(graph, target.getBase(), target.getQuote());
        Double reverse = bestConversionRate(graph, target.getQuote(), target.getBase());

        if (bid == null || reverse == null) {
            throw new RuntimeException("No path found for: " + target);
        }

        double ask = 1.0 / reverse;
        double safeBid = Math.min(bid, ask);
        double mid = (safeBid + ask) / 2.0;

        return new Price(
                round(mid),
                round(safeBid),
                round(ask),
                false
        );
    }

    public boolean detectArbitrage(Map<CurrencyPair, Double> prices) {
        return detectArbitrageInGraph(buildMidGraph(prices));
    }

    public boolean detectArbitrageOnExecutablePrices(Map<CurrencyPair, Price> prices) {
        return detectArbitrageInGraph(buildExecutableGraph(prices, Set.of()));
    }

    private boolean detectArbitrageInGraph(Map<String, Map<String, Double>> graph) {
        List<Edge> edges = new ArrayList<>();
        for (Map.Entry<String, Map<String, Double>> fromEntry : graph.entrySet()) {
            for (Map.Entry<String, Double> edge : fromEntry.getValue().entrySet()) {
                edges.add(new Edge(fromEntry.getKey(), edge.getKey(), -Math.log(edge.getValue())));
            }
        }
        Set<String> currencies = graph.keySet();

        if (currencies.isEmpty()) {
            return false;
        }

        Map<String, Double> dist = new HashMap<>();
        for (String c : currencies) {
            dist.put(c, Double.MAX_VALUE);
        }

        String start = currencies.iterator().next();
        dist.put(start, 0.0);

        int n = currencies.size();

        for (int i = 0; i < n - 1; i++) {
            for (Edge edge : edges) {
                if (dist.get(edge.from) != Double.MAX_VALUE && dist.get(edge.from) + edge.weight < dist.get(edge.to) - EPSILON) {
                    dist.put(edge.to, dist.get(edge.from) + edge.weight);
                }
            }
        }
        for (Edge edge : edges) {
            if (dist.get(edge.from) != Double.MAX_VALUE && dist.get(edge.from) + edge.weight < dist.get(edge.to) - EPSILON) {
                return true; // Means arbitrage found
            }
        }
        return false;
    }

    private Map<String, Map<String, Double>> buildExecutableGraph(Map<CurrencyPair, Price> prices,
                                                                  Set<CurrencyPair> excludedPairs) {
        Map<String, Map<String, Double>> graph = new HashMap<>();

        for (Map.Entry<CurrencyPair, Price> entry : prices.entrySet()) {
            CurrencyPair pair = entry.getKey();
            if (excludedPairs.contains(pair) || excludedPairs.contains(pair.invert())) {
                continue;
            }

            Price price = entry.getValue();
            graph.computeIfAbsent(pair.getBase(), ignored -> new HashMap<>())
                    .put(pair.getQuote(), price.getBid().doubleValue());
            graph.computeIfAbsent(pair.getQuote(), ignored -> new HashMap<>())
                    .put(pair.getBase(), 1.0 / price.getAsk().doubleValue());
        }

        return graph;
    }

    private Double bestConversionRate(Map<String, Map<String, Double>> graph,
                                      String source,
                                      String target) {
        if (source.equals(target)) {
            return 1.0;
        }

        Set<String> currencies = new HashSet<>(graph.keySet());
        graph.values().forEach(edges -> currencies.addAll(edges.keySet()));

        if (!currencies.contains(source) || !currencies.contains(target)) {
            return null;
        }

        Map<String, Double> bestDistance = new HashMap<>();
        for (String currency : currencies) {
            bestDistance.put(currency, Double.POSITIVE_INFINITY);
        }
        bestDistance.put(source, 0.0);

        for (int i = 0; i < currencies.size() - 1; i++) {
            boolean updated = false;
            for (Map.Entry<String, Map<String, Double>> fromEntry : graph.entrySet()) {
                String from = fromEntry.getKey();
                double fromDistance = bestDistance.getOrDefault(from, Double.POSITIVE_INFINITY);
                if (Double.isInfinite(fromDistance)) {
                    continue;
                }

                for (Map.Entry<String, Double> edge : fromEntry.getValue().entrySet()) {
                    if (edge.getValue() <= 0.0) {
                        continue;
                    }

                    double candidate = fromDistance - Math.log(edge.getValue());
                    if (candidate < bestDistance.get(edge.getKey()) - EPSILON) {
                        bestDistance.put(edge.getKey(), candidate);
                        updated = true;
                    }
                }
            }

            if (!updated) {
                break;
            }
        }

        double distance = bestDistance.getOrDefault(target, Double.POSITIVE_INFINITY);
        if (Double.isInfinite(distance)) {
            return null;
        }

        return Math.exp(-distance);
    }

    private BigDecimal round(double value) {
        return BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP);
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
