package com.financeanalyzer.indicators;

import com.financeanalyzer.model.Candle;

import java.util.ArrayList;
import java.util.List;

public class RSI {

    // RSI de Wilder : les `period` premières variations donnent les moyennes initiales,
    // puis chaque moyenne est lissée avec (précédente * (period - 1) + courante) / period.
    // Les `period` premières valeurs sont null (il faut period + 1 clôtures pour la première).
    public static List<Double> relativeStrengthIndex(List<Candle> candles, int period) {
        if (period <= 0) {
            throw new IllegalArgumentException("La période doit être strictement positive : " + period);
        }

        List<Double> rsiValues = new ArrayList<>();
        double avgGain = 0;
        double avgLoss = 0;

        for (int i = 0; i < candles.size(); i++) {
            if (i == 0) {
                rsiValues.add(null);
                continue;
            }

            double change = candles.get(i).getClose() - candles.get(i - 1).getClose();
            double gain = Math.max(change, 0);
            double loss = Math.max(-change, 0);

            if (i < period) {
                avgGain += gain;
                avgLoss += loss;
                rsiValues.add(null);
            } else {
                if (i == period) {
                    avgGain = (avgGain + gain) / period;
                    avgLoss = (avgLoss + loss) / period;
                } else {
                    avgGain = (avgGain * (period - 1) + gain) / period;
                    avgLoss = (avgLoss * (period - 1) + loss) / period;
                }
                rsiValues.add(toRsi(avgGain, avgLoss));
            }
        }

        return rsiValues;
    }

    private static double toRsi(double avgGain, double avgLoss) {
        if (avgLoss == 0) {
            return avgGain == 0 ? 50 : 100;
        }
        double rs = avgGain / avgLoss;
        return 100 - 100 / (1 + rs);
    }
}
