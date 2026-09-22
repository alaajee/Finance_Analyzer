package com.financeanalyzer.portfolio;

import java.util.Objects;

public abstract class Asset {
    private final String ticker;
    private final String name;
    private final String currency;
    private final AssetType type;

    public Asset(String ticker, String name, String currency, AssetType type) {
        this.ticker = ticker;
        this.name = name;
        this.currency = currency;
        this.type = type;
    }

    public String getTicker() {
        return ticker;
    }

    public String getName() {
        return name;
    }

    public String getCurrency() {
        return currency;
    }

    public AssetType getType(){
        return type;
    }

    /**
     * Montant de liquidités mobilisé pour ouvrir une position de {@code quantity} unités
     * au prix (ou taux) {@code price}. Par défaut, correspond au coût plein — quantité fois
     * prix — comme pour une action, une obligation, un ETF, etc. achetés au comptant.
     * <p>
     * Les produits à effet de levier redéfinissent ce comportement : {@link Future}, par
     * exemple, n'immobilise que la marge initiale, pas la valeur notionnelle complète.
     * Les produits sans échange de capital à l'ouverture (ex. {@link InterestRateSwap})
     * peuvent retourner 0.
     */
    public double getCashRequirement(double quantity, double price) {
        return quantity * price;
    }

    /**
     * Liquidités obtenues en clôturant (vendant) {@code quantity} unités entrées au prix
     * {@code entryPrice}, au prix courant {@code exitPrice}. Par défaut, ignore le prix
     * d'entrée et retourne le produit plein de la vente (quantité fois prix de sortie),
     * comme pour une action classique.
     * <p>
     * Cette même méthode sert aussi à évaluer la valeur de marché courante d'une position
     * ouverte (« ce que je récupérerais si je clôturais maintenant ») : voir
     * {@link Position#getMarketValue(double)}.
     */
    public double getCashReturned(double quantity, double entryPrice, double exitPrice) {
        return quantity * exitPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Asset)) return false;
        Asset asset = (Asset) o;
        return ticker.equals(asset.ticker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticker);
    }

    @Override
    public String toString() {
        return ticker + " (" + name  +  ") [" + type.toString() + "]";
    }
}