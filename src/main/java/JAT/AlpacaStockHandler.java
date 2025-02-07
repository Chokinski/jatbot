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
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockSnapshot;

/**
 * The AlpacaStockHandler class extends the StockApi and provides methods to
 * interact with the Alpaca stock API.
 * It includes methods to fetch and write stock data asynchronously, as well as
 * methods to get stock bars and latest stock bars.
 * 
 * <p>
 * Dependencies:
 * <p>
 * - JATInfoHandler: Handles loading properties and handles information
 * asynchronously.
 * <p>
 * - ApiClient: The client used to interact with the Alpaca API.
 * <p>
 * - AlpacaController: Controller for Alpaca-specific operations.
 * <p>
 * - OkHttpClient: HTTP client for making API requests.
 * 
 * <p>
 * Methods:
 * <ul>
 * <li>{@link #fetchAndWriteStockData(String, String)}: Fetches stock data for a
 * given symbol and timeframe, and writes it to a file.</li>
 * <li>{@link #getStockBarsAsync(String, String, Long, ApiCallback)}: Fetches
 * stock bars asynchronously.</li>
 * <li>{@link #getStockBarsWithOHLCDateAsync(String, String, OHLCData, ApiCallback)}:
 * Fetches stock bars asynchronously using an OHLCData object for the start
 * date.</li>
 * <li>{@link #getLatestStockbarAsync(String, ApiCallback)}: Fetches the latest
 * stock bar asynchronously.</li>
 * <li>{@link #getLatestBarDataAsync(String)}: Fetches the latest OHLC data for
 * a specified symbol asynchronously.</li>
 * <li>{@link #getBarsDataWithDateAsync(String, String, OHLCData)}: Fetches OHLC
 * data for a specified symbol using an OHLCData object for the start date
 * asynchronously.</li>
 * <li>{@link #getBarsDataAsync(String, String)}: Fetches OHLC data for a
 * specified symbol and timeframe asynchronously.</li>
 * </ul>
 * <p>
 * Example usage:
 * 
 * <pre>
 * {@code
 * 
 * AlpacaController ac = new AlpacaController();
 * AlpacaStockHandler stockHandler = new AlpacaStockHandler(ac.alpaca.marketData().getInternalAPIClient(), controller);
 * 
 * stockHandler.fetchAndWriteStockData("AAPL", "1Day").thenRun(() -> {
 *     System.out.println("Stock data fetched and written to file.");
 * });
 * }
 * </pre>
 * 
 * <p>
 * Note: This class requires the Alpaca API and related dependencies to be
 * properly configured.
 * 
 * @see StockApi
 * @see JATInfoHandler
 * @see ApiClient
 * @see AlpacaController
 * @see OkHttpClient
 */
public class AlpacaStockHandler extends StockApi {
    private JATInfoHandler infoHandler = new JATInfoHandler();
    String[] props = JATInfoHandler.loadProperties();
    private String apiKey = props[0];
    private String apiSecret = props[1];
    private String apiEndpoint = props[2];
    private ApiClient apiClient;

    private static OkHttpClient okClient;

    public AlpacaStockHandler(ApiClient ac) {
        super(ac);

        this.apiClient = ac;

    }

    public CompletableFuture<Void> fetchAndWriteStockData(String sym, String timeframe, AtomicInteger c) {
        return JATInfoHandler.doesFileExist(sym, timeframe) // Check if the file exists
                .thenCompose(fileExists -> {
                    if (fileExists) {
                        // If the file exists, delete it first
                        return JATInfoHandler.deleteAssetFile(sym, timeframe);
                    } else {
                        // If the file doesn't exist, no need to delete, just continue
                        return CompletableFuture.completedFuture(null);
                    }
                })
                .thenCompose(aVoid -> getBarsDataAsync(sym, timeframe, c)) // After (potential) deletion, fetch stock
                                                                           // data
                .thenAccept(data -> {
                    // Create StringBuilder from fetched data
                    StringBuilder sb = new StringBuilder();
                    for (OHLCData ohlc : data) {
                        sb.append(ohlc.toString()).append("\n");
                    }
                    // Create file path for asset-specific file
                    Path filePath = Paths.get(System.getProperty("user.home"), "JAT", sym + "_" + timeframe + ".txt");

                    // Write asynchronously to the file
                    JATInfoHandler.asyncWrite(sb, filePath);
                })
                .exceptionally(e -> {
                    JATbot.botLogger.error("Error during fetch and write process: {}", e.getMessage());
                    return null;
                });
    }

