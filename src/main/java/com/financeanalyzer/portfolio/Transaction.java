package com.financeanalyzer.portfolio;

import java.time.LocalDate;

public class Transaction {
    private final Asset asset;
    private final TransactionType type;
    private final double quantity;
    private final double price;
    private final LocalDate date;

    public Transaction(Asset asset, TransactionType type, double quantity, double price, LocalDate date) {
        this.asset = asset;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.date = date;
    }

    public double getTotalAmount() {
        return quantity * price;
    }

    public Asset getAsset() {
        return asset;
    }

    public TransactionType getType() {
        return type;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public String toString() {
        return date + " | " + type + " " + quantity + " " + asset.getTicker() + " @ " + price;
    }
}