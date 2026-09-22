package com.financeanalyzer.portfolio;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InterestRateSwapTest {
    private static final double DELTA = 1e-9;
    private static final LocalDate MATURITY = LocalDate.of(2029, 1, 1);

    private InterestRateSwap payerSwap() {
        // Paie 3 % fixe, reçoit l'EURIBOR 3M, sur 1 000 000 EUR, réglé trimestriellement.
        return new InterestRateSwap("SWAP1", "EUR IRS 3Y", "EUR",
                1_000_000, 0.03, "EURIBOR 3M", MATURITY, 4, true);
    }

    @Test
    void keepsItsTermsAndType() {
        InterestRateSwap swap = payerSwap();

        assertEquals(AssetType.SWAP, swap.getType());
        assertEquals(1_000_000, swap.getNotional(), DELTA);
        assertEquals(0.03, swap.getFixedRate(), DELTA);
        assertEquals("EURIBOR 3M", swap.getFloatingRateIndex());
        assertEquals(MATURITY, swap.getMaturityDate());
        assertEquals(4, swap.getPaymentsPerYear());
        assertTrue(swap.isPayerFixed());
    }

    @Test
    void legPaymentsAreProratedByFrequency() {
        InterestRateSwap swap = payerSwap();

        assertEquals(7_500, swap.fixedLegPayment(), DELTA); // 1M * 3% / 4
        assertEquals(6_250, swap.floatingLegPayment(0.025), DELTA); // 1M * 2.5% / 4
    }

    @Test
    void payerOfFixedLosesWhenFloatingRateFallsBelowFixedRate() {
        InterestRateSwap swap = payerSwap();

        // Taux variable (2.5 %) < taux fixe (3 %) : le payeur du fixe règle la différence.
        assertEquals(-1_250, swap.netSettlement(0.025), DELTA);
    }

    @Test
    void payerOfFixedGainsWhenFloatingRateRisesAboveFixedRate() {
        InterestRateSwap swap = payerSwap();

        // Taux variable (4 %) > taux fixe (3 %) : le payeur du fixe reçoit la différence.
        assertEquals(2_500, swap.netSettlement(0.04), DELTA);
    }

    @Test
    void receiverOfFixedHasTheOppositeSettlement() {
        InterestRateSwap receiver = new InterestRateSwap("SWAP2", "EUR IRS 3Y (receveur)", "EUR",
                1_000_000, 0.03, "EURIBOR 3M", MATURITY, 4, false);

        assertEquals(1_250, receiver.netSettlement(0.025), DELTA);
        assertEquals(-2_500, receiver.netSettlement(0.04), DELTA);
    }

    @Test
    void enteringOrExitingASwapNeverMobilizesUpfrontCash() {
        InterestRateSwap swap = payerSwap();

        assertEquals(0, swap.getCashRequirement(1, 0.03), DELTA);
        assertEquals(0, swap.getCashReturned(1, 0.03, 0.025), DELTA);
    }

    @Test
    void rejectsInvalidSwapParameters() {
        assertThrows(IllegalArgumentException.class, () -> new InterestRateSwap(
                "SWAP1", "EUR IRS 3Y", "EUR", 0, 0.03, "EURIBOR 3M", MATURITY, 4, true));
        assertThrows(IllegalArgumentException.class, () -> new InterestRateSwap(
                "SWAP1", "EUR IRS 3Y", "EUR", 1_000_000, -0.01, "EURIBOR 3M", MATURITY, 4, true));
        assertThrows(IllegalArgumentException.class, () -> new InterestRateSwap(
                "SWAP1", "EUR IRS 3Y", "EUR", 1_000_000, 0.03, "EURIBOR 3M", null, 4, true));
        assertThrows(IllegalArgumentException.class, () -> new InterestRateSwap(
                "SWAP1", "EUR IRS 3Y", "EUR", 1_000_000, 0.03, "EURIBOR 3M", MATURITY, 0, true));
    }
}
