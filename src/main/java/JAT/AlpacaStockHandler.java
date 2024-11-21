package JAT;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;



import com.jat.OHLCData;
import okhttp3.OkHttpClient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.jacobpeterson.alpaca.openapi.marketdata.ApiCallback;
import net.jacobpeterson.alpaca.openapi.marketdata.ApiClient;
import net.jacobpeterson.alpaca.openapi.marketdata.ApiException;
import net.jacobpeterson.alpaca.openapi.marketdata.api.StockApi;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockAdjustment;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBarsResp;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockFeed;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockLatestBarsRespSingle;

public class AlpacaStockHandler extends StockApi {
    private JATInfoHandler infoHandler = new JATInfoHandler();
    String[] props = infoHandler.loadProperties();
    private String apiKey = props[0];
    private String apiSecret = props[1];
    private String apiEndpoint = props[2];
    private ApiClient apiClient;

    private static AlpacaController controlla;
    private static OkHttpClient okClient;

    public AlpacaStockHandler(ApiClient ac, AlpacaController controlla) {
        super(ac);
        this.controlla = controlla;
        this.apiClient = ac;

    }

    public CompletableFuture<Void> fetchAndWriteStockData(String sym, String timeframe) {
        return getBarsDataAsync(sym, timeframe)
                .thenAccept(data -> {
                    // Create StringBuilder from data
                    StringBuilder sb = new StringBuilder();
                    for (OHLCData ohlc : data) {
                        sb.append(ohlc.toString()).append("\n");
                    }

                    // Create file path for asset-specific file
                    Path filePath = Paths.get(System.getProperty("user.home"), "JAT", sym + "_" + timeframe + ".txt");

                    // Write asynchronously to file
                    infoHandler.asyncWrite(sb, filePath);
                });
    }

    public okhttp3.Call getStockBarsAsync(String symbols, String timeframe, Long limit,
            final ApiCallback<StockBarsResp> _callback)
            throws net.jacobpeterson.alpaca.openapi.marketdata.ApiException {
        // Set the start and end dates
        ZoneId zid = ZoneId.of("America/New_York");
        ZoneOffset zoffset = zid.getRules().getOffset(LocalDateTime.now());
        OffsetDateTime startDateTime = OffsetDateTime.of(2016, 01, 10, 9, 30, 0, 0, zoffset);
        return stockBarsAsync(symbols, timeframe, startDateTime, null, limit, StockAdjustment.ALL, "2016-01-10",
                StockFeed.IEX, null,
                null, null, _callback);
    }
    public okhttp3.Call getStockBarsWithOHLCDateAsync(String symbols, String timeframe,OHLCData od,
    final ApiCallback<StockBarsResp> _callback)
    throws net.jacobpeterson.alpaca.openapi.marketdata.ApiException {
// Set the start and end dates
ZoneId zid = ZoneId.of("America/New_York");
ZoneOffset zoffset = zid.getRules().getOffset(LocalDateTime.now());
OffsetDateTime startDateTime = OffsetDateTime.of(
od.getDateTime().getYear(),od.getDateTime().getMonthValue(),
od.getDateTime().getDayOfMonth(),od.getDateTime().getHour(),
od.getDateTime().getMinute(),od.getDateTime().getSecond(),
0, zoffset);
return stockBarsAsync(symbols, timeframe, startDateTime, null, 10000L, StockAdjustment.ALL, "2016-01-10",
        StockFeed.IEX, null,
        null, null, _callback);
}

    public okhttp3.Call getLatestStockbarAsync(String symbol,
            final ApiCallback<StockLatestBarsRespSingle> _callback) throws ApiException {

        return stockLatestBarSingleAsync(symbol, StockFeed.IEX, null, _callback);
    }

