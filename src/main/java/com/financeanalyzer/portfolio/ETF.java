package com.financeanalyzer.portfolio;

public class ETF extends Asset {
    private final String underlyingIndex;
    private final double expenseRatio; // frais annuels en fraction (0.0007 = 0,07 %)

    public ETF(String ticker, String name, String currency, String underlyingIndex, double expenseRatio) {
        super(ticker, name, currency, AssetType.ETF);
        if (expenseRatio < 0) {
            throw new IllegalArgumentException("Les frais de gestion ne peuvent pas être négatifs");
        }
        this.underlyingIndex = underlyingIndex;
        this.expenseRatio = expenseRatio;
    }

    public String getUnderlyingIndex() {
        return underlyingIndex;
    }

    public double getExpenseRatio() {
        return expenseRatio;
    }
}
