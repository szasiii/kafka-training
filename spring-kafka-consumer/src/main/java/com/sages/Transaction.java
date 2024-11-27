package com.sages;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.math.BigDecimal;
import java.util.UUID;

@JsonDeserialize
public class Transaction {

    UUID id;
    String source;
    BigDecimal amount;

    public Transaction() {
    }

    public Transaction(UUID id, String source, BigDecimal amount) {
        this.id = id;
        this.source = source;
        this.amount = amount;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", source='" + source + '\'' +
                ", amount=" + amount +
                '}';
    }
}