package com.jat.jatbot;

import java.time.LocalDateTime;

public class Trade {
    private boolean isLong;
    private double entryPrice;
    private double closePrice;
    private double netGainLoss;
    private LocalDateTime date;
    private double portfolioBalanceBefore;
    private double portfolioBalanceAfter;
    private double stopLoss;
    private double takeProfit;
    private double quantity;

    public Trade(boolean isLong, double entryPrice, LocalDateTime date, double portfolioBalanceBefore) {
        this.isLong = isLong;
        this.entryPrice = entryPrice;
        this.closePrice = entryPrice; // Initialize close price with entry price
        this.netGainLoss = 0.0;
        this.date = date;
        this.portfolioBalanceBefore = portfolioBalanceBefore;
        this.portfolioBalanceAfter = portfolioBalanceBefore;
        this.stopLoss = 0.0;
        this.takeProfit = 0.0;
        this.quantity = 0.0;

    }

    public boolean isLong() { return isLong; }
    public double getEntryPrice() { return entryPrice; }
    public double getClosePrice() { return closePrice; }
    public double getNetGainLoss() { return netGainLoss; }
    public LocalDateTime getDate() { return date; }
    public double getPortfolioBalanceBefore() { return portfolioBalanceBefore; }
    public double getPortfolioBalanceAfter() { return portfolioBalanceAfter; }
    public double getTakeProfit() {return takeProfit;}
    public double getStopLoss() {return stopLoss;}
    public void setClosePrice(double closePrice) { this.closePrice = closePrice; }
    public void setNetGainLoss(double netGainLoss) { this.netGainLoss = netGainLoss; }
    public void setPortfolioBalanceAfter(double portfolioBalanceAfter) { this.portfolioBalanceAfter = portfolioBalanceAfter; }
    public double setStopLoss(double stopLoss) {return this.stopLoss = stopLoss;}
    public double setTakeProfit(double takeProfit) {return this.takeProfit = takeProfit;}
    public double setQuantity(double quantity) {return this.quantity = quantity;}
    public double getQuantity() {return this.quantity;}
    @Override
    public String toString() {
        return (netGainLoss >= 0 ? "Profitable" : "Loss") + " " + (isLong ? "Long" : "Short") +
               " trade: Entry Price: " + entryPrice +
               ", Closing Price: " + closePrice +
               ", Profit/Loss: " + netGainLoss +
               ", Portfolio Before: " + portfolioBalanceBefore +
               ", Portfolio After: " + portfolioBalanceAfter +
               ", Date: " + date;
    }
}
