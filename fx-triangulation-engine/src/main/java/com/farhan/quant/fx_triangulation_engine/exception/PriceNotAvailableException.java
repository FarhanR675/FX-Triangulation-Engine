package com.farhan.quant.fx_triangulation_engine.exception;

public class PriceNotAvailableException extends RuntimeException {
    public PriceNotAvailableException(String message) {
        super(message);
    }
}
