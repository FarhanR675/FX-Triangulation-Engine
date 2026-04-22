package com.farhan.quant.fx_triangulation_engine.pricing.websocket;

import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;
import com.farhan.quant.fx_triangulation_engine.pricing.service.PricingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PriceStreamer {

    private final PricingService pricingService;
    private final PricePublisher pricePublisher;

    public PriceStreamer(PricingService pricingService,
                         PricePublisher pricePublisher) {
        this.pricingService = pricingService;
        this.pricePublisher = pricePublisher;
    }

    @Scheduled(fixedRate = 1000)
    public void streamPrices() {

        var eurUsd = pricingService.getPrice(
                "CLIENT_A",
                new CurrencyPair("EUR", "USD")
        );

        var eurJpy = pricingService.getPrice(
                "CLIENT_A",
                new CurrencyPair("EUR", "JPY")
        );

        pricePublisher.publish("EURUSD", eurUsd);
        pricePublisher.publish("EURJPY", eurJpy);
    }

}
