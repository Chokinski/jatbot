package com.jat.jatbot;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;


import net.jacobpeterson.alpaca.openapi.trader.ApiCallback;
import net.jacobpeterson.alpaca.openapi.trader.ApiClient;
import net.jacobpeterson.alpaca.openapi.trader.ApiException;
import net.jacobpeterson.alpaca.openapi.trader.api.AssetsApi;
import net.jacobpeterson.alpaca.openapi.trader.model.Assets;

public class AlpacaAssetHandler extends AssetsApi{

public AlpacaAssetHandler(ApiClient ac) {
    super(ac);
}

    public okhttp3.Call getWrappedAssetAsync(String symbol,
    final net.jacobpeterson.alpaca.openapi.trader.ApiCallback<Assets> _callback) throws ApiException
    {
        return getV2AssetsSymbolOrAssetIdAsync(symbol,_callback);

    }

    public CompletableFuture<Assets> getAssetAsync(String sym, AtomicInteger count) {

        CompletableFuture<Assets> future = new CompletableFuture<>();
        return CompletableFuture.supplyAsync(()-> {
            try {
            getWrappedAssetAsync(sym,
            new ApiCallback<Assets>() {

                @Override
                public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
                    // TODO Auto-generated method stub
                    future.completeExceptionally(e);
                }

                @Override
                public void onSuccess(Assets result, int statusCode, Map<String, List<String>> responseHeaders) {
                    count.incrementAndGet();
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
        return future.join();
    });



        
    }

    public CompletableFuture<Map<String, int[]>> getAssetWithInfo(String sym, AlpacaStockHandler ash,AtomicInteger c) {
        CompletableFuture<Map<String, int[]>> future = new CompletableFuture<>();
        Map<String, int[]> symInfo = new HashMap<>();
        c.incrementAndGet();
        ash.getLatestBarDataAsync(sym).thenAccept(latestBar -> {
            // Convert OHLC data into the desired format (int array)
            symInfo.put(sym.toUpperCase(), new int[] {
                (int) latestBar.getClose(),  // Open price as integer
                (int) latestBar.getHigh(),  // High price as integer
                (int) latestBar.getLow(),   // Low price as integer
            });
    
            // Complete the future with the populated map
            future.complete(symInfo);
        }).exceptionally(e -> {
            // Handle errors and complete the future exceptionally
            future.completeExceptionally(e);
            return null;
        });
    
        return future;
    }
    }