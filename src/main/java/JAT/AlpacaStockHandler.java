package JAT;

import java.io.IOException;
import java.nio.file.Files;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.jat.OHLCData;
import okhttp3.OkHttpClient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.jacobpeterson.alpaca.openapi.marketdata.ApiCallback;
import net.jacobpeterson.alpaca.openapi.marketdata.ApiClient;
import net.jacobpeterson.alpaca.openapi.marketdata.api.StockApi;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockAdjustment;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBarsResp;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockFeed;

public class AlpacaStockHandler extends StockApi {
    private static JATInfoHandler infoHandler = new JATInfoHandler();
    static String[] props = infoHandler.loadProperties();
    private static String apiKey = props[0];
    private static String apiSecret = props[1];
    private static String apiEndpoint = props[2];
    private static ApiClient apiClient;

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
        CompletableFuture<ObservableList<OHLCData>> future = new CompletableFuture<>();

        // StockApi stockApi = new StockApi(apiClient);


        // Make the asynchronous API call using the provided getStockBarsAsync method
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
                            future.completeExceptionally(e); // Complete exceptionally if there is an error
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
                                JATbot.botLogger.info("\nSuccessfully fetched " + count.get() + " bars for {}" + timeframe, sym);
                            });
                            future.complete(ohlcDataList); // Complete the future with the data list

                        }
                    });
        } catch (net.jacobpeterson.alpaca.openapi.marketdata.ApiException e) {
            JATbot.botLogger.error("Error initiating async bars data call: {}", e.getMessage());
            future.completeExceptionally(e); // Complete exceptionally if there is an error
        }

        return future;
    }

    public void checkAndUpdate(String sym) {
        // Define the path to the JAT directory where data files are stored
        Path jatDir = JATInfoHandler.jatDir;

        // Define a pattern to match files for this asset and extract the timeframe
        Pattern filePattern = Pattern.compile(sym + "_(\\w+).txt"); // Matches files like AAPL_1day.txt

        try (Stream<Path> files = Files.list(jatDir)) {
            // Iterate over files in the JAT directory
            files.filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().startsWith(sym + "_"))
                    .forEach(file -> {
                        Matcher matcher = filePattern.matcher(file.getFileName().toString());

                        if (matcher.matches()) {
                            String timeframe = matcher.group(1); // Extract the timeframe part (e.g., "1day", "4hr")
                            System.out.println("Updating " + sym + " for timeframe: " + timeframe);

                            // Call method to update specific file for this timeframe
                            //updateAssetDataForTimeframe(file, timeframe, sym);
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*private void updateAssetDataForTimeframe(Path filePath, String timeframe, String sym) {
        // Load the last data entry timestamp from the file
        // This method assumes you have a way to read and parse the last timestamp
        String lastTimestamp = getLastTimestampFromFile(filePath);

        // Fetch the latest bar for this symbol and timeframe
        String latestTimestamp = fetchLatestTimestamp(sym, timeframe);

        // If the last data entry is outdated, fetch and update
        if (isOutdated(lastTimestamp, latestTimestamp)) {
            fetchAndWriteStockData(sym, timeframe, lastTimestamp, latestTimestamp, filePath);
        }
    }*/

    // Helper methods
    private String getLastTimestampFromFile(Path filePath) {
        // Logic to read the last timestamp in the file
        return "2024-11-12T00:00:00Z";
    }

    private String fetchLatestTimestamp(String sym, String timeframe) {
        // Logic to get the latest timestamp available for this asset and timeframe
        return "2024-11-13T00:00:00Z"; // Placeholder
    }

    private boolean isOutdated(String lastTimestamp, String latestTimestamp) {
        // Compare timestamps to determine if an update is needed
        return !lastTimestamp.equals(latestTimestamp);
    }
}
