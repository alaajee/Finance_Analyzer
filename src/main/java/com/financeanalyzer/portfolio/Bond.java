package com.financeanalyzer.portfolio;

import java.time.LocalDate;

public class Bond extends Asset {
    private final double couponRate; // taux annuel en fraction (0.045 = 4,5 %)
    private final LocalDate maturityDate;

    public Bond(String ticker, String name, String currency, double couponRate, LocalDate maturityDate) {
        super(ticker, name, currency, AssetType.BOND);
        if (couponRate < 0) {
            throw new IllegalArgumentException("Le taux du coupon ne peut pas être négatif");
        }
        if (maturityDate == null) {
            throw new IllegalArgumentException("La date d'échéance est obligatoire");
        }
        this.couponRate = couponRate;
        this.maturityDate = maturityDate;
    }

    public double getCouponRate() {
        return couponRate;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }
}
