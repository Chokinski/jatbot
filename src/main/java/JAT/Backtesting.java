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

    }

    public ObservableList<OHLCData> getData(String sym) {
        return ac.getBarsData(sym,
                LocalDate.of(2021, 1, 1).getYear(), LocalDate.of(2021, 1, 1).getMonthValue(), LocalDate.of(2021, 1, 1).getDayOfMonth(),
                LocalDate.of(2024, 1, 20).getYear(), LocalDate.of(2024, 1, 20).getMonthValue(), LocalDate.of(2024, 1, 20).getDayOfMonth(),
                BarTimePeriod.HOUR, 4);
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