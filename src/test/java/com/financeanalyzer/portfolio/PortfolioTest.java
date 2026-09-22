package com.financeanalyzer.portfolio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PortfolioTest {
    private static final double DELTA = 1e-9;
    private static final LocalDate DATE = LocalDate.of(2026, 5, 4);

    private Asset aapl;
    private Portfolio portfolio;

    @BeforeEach
    void setUp() {
        aapl = new Stock("AAPL", "Apple Inc.", "USD", "Technology");
        portfolio = new Portfolio(10000);
    }

    private Transaction buy(double quantity, double price) {
        return new Transaction(aapl, TransactionType.BUY, quantity, price, DATE);
    }

    private Transaction sell(double quantity, double price) {
        return new Transaction(aapl, TransactionType.SELL, quantity, price, DATE);
    }

    @Test
    void buyWithEnoughCashDecreasesCashAndCreatesPosition() {
        portfolio.applyTransaction(buy(10, 300));

        assertEquals(7000, portfolio.getCash(), DELTA);
        Position position = portfolio.getPositions().get(aapl);
        assertNotNull(position);
        assertEquals(10, position.getQuantity(), DELTA);
        assertEquals(300, position.getAveragePrice(), DELTA);
    }

    @Test
    void buyExceedingCashThrowsAndLeavesPortfolioUnchanged() {
        assertThrows(InsufficientFundsException.class, () -> portfolio.applyTransaction(buy(100, 300)));

        assertEquals(10000, portfolio.getCash(), DELTA);
        assertTrue(portfolio.getPositions().isEmpty());
    }

    @Test
    void buyCostingExactlyTheCashIsAllowed() {
        portfolio.applyTransaction(buy(10, 1000));

        assertEquals(0, portfolio.getCash(), DELTA);
    }

    @Test
    void secondBuyUpdatesAveragePrice() {
        portfolio.applyTransaction(buy(10, 300));
        portfolio.applyTransaction(buy(10, 320));

        Position position = portfolio.getPositions().get(aapl);
        assertEquals(20, position.getQuantity(), DELTA);
        assertEquals(310, position.getAveragePrice(), DELTA);
        assertEquals(10000 - 3000 - 3200, portfolio.getCash(), DELTA);
    }

    @Test
    void partialSellReducesQuantityAndCreditsCash() {
        portfolio.applyTransaction(buy(10, 300));
        portfolio.applyTransaction(sell(4, 350));

        Position position = portfolio.getPositions().get(aapl);
        assertEquals(6, position.getQuantity(), DELTA);
        assertEquals(300, position.getAveragePrice(), DELTA);
        assertEquals(7000 + 1400, portfolio.getCash(), DELTA);
    }

    @Test
    void sellingEverythingRemovesThePosition() {
        portfolio.applyTransaction(buy(10, 300));
        portfolio.applyTransaction(sell(10, 350));

        assertFalse(portfolio.getPositions().containsKey(aapl));
        assertEquals(7000 + 3500, portfolio.getCash(), DELTA);
    }

    @Test
    void sellWithoutPositionThrows() {
        assertThrows(IllegalStateException.class, () -> portfolio.applyTransaction(sell(1, 300)));

        assertEquals(10000, portfolio.getCash(), DELTA);
    }

    @Test
    void sellingMoreThanHeldThrowsAndKeepsCash() {
        portfolio.applyTransaction(buy(10, 300));

        assertThrows(IllegalArgumentException.class, () -> portfolio.applyTransaction(sell(11, 350)));

        assertEquals(7000, portfolio.getCash(), DELTA);
        assertEquals(10, portfolio.getPositions().get(aapl).getQuantity(), DELTA);
    }

    @Test
    void totalValueIsCashPlusMarketValueOfPositions() {
        portfolio.applyTransaction(buy(10, 300));

        double total = portfolio.getTotalValue(java.util.Map.of(aapl, 332.41));

        assertEquals(7000 + 3324.1, total, DELTA);
    }
}
