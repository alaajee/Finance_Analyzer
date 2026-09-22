package org.example;

import com.financeanalyzer.indicators.MovingAverage;
import com.financeanalyzer.model.Candle;
import com.financeanalyzer.model.MarketDataFetcher;
import com.financeanalyzer.portfolio.*;

import java.time.LocalDate;
import java.time.temporal.TemporalField;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws Exception {
        Asset apple = new Stock("AAPL", "Apple Inc.", "USD", "Technology");
        Position position = new Position(apple, 10, 300.0);
        Portfolio portfolio = new Portfolio(10000);
        portfolio.applyTransaction(new Transaction(apple, TransactionType.BUY, 10, 320, LocalDate.of(2026, 5, 4)));

        System.out.println(portfolio);
        // Cash attendu : 10000 - 3000 - 3200 = 3800

        Map<Asset, Double> currentPrices = new HashMap<>();
        currentPrices.put(apple, 332.41); // close du 16 septembre

        System.out.println("Valeur totale du portefeuille : " + portfolio.getTotalValue(currentPrices));
    }
}