package JAT;

import com.jat.OHLCData;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class HiddenDivergenceStrategy {
    private double initialCapital;
    private double currentCapital;
    private int totalTrades;
    private int profitableTrades;
    private double grossProfit;
    private double grossLoss;
    private List<OHLCData> barsData;

    public HiddenDivergenceStrategy(double initialCapital, List<OHLCData> barsData) {
        this.initialCapital = initialCapital;
        this.currentCapital = initialCapital;
        this.barsData = barsData;
    }

    public void executeStrategy() {
        double capital = initialCapital;
        double riskPerTrade = 0.01 * initialCapital; // Risk 1% of capital on each trade
        int tradeCount = 0;
        double netProfit = 0;
        double maxDrawdown = 0;
        int wins = 0;
        int losses = 0;
        double stopLossFactor = 1.5; // Multiplier for ATR to set stop loss
        double takeProfitFactor = 2.0; // Multiplier for ATR to set take profit

        // Implement Hidden Divergence Strategy logic
        for (int i = 2; i < barsData.size(); i++) {
            OHLCData currentBar = barsData.get(i);
            OHLCData previousBar = barsData.get(i - 1);
            OHLCData previousPreviousBar = barsData.get(i - 2);

            // Adjust RSI period dynamically (you need to implement this method)
            int rsiPeriod = adjustRSIPeriod(barsData.subList(0, i));

            // Adjust MACD periods dynamically (you need to implement these methods)
            int shortTermPeriod = adjustShortTermPeriod(barsData.subList(0, i));
            int longTermPeriod = adjustLongTermPeriod(barsData.subList(0, i));

            // Calculate RSI indicator values (you need to implement this method)
            double currentRSI = calculateRSI(barsData.subList(0, i + 1), rsiPeriod);
            double previousRSI = calculateRSI(barsData.subList(0, i), rsiPeriod);
            double previousPreviousRSI = calculateRSI(barsData.subList(0, i - 1), rsiPeriod);

            // Calculate MACD indicator values (you need to implement this method)
            double currentMACD = calculateMACD(barsData.subList(0, i + 1), shortTermPeriod, longTermPeriod);
            double previousMACD = calculateMACD(barsData.subList(0, i), shortTermPeriod, longTermPeriod);
            double previousPreviousMACD = calculateMACD(barsData.subList(0, i - 1), shortTermPeriod, longTermPeriod);

            // Calculate ATR for stop loss and take profit levels (you need to implement this method)
            double atr = calculateATR(barsData.subList(0, i + 1), 14); // 14 is a commonly used period for ATR
            double stopLossLevel = currentBar.getClose() - (atr * stopLossFactor);
            double takeProfitLevel = currentBar.getClose() + (atr * takeProfitFactor);

            // Calculate available capital for trading
            double availableCapital = currentCapital * 0.95; // Allowing 5% reserve capital

            // Bullish Hidden Divergence
            if (i >= 3 && currentBar.getLow() > previousBar.getLow() && currentRSI < previousRSI
                    && previousBar.getLow() < previousPreviousBar.getLow() && previousRSI > previousPreviousRSI
                    && currentMACD > previousMACD && previousMACD > previousPreviousMACD) {
                double riskedAmount = Math.min(availableCapital * 0.02, currentCapital); // Risking 2% of available capital
                double positionSize = riskedAmount / (currentBar.getClose() - currentBar.getLow() + 0.0001);
                double cost = positionSize * currentBar.getClose();
                if (cost > currentCapital) {
                    positionSize = currentCapital / currentBar.getClose(); // Adjust position size if cost exceeds capital
                    cost = currentCapital;
                }
                currentCapital -= cost;

                // Update trade statistics
                totalTrades++;
                tradeCount++;
                if (currentBar.getClose() > previousBar.getClose()) {
                    profitableTrades++;
                    wins++;
                    grossProfit += (positionSize * currentBar.getClose()) - cost; // Update grossProfit
                } else {
                    grossLoss += cost; // Update grossLoss
                    losses++;
                }

                // Update net profit and max drawdown for every 5 trades
                if (tradeCount % 5 == 0) {
                    netProfit = currentCapital - capital;
                    maxDrawdown = Math.max(maxDrawdown, capital - currentCapital);
                    double winRate = (double) wins / 5 * 100; // Calculate win rate for the last 5 trades

                    // Output the results
                    System.out.println("Net Profit for last 5 trades: " + netProfit);
                    System.out.println("Max Drawdown for last 5 trades: " + maxDrawdown);
                    System.out.println("Win Rate for last 5 trades: " + winRate + "%");
                    System.out.println("Wins for last 5 trades: " + wins);
                    System.out.println("Losses for last 5 trades: " + losses);

                    // Reset the counters
                    netProfit = 0;
                    maxDrawdown = 0;
                    wins = 0;
                    losses = 0;
                }
            }
            // Bearish Hidden Divergence
            else if (i >= 3 && currentBar.getHigh() < previousBar.getHigh() && currentRSI > previousRSI
                    && previousBar.getHigh() > previousPreviousBar.getHigh() && previousRSI < previousPreviousRSI
                    && currentMACD < previousMACD && previousMACD < previousPreviousMACD) {
                double riskedAmount = Math.min(availableCapital * 0.02, currentCapital); // Risking 2% of available capital
                double positionSize = riskedAmount / (currentBar.getHigh() - currentBar.getClose() + 0.0001);
                double revenue = positionSize * currentBar.getClose();
                if (revenue > currentCapital) {
                    positionSize = currentCapital / currentBar.getClose(); // Adjust position size if revenue exceeds capital
                    revenue = currentCapital;
                }
                currentCapital += revenue;

                // Update trade statistics
                totalTrades++;
                if (currentBar.getClose() < previousBar.getClose()) {
                    profitableTrades++;
                    grossProfit += revenue; // Update grossProfit
                } else {
                    grossLoss += revenue; // Update grossLoss
                }
            }
        }

        // Update current capital
        currentCapital = capital;
    }

    private int adjustRSIPeriod(List<OHLCData> data) {
        int minPeriod = 5;
        int maxPeriod = 50;
        int optimalPeriod = 14; // Default period
        double highestRSI = 0;

        // Iterate over different periods and find the one with the highest RSI
        for (int period = minPeriod; period <= maxPeriod; period++) {
            double rsi = calculateRSI(data, period);
            if (rsi > highestRSI) {
                highestRSI = rsi;
                optimalPeriod = period;
            }
        }
        return optimalPeriod;
    }

    private int adjustShortTermPeriod(List<OHLCData> data) {
        // Calculate short term period for MACD based on data
        // For example, find the optimal period by maximizing the MACD histogram
        int optimalPeriod = 12; // Default value
        double maxHistogram = Double.MIN_VALUE;
    
        for (int period = 5; period <= 20; period++) { // Consider periods from 5 to 20
            double macdHistogram = calculateMACDHistogram(data, period, 26); // Assuming long term period is fixed at 26
            if (macdHistogram > maxHistogram) {
                maxHistogram = macdHistogram;
                optimalPeriod = period;
            }
        }
        return optimalPeriod;
    }
    
    private int adjustLongTermPeriod(List<OHLCData> data) {
        // Calculate long term period for MACD based on data
        // For example, find the optimal period by maximizing the MACD histogram
        int optimalPeriod = 26; // Default value
        double maxHistogram = Double.MIN_VALUE;
    
        for (int period = 20; period <= 50; period++) { // Consider periods from 20 to 50
            double macdHistogram = calculateMACDHistogram(data, 12, period); // Assuming short term period is fixed at 12
            if (macdHistogram > maxHistogram) {
                maxHistogram = macdHistogram;
                optimalPeriod = period;
            }
        }
        return optimalPeriod;
    }
    
    private double calculateEMA(List<OHLCData> historicalData, int period) {
        double multiplier = 2.0 / (period + 1);
        double ema = historicalData.get(0).getClose(); // Start with the first price
    
        // Calculate EMA for each price
        for (int i = 1; i < historicalData.size(); i++) {
            double closePrice = historicalData.get(i).getClose();
            ema = (closePrice - ema) * multiplier + ema;
        }
    
        return ema;
    }
    
    private double calculateMACDHistogram(List<OHLCData> data, int shortTermPeriod, int longTermPeriod) {
        // Calculate MACD histogram by subtracting MACD line from the signal line
        double macdLine = calculateEMA(data, shortTermPeriod) - calculateEMA(data, longTermPeriod);
        double signalLine = calculateEMA(data, 9); // Assuming signal line period is fixed at 9
        double macdHistogram = macdLine - signalLine;
        return macdHistogram;
    }

    private double calculateRSI(List<OHLCData> data, int period) {
        // Calculate RSI using Wilder's smoothing method
        double sumGain = 0;
        double sumLoss = 0;

        // Calculate initial gain and loss
        for (int i = 1; i <= period; i++) {
            double priceDiff = data.get(i).getClose() - data.get(i - 1).getClose();
            if (priceDiff >= 0) {
                sumGain += priceDiff;
            } else {
                sumLoss -= priceDiff;
            }
        }

        // Calculate average gain and loss
        double avgGain = sumGain / period;
        double avgLoss = sumLoss / period;

        // Calculate RS (Relative Strength)
        double rs = avgGain / avgLoss;

        // Calculate RSI
        return 100 - (100 / (1 + rs));
    }

    private double calculateMACD(List<OHLCData> data, int shortTermPeriod, int longTermPeriod) {
        // Calculate short-term exponential moving average (EMA)
        double shortTermEMA = calculateEMA(data, shortTermPeriod);

        // Calculate long-term exponential moving average (EMA)
        double longTermEMA = calculateEMA(data, longTermPeriod);

        // Calculate MACD line
        return shortTermEMA - longTermEMA;
    }

    private double calculateATR(List<OHLCData> data, int period) {
        double sumATR = 0;

        // Calculate ATR based on True Range for each period
        for (int i = 1; i < data.size(); i++) {
            double trueRange = calculateTrueRange(data.get(i - 1), data.get(i));
            sumATR += trueRange;
        }

        // Average True Range (ATR)
        return sumATR / period;
    }

    private double calculateTrueRange(OHLCData previousBar, OHLCData currentBar) {
        // Calculate True Range
        double trueRange = Math.max(currentBar.getHigh() - currentBar.getLow(),
                Math.max(Math.abs(currentBar.getHigh() - previousBar.getClose()),
                        Math.abs(currentBar.getLow() - previousBar.getClose())));
        return trueRange;
    }

    // Add any additional methods required for the strategy

    // Getters for performance metrics
    public double getInitialCapital() {
        return initialCapital;
    }

    public double getCurrentCapital() {
        return currentCapital;
    }

    public double getNetProfit() {
        return currentCapital - initialCapital;
    }

    public double getROI() {
        if (initialCapital == 0) {
            return 0; // Return 0 if initialCapital is 0 to prevent division by zero
        }
        return (currentCapital - initialCapital) / initialCapital * 100;
    }

    public double getWinRate() {
        if (totalTrades == 0) {
            return 0; // Return 0 if totalTrades is 0 to prevent division by zero
        }
        return (double) profitableTrades / totalTrades * 100;
    }

    public double getProfitFactor() {
        if (grossLoss == 0) {
            return grossProfit; // or return some other value indicating undefined profit factor
        }
        return grossProfit / Math.abs(grossLoss);
    }

    public double getAverageProfit() {
        if (profitableTrades == 0) {
            return 0; // Return 0 if profitableTrades is 0 to prevent division by zero
        }
        return grossProfit / profitableTrades;
    }

    public double getAverageLoss() {
        if (totalTrades == profitableTrades) {
            return 0; // Return 0 if there are no losing trades to prevent division by zero
        }
        return grossLoss / (totalTrades - profitableTrades);
    }

    public double getExpectancy() {
        if (totalTrades == 0) {
            return 0; // or return some other value indicating undefined expectancy
        }
        return (getWinRate() / 100 * getAverageProfit()) - ((1 - getWinRate() / 100) * getAverageLoss());
    }

    public double getMaxDrawdown() {
        double maxDrawdown = 0.0;
        double peak = initialCapital;
        double trough = initialCapital;

        for (OHLCData bar : barsData) {
            double currentCapital = initialCapital;
            // Update currentCapital based on trading decisions made so far
            // For example: currentCapital -= costOfTrade; or currentCapital += revenueOfTrade

            if (currentCapital > peak) {
                peak = currentCapital;
                trough = peak;
            }

            double drawdown = (peak - currentCapital) / peak;
            if (drawdown > maxDrawdown) {
                maxDrawdown = drawdown;
            }
        }

        return maxDrawdown * 100; // Return as percentage
    }

    public double getSharpeRatio() {
        // Calculate daily returns
        List<Double> returns = new ArrayList<>();
        for (int i = 1; i < barsData.size(); i++) {
            double currentCapital = initialCapital;
            // Update currentCapital based on trading decisions made so far
            // For example: currentCapital -= costOfTrade; or currentCapital += revenueOfTrade

            double previousCapital = initialCapital; // Replace with previous capital if applicable
            double dailyReturn = (currentCapital - previousCapital) / previousCapital;
            returns.add(dailyReturn);
        }

        // Calculate average return
        double sumReturns = 0;
        for (double ret : returns) {
            sumReturns += ret;
        }
        double avgReturn = sumReturns / returns.size();

        // Calculate standard deviation of returns
        double sumSquaredDifferences = 0;
        for (double ret : returns) {
            sumSquaredDifferences += Math.pow(ret - avgReturn, 2);
        }
        double stdDev = Math.sqrt(sumSquaredDifferences / returns.size());

        // Assume risk-free rate (for example, 0%)
        double riskFreeRate = 0.0;

        // Calculate Sharpe ratio
        double sharpeRatio = (avgReturn - riskFreeRate) / stdDev;

        return sharpeRatio;
    }
}