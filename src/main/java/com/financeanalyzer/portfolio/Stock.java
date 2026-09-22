package com.financeanalyzer.portfolio;

public class Stock extends Asset {
    private final String sector;

    public Stock(String ticker, String name, String currency, String sector) {
        super(ticker, name, currency, AssetType.STOCK);
        this.sector = sector;
    }

    public String getSector() {
        return sector;
    }
}
