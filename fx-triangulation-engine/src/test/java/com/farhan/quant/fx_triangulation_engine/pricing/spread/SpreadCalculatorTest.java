package com.farhan.quant.fx_triangulation_engine.pricing.spread;

import com.farhan.quant.fx_triangulation_engine.domain.Price;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpreadCalculatorTest {

    @Test
    void shouldApplySpreadCorrectly() {
        //Arrange (Set Up)
        SpreadCalculator spreadCalculator = new FixedSpreadCalculator();
        double mid = 1.1000;
        double spread = 0.0002;

        //Act
        Price price = spreadCalculator.applySpread(mid, spread);

        //Assert
        assertEquals(1.1000, price.getMid(), 1e-9);
        assertEquals(1.0999, price.getBid(), 1e-9);
        assertEquals(1.1001, price.getAsk(), 1e-9);

        System.out.println("Mid: " + price.getMid());
        System.out.println("Bid: " + price.getBid());
        System.out.println("Ask: " + price.getAsk());
    }
}