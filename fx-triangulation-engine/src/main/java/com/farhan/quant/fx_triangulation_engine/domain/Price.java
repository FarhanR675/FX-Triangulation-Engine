package com.farhan.quant.fx_triangulation_engine.domain;

public class Price {

    private final double mid;
    private final double bid;
    private final double ask;

    public Price(double mid, double bid, double ask) {
        this.mid = mid;
        this.bid = bid;
        this.ask = ask;
    }

    public double getMid() {
        return mid;
    }
    public double getBid() {
        return bid;
    }
    public double getAsk() {
        return ask;
    }

    @Override
    public String toString() {
        return "Price{" +
                "mid=" + mid +
                ", bid=" + bid +
                ", ask=" + ask +
                '}';
    }
}
