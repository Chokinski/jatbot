package JAT;

import com.jat.OHLCData;
import java.time.LocalDateTime;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SupportResistanceStrategy {
    public Indicator indi;
    private List<Object> trades;
    private List<Object> completeTrades;
    private List<Double> stopLossLevels;
    private List<Double> takeProfitLevels;
    private double accountBalance;
    private double initialCapital;
    private String symbol;
    private double trailingStopPercent; // Trailing stop percentage

    private List<OHLCData> barsData;

    public SupportResistanceStrategy(double initialCapital, List<OHLCData> barsData, String symbol) {
        this.barsData = barsData;
        this.accountBalance = initialCapital;
        this.initialCapital = initialCapital;
        this.symbol = symbol;
        this.trailingStopPercent = 1.0; // Set trailing stop percentage to 1%
        trades = new ArrayList<>();
        completeTrades = new ArrayList<>();
        stopLossLevels = new ArrayList<>();
        takeProfitLevels = new ArrayList<>();
        executeStrategy();
    }

    public void executeStrategy() {
        int checkLeft = 20; // Increased window size
        int checkRight = 20; // Increased window size
        double minPriceDifferencePercent = 0.5; // Fine-tuned minimum price difference in percent
        int minTouches = 8; // Increased minimum number of touches

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
                orderConfirm(barsData.get(i).getClose(), barsData.get(i).getDateTime(), true);
            }

            
            if (resistanceTouches >= minTouches) {
                orderConfirm(barsData.get(i).getClose(), barsData.get(i).getDateTime(), false);
            }
        executeTrades(barsData.get(i).getClose());
        }
    }
    
    public void orderConfirm(double currentPrice, LocalDateTime date, boolean uptrend) {
        // Note the trade along with the portfolio balances
        List<Object> trade = new ArrayList<>();
        double riskPercentage = trades.isEmpty() ? 0.02 : 0.01; // Adjusted initial risk percentage to 2%, then 1%
        double riskAmount = accountBalance * riskPercentage;
        double positionSize = riskAmount / currentPrice;
        double portfolioBalanceBefore = accountBalance; // Store portfolio balance before the trade
    
        double stopLossLevel;
        double takeProfitLevel;
        double moneyUsed = positionSize * currentPrice; // Calculate money used in this trade
        accountBalance -= moneyUsed;
        if (uptrend) {
            // Adjust stop loss and take profit levels for uptrend trades
            trade.add(1);
            stopLossLevel = currentPrice * 0.97; // Tightened stop loss level
            takeProfitLevel = currentPrice * 1.05; // Extended take profit level
        } else {
            trade.add(0);
            // Adjust stop loss and take profit levels for downtrend trades
            stopLossLevel = currentPrice * 1.05; // Extended stop loss level
            takeProfitLevel = currentPrice * 0.97; // Tightened take profit level
        }
    
        trade.add(currentPrice); // Entry price                                
        trade.add(currentPrice); // Close price (initially same as entry price)
        trade.add(0.0); // Net Gain/Loss, initially 0
        trade.add(date); // Date
        trade.add(portfolioBalanceBefore); // Portfolio balance before the trade
        trade.add(accountBalance); // Portfolio balance after the trade
        trades.add(trade);
    
        stopLossLevels.add(stopLossLevel);
        takeProfitLevels.add(takeProfitLevel);
        
        //Buy/Sell indication 0 for sell 1 for buy accessed by trade.get(0)
        //Entry price accessed by trade.get(1)
        //Close price accessed by trade.get(2)
        //Net Gain/Loss accessed by trade.get(3)
        //Date accessed by trade.get(4)
        //Portfolio balance before the trade accessed by trade.get(5)
        //Portfolio balance after the trade accessed by trade.get(6)
    }
    public void executeTrades(double currentPrice) {
        for (int i = 0; i < trades.size(); i++) {
            double stopLossLevel = stopLossLevels.get(i);
            double takeProfitLevel = takeProfitLevels.get(i);
            double entryPrice = (double) ((List<Object>) trades.get(i)).get(1);
            double closePrice = (double) ((List<Object>) trades.get(i)).get(2);
            double portfolioBalanceBefore = (double) ((List<Object>) trades.get(i)).get(5); // Portfolio balance before the trade

            // Check if the trade hits stop loss or take profit
            if (currentPrice <= stopLossLevel || currentPrice >= takeProfitLevel) {
                double tradeProfit = currentPrice - entryPrice;
                double netGainLoss = tradeProfit * (closePrice <= entryPrice ? -1 : 1);
                accountBalance += netGainLoss;
                ((List<Object>) trades.get(i)).set(2, currentPrice); // Update close price
                ((List<Object>) trades.get(i)).set(3, netGainLoss); // Update net gain/loss
                ((List<Object>) trades.get(i)).set(6, accountBalance); // Update portfolio balance after the trade

                // Move trade to completeTrades and remove from trades
                completeTrades.add(trades.get(i));
                trades.remove(i);
                stopLossLevels.remove(i);
                takeProfitLevels.remove(i);
                i--; // Adjust index after removal
            } else {
                // Implement trailing stop loss
                double trailingStopLevel = entryPrice * (1 - trailingStopPercent / 100);
                if (currentPrice <= trailingStopLevel || currentPrice >= takeProfitLevel) {
                    double tradeProfit = currentPrice - entryPrice;
                    double netGainLoss = tradeProfit * (closePrice <= entryPrice ? -1 : 1);
                    accountBalance += netGainLoss;
                    ((List<Object>) trades.get(i)).set(2, currentPrice); // Update close price
                    ((List<Object>) trades.get(i)).set(3, netGainLoss); // Update net gain/loss
                    ((List<Object>) trades.get(i)).set(6, accountBalance); // Update portfolio balance after the trade

                    // Move trade to completeTrades and remove from trades
                    completeTrades.add(trades.get(i));
                    trades.remove(i);
                    stopLossLevels.remove(i);
                    takeProfitLevels.remove(i);
                    i--; // Adjust index after removal
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
        return completeTrades.size();
    }

    public double getWinRate() {
        int profitableTrades = 0;
        for (Object trade : completeTrades) {
            double netGainLoss = (double) ((List) trade).get(3);
            if (netGainLoss > 0) {
                profitableTrades++;
            }
        }
        return (double) profitableTrades / trades.size() * 100;
    }

    public double getProfitFactor() {
        double totalProfit = 0;
        double totalLoss = 0;
        for (Object trade : completeTrades) {
            double netGainLoss = (double) ((List) trade).get(3);
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
        for (Object trade : completeTrades) {
            double netGainLoss = (double) ((List) trade).get(3);
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
        for (Object trade : completeTrades) {
            double netGainLoss = (double) ((List) trade).get(3);
            if (netGainLoss < 0) {
                totalLoss += netGainLoss;
                losingTrades++;
            }
        }
        return losingTrades != 0 ? totalLoss / losingTrades : 0;
    }

    public double getExpectancy() {
        double totalExpectancy = 0;
        for (Object trade : completeTrades) {
            double netGainLoss = (double) ((List) trade).get(3);
            totalExpectancy += netGainLoss;
        }
        return trades.size() != 0 ? totalExpectancy / trades.size() : 0;
    }

    public double getMaxDrawdown() {
        double maxDrawdown = 0;
        double peak = initialCapital;
        double balance = initialCapital;
        for (Object trade : completeTrades) {
            double netGainLoss = (double) ((List) trade).get(3);
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
        for (Object trade : completeTrades) {
            double netGainLoss = (double) ((List) trade).get(2);
            totalProfit += netGainLoss;
        }
        double meanReturn = totalProfit / trades.size();
    
        double sumSquaredDifferences = 0;
        for (Object trade : completeTrades) {
            double netGainLoss = (double) ((List) trade).get(2);
            sumSquaredDifferences += Math.pow(netGainLoss - meanReturn, 2);
        }
        double variance = sumSquaredDifferences / trades.size();
        double volatility = Math.sqrt(variance);
    
        // Correct calculation using actual volatility
        return volatility != 0 ? meanReturn / volatility : 0;
    }

    public void printTradesSummary() {
        DecimalFormat df = new DecimalFormat("#.##");

        for (int i = 0; i < trades.size(); i++) {
            int typeInt = (int) ((List<Object>) completeTrades.get(i)).get(0);
            double entryPrice = (double) ((List<Object>) completeTrades.get(i)).get(1);
            double closePrice = (double) ((List<Object>) completeTrades.get(i)).get(2);
            double netGainLoss = (double) ((List<Object>) completeTrades.get(i)).get(3);
            LocalDateTime date = (LocalDateTime) ((List<Object>) completeTrades.get(i)).get(4);
            double portfolioBalanceBefore = (double) ((List<Object>) completeTrades.get(i)).get(5);
            double portfolioBalanceAfter = (double) ((List<Object>) completeTrades.get(i)).get(6);

            String tradeType = netGainLoss >= 0 ? "Profitable" : "Loss";
            double absoluteProfit = Math.abs(netGainLoss);

            // Calculate the actual balance after the trade
            double balanceAfterTrade = portfolioBalanceAfter + netGainLoss;
            String type = (typeInt == 1)  ? "Long" : "Short";
            // Calculate the money used in the trade
            double moneyUsed = portfolioBalanceAfter - portfolioBalanceBefore;

            // Add symbol, position type, balance details to the trade summary
            System.out.println(tradeType +" "+ type + " trade " + (i + 1) + " on " + symbol + ": Entry Price: " + entryPrice
                    + ", Closing Price: " + closePrice + ", Profit/Loss: " + df.format(absoluteProfit)
                    + ", Portfolio Before: " + df.format(portfolioBalanceBefore)
                    + ", Portfolio After: " + df.format(portfolioBalanceAfter)
                    + ", $ Used in Trade: " + df.format(moneyUsed)
                    + ", Balance After Trade: " + df.format(balanceAfterTrade)
                    + ", Date: " + date);
        }
    }
    // Method to determine if the current setup is high probability
    public boolean isHighProbabilitySetup(int index) {
        // Implement your logic here to determine if the current setup is high probability
        // This could be based on various factors like volume, price action, indicators, etc.
        // For now, let's assume it's high probability if the current price is higher than the previous day's close price
        if (index > 0 && barsData.get(index).getClose() > barsData.get(index - 1).getClose()) {
            return true;
        }
        return false;
    }


    public String generateSignal() {
        List<Double> sma20 = new ArrayList<>();
        List<Double> ema21 = new ArrayList<>();
        List<Double> ema10 = new ArrayList<>();

        for (int i = 0; i < barsData.size(); i++) {
            List<Double> prices = barsData.subList(Math.max(0, i - 20), i).stream().map(OHLCData::getClose).collect(Collectors.toList());
            sma20.add(indi.calculateSMA(prices, 20));
            ema21.add(indi.calculateEMA(prices, 21, i > 0 ? ema21.get(i - 1) : sma20.get(0)));
            ema10.add(indi.calculateEMA(prices, 10, i > 0 ? ema10.get(i - 1) : sma20.get(0)));

            if (i >= 20 && sma20.get(i) == ema21.get(i) && sma20.get(i) == ema10.get(i)) {
                return "Signal entry at price: " + barsData.get(i).getClose();
            }
        }

        return "No signal entry found.";
    }

    public String[] getFormattedResults() {
        return new String[] {
            "----------Backtesting Results----------",
            "Initial Capital: $" + initialCapital,
            "Net Profit: $" + getNetProfit(),
            "ROI: " + getROI() + "%",
            "Total Trades: " + getTotalTrades(),
            "Win Rate: " + getWinRate() + "%",
            "Profit Factor: " + getProfitFactor(),
            "Average Profit: $" + getAverageProfit(),
            "Average Loss: $" + getAverageLoss(),
            "Expectancy: $" + getExpectancy(),
            "Max Drawdown: " + getMaxDrawdown(),
            "Sharpe Ratio: " + getSharpeRatio()
        };
    }
}