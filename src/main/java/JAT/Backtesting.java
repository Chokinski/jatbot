package JAT;
                         
import com.jat.OHLCData;


import java.io.IOException;

import java.util.concurrent.ExecutionException;
import javafx.collections.ObservableList;

import net.jacobpeterson.alpaca.openapi.marketdata.ApiException;

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

        backtesting.run("NVDA");
        //OHLCParser.parseFile(null);
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
        this.SRstrat = new SupportResistanceStrategy(initialCapital, barsData, symbol);
        System.out.println("\n\nRunning backtest for " + symbol);
        
        try {
            OHLCParser.writeResultsUsingStandardOutput(barsData);
            
            outputResults(passResults());
            OHLCParser.parseFile(null);
        } catch (IOException| ExecutionException | InterruptedException e) {
            JATbot.botLogger.error("Error writing results to file");
            
        }
    }

    public ObservableList<OHLCData> getData(String sym) {
        try {
            return ac.getBarsData(sym,
                    2023,1,1,
                    2024,
                    07,9,
                    "30Min");
        } catch (ApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public void outputResults(String[] results) {
        for (String result : results) {
            System.out.println(result);
        }
        //SRstrat.printTradesSummary();
    }



    public String[] passResults() {
        return SRstrat.getFormattedResults();
    }
}