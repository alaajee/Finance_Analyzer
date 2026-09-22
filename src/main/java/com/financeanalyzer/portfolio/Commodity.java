package com.financeanalyzer.portfolio;

public class Commodity extends Asset {
    private final String unit; // unité de cotation (ex. once, baril, tonne)

    public Commodity(String ticker, String name, String currency, String unit) {
        super(ticker, name, currency, AssetType.COMMODITY);
        this.unit = unit;
    }

    public String getUnit() {
        return unit;
    }
}
