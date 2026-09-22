package com.financeanalyzer.portfolio;

public class Forex extends Asset {
    private final String baseCurrency;

    // La devise de cotation (quote) est passée à Asset comme "currency" : EUR/USD cote en USD.
    public Forex(String ticker, String name, String baseCurrency, String quoteCurrency) {
        super(ticker, name, quoteCurrency, AssetType.FOREX);
        this.baseCurrency = baseCurrency;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public String getQuoteCurrency() {
        return getCurrency();
    }
}
