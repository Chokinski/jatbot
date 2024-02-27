package JAT;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.jat.OHLCData;
import javafx.collections.ObservableList;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.bar.enums.BarTimePeriod;


public class Backtesting {
    public ObservableList<OHLCData> barsData;
    private AlpacaController alpacaController;
    private double initialCapital;
    private double currentCapital;
    private static LocalDate startDate = LocalDate.of(2021, 1, 1);
    private static LocalDate endDate = LocalDate.of(2024, 1, 20);

    private int totalTrades = 0;
    public HiddenDivergenceStrategy strategy;
    public Backtesting(AlpacaController ac, double initialCapital) {
        this.alpacaController = ac;
        this.initialCapital = initialCapital;
        this.currentCapital = initialCapital;
    }

    public static void main(String[] args) {
        Backtesting backtesting = new Backtesting(new AlpacaController(), 500);
        backtesting.run("AAPL", startDate, endDate);
        backtesting.outputResults();
    }

    public void run(String symbol, LocalDate startDate, LocalDate endDate) {
        barsData = getData();
        System.out.println(barsData.size() + " bars of data retrieved.");
        strategy = new HiddenDivergenceStrategy(initialCapital,barsData);
        
    }

    public ObservableList<OHLCData> getData() {
        // Get historical data from Alpaca
        ObservableList<OHLCData> yummydata = alpacaController.getBarsData("AMD",
                startDate.getYear(), startDate.getMonthValue(), startDate.getDayOfMonth(),
                endDate.getYear(), endDate.getMonthValue(), endDate.getDayOfMonth(),
                BarTimePeriod.MINUTE, 15);
        return yummydata;
    }



    public void outputResults() {
        System.out.println("Initial Capital: $" + initialCapital);
        System.out.println("Final Capital: $" + currentCapital);
        System.out.println("Net Profit: $" + strategy.getNetProfit());
        System.out.println("ROI: " + strategy.getROI() + "%");
        System.out.println("Total Trades: " + totalTrades);
        System.out.println("Win Rate: " + strategy.getWinRate() + "%");
        System.out.println("Profit Factor: " + strategy.getProfitFactor());
        System.out.println("Average Profit: $" + strategy.getAverageProfit());
        System.out.println("Average Loss: $" + strategy.getAverageLoss());
        System.out.println("Expectancy: $" + strategy.getExpectancy());
        System.out.println("Max Drawdown: " + strategy.getMaxDrawdown());
        System.out.println("Sharpe Ratio: " + strategy.getSharpeRatio());
    }
}