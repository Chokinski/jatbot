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
    private List<Trade> trades;
    private List<Trade> completeTrades;
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
        executeStrategy();
    }

    public void executeStrategy() {
        int checkLeft = 20; // Increased window size
        int checkRight = 20; // Increased window size
        double minPriceDifferencePercent = 0.5; // Fine-tuned minimum price difference in percent
        int minTouches = 3; // Increased minimum number of touches

        for (int i = checkLeft; i < barsData.size() - checkRight; i++) {
            OHLCData d = barsData.get(i);
            int supportTouches = 0;
            int resistanceTouches = 0;

            for (int l = i - checkLeft; l < i; l++) {
                double priceDifferencePercent = Math.abs(d.getLow() - barsData.get(l).getLow()) / barsData.get(l).getLow() * 100;
                if (priceDifferencePercent <= minPriceDifferencePercent && d.getLow() < barsData.get(i + 1).getLow()) {
                    supportTouches++;
                }
            }

            for (int r = i + 1; r < i + checkRight; r++) {
                double priceDifferencePercent = Math.abs(d.getHigh() - barsData.get(r).getHigh()) / barsData.get(r).getHigh() * 100;
                if (priceDifferencePercent <= minPriceDifferencePercent && d.getHigh() > barsData.get(i - 1).getHigh()) {
                    resistanceTouches++;
                }
            }

            if (supportTouches >= minTouches) {
                orderConfirm(d.getClose(), d.getDateTime(), true);
            }

            
            if (resistanceTouches >= minTouches) {
                orderConfirm(d.getClose(), d.getDateTime(), false);
            }
        executeTrades(d.getClose());
        }
    }
    
    public void orderConfirm(double currentPrice, LocalDateTime date, boolean uptrend) {
        // Note the trade along with the portfolio balances
        
        double riskPercentage = trades.isEmpty() ? 0.02 : 0.01; // Adjusted initial risk percentage to 2%, then 1%
        double riskAmount = accountBalance * riskPercentage;
        double positionSize = riskAmount / currentPrice;
        double portfolioBalanceBefore = accountBalance; // Store portfolio balance before the trade
    
        double stopLossLevel;
        double takeProfitLevel;
        double moneyUsed = positionSize * currentPrice; // Calculate money used in this trade
        accountBalance -= moneyUsed;
        Trade trade = new Trade(uptrend, currentPrice, date, portfolioBalanceBefore);
        if (uptrend) {
            // Adjust stop loss and take profit levels for uptrend trades
            stopLossLevel = currentPrice * 0.97; // Tightened stop loss level
            takeProfitLevel = currentPrice * 1.05; // Extended take profit level
        } else {

            // Adjust stop loss and take profit levels for downtrend trades
            stopLossLevel = currentPrice * 1.05; // Extended stop loss level
            takeProfitLevel = currentPrice * 0.97; // Tightened take profit level
        }
        trade.setStopLoss(stopLossLevel);
        trade.setTakeProfit(takeProfitLevel);
        trades.add(trade);
    }
    public void executeTrades(double currentPrice) {
        for (int i = 0; i < trades.size(); i++) {
            Trade trade = trades.get(i);
            double stopLossLevel = trade.getStopLoss();
            double takeProfitLevel = trade.getTakeProfit();
            double entryPrice = trade.getEntryPrice();
            double closePrice = trade.getClosePrice();

            // Check if the trade hits stop loss or take profit
            if (currentPrice <= stopLossLevel || currentPrice >= takeProfitLevel) {
                double tradeProfit = currentPrice - entryPrice;
                double netGainLoss = tradeProfit * (closePrice <= entryPrice ? -1 : 1);
                accountBalance += netGainLoss;
                trade.setClosePrice(currentPrice); // Update close price
                trade.setNetGainLoss(netGainLoss); // Update net gain/loss
                trade.setPortfolioBalanceAfter(accountBalance); // Update portfolio balance after the trade

                // Move trade to completeTrades and remove from trades
                completeTrades.add(trade);
                trades.remove(i);
                i--; // Adjust index after removal
            } else {
                // Implement trailing stop loss
                double trailingStopLevel = entryPrice * (1 - trailingStopPercent / 100);
                
                if (currentPrice <= trailingStopLevel || currentPrice >= takeProfitLevel) {
                    double tradeProfit = currentPrice - entryPrice;
                    double netGainLoss = tradeProfit * (closePrice <= entryPrice ? -1 : 1);
                    accountBalance += netGainLoss;
                    trade.setClosePrice(currentPrice); // Update close price
                    trade.setNetGainLoss(netGainLoss); // Update net gain/loss
                    trade.setPortfolioBalanceAfter(accountBalance); // Update portfolio balance after the trade

                    // Move trade to completeTrades and remove from trades
                    completeTrades.add(trade);
                    trades.remove(i);
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
        for (Trade trade : completeTrades) {
            double netGainLoss = trade.getNetGainLoss();
            if (netGainLoss > 0) {
                profitableTrades++;
            }
        }
        return (double) profitableTrades / trades.size() * 100;
    }

    public double getProfitFactor() {
        double totalProfit = 0;
        double totalLoss = 0;
        for (Trade trade : completeTrades) {
            double netGainLoss = trade.getNetGainLoss();
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
        for (Trade trade : completeTrades) {
            double netGainLoss = trade.getNetGainLoss();
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
        for (Trade trade : completeTrades) {
            double netGainLoss = trade.getNetGainLoss();
            if (netGainLoss < 0) {
                totalLoss += netGainLoss;
                losingTrades++;
            }
        }
        return losingTrades != 0 ? totalLoss / losingTrades : 0;
    }

    public double getExpectancy() {
        double totalExpectancy = 0;
        for (Trade trade : completeTrades) {
            double netGainLoss = trade.getNetGainLoss();
            totalExpectancy += netGainLoss;
        }
        return trades.size() != 0 ? totalExpectancy / trades.size() : 0;
    }

    public double getMaxDrawdown() {
        double maxDrawdown = 0;
        double peak = initialCapital;
        double balance = initialCapital;
        for (Trade trade : completeTrades) {
            double netGainLoss = trade.getNetGainLoss();
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
        for (Trade trade : completeTrades) {
            double netGainLoss = trade.getNetGainLoss();
            totalProfit += netGainLoss;
        }
        double meanReturn = totalProfit / trades.size();
    
        double sumSquaredDifferences = 0;
        for (Trade trade : completeTrades) {
            double netGainLoss = trade.getNetGainLoss();
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
            Trade trade = trades.get(i);
            int typeInt = trade.isLong() ? 1 : 0;
            double entryPrice = trade.getEntryPrice();
            double closePrice = trade.getClosePrice();
            double netGainLoss = trade.getNetGainLoss();
            LocalDateTime date = trade.getDate();
            double portfolioBalanceBefore = trade.getPortfolioBalanceBefore();
            double portfolioBalanceAfter = trade.getPortfolioBalanceAfter();

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
            OHLCData d = barsData.get(i);
            List<Double> prices = barsData.subList(Math.max(0, i - 20), i).stream().map(OHLCData::getClose).collect(Collectors.toList());
            sma20.add(indi.calculateSMA(prices, 20));
            ema21.add(indi.calculateEMA(prices, 21, i > 0 ? ema21.get(i - 1) : sma20.get(0)));
            ema10.add(indi.calculateEMA(prices, 10, i > 0 ? ema10.get(i - 1) : sma20.get(0)));

            if (i >= 20 && sma20.get(i) == ema21.get(i) && sma20.get(i) == ema10.get(i)) {
                return "Signal entry at price: " + d.getClose();
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