    public okhttp3.Call getStockBarsAsync(String symbols, String timeframe, Long limit,
            final ApiCallback<StockBarsResp> _callback)
            throws net.jacobpeterson.alpaca.openapi.marketdata.ApiException {
        // Set the start and end dates
        ZoneId zid = ZoneId.of("America/New_York");
        ZoneOffset zoffset = zid.getRules().getOffset(LocalDateTime.now());

        OffsetDateTime startDateTime = OffsetDateTime.of(2016, 01, 10, 9, 30, 0, 0, zoffset);
        // Adjust start date dynamically based on timeframe
        startDateTime = OffsetDateTime.of(2016, 01, 10, 9, 30, 0, 0, zoffset); // Earlier start for daily
        switch (timeframe) {
            case "1Day":
                startDateTime = OffsetDateTime.of(2016, 01, 10, 9, 30, 0, 0, zoffset); // Earlier start for daily
                break;
            case "15min":
                startDateTime = OffsetDateTime.now().minusDays(30).withOffsetSameLocal(zoffset); // For example, limit
                                                                                                 // to 30 days ago for
                                                                                                 // 15min data
                break;
            case "1min":
                startDateTime = OffsetDateTime.now().minusDays(7).withOffsetSameLocal(zoffset); // For example, limit to
                                                                                                // 7 days ago for 1min
                                                                                                // data
                break;
            case "4Hour":
                startDateTime = OffsetDateTime.of(2016, 01, 10, 9, 30, 0, 0, zoffset); // Earlier start for daily
                // startDateTime = startDateTime.minusDays(90); // For example, limit to 90 days
                // ago for 4Hour data
                break;
            case "1Week":
                startDateTime = OffsetDateTime.now().minusDays(365).withOffsetSameLocal(zoffset); // For example, limit
                                                                                                  // to 365 days ago for
                                                                                                  // 1Week data
            case "1Month":
                startDateTime = OffsetDateTime.of(2016, 01, 10, 9, 30, 0, 0, zoffset); // Earlier start for daily
            default:
                break;

        }

        return stockBarsAsync(symbols, timeframe, startDateTime, null, limit, StockAdjustment.ALL, null,
                StockFeed.IEX, null,
                null, null, _callback);
    }

