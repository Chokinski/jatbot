package JAT;

import net.jacobpeterson.alpaca.AlpacaAPI;


import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.*;
import net.jacobpeterson.alpaca.websocket.marketdata.streams.crypto.CryptoMarketDataListener;
import net.jacobpeterson.alpaca.websocket.marketdata.streams.crypto.CryptoMarketDataListenerAdapter;
import net.jacobpeterson.alpaca.websocket.marketdata.streams.stock.StockMarketDataListener;
import net.jacobpeterson.alpaca.websocket.marketdata.streams.stock.StockMarketDataListenerAdapter;
import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model.*;


import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * This class represents a StreamListener that extends StockMarketDataWebsocket and implements MarketDataListener.
 * It is responsible for handling market data streaming and WebSocket events.
 */
 public class StreamListener  {

    protected AlpacaAPI alpacaAPICrypto;
    protected AlpacaAPI alpacaAPIStocks;
    protected StockMarketDataListener sml;
    protected CryptoMarketDataListener cml;
    public StreamListener() {

        
        sml = new StockMarketDataListenerAdapter();
        cml = new CryptoMarketDataListenerAdapter();
        AlpacaController ac = new AlpacaController();
        this.alpacaAPIStocks = ac.connect();
        this.alpacaAPICrypto = ac.connect();
    }

    public void connectStockStream() {
        alpacaAPIStocks.stockMarketDataStream().connect();
        if (!alpacaAPIStocks.stockMarketDataStream().waitForAuthorization(3, TimeUnit.SECONDS)) 
        {throw new RuntimeException("Failed to authorize stock stream");}
        alpacaAPIStocks.stockMarketDataStream().setListener(sml);                                                            
    }

    public void connectCryptoStream() {

        alpacaAPICrypto.cryptoMarketDataStream().connect();
        if (!alpacaAPICrypto.cryptoMarketDataStream().waitForAuthorization(3, TimeUnit.SECONDS))
        {throw new RuntimeException("Failed to authorize crypto stream");}
        alpacaAPICrypto.cryptoMarketDataStream().setListener(cml);                                                                                                                      
    }

    public boolean[] areStreamsConnected() {

        if (alpacaAPIStocks.stockMarketDataStream().isConnected() && alpacaAPICrypto.cryptoMarketDataStream().isConnected()) {
            return new boolean[]{true, true};
        } else if (alpacaAPIStocks.stockMarketDataStream().isConnected() && !alpacaAPICrypto.cryptoMarketDataStream().isConnected()) {
            
            return new boolean[]{true, false};
        } else if (!alpacaAPIStocks.stockMarketDataStream().isConnected() && alpacaAPICrypto.cryptoMarketDataStream().isConnected()) {
            return new boolean[]{false, true};
        }
        return null;
    }

    public void disconnectStream() {
        alpacaAPIStocks.stockMarketDataStream().disconnect();
        alpacaAPICrypto.cryptoMarketDataStream().disconnect();
    }

    public void disconnectAlpacaAPI() {
        alpacaAPIStocks.getOkHttpClient().dispatcher().cancelAll();
        alpacaAPIStocks.getOkHttpClient().connectionPool().evictAll();
        alpacaAPICrypto.getOkHttpClient().dispatcher().cancelAll();
        alpacaAPICrypto.getOkHttpClient().connectionPool().evictAll();

        try {
            alpacaAPICrypto.getOkHttpClient().dispatcher().executorService().awaitTermination(2, TimeUnit.SECONDS);
            alpacaAPIStocks.getOkHttpClient().dispatcher().executorService().awaitTermination(2, TimeUnit.SECONDS);
            alpacaAPICrypto.getOkHttpClient().dispatcher().executorService().shutdown();
            alpacaAPIStocks.getOkHttpClient().dispatcher().executorService().shutdown();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            JATbot.botLogger.error(e.getMessage());
        }

    }

    public void listenToStock(Set<String> symbols) {
        alpacaAPIStocks.stockMarketDataStream().setTradeSubscriptions(symbols);
    }

    public void listenToCoin(Set<String> symbols) {
        alpacaAPICrypto.cryptoMarketDataStream().setTradeSubscriptions(symbols);
    }


    public String[] getMessages(CryptoMarketDataMessageType msgType, CryptoMarketDataMessage msg){
        return new String[]{msgType.name(), msg.toString()};
    }

    public String[] getMessages(StockMarketDataMessageType msgType, StockMarketDataMessage msg){
        return new String[]{msgType.name(), msg.toString()};
    }


}