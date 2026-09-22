package com.financeanalyzer.portfolio;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AssetSubclassesTest {
    private static final double DELTA = 1e-9;

    @Test
    void stockKeepsItsSectorAndType() {
        Stock stock = new Stock("AAPL", "Apple Inc.", "USD", "Technology");

        assertEquals(AssetType.STOCK, stock.getType());
        assertEquals("Technology", stock.getSector());
        assertEquals("AAPL", stock.getTicker());
    }

    @Test
    void etfKeepsIndexAndExpenseRatio() {
        ETF etf = new ETF("SPY", "SPDR S&P 500", "USD", "S&P 500", 0.0009);

        assertEquals(AssetType.ETF, etf.getType());
        assertEquals("S&P 500", etf.getUnderlyingIndex());
        assertEquals(0.0009, etf.getExpenseRatio(), DELTA);
    }

    @Test
    void etfRejectsNegativeExpenseRatio() {
        assertThrows(IllegalArgumentException.class,
                () -> new ETF("SPY", "SPDR S&P 500", "USD", "S&P 500", -0.01));
    }

    @Test
    void bondKeepsCouponAndMaturity() {
        LocalDate maturity = LocalDate.of(2035, 5, 15);
        Bond bond = new Bond("US10Y", "US Treasury 10Y", "USD", 0.045, maturity);

        assertEquals(AssetType.BOND, bond.getType());
        assertEquals(0.045, bond.getCouponRate(), DELTA);
        assertEquals(maturity, bond.getMaturityDate());
    }

    @Test
    void bondRejectsNegativeCoupon() {
        assertThrows(IllegalArgumentException.class,
                () -> new Bond("US10Y", "US Treasury 10Y", "USD", -0.01, LocalDate.of(2035, 5, 15)));
    }

    @Test
    void bondRejectsMissingMaturity() {
        assertThrows(IllegalArgumentException.class,
                () -> new Bond("US10Y", "US Treasury 10Y", "USD", 0.045, null));
    }

    @Test
    void cryptoKeepsItsNetwork() {
        Crypto crypto = new Crypto("ETH", "Ethereum", "USD", "Ethereum");

        assertEquals(AssetType.CRYPTO, crypto.getType());
        assertEquals("Ethereum", crypto.getNetwork());
    }

    @Test
    void commodityKeepsItsUnit() {
        Commodity gold = new Commodity("XAU", "Or", "USD", "once");

        assertEquals(AssetType.COMMODITY, gold.getType());
        assertEquals("once", gold.getUnit());
    }

    @Test
    void forexQuotesInTheQuoteCurrency() {
        Forex eurUsd = new Forex("EURUSD", "Euro / Dollar", "EUR", "USD");

        assertEquals(AssetType.FOREX, eurUsd.getType());
        assertEquals("EUR", eurUsd.getBaseCurrency());
        assertEquals("USD", eurUsd.getQuoteCurrency());
        assertEquals("USD", eurUsd.getCurrency());
    }

    @Test
    void assetsWithTheSameTickerAreEqual() {
        Asset first = new Stock("AAPL", "Apple Inc.", "USD", "Technology");
        Asset second = new Stock("AAPL", "Apple", "USD", "Tech");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
