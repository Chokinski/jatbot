package JAT;

import net.jacobpeterson.alpaca.AlpacaAPI;

import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.realtime.MarketDataMessage;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.realtime.enums.MarketDataMessageType;

import net.jacobpeterson.alpaca.websocket.marketdata.MarketDataListener;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This class represents a StreamListener that extends StockMarketDataWebsocket and implements MarketDataListener.
 * It is responsible for handling market data streaming and WebSocket events.
 */
public class StreamListener implements MarketDataListener {
    protected AlpacaAPI alpacaAPICrypto;
    protected AlpacaAPI alpacaAPIStocks;
    public StreamListener() {
        AlpacaController ac = new AlpacaController();
        this.alpacaAPIStocks = ac.connect();
        this.alpacaAPICrypto = ac.connect();
    }

    public void connectStream() {
        alpacaAPICrypto.cryptoMarketDataStreaming().subscribeToControl(MarketDataMessageType.ERROR,
                MarketDataMessageType.SUBSCRIPTION,
                MarketDataMessageType.SUCCESS, MarketDataMessageType.TRADE);
        alpacaAPIStocks.stockMarketDataStreaming().subscribeToControl(MarketDataMessageType.ERROR,
                MarketDataMessageType.SUBSCRIPTION,
                MarketDataMessageType.SUCCESS,MarketDataMessageType.TRADE);
                                                                                                                                 
        alpacaAPIStocks.stockMarketDataStreaming().connect();
        alpacaAPICrypto.cryptoMarketDataStreaming().connect();
    }

    public void connectCryptoStream() {
        alpacaAPICrypto.cryptoMarketDataStreaming().subscribeToControl(MarketDataMessageType.ERROR,
                MarketDataMessageType.SUBSCRIPTION,
                MarketDataMessageType.SUCCESS, MarketDataMessageType.TRADE);                                                                                                                           
        alpacaAPICrypto.cryptoMarketDataStreaming().connect();
    }

    public boolean[] areStreamsConnected() {

        if (alpacaAPIStocks.stockMarketDataStreaming().isConnected() && alpacaAPICrypto.cryptoMarketDataStreaming().isConnected()) {
            return new boolean[]{true, true};
        } else if (alpacaAPIStocks.stockMarketDataStreaming().isConnected() && !alpacaAPICrypto.cryptoMarketDataStreaming().isConnected()) {
            return new boolean[]{true, false};
        } else if (!alpacaAPIStocks.stockMarketDataStreaming().isConnected() && alpacaAPICrypto.cryptoMarketDataStreaming().isConnected()) {
            return new boolean[]{false, true};
        }
        return null;
    }


    public void disconnectStream() {
        alpacaAPIStocks.stockMarketDataStreaming().disconnect();
        alpacaAPICrypto.cryptoMarketDataStreaming().disconnect();
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
            e.printStackTrace();
        }

    }

    public void listenToStock(List<String> symbols) {
        alpacaAPIStocks.stockMarketDataStreaming().subscribe(symbols, null, Arrays.asList("*"));
    }

    public void listenToCoin(List<String> symbols) {
        alpacaAPICrypto.cryptoMarketDataStreaming().subscribe(null, symbols, null);
    }
    @Override
    public void onMessage(MarketDataMessageType messageType, MarketDataMessage message) {
        JATbot.botLogger.info("Received {} : {}", messageType.name(), message);
        DashController dc = new DashController();
        dc.updateTokenDisplay(String.format("Received %s \n%s",messageType, message));

    }

    public String[] getMessages(MarketDataMessageType msgType, MarketDataMessage msg){
        return new String[]{msgType.name(), msg.toString()};
    }
    

}