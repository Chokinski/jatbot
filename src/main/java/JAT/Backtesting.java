package JAT;

import com.jat.OHLCData;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import java.time.LocalDate;
import java.util.List;
import javafx.collections.ObservableList;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.bar.enums.BarTimePeriod;

public class Backtesting {
    private ObservableList<OHLCData> barsData;
    private double initialCapital;
    private double lastTradeClosePrice = 0;
    private double trendThreshold = 0.02;
    private SupportResistanceStrategy SRstrat;
    private AlpacaController ac;

    public Backtesting(AlpacaController ac, double initialCapital) {
        this.ac = ac;
        this.initialCapital = initialCapital;
    }

    public static void main(String[] args) {
        AlpacaController ac = new AlpacaController();
        Backtesting backtesting = new Backtesting(ac, 500);
        backtesting.run("AAPL");
        backtesting.outputResults();
    }

    public void run(String symbol) {
        barsData = getData(symbol);
        this.SRstrat = new SupportResistanceStrategy(initialCapital, barsData);
        executeTrades();
    }

    public ObservableList<OHLCData> getData(String sym) {
        return ac.getBarsData(sym,
                LocalDate.of(2021, 1, 1).getYear(), LocalDate.of(2021, 1, 1).getMonthValue(), LocalDate.of(2021, 1, 1).getDayOfMonth(),
                LocalDate.of(2024, 1, 20).getYear(), LocalDate.of(2024, 1, 20).getMonthValue(), LocalDate.of(2024, 1, 20).getDayOfMonth(),
                BarTimePeriod.HOUR, 4);
    }

    public void executeTrades() {
        boolean uptrend = true;
        for (OHLCData bar : barsData) {
            uptrend = bar.getClose() > lastTradeClosePrice * (1 + trendThreshold);
            double threshold = determineThreshold(bar.getClose(), uptrend);
            
            SRstrat.executeTrades(uptrend);
            
            if (uptrend && bar.getClose() > threshold && bar.getClose() != lastTradeClosePrice) {
                if (SRstrat.orderConfirm(bar.getClose(), uptrend)) {
                    lastTradeClosePrice = bar.getClose();
                    // Call the strategy's method to handle the order confirmation
                }
            } else if (!uptrend && bar.getClose() < threshold && bar.getClose() != lastTradeClosePrice) {
                if (SRstrat.orderConfirm(bar.getClose(), uptrend)) {
                    lastTradeClosePrice = bar.getClose();
                    // Call the strategy's method to handle the order confirmation
                }
            }
        }
    }

    private double determineThreshold(double closePrice, boolean uptrend) {
        // Calculate the average true range (ATR) to determine threshold dynamically
        double averageTrueRange = calculateAverageTrueRange(barsData);
        
        // Calculate the threshold based on ATR and trend direction
        return uptrend ? closePrice - (2 * averageTrueRange) : closePrice + (2 * averageTrueRange);
    }
    
    private double calculateAverageTrueRange(List<OHLCData> data) {
        double sumATR = 0;
    
        for (int i = 1; i < data.size(); i++) {
            double trueRange = calculateTrueRange(data.get(i - 1), data.get(i));
            sumATR += trueRange;
        }
    
        return sumATR / data.size();
    }
    
    private double calculateTrueRange(OHLCData previousBar, OHLCData currentBar) {
        return Math.max(currentBar.getHigh() - currentBar.getLow(),
                Math.max(Math.abs(currentBar.getHigh() - previousBar.getClose()),
                        Math.abs(currentBar.getLow() - previousBar.getClose())));
    }

    public void outputResults() {
        System.out.println("----------Backtesting Results----------\n");
        System.out.println("Initial Capital: $" + initialCapital);
        System.out.println("Net Profit: $" + SRstrat.getNetProfit());
        System.out.println("ROI: " + SRstrat.getROI() + "%");
        System.out.println("Total Trades: " + SRstrat.getTotalTrades());
        System.out.println("Win Rate: " + SRstrat.getWinRate() + "%");
        System.out.println("Profit Factor: " + SRstrat.getProfitFactor());
        System.out.println("Average Profit: $" + SRstrat.getAverageProfit());
        System.out.println("Average Loss: $" + SRstrat.getAverageLoss());
        System.out.println("Expectancy: $" + SRstrat.getExpectancy());
        System.out.println("Max Drawdown: " + SRstrat.getMaxDrawdown());
        System.out.println("Sharpe Ratio: " + SRstrat.getSharpeRatio());
    }
}