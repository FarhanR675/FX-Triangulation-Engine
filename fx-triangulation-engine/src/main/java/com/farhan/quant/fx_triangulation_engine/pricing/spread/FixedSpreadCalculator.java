package com.farhan.quant.fx_triangulation_engine.pricing.spread;

import com.farhan.quant.fx_triangulation_engine.domain.Price;

public class FixedSpreadCalculator implements SpreadCalculator {

    public Price applySpread(double mid, double spread) {

        double bid = mid - spread / 2;
        double ask = mid + spread / 2;

        return new Price(mid, bid, ask);
    }
}
