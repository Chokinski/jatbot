package JAT;

import com.jat.OHLCData;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SupportResistanceStrategy {

    private List<Object> trades;
    private List<Double> stopLossLevels;
    private List<Double> takeProfitLevels;
    private double accountBalance;
    private double initialCapital;
    private double trailingStopPercent; // New parameter for trailing stop percentage

    private List<OHLCData> barsData;

    public SupportResistanceStrategy(double initialCapital, List<OHLCData> barsData, double trailingStopPercent) {
        this.barsData = barsData;
        this.accountBalance = initialCapital;
        this.initialCapital = initialCapital;
        this.trailingStopPercent = trailingStopPercent;
        trades = new ArrayList<>();
        stopLossLevels = new ArrayList<>();
        takeProfitLevels = new ArrayList<>();
        identifySupportResistanceLevels();
        executeTrades();
    }

    public void identifySupportResistanceLevels() {
        int checkLeft = 20; // Increased window size
        int checkRight = 20; // Increased window size
        double minPriceDifferencePercent = 0.5; // Fine-tuned minimum price difference in percent
        int minTouches = 3; // Increased minimum number of touches

        for (int i = checkLeft; i < barsData.size() - checkRight; i++) {
            int supportTouches = 0;
            int resistanceTouches = 0;

            for (int l = i - checkLeft; l < i; l++) {
                double priceDifferencePercent = Math.abs(barsData.get(i).getLow() - barsData.get(l).getLow()) / barsData.get(l).getLow() * 100;
                if (priceDifferencePercent <= minPriceDifferencePercent && barsData.get(i).getLow() < barsData.get(i + 1).getLow()) {
                    supportTouches++;
                }
            }

            for (int r = i + 1; r < i + checkRight; r++) {
                double priceDifferencePercent = Math.abs(barsData.get(i).getHigh() - barsData.get(r).getHigh()) / barsData.get(r).getHigh() * 100;
                if (priceDifferencePercent <= minPriceDifferencePercent && barsData.get(i).getHigh() > barsData.get(i - 1).getHigh()) {
                    resistanceTouches++;
                }
            }

            if (supportTouches >= minTouches) {
                orderConfirm(barsData.get(i).getClose(), barsData.get(i).getDate(), false);
            }

            if (resistanceTouches >= minTouches) {
                orderConfirm(barsData.get(i).getClose(), barsData.get(i).getDate(), true);
            }
        }
    }
    
    public void orderConfirm(double currentPrice, LocalDate date, boolean uptrend) {
        double riskPercentage = trades.isEmpty() ? 0.02 : 0.01; // Adjusted initial risk percentage to 2%, then 1%
        double riskAmount = accountBalance * riskPercentage;
        double positionSize = riskAmount / currentPrice;

        double stopLossLevel;
        double takeProfitLevel;

        if (uptrend) {
            // Adjust stop loss and take profit levels for uptrend trades
            accountBalance -= positionSize * currentPrice;
            stopLossLevel = currentPrice * 0.97; // Tightened stop loss level
            takeProfitLevel = currentPrice * 1.05; // Extended take profit level
        } else {
            // Adjust stop loss and take profit levels for downtrend trades
            accountBalance += positionSize * currentPrice;
            stopLossLevel = currentPrice * 1.05; // Extended stop loss level
            takeProfitLevel = currentPrice * 0.97; // Tightened take profit level
        }
    
        // Note the trade
        List<Object> trade = new ArrayList<>();
        trade.add(currentPrice); // Entry price
        trade.add(currentPrice); // Close price (initially same as entry price)
        trade.add(0.0); // Net Gain/Loss, initially 0
        trade.add(date); // Date
        trades.add(trade);
    
        stopLossLevels.add(stopLossLevel);
        takeProfitLevels.add(takeProfitLevel);
    }

    public void executeTrades() {
        for (int i = 0; i < trades.size(); i++) {
            double currentPrice = barsData.get(i).getClose();
            double stopLossLevel = stopLossLevels.get(i);
            double takeProfitLevel = takeProfitLevels.get(i);
            double entryPrice = (double) ((List<Object>) trades.get(i)).get(0);
            double closePrice = (double) ((List<Object>) trades.get(i)).get(1);

            // Apply filter rules to avoid low-probability trades
            if (currentPrice > stopLossLevel && currentPrice < takeProfitLevel) {
                continue; // Skip this trade
            }

            // Check if the trade hits stop loss or take profit
            if (currentPrice <= stopLossLevel || currentPrice >= takeProfitLevel) {
                double tradeProfit = currentPrice - entryPrice;
                double netGainLoss = tradeProfit * (closePrice <= entryPrice ? -1 : 1);
                accountBalance += netGainLoss;

                ((List<Object>) trades.get(i)).set(1, currentPrice); // Update close price
                ((List<Object>) trades.get(i)).set(2, netGainLoss); // Update net gain/loss
            } else {
                // Implement trailing stop loss
                double trailingStopLevel = entryPrice * (closePrice <= entryPrice ? 1 - trailingStopPercent / 100 : 1 + trailingStopPercent / 100);
                if (closePrice <= trailingStopLevel || closePrice >= takeProfitLevel) {
                    double tradeProfit = closePrice - entryPrice;
                    double netGainLoss = tradeProfit * (closePrice <= entryPrice ? -1 : 1);
                    accountBalance += netGainLoss;

                    ((List<Object>) trades.get(i)).set(1, closePrice); // Update close price
                    ((List<Object>) trades.get(i)).set(2, netGainLoss); // Update net gain/loss
                }
            }
        }
    }

    // Getters for strategy performance metrics
    public double getNetProfit() {
        return accountBalance - initialCapital;
    }

    public double getROI() {
        return (getNetProfit() / initialCapital) * 100;
    }

    public int getTotalTrades() {
        return trades.size();
    }

    public double getWinRate() {
        int profitableTrades = 0;
        for (Object trade : trades) {
            double netGainLoss = (double) ((List) trade).get(2);
            if (netGainLoss > 0) {
                profitableTrades++;
            }
        }
        return (double) profitableTrades / trades.size() * 100;
    }

    public double getProfitFactor() {
        double totalProfit = 0;
        double totalLoss = 0;
        for (Object trade : trades) {
            double netGainLoss = (double) ((List) trade).get(2);
            if (netGainLoss > 0) {
                totalProfit += netGainLoss;
            } else {
                totalLoss += netGainLoss;
            }
        }
        return Math.abs(totalProfit / totalLoss);
    }

    public double getAverageProfit() {
        double totalProfit = 0;
        int profitableTrades = 0;
        for (Object trade : trades) {
            double netGainLoss = (double) ((List) trade).get(2);
            if (netGainLoss > 0) {
                totalProfit += netGainLoss;
                profitableTrades++;
            }
        }
        return profitableTrades != 0 ? totalProfit / profitableTrades : 0;
    }

    public double getAverageLoss() {
        double totalLoss = 0;
        int losingTrades = 0;
        for (Object trade : trades) {
            double netGainLoss = (double) ((List) trade).get(2);
            if (netGainLoss < 0) {
                totalLoss += netGainLoss;
                losingTrades++;
            }
        }
        return losingTrades != 0 ? totalLoss / losingTrades : 0;
    }

    public double getExpectancy() {
        double totalExpectancy = 0;
        for (Object trade : trades) {
            double netGainLoss = (double) ((List) trade).get(2);
            totalExpectancy += netGainLoss;
        }
        return trades.size() != 0 ? totalExpectancy / trades.size() : 0;
    }

    public double getMaxDrawdown() {
        double maxDrawdown = 0;
        double peak = initialCapital;
        double balance = initialCapital;
        for (Object trade : trades) {
            double netGainLoss = (double) ((List) trade).get(2);
            balance += netGainLoss;
            if (balance > peak) {
                peak = balance;
            }
            double drawdown = (peak - balance) / peak * 100;
            if (drawdown > maxDrawdown) {
                maxDrawdown = drawdown;
            }
        }
        return maxDrawdown;
    }

    public double getSharpeRatio() {
        double totalProfit = 0;
        for (Object trade : trades) {
            double netGainLoss = (double) ((List) trade).get(2);
            totalProfit += netGainLoss;
        }
        double meanReturn = totalProfit / trades.size();
        double volatility = Math.sqrt(Math.pow(meanReturn, 2));
        return volatility != 0 ? meanReturn / volatility : 0;
    }

    public void printTradesSummary() {
        DecimalFormat df = new DecimalFormat("#.##");
        
        for (int i = 0; i < trades.size(); i++) {
            double entryPrice = (double) ((List<Object>) trades.get(i)).get(0);
            double closePrice = (double) ((List<Object>) trades.get(i)).get(1);
            double netGainLoss = (double) ((List<Object>) trades.get(i)).get(2);
            
            String tradeType = netGainLoss >= 0 ? "Profitable" : "Loss";
            double absoluteProfit = Math.abs(netGainLoss);
            
            System.out.println("Trade " + (i + 1) + ":");
            System.out.println("Entry Price: " + entryPrice);
            System.out.println("Closing Price: " + closePrice);
            System.out.println("Profit/Loss: " + df.format(absoluteProfit) + " (" + tradeType + ")");
            System.out.println();
        }
    }
}