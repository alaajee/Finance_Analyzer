package com.financeanalyzer.portfolio;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

// Vérifie que Portfolio traite correctement les produits qui redéfinissent
// getCashRequirement/getCashReturned (Future à effet de levier, Swap sans capital échangé),
// sans casser le comportement au comptant déjà couvert par PortfolioTest.
class PortfolioLeveragedProductsTest {
    private static final double DELTA = 1e-9;
    private static final LocalDate DATE = LocalDate.of(2026, 5, 4);

    private Future eMiniSp500() {
        return new Future("ESZ25", "E-mini S&P 500 Dec25", "USD", "SPX", 50,
                LocalDate.of(2025, 12, 19), 0.10, 0.08);
    }

    @Test
    void buyingAFutureOnlyMobilizesTheInitialMarginNotTheFullNotional() {
        Future future = eMiniSp500();
        Portfolio portfolio = new Portfolio(50_000);

        portfolio.applyTransaction(new Transaction(future, TransactionType.BUY, 1, 4000, DATE));

        // Marge exigée : 1 * 50 * 4000 * 10 % = 20 000, pas la valeur notionnelle de 200 000.
        assertEquals(50_000 - 20_000, portfolio.getCash(), DELTA);
        Position position = portfolio.getPositions().get(future);
        assertEquals(1, position.getQuantity(), DELTA);
        assertEquals(4000, position.getAveragePrice(), DELTA);
    }

    @Test
    void buyingMoreFuturesContractsThanMarginAllowsThrows() {
        Future future = eMiniSp500();
        Portfolio portfolio = new Portfolio(50_000);

        // 3 contrats exigeraient 60 000 de marge pour 50 000 de cash disponible.
        assertThrows(InsufficientFundsException.class,
                () -> portfolio.applyTransaction(new Transaction(future, TransactionType.BUY, 3, 4000, DATE)));
        assertEquals(50_000, portfolio.getCash(), DELTA);
    }

    @Test
    void closingAFutureReturnsMarginPlusRealizedPnL() {
        Future future = eMiniSp500();
        Portfolio portfolio = new Portfolio(50_000);
        portfolio.applyTransaction(new Transaction(future, TransactionType.BUY, 1, 4000, DATE));

        portfolio.applyTransaction(new Transaction(future, TransactionType.SELL, 1, 4100, DATE));

        // Marge rendue (20 000) + gain réalisé ((4100-4000) * 50 * 1 = 5000).
        assertEquals(50_000 + 5_000, portfolio.getCash(), DELTA);
        assertFalse(portfolio.getPositions().containsKey(future));
    }

    @Test
    void futurePositionMarketValueAndPnlReflectLeverage() {
        Future future = eMiniSp500();
        Portfolio portfolio = new Portfolio(50_000);
        portfolio.applyTransaction(new Transaction(future, TransactionType.BUY, 1, 4000, DATE));
        Position position = portfolio.getPositions().get(future);

        assertEquals(20_000 + 2_500, position.getMarketValue(4050), DELTA);
        assertEquals(2_500, position.getUnrealizedPnL(4050), DELTA);
    }

    @Test
    void enteringASwapDoesNotMoveCashOnlyPeriodicSettlementsDo() {
        InterestRateSwap swap = new InterestRateSwap("SWAP1", "EUR IRS 3Y", "EUR",
                1_000_000, 0.03, "EURIBOR 3M", LocalDate.of(2029, 1, 1), 4, true);
        Portfolio portfolio = new Portfolio(100_000);

        portfolio.applyTransaction(new Transaction(swap, TransactionType.BUY, 1, swap.getFixedRate(), DATE));
        assertEquals(100_000, portfolio.getCash(), DELTA);

        // Taux variable tombé à 2.5 % < taux fixe 3 % : le payeur du fixe règle 1250.
        portfolio.settleSwap(swap, 1, 0.025);
        assertEquals(100_000 - 1_250, portfolio.getCash(), DELTA);
    }
}
