package com.farhan.quant.fx_triangulation_engine.pricing.alpha;

import com.farhan.quant.fx_triangulation_engine.domain.CurrencyPair;

public interface AlphaPriceGenerator {

    double generateMidPrice(CurrencyPair currencyPair);
}
