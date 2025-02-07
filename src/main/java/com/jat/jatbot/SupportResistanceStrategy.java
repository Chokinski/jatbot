package com.jat.jatbot;

import com.jat.ctfxplotsplus.OHLCData;

import javafx.collections.ObservableList;


import java.util.List;


public class SupportResistanceStrategy extends Strategy {

    private List<Trade> trades;
    private List<Trade> completeTrades;
    private double accountBalance;
    private double initialCapital;
    private String symbol;
    private double trailingStopPercent; // Trailing stop percentage

    //private ObservableList<OHLCData> barsData;

    public SupportResistanceStrategy(double initialCapital, ObservableList<OHLCData> barsData, String symbol) {
        super(initialCapital, barsData, symbol);
    }

    @Override
    public void executeStrategy() {
        int checkLeft = 20; // Window size for backward-looking analysis
        double minPriceDifferencePercent = 0.4; // Adjusted to a realistic percentage
        int minTouches = 9; // Adjusted minimum number of touches required for validity
    
        for (int i = checkLeft; i < barsData.size(); i++) {
            OHLCData d = barsData.get(i);
            int supportTouches = 0;
            int resistanceTouches = 0;
    
            // Check for support levels using past data
            for (int l = i - checkLeft; l < i; l++) {
                double lowPrice = barsData.get(l).getLow();
                double priceDifferencePercent = Math.abs(d.getLow() - lowPrice) / lowPrice * 100;
                if (priceDifferencePercent <= minPriceDifferencePercent) {
                    supportTouches++;
                }
            }
    
            // Check for resistance levels using past data
            for (int l = i - checkLeft; l < i; l++) {
                double highPrice = barsData.get(l).getHigh();
                double priceDifferencePercent = Math.abs(d.getHigh() - highPrice) / highPrice * 100;
                if (priceDifferencePercent <= minPriceDifferencePercent) {
                    resistanceTouches++;
                }
            }
    
            // Place orders based on detected support or resistance levels
            if (supportTouches >= minTouches) {
                orderConfirm(d.getClose(), d.getDateTime(), true); // Buy signal on support
            }
            if (resistanceTouches >= minTouches) {
                orderConfirm(d.getClose(), d.getDateTime(), false); // Sell signal on resistance
            }
    
            // Execute trades based on the current close price
            executeTrades(d.getClose());
        }
    }
    





}