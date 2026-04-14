package com.farhan.quant.fx_triangulation_engine.domain;

import java.util.Objects;

public class CurrencyPair {

    // Base = Currency of buy/sell
    private final String base;

    // Quote = Currency paid in
    private final String quote;

    public CurrencyPair(String base, String quote) {
        this.base = base.toUpperCase();
        this.quote = quote.toUpperCase();
    }

    public String getBase() {
        return base;
    }
    public String getQuote() {
        return quote;
    }

    public CurrencyPair invert() {
        return new CurrencyPair(quote, base);
    }

    @Override
    public String toString() {
        return base + "/" + quote;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CurrencyPair that = (CurrencyPair) o;
            return base.equals(that.base) && quote.equals(that.quote);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, quote);
    }
}
