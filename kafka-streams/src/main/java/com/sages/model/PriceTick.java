package com.sages.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigDecimal;
import java.util.UUID;

@JsonSerialize
public class PriceTick {

    UUID id;
    String instrument;
    BigDecimal price;

    public PriceTick() {
    }

    public PriceTick(UUID id, String instrument, BigDecimal amount) {
        this.id = id;
        this.instrument = instrument;
        this.price = amount;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
