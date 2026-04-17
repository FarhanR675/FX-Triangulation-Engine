package com.farhan.quant.fx_triangulation_engine.controller;

import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;
import com.farhan.quant.fx_triangulation_engine.domain.Price;
import com.farhan.quant.fx_triangulation_engine.pricing.service.PricingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }
    @GetMapping("/price")
    public Price getPrice(@RequestParam String base,
                          @RequestParam String quote) {
        CurrencyPair currencyPair = new CurrencyPair(base, quote);
        return pricingService.getPrice(currencyPair);
    }
}
