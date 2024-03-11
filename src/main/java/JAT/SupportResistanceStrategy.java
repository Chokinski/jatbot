package JAT;

import com.jat.OHLCData;

import java.util.ArrayList;
import java.util.List;

public class SupportResistanceStrategy {
    private List<Double> supportLevels;
    private List<Double> resistanceLevels;
    private List<Double> trades;
    private List<Double> stopLossLevels;
    private List<Double> takeProfitLevels;
    private List<Double> tradePrices;
    private List<Double> tradeProfits;
    private int tradesToday;
    private double accountBalance;
    private double initialCapital;

    private List<OHLCData> barsData;

    public SupportResistanceStrategy(double initialCapital, List<OHLCData> barsData) {
        this.barsData = barsData;
        this.accountBalance = initialCapital;
        this.initialCapital = initialCapital;
        supportLevels = new ArrayList<>();
        resistanceLevels = new ArrayList<>();
        trades = new ArrayList<>();
        stopLossLevels = new ArrayList<>();
        takeProfitLevels = new ArrayList<>();
        tradePrices = new ArrayList<>();
        tradeProfits = new ArrayList<>();
        tradesToday = 0;
        identifySupportResistanceLevels();
    }

    public void identifySupportResistanceLevels() {
        int checkLeft = 10;
        int checkRight = 10;
        for (int i = 1; i < barsData.size() - 1; i++) {
            if (i - checkLeft < 0 || i + checkRight >= barsData.size() - 1) {
                continue; 
            };    
            for (int l = i - checkLeft; l < i; l++) {
                if (barsData.get(i).getLow() < barsData.get(l).getLow() 
                &&  barsData.get(i).getLow() < barsData.get(i + 1).getLow()) 
                {
                    supportLevels.add(barsData.get(i).getLow());
                }
            }

            for (int r = i + 1; r < i + checkRight; r++) {
                if (barsData.get(i).getHigh() > barsData.get(r).getHigh() 
                &&  barsData.get(i).getHigh() > barsData.get(i - 1).getHigh()) 
                {
                    resistanceLevels.add(barsData.get(i).getHigh());
                }
            }

        }
    }

    public boolean orderConfirm(double currentPrice, boolean uptrend) {
        if (uptrend) {
            return !resistanceLevels.isEmpty() && currentPrice > resistanceLevels.get(resistanceLevels.size() - 1);
        } else {
            return !supportLevels.isEmpty() && currentPrice < supportLevels.get(supportLevels.size() - 1);
        }
    }

    public void executeTrades(boolean uptrend) {
        for (OHLCData bar : barsData) {
            if (tradesToday < 2) {
                double currentPrice = bar.getClose();

                double riskPercentage = tradesToday == 0 ? 0.03 : 0.01;
                double riskAmount = accountBalance * riskPercentage;
                double positionSize = 0;
                if (!stopLossLevels.isEmpty()) {
                    positionSize = riskAmount / (currentPrice - stopLossLevels.get(stopLossLevels.size() - 1));
                }
                else{
                boolean orderConfirmed = orderConfirm(currentPrice, uptrend);
                    
                if (!trades.isEmpty() && orderConfirmed) {
                    double tradeProfit = uptrend ? positionSize * (currentPrice - tradePrices.get(tradePrices.size() - 1)) :
                            positionSize * (tradePrices.get(tradePrices.size() - 1) - currentPrice);
                    tradeProfits.add(tradeProfit);

                    accountBalance += tradeProfit;

                    trades.add(currentPrice);
                    tradePrices.add(currentPrice);
                    stopLossLevels.add(uptrend ? currentPrice * 0.98 : currentPrice * 1.02);
                    takeProfitLevels.add(uptrend ? currentPrice * 1.02 : currentPrice * 0.98);
                    tradesToday++;
                }}
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
        for (double profit : tradeProfits) {
            if (profit > 0) {
                profitableTrades++;
            }
        }
        return ((double) profitableTrades / tradeProfits.size()) * 100;
    }

    public double getProfitFactor() {
        double totalProfit = 0;
        double totalLoss = 0;
        for (double profit : tradeProfits) {
            if (profit > 0) {
                totalProfit += profit;
            } else {
                totalLoss += profit;
            }
        }
        return Math.abs(totalProfit / totalLoss);
    }

    public double getAverageProfit() {
        double totalProfit = 0;
        int profitableTrades = 0;
        for (double profit : tradeProfits) {
            if (profit > 0) {
                totalProfit += profit;
                profitableTrades++;
            }
        }
        return profitableTrades != 0 ? totalProfit / profitableTrades : 0;
    }

    public double getAverageLoss() {
        double totalLoss = 0;
        int losingTrades = 0;
        for (double profit : tradeProfits) {
            if (profit < 0) {
                totalLoss += profit;
                losingTrades++;
            }
        }
        return losingTrades != 0 ? totalLoss / losingTrades : 0;
    }

    public double getExpectancy() {
        double totalExpectancy = 0;
        for (double profit : tradeProfits) {
            totalExpectancy += profit;
        }
        return tradeProfits.size() != 0 ? totalExpectancy / tradeProfits.size() : 0;
    }

    public double getMaxDrawdown() {
        double maxDrawdown = 0;
        double peak = accountBalance;
        for (double profit : tradeProfits) {
            double balance = accountBalance - profit;
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
        for (double profit : tradeProfits) {
            totalProfit += profit;
        }
        double meanReturn = totalProfit / tradeProfits.size();
        double volatility = Math.sqrt(Math.pow(meanReturn, 2));
        return volatility != 0 ? meanReturn / volatility : 0;
    }
}