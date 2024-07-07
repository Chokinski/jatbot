package JAT;
                         
import com.jat.OHLCData;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import javafx.collections.ObservableList;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.bar.enums.BarTimePeriod;

/*
 * This Backtesting class must be instance based
 * 
 * 
 * 
 */



public class Backtesting {
    private ObservableList<OHLCData> barsData;
    private double initialCapital;
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
        /*backtesting.run("TSLA");
        backtesting.run("GME");
        backtesting.run("AMD");
        backtesting.run("SPY");
        backtesting.run("NVDA");


        backtesting.run("META");
        backtesting.run("ADBE");
        backtesting.run("BA");
        backtesting.run("AMZN");
        backtesting.run("ATVI");
        backtesting.run("NFLX");
        backtesting.run("MSFT");
        backtesting.run("GOOGL");*/
        
        
        
    }

    public void run(String symbol) {
        barsData = getData(symbol);
        this.SRstrat = new SupportResistanceStrategy(symbol, initialCapital, barsData);
        System.out.println("\n\nRunning backtest for " + symbol);
        
        try {
            outputResults(passResults());
            writeResultsUsingStandardOutput(barsData);
        } catch (IOException e) {
            JATbot.botLogger.error("Error writing results to file");

        }
    }

    public ObservableList<OHLCData> getData(String sym) {
        return ac.getBarsData(sym,
                LocalDate.of(2021, 1, 1).getYear(), LocalDate.of(2021, 1, 1).getMonthValue(),
                LocalDate.of(2021, 1, 1).getDayOfMonth(),
                LocalDate.of(2024, 3, 25).getYear(), LocalDate.of(2024, 3, 25).getMonthValue(),
                LocalDate.of(2024, 3, 25).getDayOfMonth(),
                BarTimePeriod.MINUTE, 30);
    }

    public void outputResults(String[] results) {
        for (String result : results) {
            System.out.println(result);
        }
        SRstrat.printTradesSummary();
    }
    public void writeResultsUsingStandardOutput(ObservableList<OHLCData> data) throws IOException {
        try(FileOutputStream fileOutputStream = new FileOutputStream("data.txt")){
            BufferedWriter writer = new BufferedWriter(new FileWriter("data.txt"));
            JATbot.botLogger.info("Writing results to file path: data.txt/src/main/java/JAT/data.txt");
            for (OHLCData l : data) {

                writer.write(l + "\n");
            }
            writer.close();
        }
        catch (FileNotFoundException e) {
            JATbot.botLogger.error("File not found");
        }
    }

    public String[] passResults() {
        return SRstrat.getFormattedResults();
    }
}