package com.jat.jatbot.algologic;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.ObservableList;
import com.jat.ctfxplotsplus.OHLCData;

public class Indicator {

    public static double[] parseData(ObservableList<OHLCData> data) {
        return data.stream().mapToDouble(OHLCData::getClose).toArray();
    }

    public static double calculateSMA(List<Double> prices, int period) {
        if (prices.size() < period) return Double.NaN;
        return prices.subList(prices.size() - period, prices.size())
                     .stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
    }

    public static List<Double> calculateEMAList(List<Double> prices, int period) {
        if (prices.size() < period) return new ArrayList<>();
        else{

        List<Double> emaList = new ArrayList<>();
        double sma = calculateSMA(prices.subList(0, period), period);
        double smoothingConstant = 2.0 / (period + 1);
        double ema = sma;
        emaList.add(ema);

        for (int i = period; i < prices.size(); i++) {
            ema = (prices.get(i) - ema) * smoothingConstant + ema;
            emaList.add(ema);
        }
        return emaList;}
    }

    public static double calculateATR(List<Double> highPrices, List<Double> lowPrices, List<Double> closePrices, int period) {
        if (highPrices.size() < period || lowPrices.size() < period || closePrices.size() < period) return Double.NaN;

        List<Double> trueRanges = new ArrayList<>();
        for (int i = 1; i < highPrices.size(); i++) {
            double highLow   =  highPrices.get(i) - lowPrices.get(i);
            double highClose = Math.abs(highPrices.get(i) - closePrices.get(i - 1));
            double lowClose  =  Math.abs(lowPrices.get(i) - closePrices.get(i - 1));
            trueRanges.add(Math.max(highLow, Math.max(highClose, lowClose)));
        }
        return calculateSMA(trueRanges, period);
    }

    public static double[] calculateBollingerBands(List<Double> prices, int period) {
        if (prices.size() < period) return new double[]{Double.NaN, Double.NaN, Double.NaN};

        List<Double> subset = prices.subList(prices.size() - period, prices.size());
        double sma = calculateSMA(subset, period);
        double variance = subset.stream().mapToDouble(p -> Math.pow(p - sma, 2)).sum() / period;
        double standardDeviation = Math.sqrt(variance);

        return new double[]{sma + (2 * standardDeviation), sma, sma - (2 * standardDeviation)};
    }

    public static List<Double> calculateMACD(List<Double> prices, int shortPeriod, int longPeriod, int signalPeriod) {
        if (prices.size() < longPeriod) return new ArrayList<>();
        else{
    
        List<Double> shortEMA = calculateEMAList(prices, shortPeriod);
        List<Double> longEMA = calculateEMAList(prices, longPeriod);
        List<Double> macdLine = new ArrayList<>();
    
        int diff = shortEMA.size() - longEMA.size(); // = longPeriod - shortPeriod
        for (int i = 0; i < longEMA.size(); i++) {
            macdLine.add(shortEMA.get(i + diff) - longEMA.get(i));
        }
    
        // Return the EMA of the MACD line as the final MACD signal
        return calculateEMAList(macdLine, signalPeriod);
    }
    }
    public static double calculateEMA(List<Double> prices, int period, double previousEMA) {
        if (prices == null || prices.isEmpty()) {
            return previousEMA;
        }
        double smoothingConstant = 2.0 / (period + 1);
        double currentPrice = prices.get(prices.size() - 1);
        return (currentPrice - previousEMA) * smoothingConstant + previousEMA;
    }
    public static double calculateRSI(List<Double> prices, int period) {
        if (prices.size() < period + 1) return Double.NaN;

        double avgGain = 0, avgLoss = 0;
        for (int i = 1; i <= period; i++) {
            double change = prices.get(i) - prices.get(i - 1);
            avgGain += Math.max(change, 0);
            avgLoss += Math.max(-change, 0);
        }
        avgGain /= period;
        avgLoss /= period;

        for (int i = period + 1; i < prices.size(); i++) {
            double change = prices.get(i) - prices.get(i - 1);
            avgGain = ((avgGain * (period - 1)) + Math.max(change, 0)) / period;
            avgLoss = ((avgLoss * (period - 1)) + Math.max(-change, 0)) / period;
        }

        return (avgLoss == 0) ? 100 : 100 - (100 / (1 + (avgGain / avgLoss)));
    }
}
