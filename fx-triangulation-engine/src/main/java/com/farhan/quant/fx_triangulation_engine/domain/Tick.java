package com.farhan.quant.fx_triangulation_engine.domain;

import java.time.Instant;

public class Tick {

    private final CurrencyPair currencyPair;
    private final Price price;
    private final Instant timestamp;

    public Tick(CurrencyPair currencyPair, Price price, Instant timestamp) {
        this.currencyPair = currencyPair;
        this.price = price;
        this.timestamp = timestamp;
    }

    public CurrencyPair getCurrencyPair() {
        return currencyPair;
    }
    public Price getPrice() {
        return price;
    }
    public Instant getTimestamp() {
        return timestamp;
    }
}