    /**
     * Asynchronously fetches the latest OHLC data for a specified symbol and
     * returns it as an
     * ObservableList.
     *
     * @param sym  the stock symbol
     * @param feed the feed to use for the data
     * @return a CompletableFuture that completes with an ObservableList of OHLCData
     */
    public CompletableFuture<OHLCData> getLatestBarDataAsync(String sym) {
        CompletableFuture<OHLCData> future = new CompletableFuture<>();
        // Make the asynchronous API call using the provided getStockBarsAsync method
        return CompletableFuture.supplyAsync(() -> {
            try {

                getLatestStockbarAsync(
                        sym,
                        new ApiCallback<StockLatestBarsRespSingle>() {

                            @Override
                            public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {
                                // Not used for download
                            }

                            @Override
                            public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {
                                // Not used for download
                            }

                            @Override
                            public void onFailure(net.jacobpeterson.alpaca.openapi.marketdata.ApiException e,
                                    int statusCode,
                                    Map<String, List<String>> responseHeaders) {
                                JATbot.botLogger.error("Error fetching async bar data: {}", e.getMessage());
                                JATbot.botLogger.error("Status Code: {}", statusCode);
                                JATbot.botLogger.error("Response Headers: {}", responseHeaders);
                                future.completeExceptionally(e);
                            }

                            @Override
                            public void onSuccess(StockLatestBarsRespSingle result, int statusCode,
                                    Map<String, List<String>> responseHeaders) {

                                // Populate ohlcDataList with the response data

                                OffsetDateTime timestamp = result.getBar().getT();
                                LocalDateTime date = timestamp.toLocalDateTime();

                                double open = result.getBar().getO();
                                double high = result.getBar().getH();
                                double low = result.getBar().getL();
                                double close = result.getBar().getC();
                                long volume = result.getBar().getV();

                                future.complete(new OHLCData(date, open, high, low, close, volume));
                                    
                                JATbot.botLogger.info("\nSuccessfully fetched latest bar for {}", sym);
                                
                            }

                        });
            } catch (net.jacobpeterson.alpaca.openapi.marketdata.ApiException e) {
                JATbot.botLogger.error("Error initiating async bars data call: {}", e.getMessage());
                future.completeExceptionally(e);
            }
        return future.join();
        });
    }

    /**
     * Asynchronously fetches OHLC data for a specified symbol and returns it as an
     * ObservableList.
     *
     * @param sym       the stock symbol
     * @param stYr      start year
     * @param stMo      start month
     * @param stDay     start day
     * @param endYr     end year
     * @param endMo     end month
     * @param endDay    end day
     * @param timeframe the timeframe of the bars (e.g., "1Min", "1Day")
     * @return a CompletableFuture that completes with an ObservableList of OHLCData
     */
    public CompletableFuture<ObservableList<OHLCData>> getBarsDataWithDateAsync(String sym, String timeframe,OHLCData od) {

        ObservableList<OHLCData> ohlcDataList = FXCollections.observableArrayList();
        // Make the asynchronous API call using the provided getStockBarsAsync method
        return CompletableFuture.supplyAsync(() -> {
            try {

                getStockBarsWithOHLCDateAsync(
                        sym,timeframe,
                        od,
                        new ApiCallback<StockBarsResp>() {

                            @Override
                            public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {
                                // Not used for download
                            }

                            @Override
                            public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {
                                // Not used for download
                            }

                            @Override
                            public void onFailure(net.jacobpeterson.alpaca.openapi.marketdata.ApiException e,
                                    int statusCode,
                                    Map<String, List<String>> responseHeaders) {
                                JATbot.botLogger.error("Error fetching async bars data: {}", e.getMessage());
                                JATbot.botLogger.error("Status Code: {}", statusCode);
                                JATbot.botLogger.error("Response Headers: {}", responseHeaders);
                            }

                            @Override
                            public void onSuccess(StockBarsResp result, int statusCode,
                                    Map<String, List<String>> responseHeaders) {

                                // Populate ohlcDataList with the response data
                                result.getBars().forEach((symbol, barsList) -> {
                                    AtomicInteger count = new AtomicInteger();
                                    count.set(0);
                                    barsList.forEach(bar -> {
                                        OffsetDateTime timestamp = bar.getT();
                                        LocalDateTime date = timestamp.toLocalDateTime();

                                        double open = bar.getO();
                                        double high = bar.getH();
                                        double low = bar.getL();
                                        double close = bar.getC();
                                        long volume = bar.getV();

                                        OHLCData ohlcData = new OHLCData(date, open, high, low, close, volume);
                                        ohlcDataList.add(ohlcData);
                                        count.getAndIncrement();
                                    });
                                    JATbot.botLogger.info(
                                            "\nSuccessfully fetched " + count.get() + " bars for {}" + timeframe, sym);
                                });
                            }

                        });
            } catch (net.jacobpeterson.alpaca.openapi.marketdata.ApiException e) {
                JATbot.botLogger.error("Error initiating async bars data call: {}", e.getMessage());
            }
            return ohlcDataList;
        });
    }


