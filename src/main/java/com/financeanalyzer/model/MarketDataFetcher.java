package com.financeanalyzer.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeanalyzer.model.Candle;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

public class MarketDataFetcher {

    public static List<Candle> loadFromResource(String resourcePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Lire le fichier depuis src/main/resources
        InputStream inputStream = MarketDataFetcher.class.getClassLoader()
                .getResourceAsStream(resourcePath);

        if (inputStream == null) {
            throw new RuntimeException("Fichier introuvable : " + resourcePath);
        }

        JsonNode root = mapper.readTree(inputStream);
        JsonNode timeSeries = root.get("Time Series (Daily)");

        List<Candle> candles = new ArrayList<>();

        Iterator<Map.Entry<String, JsonNode>> fields = timeSeries.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();

            String dateStr = entry.getKey();
            JsonNode dayData = entry.getValue();

            LocalDate date = LocalDate.parse(dateStr);
            double open = dayData.get("1. open").asDouble();
            double high = dayData.get("2. high").asDouble();
            double low = dayData.get("3. low").asDouble();
            double close = dayData.get("4. close").asDouble();
            long volume = dayData.get("5. volume").asLong();

            candles.add(new Candle(date, open, high, low, close, volume));
        }

        return candles;
    }

    public static List<Candle> sortByDateAscending(List<Candle> candles){
        candles.sort(Comparator.comparing(Candle::getDate));
        return candles;
    }

    public static void main(String[] args) throws Exception {
        List<Candle> candles = loadFromResource("aapl_daily.json");
        candles = sortByDateAscending(candles);

        System.out.println("Nombre de bougies chargées : " + candles.size());
        System.out.println("Première date : " + candles.get(0).getDate());
        System.out.println("Dernière date : " + candles.get(candles.size() - 1).getDate());
    }
}