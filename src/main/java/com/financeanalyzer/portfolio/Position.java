package com.financeanalyzer.portfolio;

public class Position {
    private final Asset asset;
    private double quantity;
    private double averagePrice;

    public Position(Asset asset, double quantity, double averagePrice) {
        this.asset = asset;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
    }

    public void addUnits(double additionalQuantity, double purchasePrice) {
        double totalCostBefore = this.quantity * this.averagePrice;
        double totalCostNew = additionalQuantity * purchasePrice;

        this.quantity += additionalQuantity;
        this.averagePrice = (totalCostBefore + totalCostNew) / this.quantity;
    }

    public void removeUnits(double quantityToRemove) {
        if (quantityToRemove > this.quantity) {
            throw new IllegalArgumentException(
                    "Impossible de vendre plus que ce qui est détenu");
        }
        this.quantity -= quantityToRemove;
        // Le prix moyen ne change PAS quand on vend, seulement quand on achète
    }

    // Déléguée à l'actif : « ce que je récupérerais si je clôturais la position maintenant ».
    // Pour une action, c'est quantité * prix courant. Pour un future, c'est la marge
    // immobilisée plus le P&L latent (voir Asset.getCashReturned et Future).
    public double getMarketValue(double currentPrice) {
        return asset.getCashReturned(this.quantity, this.averagePrice, currentPrice);
    }

    public double getUnrealizedPnL(double currentPrice) {
        return getMarketValue(currentPrice) - asset.getCashRequirement(this.quantity, this.averagePrice);
    }

    public Asset getAsset() {
        return asset;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getAveragePrice() {
        return averagePrice;
    }

    @Override
    public String toString() {
        return asset.getTicker() + " : " + quantity + " unités @ " + averagePrice + " (prix moyen)";
    }
}