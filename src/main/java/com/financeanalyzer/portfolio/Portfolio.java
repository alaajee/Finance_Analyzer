package com.financeanalyzer.portfolio;

import java.util.HashMap;
import java.util.Map;

public class Portfolio {
    private double cash;
    private HashMap<Asset, Position> positions;

    public Portfolio(double firstCash){
        positions = new HashMap<Asset, Position>();
        cash = firstCash;
    }

    public void applyTransaction(Transaction transaction){
        Asset asset = transaction.getAsset();
        double quantity = transaction.getQuantity();
        double price = transaction.getPrice();
        if (transaction.getType() == TransactionType.BUY){
            // Coût mobilisé à l'ouverture : plein montant pour un actif au comptant,
            // seulement la marge initiale pour un future, 0 pour un swap (voir Asset.getCashRequirement).
            double cashRequired = asset.getCashRequirement(quantity, price);

            if (cashRequired > cash ){
                throw new InsufficientFundsException(String.format(
                        "Fonds insuffisants : il faut %.2f $ mais seulement %.2f $ sont disponibles", cashRequired, cash));
            }
            cash -= cashRequired;
            Position position = positions.get(asset);
            if (position == null){
                positions.put(asset, new Position(asset, quantity,price));
            }
            else {
                position.addUnits(quantity,price);
            }


        }
        else if (transaction.getType() == TransactionType.SELL){
            Position position = positions.get(asset);
            if (position == null){
                throw new IllegalStateException("Impossible de vendre : aucune position détenue sur cet actif");

            }
            else {
                double entryPrice = position.getAveragePrice();
                position.removeUnits(quantity);

                // Produit de la clôture : plein montant pour un actif au comptant,
                // marge rendue + P&L réalisé pour un future (voir Asset.getCashReturned).
                cash += asset.getCashReturned(quantity, entryPrice, price);
            }
            if (position.getQuantity() == 0 ){
                positions.remove(asset);
            }
        }
    }

    /**
     * Règle un flux périodique de swap de taux : crédite (ou débite) le cash du portefeuille
     * du montant net dû sur {@code quantity} contrats, au taux variable courant.
     * Contrairement à {@link #applyTransaction}, cette méthode ne modifie pas de position :
     * un swap ne s'achète ni ne se vend, il génère seulement des règlements périodiques.
     */
    public void settleSwap(InterestRateSwap swap, double quantity, double currentFloatingRate) {
        cash += swap.netSettlement(currentFloatingRate) * quantity;
    }

    public double getCash(){
        return cash;
    }

    public Map<Asset, Position> getPositions(){
        return positions;
    }

    public double getTotalValue(Map<Asset, Double> currentPrices) {
        double total = cash;
        for (Position position: positions.values()){
            Double price = currentPrices.get(position.getAsset());
            if (price != null){
                total += position.getMarketValue(price);
            }
        }
        return total;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Cash: ").append(cash).append("\n");
        for (Position p : positions.values()) {
            sb.append("  ").append(p).append("\n");
        }
        return sb.toString();
    }

}