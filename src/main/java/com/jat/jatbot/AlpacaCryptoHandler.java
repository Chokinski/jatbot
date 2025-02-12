package com.jat.jatbot;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import com.jat.ctfxplotsplus.OHLCData;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.jacobpeterson.alpaca.openapi.marketdata.ApiCallback;
import net.jacobpeterson.alpaca.openapi.marketdata.ApiClient;
import net.jacobpeterson.alpaca.openapi.marketdata.ApiException;
import net.jacobpeterson.alpaca.openapi.marketdata.api.CryptoApi;
import net.jacobpeterson.alpaca.openapi.marketdata.model.CryptoBar;
import net.jacobpeterson.alpaca.openapi.marketdata.model.CryptoBarsResp;
import net.jacobpeterson.alpaca.openapi.marketdata.model.CryptoLatestBarsResp;
import net.jacobpeterson.alpaca.openapi.marketdata.model.CryptoLoc;
import net.jacobpeterson.alpaca.openapi.marketdata.model.CryptoSnapshotsResp;

@Service
public class AlpacaCryptoHandler extends CryptoApi {



    public AlpacaCryptoHandler(ApiClient ac) {
        super(ac);

    }

    public okhttp3.Call getCryptoBarsAsync(String symbols, String timeframe, Long limit,
            final ApiCallback<CryptoBarsResp> _callback)
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

        return cryptoBarsAsync(CryptoLoc.US, symbols, timeframe, startDateTime, null, limit, null, null,
                _callback);
    }

    public okhttp3.Call getCryptoBarsWithOHLCDateAsync(String symbols, String timeframe, OHLCData od,
            final ApiCallback<CryptoBarsResp> _callback)
            throws net.jacobpeterson.alpaca.openapi.marketdata.ApiException {
        // Set the start and end dates
        ZoneId zid = ZoneId.of("America/New_York");
        ZoneOffset zoffset = zid.getRules().getOffset(LocalDateTime.now());
        OffsetDateTime startDateTime = OffsetDateTime.of(
                od.getDateTime().getYear(), od.getDateTime().getMonthValue(),
                od.getDateTime().getDayOfMonth(), od.getDateTime().getHour(),
                od.getDateTime().getMinute(), od.getDateTime().getSecond(),
                0, zoffset);
        return cryptoBarsAsync(CryptoLoc.US, symbols, timeframe, startDateTime, null, 1000L, null, null,
                _callback);
    }

    public okhttp3.Call getLatestCryptobarAsync(String symbol,
            final ApiCallback<CryptoLatestBarsResp> _callback) throws ApiException {

        return cryptoLatestBarsAsync(CryptoLoc.US, symbol, _callback);
    }

    /**
     * Asynchronously fetches the latest OHLC data for a specified symbol and
     * returns it as an
     * ObservableList.
     *
     * @param sym  the crypto symbol
     * @param feed the feed to use for the data
     * @return a CompletableFuture that completes with an ObservableList of OHLCData
     */
    public CompletableFuture<OHLCData> getLatestBarDataAsync(String sym) {
        CompletableFuture<OHLCData> future = new CompletableFuture<>();
        // Make the asynchronous API call using the provided getcryptoBarsAsync method
        return CompletableFuture.supplyAsync(() -> {
            try {

                getLatestCryptobarAsync(
                        sym,
                        new ApiCallback<CryptoLatestBarsResp>() {

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
                            public void onSuccess(CryptoLatestBarsResp result, int statusCode,
                                    Map<String, List<String>> responseHeaders) {

                                // Populate ohlcDataList with the response data
                                CryptoBar bar = result.getBars().get(sym);
                                OffsetDateTime timestamp = bar.getT();
                                LocalDateTime date = timestamp.toLocalDateTime();

                                double high = bar.getH();
                                double open = bar.getO();
                                double low = bar.getL();
                                double close = bar.getC();
                                double volume = bar.getV();
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
     * @param sym       the crypto symbol
     * @param timeframe the timeframe of the bars (e.g., "1Min", "1Day")
     * @param od        the OHLCData object to use for the start date
     * @return a CompletableFuture that completes with an ObservableList of OHLCData
     */
    public CompletableFuture<ObservableList<OHLCData>> getBarsDataWithDateAsync(String sym, String timeframe,
            OHLCData od) {

        ObservableList<OHLCData> ohlcDataList = FXCollections.observableArrayList();
        // Make the asynchronous API call using the provided getcryptoBarsAsync method
        return CompletableFuture.supplyAsync(() -> {
            try {

                getCryptoBarsWithOHLCDateAsync(
                        sym, timeframe,
                        od,
                        new ApiCallback<CryptoBarsResp>() {

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
                            public void onSuccess(CryptoBarsResp result, int statusCode,
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
                                        double volume = bar.getV();

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
     * @param sym       the crypto symbol
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
            getCryptoBarsAsync(sym, timeframe, 1000L, new ApiCallback<CryptoBarsResp>() {
                @Override
                public void onSuccess(CryptoBarsResp result, int statusCode,
                        Map<String, List<String>> responseHeaders) {
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

    public okhttp3.Call getCryptoSnapshotsAsync(String symbols, final ApiCallback<CryptoSnapshotsResp> _callback)
            throws ApiException {
        return cryptoSnapshotsAsync(symbols, CryptoLoc.US, _callback);
    }

    public CompletableFuture<CryptoSnapshotsResp> getCryptoSnapshots(String symbols) {

        CompletableFuture<CryptoSnapshotsResp> future = new CompletableFuture<>();
        try {
            getCryptoSnapshotsAsync(symbols, new ApiCallback<CryptoSnapshotsResp>() {

                @Override
                public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
                    // TODO Auto-generated method stub
                    throw new UnsupportedOperationException("Unimplemented method 'onFailure'");
                }

                @Override
                public void onSuccess(CryptoSnapshotsResp result, int statusCode,
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
