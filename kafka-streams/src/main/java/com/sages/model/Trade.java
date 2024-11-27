package com.sages.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize
public class Trade {

    String instrument;
    double quantity;
    double price;

    public Trade() {
    }

    public Trade(String instrument, double quantity, double price) {
        this.instrument = instrument;
        this.quantity = quantity;
        this.price = price;
    }

    public String setInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String toString() {
        return "Trade{" +
                "instrument='" + instrument + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }

}
