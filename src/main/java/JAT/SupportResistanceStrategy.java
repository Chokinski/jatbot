package JAT;

import com.jat.OHLCData;

import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        double minPriceDifferencePercent = 0.5; // Minimum price difference in percent for valid touches
        int minTouches = 3; // Minimum number of touches required
    
        for (int i = checkLeft; i < barsData.size(); i++) {
            OHLCData d = barsData.get(i);
            int supportTouches = 0;
            int resistanceTouches = 0;
    
            // Check for support levels using only past data
            for (int l = i - checkLeft; l < i; l++) {
                double priceDifferencePercent = Math.abs(d.getLow() - barsData.get(l).getLow()) / barsData.get(l).getLow() * 100;
                if (priceDifferencePercent <= minPriceDifferencePercent && d.getLow() < barsData.get(l).getLow()) {
                    supportTouches++;
                }
            }
    
            // Check for resistance levels using only past data
            for (int l = i - checkLeft; l < i; l++) {
                double priceDifferencePercent = Math.abs(d.getHigh() - barsData.get(l).getHigh()) / barsData.get(l).getHigh() * 100;
                if (priceDifferencePercent <= minPriceDifferencePercent && d.getHigh() > barsData.get(l).getHigh()) {
                    resistanceTouches++;
                }
            }
    
            // Place orders based on detected support or resistance levels
            if (supportTouches >= minTouches) {
                orderConfirm(d.getClose(), d.getDateTime(), true);
            }
            if (resistanceTouches >= minTouches) {
                orderConfirm(d.getClose(), d.getDateTime(), false);
            }
    
            // Execute trades based on the current close price
            executeTrades(d.getClose());
        }
    }
    





}