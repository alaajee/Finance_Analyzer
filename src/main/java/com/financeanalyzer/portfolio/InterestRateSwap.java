package com.financeanalyzer.portfolio;

import java.time.LocalDate;

/**
 * Swap de taux d'intérêt (interest rate swap) : deux contreparties échangent périodiquement
 * des flux calculés sur un même montant notionnel — l'une paie un taux fixe, l'autre un taux
 * variable indexé sur un taux de référence (ex. EURIBOR 3M). Aucun capital n'est échangé :
 * seul le différentiel d'intérêts (le règlement net) est versé à chaque date de paiement.
 * <p>
 * Cette classe représente la position du point de vue d'une contrepartie : si
 * {@code payerFixed} vaut {@code true}, elle paie le taux fixe et reçoit le taux variable
 * (elle profite d'une hausse des taux) ; si {@code false}, c'est l'inverse (elle se couvre
 * contre une hausse des taux, ou parie sur une baisse).
 * <p>
 * Contrairement aux autres actifs de ce projet, un swap ne s'achète ni ne se vend au comptant :
 * entrer en position ne mobilise aucun cash (voir {@link #getCashRequirement}), et les gains ou
 * pertes se matérialisent uniquement via les règlements périodiques, appliqués avec
 * {@link Portfolio#settleSwap}.
 */
public class InterestRateSwap extends Asset {
    private final double notional;
    private final double fixedRate;          // taux fixe annuel, en fraction (0.03 = 3 %)
    private final String floatingRateIndex;  // ex. "EURIBOR 3M"
    private final LocalDate maturityDate;
    private final int paymentsPerYear;       // fréquence des règlements (ex. 4 = trimestriel)
    private final boolean payerFixed;

    public InterestRateSwap(String ticker, String name, String currency, double notional,
                             double fixedRate, String floatingRateIndex, LocalDate maturityDate,
                             int paymentsPerYear, boolean payerFixed) {
        super(ticker, name, currency, AssetType.SWAP);
        if (notional <= 0) {
            throw new IllegalArgumentException("Le notionnel doit être strictement positif");
        }
        if (fixedRate < 0) {
            throw new IllegalArgumentException("Le taux fixe ne peut pas être négatif");
        }
        if (maturityDate == null) {
            throw new IllegalArgumentException("La date d'échéance est obligatoire");
        }
        if (paymentsPerYear <= 0) {
            throw new IllegalArgumentException("La fréquence de règlement doit être strictement positive");
        }
        this.notional = notional;
        this.fixedRate = fixedRate;
        this.floatingRateIndex = floatingRateIndex;
        this.maturityDate = maturityDate;
        this.paymentsPerYear = paymentsPerYear;
        this.payerFixed = payerFixed;
    }

    /** Flux de la jambe fixe pour une période de paiement. */
    public double fixedLegPayment() {
        return notional * fixedRate / paymentsPerYear;
    }

    /** Flux de la jambe variable pour une période de paiement, selon le taux de référence courant. */
    public double floatingLegPayment(double currentFloatingRate) {
        return notional * currentFloatingRate / paymentsPerYear;
    }

    /**
     * Règlement net reçu par le détenteur de cette position pour une période (négatif s'il
     * doit payer). Le payeur du fixe reçoit (variable - fixe) ; le receveur du fixe reçoit
     * (fixe - variable).
     */
    public double netSettlement(double currentFloatingRate) {
        double fixedPmt = fixedLegPayment();
        double floatingPmt = floatingLegPayment(currentFloatingRate);
        return payerFixed ? (floatingPmt - fixedPmt) : (fixedPmt - floatingPmt);
    }

    // Entrer dans un swap n'implique aucun échange de capital initial : seuls les règlements
    // périodiques (netSettlement, via Portfolio.settleSwap) mobilisent du cash.
    @Override
    public double getCashRequirement(double quantity, double price) {
        return 0;
    }

    // De même, il n'y a pas de "produit de vente" : ce modèle simplifié ne valorise pas le
    // swap en continu (il faudrait une courbe de taux), seuls les règlements réalisés comptent.
    @Override
    public double getCashReturned(double quantity, double entryPrice, double exitPrice) {
        return 0;
    }

    public double getNotional() {
        return notional;
    }

    public double getFixedRate() {
        return fixedRate;
    }

    public String getFloatingRateIndex() {
        return floatingRateIndex;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public int getPaymentsPerYear() {
        return paymentsPerYear;
    }

    public boolean isPayerFixed() {
        return payerFixed;
    }
}
