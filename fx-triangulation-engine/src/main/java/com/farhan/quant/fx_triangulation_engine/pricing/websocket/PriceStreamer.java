package com.farhan.quant.fx_triangulation_engine.pricing.websocket;

import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;
import com.farhan.quant.fx_triangulation_engine.pricing.service.PricingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PriceStreamer {

    private final PricingService pricingService;
    private final PricePublisher pricePublisher;

    private final List<CurrencyPair> pairs = List.of(
            new CurrencyPair("EUR", "USD"),
            new CurrencyPair("EUR", "JPY"),
            new CurrencyPair("USD", "JPY")
    );

    public PriceStreamer(PricingService pricingService,
                         PricePublisher pricePublisher) {
        this.pricingService = pricingService;
        this.pricePublisher = pricePublisher;
    }

    @Scheduled(fixedRate = 1000)
    public void streamPrices() {

        for (CurrencyPair pair : pairs) {
            var price = pricingService.getPrice("CLIENT_A", pair);

            String pairCode = pair.getBase() + pair.getQuote();

            pricePublisher.publish(pairCode, price);
        }
    }
}
