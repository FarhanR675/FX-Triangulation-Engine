package com.farhan.quant.fx_triangulation_engine.pricing.service;

import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;
import com.farhan.quant.fx_triangulation_engine.domain.Price;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class PriceStreamingService {

    private final PricingService pricingService;
    private final SimpMessagingTemplate messagingTemplate;

    public PriceStreamingService(PricingService pricingService,
                                 SimpMessagingTemplate messagingTemplate) {
        this.pricingService = pricingService;
        this.messagingTemplate = messagingTemplate;
    }

    @Scheduled(fixedRate = 2000) // streams price every 2 seconds
    public void streamPrices() {

        Price eurUsd = pricingService.getPrice("CLIENT_A", new CurrencyPair("EUR", "USD"));
        Price eurJpy = pricingService.getPrice("CLIENT_A", new CurrencyPair("EUR", "JPY"));

        messagingTemplate.convertAndSend("/topic/prices/EURUSD", eurUsd);
        messagingTemplate.convertAndSend("/topic/prices/EURJPY", eurJpy);
    }
}
