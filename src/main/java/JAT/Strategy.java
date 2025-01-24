package JAT;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.jat.OHLCData;

public abstract class Strategy {

    protected List<Trade> trades = new ArrayList<>();
    protected List<Trade> completeTrades = new ArrayList<>();
    protected double accountBalance;
    protected double initialCapital;
    protected String symbol;
    protected List<OHLCData> barsData;
    // Dynamic Parameters for AI Tuning
    protected double riskPercentage;
    protected double trailingStopPercent;
    protected double stopLossBuffer;
    protected double takeProfitBuffer;

    public Strategy(double initialCapital, List<OHLCData> barsData, String symbol) {
        this.barsData = barsData;
        this.accountBalance = initialCapital;
        this.initialCapital = initialCapital;
        this.symbol = symbol; 
        this.trailingStopPercent = 1.0; // Set trailing stop percentage to 1%
        trades = new ArrayList<>();
        completeTrades = new ArrayList<>();
        executeStrategy();
    }

    public abstract void executeStrategy();

    public void executeTrades(double currentPrice) {
        for (int i = 0; i < trades.size(); i++) {
            Trade trade = trades.get(i);
            double stopLossLevel = trade.getStopLoss();
            double takeProfitLevel = trade.getTakeProfit();
            double entryPrice = trade.getEntryPrice();
            boolean isLong = trade.isLong(); // Access the isLong flag
    
            // Determine if stop loss or take profit levels are hit for long/short positions
            boolean stopLossHit = isLong ? currentPrice <= stopLossLevel : currentPrice >= stopLossLevel;
            boolean takeProfitHit = isLong ? currentPrice >= takeProfitLevel : currentPrice <= takeProfitLevel;
    
            if (stopLossHit || takeProfitHit) {
                double tradeProfit = isLong ? currentPrice - entryPrice : entryPrice - currentPrice;
                double netGainLoss = tradeProfit * trade.getQuantity(); // Assuming quantity is a property of Trade
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

    public void orderConfirm(double currentPrice, LocalDateTime date, boolean uptrend) {
        double riskPercentage = trades.isEmpty() ? 0.02 : 0.01; // 2% for the first trade, then 1%
        double riskAmount = accountBalance * riskPercentage; // Calculate the risk amount (1% of account balance)
        double positionSize =(riskAmount / currentPrice); // Calculate the number of shares to purchase
        double moneyUsed = positionSize * currentPrice; // Calculate money used in this trade
    
        if (moneyUsed > accountBalance) {
            System.out.println("Insufficient funds to execute this trade.");
            return; // Exit if there's not enough balance
        }
    
        double portfolioBalanceBefore = accountBalance; // Store portfolio balance before the trade
        accountBalance -= moneyUsed; // Deduct the amount used for the trade from the account balance
    
        // Create a new Trade object with relevant details
        Trade trade = new Trade(uptrend, currentPrice, date, portfolioBalanceBefore);
        trade.setQuantity(positionSize); // Set the quantity of shares for the trade
    
        double stopLossLevel;
        double takeProfitLevel;
    
if (uptrend) {
    stopLossLevel = currentPrice * 0.98; // Slightly wider stop loss for flexibility
    takeProfitLevel = currentPrice * 1.1; // More conservative take profit
} else {
    stopLossLevel = currentPrice * 1.01; // More conservative stop loss for downtrend
    takeProfitLevel = currentPrice * 0.90; // Slightly less aggressive take profit
}
    
        trade.setStopLoss(stopLossLevel); // Set stop loss level
        trade.setTakeProfit(takeProfitLevel); // Set take profit level
        trades.add(trade); // Add the trade to the list of active trades
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
            if (trade.getNetGainLoss() > 0) {
                profitableTrades++;
            }
        }
        return completeTrades.size() != 0 ? (double) profitableTrades / completeTrades.size() * 100 : 0;
    }

    public double getProfitFactor() {
        double totalProfit = 0;
        double totalLoss = 0;
        for (Trade trade : completeTrades) {
            double netGainLoss = trade.getNetGainLoss();
            if (netGainLoss > 0) {
                totalProfit += netGainLoss;
            } else {
                totalLoss += Math.abs(netGainLoss); // Convert losses to positive for accurate comparison.
            }
        }
        return totalLoss != 0 ? totalProfit / totalLoss : Double.POSITIVE_INFINITY;
    }

    public double getAverageProfit() {
        double totalProfit = completeTrades.stream()
                .filter(trade -> trade.getNetGainLoss() > 0)
                .mapToDouble(Trade::getNetGainLoss)
                .sum();
        long profitableTrades = completeTrades.stream()
                .filter(trade -> trade.getNetGainLoss() > 0)
                .count();
        return profitableTrades != 0 ? totalProfit / profitableTrades : 0;
    }

    public double getAverageLoss() {
        double totalLoss = completeTrades.stream()
                .filter(trade -> trade.getNetGainLoss() < 0)
                .mapToDouble(Trade::getNetGainLoss)
                .sum();
        long losingTrades = completeTrades.stream()
                .filter(trade -> trade.getNetGainLoss() < 0)
                .count();
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
            String type = (typeInt == 1) ? "Long" : "Short";
            // Calculate the money used in the trade
            double moneyUsed = portfolioBalanceAfter - portfolioBalanceBefore;

            // Add symbol, position type, balance details to the trade summary
            System.out.println(
                    tradeType + " " + type + " trade " + (i + 1) + " on " + symbol + ": Entry Price: " + entryPrice
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
        // Implement your logic here to determine if the current setup is high
        // probability
        // This could be based on various factors like volume, price action, indicators,
        // etc.
        // For now, let's assume it's high probability if the current price is higher
        // than the previous day's close price
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
            List<Double> prices = barsData.subList(Math.max(0, i - 20), i).stream().map(OHLCData::getClose)
                    .collect(Collectors.toList());
            sma20.add(Indicator.calculateSMA(prices, 20));
            ema21.add(Indicator.calculateEMA(prices, 21, i > 0 ? ema21.get(i - 1) : sma20.get(0)));
            ema10.add(Indicator.calculateEMA(prices, 10, i > 0 ? ema10.get(i - 1) : sma20.get(0)));

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
                "Max Drawdown: " + getMaxDrawdown()
        };
    }
}