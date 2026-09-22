package com.financeanalyzer.indicators;

import com.financeanalyzer.model.Candle;

import java.util.ArrayList;
import java.util.List;

public class MovingAverage {

    public static List<Double> simpleMovingAverage(List<Candle> candles, int period) {
        checkPeriod(period);
        List<Double> smaValues = new ArrayList<>();

        for (int i = 0; i < candles.size(); i++) {
            if (i < period - 1) {
                // Pas assez de données en arrière pour calculer une SMA ici
                smaValues.add(null);
            } else {
                double sum = 0;
                for (int j = i - period + 1; j <= i; j++) {
                    sum += candles.get(j).getClose();
                }
                smaValues.add(sum / period);
            }
        }

        return smaValues;
    }

    // EMA initialisée avec la SMA des `period` premières clôtures, puis lissée avec k = 2 / (period + 1)
    public static List<Double> exponentialMovingAverage(List<Candle> candles, int period) {
        checkPeriod(period);
        List<Double> emaValues = new ArrayList<>();
        double k = 2.0 / (period + 1);
        double previous = 0;

        for (int i = 0; i < candles.size(); i++) {
            if (i < period - 1) {
                emaValues.add(null);
            } else if (i == period - 1) {
                double sum = 0;
                for (int j = 0; j < period; j++) {
                    sum += candles.get(j).getClose();
                }
                previous = sum / period;
                emaValues.add(previous);
            } else {
                previous = candles.get(i).getClose() * k + previous * (1 - k);
                emaValues.add(previous);
            }
        }

        return emaValues;
    }

    private static void checkPeriod(int period) {
        if (period <= 0) {
            throw new IllegalArgumentException("La période doit être strictement positive : " + period);
        }
    }
}
