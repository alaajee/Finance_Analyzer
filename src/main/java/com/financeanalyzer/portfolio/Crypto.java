package com.financeanalyzer.portfolio;

public class Crypto extends Asset {
    private final String network; // blockchain sur laquelle circule l'actif (ex. Ethereum)

    public Crypto(String ticker, String name, String currency, String network) {
        super(ticker, name, currency, AssetType.CRYPTO);
        this.network = network;
    }

    public String getNetwork() {
        return network;
    }
}
