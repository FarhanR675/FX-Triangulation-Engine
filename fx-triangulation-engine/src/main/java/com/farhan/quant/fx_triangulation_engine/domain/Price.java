package com.farhan.quant.fx_triangulation_engine.domain;

import java.math.BigDecimal;

public class Price {

    private final BigDecimal mid;
    private final BigDecimal bid;
    private final BigDecimal ask;
    private final boolean arbitrage;

    public Price(BigDecimal mid, BigDecimal bid, BigDecimal ask, boolean arbitrage) {
        this.mid = mid;
        this.bid = bid;
        this.ask = ask;
        this.arbitrage = arbitrage;
    }

    public BigDecimal getMid() {
        return mid;
    }
    public BigDecimal getBid() {
        return bid;
    }
    public BigDecimal getAsk() {
        return ask;
    }
    public boolean isArbitrage() {
        return arbitrage;
    }

    @Override
    public String toString() {
        return "Price{" +
                "mid=" + mid +
                ", bid=" + bid +
                ", ask=" + ask +
                ", arbitrage=" + arbitrage +
                '}';
    }
}