    public okhttp3.Call getStockBarsWithOHLCDateAsync(String symbols, String timeframe, OHLCData od,
            final ApiCallback<StockBarsResp> _callback)
            throws net.jacobpeterson.alpaca.openapi.marketdata.ApiException {
        // Set the start and end dates
        ZoneId zid = ZoneId.of("America/New_York");
        ZoneOffset zoffset = zid.getRules().getOffset(LocalDateTime.now());
        OffsetDateTime startDateTime = OffsetDateTime.of(
                od.getDateTime().getYear(), od.getDateTime().getMonthValue(),
                od.getDateTime().getDayOfMonth(), od.getDateTime().getHour(),
                od.getDateTime().getMinute(), od.getDateTime().getSecond(),
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
                                double volume = result.getBar().getV().doubleValue();
                                OHLCData od = new OHLCData(date, open, high, low, close, volume);
                                od.symbol = sym;
                                future.complete(od);

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
     * Asynchronously fetches OHLC data for a specified symbol using an OHLCData
     * object
     * for the start date and returns it as an ObservableList.
     *
     * @param sym       the stock symbol
     * @param timeframe the timeframe of the bars (e.g., "1Min", "1Day")
     * @param od        the OHLCData object to use for the start date
     * @return a CompletableFuture that completes with an ObservableList of OHLCData
     */
    public CompletableFuture<ObservableList<OHLCData>> getBarsDataWithDateAsync(String sym, String timeframe,
            OHLCData od) {

        ObservableList<OHLCData> ohlcDataList = FXCollections.observableArrayList();
        // Make the asynchronous API call using the provided getStockBarsAsync method
        return CompletableFuture.supplyAsync(() -> {
            try {

                getStockBarsWithOHLCDateAsync(
                        sym, timeframe,
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
                                        double volume = bar.getV().doubleValue();

                                        OHLCData ohlcData = new OHLCData(date, open, high, low, close, volume);
                                        ohlcData.symbol = sym;
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
    public CompletableFuture<ObservableList<OHLCData>> getBarsDataAsync(String sym, String timeframe, AtomicInteger c) {
        CompletableFuture<ObservableList<OHLCData>> future = new CompletableFuture<>();
        ObservableList<OHLCData> ohlcDataList = FXCollections.observableArrayList();

        try {
            getStockBarsAsync(sym, timeframe, 1000L, new ApiCallback<StockBarsResp>() {
                @Override
                public void onSuccess(StockBarsResp result, int statusCode, Map<String, List<String>> responseHeaders) {
                    c.incrementAndGet();
                    result.getBars().forEach((symbol, barsList) -> {
                        barsList.forEach(bar -> {
                            OffsetDateTime timestamp = bar.getT();
                            LocalDateTime date = timestamp.toLocalDateTime();
                            double open = bar.getO();
                            double high = bar.getH();
                            double low = bar.getL();
                            double close = bar.getC();
                            double volume = bar.getV().doubleValue();

                            OHLCData ohlcData = new OHLCData(date, open, high, low, close, volume);
                            ohlcData.symbol = sym;
                            ohlcDataList.add(ohlcData);
                        });
                    });
                    JATbot.botLogger.info("Successfully fetched {} bars for {}{}", ohlcDataList.size(), sym, timeframe);
                    future.complete(ohlcDataList); // Complete the future with the data
                }

                @Override
                public void onFailure(net.jacobpeterson.alpaca.openapi.marketdata.ApiException e, int statusCode,
                        Map<String, List<String>> responseHeaders) {
                    JATbot.botLogger.error("Error fetching async bars data: {}", e.getMessage());
                    future.completeExceptionally(e); // Complete with exception if failure
                }

                @Override
                public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {
                }

                @Override
                public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {
                }
            });
        } catch (net.jacobpeterson.alpaca.openapi.marketdata.ApiException e) {
            JATbot.botLogger.error("Error initiating async bars data call: {}", e.getMessage());
            future.completeExceptionally(e); // Complete with exception if the API call fails
        }

        return future;
    }

    public okhttp3.Call getStockSnapshotsAsync(String symbols, final ApiCallback<Map<String, StockSnapshot>> _callback)
            throws ApiException {
        return stockSnapshotsAsync(symbols, StockFeed.IEX, null, _callback);
    }

    public CompletableFuture<Map<String, StockSnapshot>> getStockSnapshots(String symbols) {

        CompletableFuture<Map<String, StockSnapshot>> future = new CompletableFuture<>();
        try {
            getStockSnapshotsAsync(symbols, new ApiCallback<Map<String, StockSnapshot>>() {

                @Override
                public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
                    // TODO Auto-generated method stub
                    throw new UnsupportedOperationException("Unimplemented method 'onFailure'");
                }

                @Override
                public void onSuccess(Map<String, StockSnapshot> result, int statusCode,
                        Map<String, List<String>> responseHeaders) {
                    future.complete(result);
                }

                @Override
                public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {
                    // TODO Auto-generated method stub
                    throw new UnsupportedOperationException("Unimplemented method 'onUploadProgress'");
                }

                @Override
                public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {
                    // TODO Auto-generated method stub
                    throw new UnsupportedOperationException("Unimplemented method 'onDownloadProgress'");
                }
            });
        } catch (ApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return future;
    }

}