    /**
     * Asynchronously fetches OHLC data for a specified symbol and returns it as an
     * ObservableList.
     *
     * @param sym       the stock symbol
     * @param stYr      start year
     * @param stMo      start month
     * @param stDay     start day
     * @param endYr     end year
     * @param endMo     end month
     * @param endDay    end day
     * @param timeframe the timeframe of the bars (e.g., "1Min", "1Day")
     * @return a CompletableFuture that completes with an ObservableList of OHLCData
     */
    public CompletableFuture<ObservableList<OHLCData>> getBarsDataAsync(String sym, String timeframe) {

        ObservableList<OHLCData> ohlcDataList = FXCollections.observableArrayList();
        // Make the asynchronous API call using the provided getStockBarsAsync method
        return CompletableFuture.supplyAsync(() -> {
            try {

                getStockBarsAsync(
                        sym,
                        timeframe,
                        10000L, // Limiting to 100 bars
                        new ApiCallback<StockBarsResp>() {

                            @Override
                            public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {
                                // Not used for download
                            }

                            @Override
                            public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {
                                // Not used for download
                            }

                            @Override
                            public void onFailure(net.jacobpeterson.alpaca.openapi.marketdata.ApiException e,
                                    int statusCode,
                                    Map<String, List<String>> responseHeaders) {
                                JATbot.botLogger.error("Error fetching async bars data: {}", e.getMessage());
                                JATbot.botLogger.error("Status Code: {}", statusCode);
                                JATbot.botLogger.error("Response Headers: {}", responseHeaders);
                            }

                            @Override
                            public void onSuccess(StockBarsResp result, int statusCode,
                                    Map<String, List<String>> responseHeaders) {

                                // Populate ohlcDataList with the response data
                                result.getBars().forEach((symbol, barsList) -> {
                                    AtomicInteger count = new AtomicInteger();
                                    count.set(0);
                                    barsList.forEach(bar -> {
                                        OffsetDateTime timestamp = bar.getT();
                                        LocalDateTime date = timestamp.toLocalDateTime();

                                        double open = bar.getO();
                                        double high = bar.getH();
                                        double low = bar.getL();
                                        double close = bar.getC();
                                        long volume = bar.getV();

                                        OHLCData ohlcData = new OHLCData(date, open, high, low, close, volume);
                                        ohlcDataList.add(ohlcData);
                                        count.getAndIncrement();
                                    });
                                    JATbot.botLogger.info(
                                            "\nSuccessfully fetched " + count.get() + " bars for {}" + timeframe, sym);
                                });
                            }

                        });
            } catch (net.jacobpeterson.alpaca.openapi.marketdata.ApiException e) {
                JATbot.botLogger.error("Error initiating async bars data call: {}", e.getMessage());
            }
            return ohlcDataList;
        });
    }

    /*
     * public void checkAndUpdateStockData(String sym) {
     * infoHandler.assetFileFind(sym).thenCompose(fileMap -> {
     * // Step 1: For each file, check the last entry and get the latest available
     * timestamp
     * CompletableFuture<Void> allUpdates =
     * CompletableFuture.allOf(fileMap.entrySet().stream()
     * .map(entry -> {
     * Path filePath = entry.getKey();
     * String timeframe = entry.getValue();
     * 
     * return getLastTimestampFromFile(filePath) // Step 2: Get last timestamp from
     * file
     * .thenCompose(lastTimestamp -> {
     * return getLatestTimestamp(sym, timeframe) // Step 3: Get latest timestamp
     * from the source
     * .thenCompose(latestTimestamp -> {
     * // Step 4: Compare timestamps and determine if update is needed
     * if (lastTimestamp.isBefore(latestTimestamp)) {
     * return fetchAndWriteStockData(sym, timeframe, lastTimestamp, latestTimestamp)
     * // Step 5: Fetch and write data
     * .thenRun(() -> System.out.println("Data updated for " + sym +
     * " on timeframe " + timeframe));
     * } else {
     * System.out.println("No update needed for " + sym + " on timeframe " +
     * timeframe);
     * return CompletableFuture.completedFuture(null);
     * }
     * });
     * });
     * })
     * .toArray(CompletableFuture[]::new));
     * 
     * return allUpdates;
     * }).exceptionally(ex -> {
     * System.out.println("Error checking and updating stock data: " +
     * ex.getMessage());
     * return null;
     * });
     * }
     */

    // Method that gets the lastest timestamp of the OHLCData object within the
    // datafile.

}
