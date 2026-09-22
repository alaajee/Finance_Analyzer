package com.financeanalyzer.portfolio;

import java.time.LocalDate;

/**
 * Contrat à terme (future) : engagement d'acheter ou de vendre un actif sous-jacent
 * à une date d'échéance donnée, à un prix fixé aujourd'hui (le prix du contrat).
 * <p>
 * Contrairement à une action, un future se négocie avec effet de levier : à l'ouverture,
 * seule une fraction de la valeur notionnelle — la marge initiale — est immobilisée en cash.
 * Le gain ou la perte se calcule ensuite sur la valeur notionnelle complète (mark-to-market),
 * ce qui amplifie les variations de prix par rapport au cash réellement engagé. Si les pertes
 * latentes font tomber la marge disponible sous la marge de maintenance, le détenteur reçoit
 * un appel de marge (margin call) et doit reconstituer sa garantie ou clôturer la position.
 */
public class Future extends Asset {
    private final String underlyingTicker;
    private final double contractSize;          // nombre d'unités du sous-jacent par contrat
    private final LocalDate expiryDate;
    private final double initialMarginRate;      // fraction du notionnel exigée à l'ouverture (ex. 0.10 = 10 %)
    private final double maintenanceMarginRate;  // fraction minimale du notionnel devant rester couverte

    public Future(String ticker, String name, String currency, String underlyingTicker,
                   double contractSize, LocalDate expiryDate,
                   double initialMarginRate, double maintenanceMarginRate) {
        super(ticker, name, currency, AssetType.FUTURE);
        if (contractSize <= 0) {
            throw new IllegalArgumentException("La taille du contrat doit être strictement positive");
        }
        if (expiryDate == null) {
            throw new IllegalArgumentException("La date d'échéance est obligatoire");
        }
        if (initialMarginRate <= 0 || initialMarginRate > 1) {
            throw new IllegalArgumentException("Le taux de marge initiale doit être compris entre 0 (exclu) et 1");
        }
        if (maintenanceMarginRate <= 0 || maintenanceMarginRate > initialMarginRate) {
            throw new IllegalArgumentException(
                    "Le taux de marge de maintenance doit être compris entre 0 (exclu) et la marge initiale");
        }
        this.underlyingTicker = underlyingTicker;
        this.contractSize = contractSize;
        this.expiryDate = expiryDate;
        this.initialMarginRate = initialMarginRate;
        this.maintenanceMarginRate = maintenanceMarginRate;
    }

    /** Valeur notionnelle exposée par {@code quantity} contrats au prix {@code futuresPrice}. */
    public double notionalValue(double quantity, double futuresPrice) {
        return quantity * contractSize * futuresPrice;
    }

    /** Marge initiale à immobiliser pour ouvrir {@code quantity} contrats au prix {@code futuresPrice}. */
    public double initialMargin(double quantity, double futuresPrice) {
        return notionalValue(quantity, futuresPrice) * initialMarginRate;
    }

    /** Marge de maintenance minimale devant rester couverte pour {@code quantity} contrats. */
    public double maintenanceMargin(double quantity, double futuresPrice) {
        return notionalValue(quantity, futuresPrice) * maintenanceMarginRate;
    }

    /**
     * Gain ou perte latent(e) résultant de la variation de prix entre l'ouverture
     * ({@code entryPrice}) et le prix courant ({@code currentPrice}), pour {@code quantity}
     * contrats. C'est ce montant, calculé sur la valeur notionnelle complète alors que seule
     * la marge a été engagée, qui matérialise l'effet de levier du future.
     */
    public double markToMarketPnL(double quantity, double entryPrice, double currentPrice) {
        return (currentPrice - entryPrice) * contractSize * quantity;
    }

    /**
     * Indique si la marge disponible (marge initiale versée + P&L latent) est tombée sous
     * la marge de maintenance requise au prix courant, déclenchant un appel de marge.
     */
    public boolean isMarginCall(double quantity, double entryPrice, double currentPrice) {
        double availableMargin = initialMargin(quantity, entryPrice) + markToMarketPnL(quantity, entryPrice, currentPrice);
        return availableMargin < maintenanceMargin(quantity, currentPrice);
    }

    // À l'ouverture, seule la marge initiale est mobilisée — pas la valeur notionnelle complète.
    @Override
    public double getCashRequirement(double quantity, double price) {
        return initialMargin(quantity, price);
    }

    // À la clôture, on récupère la marge initiale plus (ou moins) le P&L réalisé sur la période.
    @Override
    public double getCashReturned(double quantity, double entryPrice, double exitPrice) {
        return initialMargin(quantity, entryPrice) + markToMarketPnL(quantity, entryPrice, exitPrice);
    }

    public String getUnderlyingTicker() {
        return underlyingTicker;
    }

    public double getContractSize() {
        return contractSize;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public double getInitialMarginRate() {
        return initialMarginRate;
    }

    public double getMaintenanceMarginRate() {
        return maintenanceMarginRate;
    }
}
