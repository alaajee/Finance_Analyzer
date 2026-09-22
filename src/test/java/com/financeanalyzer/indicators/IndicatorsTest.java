package com.financeanalyzer.indicators;

import com.financeanalyzer.model.Candle;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IndicatorsTest {
    private static final double DELTA = 1e-9;

    private static List<Candle> candles(double... closes) {
        List<Candle> candles = new ArrayList<>();
        LocalDate date = LocalDate.of(2026, 1, 1);
        for (double close : closes) {
            candles.add(new Candle(date, close, close, close, close, 1000));
            date = date.plusDays(1);
        }
        return candles;
    }

    // ---------- SMA ----------

    @Test
    void smaIsNullUntilEnoughData() {
        List<Double> sma = MovingAverage.simpleMovingAverage(candles(1, 2, 3, 4), 2);

        assertNull(sma.get(0));
        assertEquals(1.5, sma.get(1), DELTA);
        assertEquals(2.5, sma.get(2), DELTA);
        assertEquals(3.5, sma.get(3), DELTA);
    }

    @Test
    void smaWithPeriodLargerThanDataIsAllNull() {
        List<Double> sma = MovingAverage.simpleMovingAverage(candles(1, 2), 5);

        assertEquals(2, sma.size());
        assertTrue(sma.stream().allMatch(v -> v == null));
    }

    @Test
    void smaRejectsNonPositivePeriod() {
        assertThrows(IllegalArgumentException.class,
                () -> MovingAverage.simpleMovingAverage(candles(1, 2, 3), 0));
    }

    // ---------- EMA ----------

    @Test
    void emaStartsWithSmaThenSmooths() {
        // k = 2 / (3 + 1) = 0.5 ; graine = (10 + 11 + 12) / 3 = 11
        List<Double> ema = MovingAverage.exponentialMovingAverage(candles(10, 11, 12, 13, 14), 3);

        assertNull(ema.get(0));
        assertNull(ema.get(1));
        assertEquals(11, ema.get(2), DELTA);
        assertEquals(12, ema.get(3), DELTA); // 13 * 0.5 + 11 * 0.5
        assertEquals(13, ema.get(4), DELTA); // 14 * 0.5 + 12 * 0.5
    }

    @Test
    void emaOfConstantPricesIsConstant() {
        List<Double> ema = MovingAverage.exponentialMovingAverage(candles(5, 5, 5, 5, 5), 3);

        assertEquals(5, ema.get(4), DELTA);
    }

    @Test
    void emaRejectsNonPositivePeriod() {
        assertThrows(IllegalArgumentException.class,
                () -> MovingAverage.exponentialMovingAverage(candles(1, 2, 3), -1));
    }

    // ---------- RSI ----------

    @Test
    void rsiMatchesHandComputedValues() {
        // variations : +2, -1, +2 ; période 2
        // i=2 : avgGain = (2+0)/2 = 1,   avgLoss = (0+1)/2 = 0.5  -> RS = 2 -> RSI = 100 - 100/3
        // i=3 : avgGain = (1*1+2)/2 = 1.5, avgLoss = (0.5*1+0)/2 = 0.25 -> RS = 6 -> RSI = 100 - 100/7
        List<Double> rsi = RSI.relativeStrengthIndex(candles(10, 12, 11, 13), 2);

        assertNull(rsi.get(0));
        assertNull(rsi.get(1));
        assertEquals(100 - 100.0 / 3, rsi.get(2), DELTA);
        assertEquals(100 - 100.0 / 7, rsi.get(3), DELTA);
    }

    @Test
    void rsiIs100WhenPricesOnlyRise() {
        List<Double> rsi = RSI.relativeStrengthIndex(candles(1, 2, 3, 4, 5), 3);

        assertEquals(100, rsi.get(4), DELTA);
    }

    @Test
    void rsiIs0WhenPricesOnlyFall() {
        List<Double> rsi = RSI.relativeStrengthIndex(candles(5, 4, 3, 2, 1), 3);

        assertEquals(0, rsi.get(4), DELTA);
    }

    @Test
    void rsiIs50WhenPricesAreFlat() {
        List<Double> rsi = RSI.relativeStrengthIndex(candles(5, 5, 5, 5, 5), 3);

        assertEquals(50, rsi.get(4), DELTA);
    }

    @Test
    void rsiStaysBetween0And100() {
        List<Double> rsi = RSI.relativeStrengthIndex(
                candles(44, 44.3, 44.1, 44.5, 43.4, 44.0, 44.2, 45.1, 45.4, 45.8, 46.1, 45.9, 46.0, 46.5, 46.2, 46.8), 5);

        for (Double value : rsi) {
            if (value != null) {
                assertTrue(value >= 0 && value <= 100, "RSI hors bornes : " + value);
            }
        }
    }

    @Test
    void rsiRejectsNonPositivePeriod() {
        assertThrows(IllegalArgumentException.class,
                () -> RSI.relativeStrengthIndex(candles(1, 2, 3), 0));
    }
}
