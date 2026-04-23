package com.farhan.quant.fx_triangulation_engine.pricing.spread;

import com.farhan.quant.fx_triangulation_engine.domain.Price;

public interface SpreadCalculator {

    Price applySpread(double midPrice, double spread, boolean arbitrage);
}