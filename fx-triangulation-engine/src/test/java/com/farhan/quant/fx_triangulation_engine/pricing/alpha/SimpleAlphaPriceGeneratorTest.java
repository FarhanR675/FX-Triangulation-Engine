package com.farhan.quant.fx_triangulation_engine.pricing.alpha;

import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SimpleAlphaPriceGeneratorTest {

    @Test
    void shouldStayStableWithinSameSecondAndMoveSmoothlyAcrossSteps() {
        RestTemplate restTemplate = mock(RestTemplate.class);

        SimpleAlphaPriceGenerator generatorAtStepOne = new SimpleAlphaPriceGenerator(
                Clock.fixed(Instant.parse("2026-04-23T12:00:01Z"), ZoneOffset.UTC),
                restTemplate,
                ""
        );
        SimpleAlphaPriceGenerator generatorAtStepTwo = new SimpleAlphaPriceGenerator(
                Clock.fixed(Instant.parse("2026-04-23T12:00:02Z"), ZoneOffset.UTC),
                restTemplate,
                ""
        );

        CurrencyPair eurUsd = new CurrencyPair("EUR", "USD");

        double first = generatorAtStepOne.generateMidPrice(eurUsd);
        double repeated = generatorAtStepOne.generateMidPrice(eurUsd);
        double second = generatorAtStepTwo.generateMidPrice(eurUsd);

        assertEquals(first, repeated, 0.0);
        assertNotEquals(first, second);
        assertTrue(Math.abs(first - 1.10) < 0.01);
        assertTrue(Math.abs(second - first) < 0.01);
    }

    @Test
    void shouldUseFetchedTwelveDataRatesWhenApiKeyIsPresent() {
        RestTemplate restTemplate = mock(RestTemplate.class);

        SimpleAlphaPriceGenerator.TwelveDataExchangeRateResponse eurUsd = new SimpleAlphaPriceGenerator.TwelveDataExchangeRateResponse();
        eurUsd.rate = "1.1250";

        SimpleAlphaPriceGenerator.TwelveDataExchangeRateResponse usdJpy = new SimpleAlphaPriceGenerator.TwelveDataExchangeRateResponse();
        usdJpy.rate = "151.2500";

        when(restTemplate.exchange(eq("https://api.twelvedata.com/exchange_rate?symbol=EUR/USD"), eq(HttpMethod.GET), any(), eq(SimpleAlphaPriceGenerator.TwelveDataExchangeRateResponse.class)))
                .thenReturn(ResponseEntity.ok(eurUsd));
        when(restTemplate.exchange(eq("https://api.twelvedata.com/exchange_rate?symbol=USD/JPY"), eq(HttpMethod.GET), any(), eq(SimpleAlphaPriceGenerator.TwelveDataExchangeRateResponse.class)))
                .thenReturn(ResponseEntity.ok(usdJpy));

        SimpleAlphaPriceGenerator generator = new SimpleAlphaPriceGenerator(
                Clock.fixed(Instant.parse("2026-05-20T12:00:01Z"), ZoneOffset.UTC),
                restTemplate,
                "test-key"
        );

        double price = generator.generateMidPrice(new CurrencyPair("EUR", "USD"));

        assertTrue(Math.abs(price - 1.1250) < 0.01);
    }
}
