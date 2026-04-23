package com.farhan.quant.fx_triangulation_engine.pricing.spread;

import com.farhan.quant.fx_triangulation_engine.domain.Price;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class FixedSpreadCalculator implements SpreadCalculator {

    @Override
    public Price applySpread(double mid, double spread, boolean arbitrage) {

        BigDecimal midBd = BigDecimal.valueOf(mid);
        BigDecimal spreadBd = BigDecimal.valueOf(spread);

        BigDecimal halfSpread = spreadBd.divide(BigDecimal.valueOf(2), 6, RoundingMode.HALF_UP);

        BigDecimal bid = midBd.subtract(halfSpread);
        BigDecimal ask = midBd.add(halfSpread);


        return new Price(
                midBd.setScale(6, RoundingMode.HALF_UP),
                bid.setScale(6, RoundingMode.HALF_UP),
                ask.setScale(6, RoundingMode.HALF_UP),
                arbitrage);
    }
}
