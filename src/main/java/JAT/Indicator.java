package JAT;

import java.util.List;

import com.jat.OHLCData;

import javafx.collections.ObservableList;

public class Indicator {


    public double [] parseData(ObservableList<OHLCData> data){
        double [] parsedData = new double [data.size()];

        for (OHLCData d : data){
            parsedData[data.indexOf(d)] = d.getClose();
        }
        
        return parsedData;
    }

    
    public static double calculateSMA(List<Double> prices, int period) {
        if(prices.size() < period) throw new ArithmeticException("Not enough data points.");
        
        int sum = 0;
        for(int i=0; i<period; i++){
            sum += prices.get(i);
        }
        return (double)sum/period;
    }
    
    public static double calculateEMA(List<Double> prices, int period, double previousEMA) {
        double smoothingConstant = 2.0 / (period + 1);
        // Assuming that the latest price is at the end of the list
        double close = prices.get(prices.size() - 1);
        return (close - previousEMA) * smoothingConstant + previousEMA;
    }
}