package com.farhan.quant.fx_triangulation_engine.pricing.alpha;

import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;
import com.farhan.quant.fx_triangulation_engine.exception.PriceNotAvailableException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class SimpleAlphaPriceGenerator implements AlphaPriceGenerator {

    private static final Logger log = LogManager.getLogger(SimpleAlphaPriceGenerator.class);

    private static final String TWELVE_DATA_EXCHANGE_RATE_URL =
            "https://api.twelvedata.com/exchange_rate";
    private static final Duration API_REFRESH_INTERVAL = Duration.ofSeconds(15);

    private static final double DEFAULT_AMPLITUDE = 0.0025;
    private static final double DEFAULT_FREQUENCY = 0.18;
    private static final double DEFAULT_SLOPE = 2.2;

    private final Map<CurrencyPair, Double> fallbackAnchors = new LinkedHashMap<>();
    private final Map<CurrencyPair, WaveShape> waveShapes = new LinkedHashMap<>();
    private final Clock clock;
    private final RestTemplate restTemplate;
    private String apiKey = "";

    private long lastStep = Long.MIN_VALUE;
    private long lastAnchorRefreshMillis = Long.MIN_VALUE;
    private Map<CurrencyPair, Double> liveAnchors = Map.of();
    private Map<CurrencyPair, Double> snapshot = Map.of();

    public SimpleAlphaPriceGenerator() {
        this(Clock.systemUTC(), new RestTemplate(), "");
    }

    SimpleAlphaPriceGenerator(Clock clock,
                              RestTemplate restTemplate,
                              String apiKey) {
        this.clock = clock;
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;

        CurrencyPair eurUsd = new CurrencyPair("EUR", "USD");
        CurrencyPair usdJpy = new CurrencyPair("USD", "JPY");

        fallbackAnchors.put(eurUsd, 1.10);
        fallbackAnchors.put(usdJpy, 150.0);

        waveShapes.put(eurUsd, new WaveShape(0.10, DEFAULT_AMPLITUDE, DEFAULT_FREQUENCY, DEFAULT_SLOPE));
        waveShapes.put(usdJpy, new WaveShape(1.45, DEFAULT_AMPLITUDE * 1.2, DEFAULT_FREQUENCY * 0.8, DEFAULT_SLOPE));

        liveAnchors = Map.copyOf(fallbackAnchors);
    }

    @Value("${twelvedata.api-key:}")
    void setApiKey(String apiKey) {
        this.apiKey = apiKey == null ? "" : apiKey;
    }

    @Override
    public synchronized double generateMidPrice(CurrencyPair currencyPair) {
        Map<CurrencyPair, Double> prices = refreshSnapshot();
        Double currentPrice = prices.get(currencyPair);

        if (currentPrice == null) {
            throw new PriceNotAvailableException("No price available for " + currencyPair);
        }
        return currentPrice;
    }

    @Override
    public synchronized Map<CurrencyPair, Double> getAllPrices() {
        return refreshSnapshot();
    }

    private Map<CurrencyPair, Double> refreshSnapshot() {
        long nowMillis = clock.millis();
        refreshAnchorsIfNeeded(nowMillis);

        long step = nowMillis / 1000L;

        if (step == lastStep) {
            return snapshot;
        }

        Map<CurrencyPair, Double> refreshed = new LinkedHashMap<>();
        for (Map.Entry<CurrencyPair, Double> entry : liveAnchors.entrySet()) {
            CurrencyPair pair = entry.getKey();
            double anchor = entry.getValue();
            WaveShape waveShape = waveShapes.get(pair);
            double offset = smoothOffset(step, waveShape);

            refreshed.put(pair, anchor * (1.0 + offset));
        }

        snapshot = Map.copyOf(refreshed);
        lastStep = step;
        return snapshot;
    }

    private void refreshAnchorsIfNeeded(long nowMillis) {
        boolean shouldRefresh = liveAnchors.isEmpty()
                || lastAnchorRefreshMillis == Long.MIN_VALUE
                || nowMillis - lastAnchorRefreshMillis >= API_REFRESH_INTERVAL.toMillis();

        if (!shouldRefresh) {
            return;
        }

        try {
            Map<CurrencyPair, Double> fetchedAnchors = fetchAnchorsFromTwelveData();
            if (!fetchedAnchors.isEmpty()) {
                liveAnchors = Map.copyOf(fetchedAnchors);
                log.info(
                        "Using Twelve Data live anchors: EUR/USD={}, USD/JPY={}",
                        liveAnchors.get(new CurrencyPair("EUR", "USD")),
                        liveAnchors.get(new CurrencyPair("USD", "JPY"))
                );
            }
        } catch (Exception exception) {
            if (liveAnchors.isEmpty()) {
                liveAnchors = Map.copyOf(fallbackAnchors);
            }
            log.warn(
                    "Using fallback anchors because Twelve Data refresh failed: {}",
                    exception.getMessage()
            );
        }

        lastAnchorRefreshMillis = nowMillis;
    }

    private Map<CurrencyPair, Double> fetchAnchorsFromTwelveData() {
        if (apiKey == null || apiKey.isBlank()) {
            return fallbackAnchors;
        }

        double eurUsd = fetchExchangeRate("EUR/USD");
        double usdJpy = fetchExchangeRate("USD/JPY");

        Map<CurrencyPair, Double> anchors = new LinkedHashMap<>();
        anchors.put(new CurrencyPair("EUR", "USD"), eurUsd);
        anchors.put(new CurrencyPair("USD", "JPY"), usdJpy);

        return anchors;
    }

    private double fetchExchangeRate(String symbol) {
        String url = UriComponentsBuilder
                .fromHttpUrl(TWELVE_DATA_EXCHANGE_RATE_URL)
                .queryParam("symbol", symbol)
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "apikey " + apiKey);

        ResponseEntity<TwelveDataExchangeRateResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                TwelveDataExchangeRateResponse.class
        );

        TwelveDataExchangeRateResponse body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("Empty Twelve Data response for " + symbol);
        }
        if (body.code != null && body.message != null) {
            throw new IllegalStateException("Twelve Data error for " + symbol + ": " + body.message);
        }
        if (body.rate == null || body.rate.isBlank()) {
            throw new IllegalStateException("Missing rate from Twelve Data for " + symbol);
        }

        return Double.parseDouble(body.rate);
    }

    private double smoothOffset(long step, WaveShape waveShape) {
        double angle = (step * waveShape.frequency) + waveShape.phase;
        double carrier = Math.sin(angle) + (0.35 * Math.sin(angle * 0.5 + waveShape.phase));
        return waveShape.amplitude * Math.tanh(waveShape.slope * carrier);
    }

    private record WaveShape(double phase, double amplitude, double frequency, double slope) {
    }

    public static class TwelveDataExchangeRateResponse {
        public String symbol;
        public String rate;
        public String timestamp;
        public Integer code;
        public String message;
        public String status;
    }
}
