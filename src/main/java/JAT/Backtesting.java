package JAT;

import com.jat.OHLCData;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static AlpacaController ac;

    public Backtesting(AlpacaController ac, double initialCapital) {
        this.ac = ac;
        this.initialCapital = initialCapital;
    }

    public static void main(String[] args) {
        Backtesting backtesting = new Backtesting(ac, 500);
        testFileFinder("AEAEU");
    }

    public void run(String symbol, ObservableList<OHLCData> d) {
        testOnData(d);
        this.SRstrat = new SupportResistanceStrategy(initialCapital, barsData, symbol);
        System.out.println("\n\nRunning backtest for " + symbol);

        try {
            OHLCParser.writeResultsUsingStandardOutput(barsData);

            outputResults(passResults());
            OHLCParser.parseFile(null);
        } catch (IOException | ExecutionException | InterruptedException e) {
            JATbot.botLogger.error("Error writing results to file");

        }
    }

    public ObservableList<OHLCData> getData(String sym) {
        try {
            return ac.getBarsData(sym,
                    2023, 1, 1,
                    2024,
                    07, 9,
                    "30Min");
        } catch (ApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public void testOnData(ObservableList<OHLCData> d) {
        barsData = d;
    }

    public void outputResults(String[] results) {
        for (String result : results) {
            System.out.println(result);
        }
        // SRstrat.printTradesSummary();
    }

    public String[] passResults() {
        return SRstrat.getFormattedResults();
    }

    public static Map<Path, String> assetFileFind(String sym) {
        CompletableFuture<Map<Path, String>> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            Map<Path, String> fileMap = new HashMap<>();
            Pattern pattern = Pattern.compile(Pattern.quote(sym) + "_(\\w+)\\.txt");

            try {
                Path searchDir = Paths.get(System.getProperty("user.home"), "JAT");
                Files.walkFileTree(searchDir, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        Matcher matcher = pattern.matcher(file.getFileName().toString());
                        if (matcher.matches()) {
                            String timeframe = matcher.group(1);
                            fileMap.put(file, timeframe);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
                future.complete(fileMap);
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        });

        return future.join();
    }

    //This will test file finder, and return file paths and timeframes.
    public static void testFileFinder(String sym) {
        Map<Path, String> fileMap = assetFileFind(sym);
        for (Map.Entry<Path, String> entry : fileMap.entrySet()) {
            System.out.println("File: " + entry.getKey() + " Timeframe: " + entry.getValue());
        }
    }

}