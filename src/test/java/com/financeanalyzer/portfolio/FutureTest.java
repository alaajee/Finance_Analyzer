package com.financeanalyzer.portfolio;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FutureTest {
    private static final double DELTA = 1e-9;
    private static final LocalDate EXPIRY = LocalDate.of(2025, 12, 19);

    private Future eMiniSp500() {
        // 1 contrat = 50 x l'indice, marge initiale 10 %, marge de maintenance 8 %.
        return new Future("ESZ25", "E-mini S&P 500 Dec25", "USD", "SPX", 50, EXPIRY, 0.10, 0.08);
    }

    @Test
    void keepsItsContractDetailsAndType() {
        Future future = eMiniSp500();

        assertEquals(AssetType.FUTURE, future.getType());
        assertEquals("SPX", future.getUnderlyingTicker());
        assertEquals(50, future.getContractSize(), DELTA);
        assertEquals(EXPIRY, future.getExpiryDate());
    }

    @Test
    void notionalAndInitialMarginScaleWithContractSizeAndPrice() {
        Future future = eMiniSp500();

        assertEquals(200_000, future.notionalValue(1, 4000), DELTA);
        assertEquals(20_000, future.initialMargin(1, 4000), DELTA);
        assertEquals(16_000, future.maintenanceMargin(1, 4000), DELTA);
    }

    @Test
    void markToMarketPnLIsLeveragedByContractSize() {
        Future future = eMiniSp500();

        // Le prix monte de 50 points : gain = 50 (points) * 50 (taille du contrat) * 1 (contrat)
        assertEquals(2500, future.markToMarketPnL(1, 4000, 4050), DELTA);
        assertEquals(-2500, future.markToMarketPnL(1, 4000, 3950), DELTA);
    }

    @Test
    void marginCallTriggersWhenEquityFallsBelowMaintenanceMargin() {
        Future future = eMiniSp500();

        assertTrue(future.isMarginCall(1, 4000, 3900));
        assertFalse(future.isMarginCall(1, 4000, 3980));
    }

    @Test
    void cashRequirementIsOnlyTheInitialMarginNotTheFullNotional() {
        Future future = eMiniSp500();

        double cashRequirement = future.getCashRequirement(1, 4000);

        assertEquals(20_000, cashRequirement, DELTA);
        assertTrue(cashRequirement < future.notionalValue(1, 4000));
    }

    @Test
    void cashReturnedIsMarginPlusRealizedPnL() {
        Future future = eMiniSp500();

        double cashReturned = future.getCashReturned(1, 4000, 4100);

        assertEquals(20_000 + 5_000, cashReturned, DELTA);
    }

    @Test
    void rejectsInvalidContractParameters() {
        assertThrows(IllegalArgumentException.class,
                () -> new Future("ESZ25", "E-mini S&P 500", "USD", "SPX", 0, EXPIRY, 0.10, 0.08));
        assertThrows(IllegalArgumentException.class,
                () -> new Future("ESZ25", "E-mini S&P 500", "USD", "SPX", 50, null, 0.10, 0.08));
        assertThrows(IllegalArgumentException.class,
                () -> new Future("ESZ25", "E-mini S&P 500", "USD", "SPX", 50, EXPIRY, 1.5, 0.08));
        assertThrows(IllegalArgumentException.class,
                () -> new Future("ESZ25", "E-mini S&P 500", "USD", "SPX", 50, EXPIRY, 0.10, 0.15));
    }
}
