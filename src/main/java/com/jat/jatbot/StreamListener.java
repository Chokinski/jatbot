package com.jat.jatbot;

import net.jacobpeterson.alpaca.AlpacaAPI;



import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.bar.CryptoBarMessage;
import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.quote.CryptoQuoteMessage;
import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.trade.CryptoTradeMessage;

import net.jacobpeterson.alpaca.websocket.marketdata.streams.crypto.CryptoMarketDataListenerAdapter;

import net.jacobpeterson.alpaca.websocket.marketdata.streams.stock.StockMarketDataListenerAdapter;

import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model.bar.StockBarMessage;
import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model.quote.StockQuoteMessage;
import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model.trade.StockTradeMessage;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jat.ctfxplotsplus.OHLCChart;
import com.jat.ctfxplotsplus.OHLCData;

/**
 * This class represents a StreamListener that extends StockMarketDataWebsocket and implements MarketDataListener.
 * It is responsible for handling market data streaming and WebSocket events.
 */
@Service
 public class StreamListener  {
    @Autowired
    protected AlpacaAPI alpacaAPICrypto;
    @Autowired
    protected AlpacaAPI alpacaAPIStocks;
    protected StockListener sml;
    protected CryptoListener cml;
    public OHLCChart passchart;
    @Autowired
    public AlpacaAPI ac;
    public StreamListener() {
        
        
        sml = new StockListener(passchart);
        cml = new CryptoListener(passchart);
        

  
    }
    public void giveCharts(OHLCChart chart) {
    this.passchart = chart;
    sml.chart = chart;
    cml.chart = chart;
    
    }
    public void connectStockStream() {
        alpacaAPIStocks.stockMarketDataStream().connect();
        if (!alpacaAPIStocks.stockMarketDataStream().waitForAuthorization(3, TimeUnit.SECONDS)) 
        {JATbot.botLogger.error("Failed to authorize stock stream");}
        alpacaAPIStocks.stockMarketDataStream().setListener(sml);                                                            
    }

    public void connectCryptoStream() {

        alpacaAPICrypto.cryptoMarketDataStream().connect();
        if (!alpacaAPICrypto.cryptoMarketDataStream().waitForAuthorization(3, TimeUnit.SECONDS))
        {JATbot.botLogger.error("Failed to authorize crypto stream");}
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

    public void listenToStockTrades(Set<String> symbols) {
        alpacaAPIStocks.stockMarketDataStream().setTradeSubscriptions(symbols);
    }

    public void listenToCoinTrades(Set<String> symbols) {
        alpacaAPICrypto.cryptoMarketDataStream().setTradeSubscriptions(symbols);
    }
    public void listenToCoinData(Set<String> symbols) {
        alpacaAPICrypto.cryptoMarketDataStream().setMinuteBarSubscriptions(symbols);
    }

    public class CryptoListener extends CryptoMarketDataListenerAdapter {
        public OHLCChart chart;
        public CryptoListener(OHLCChart c) {
        this.chart = c;
        }
        @Override
        public void onDailyBar(CryptoBarMessage bar) {
            JATbot.botLogger.info("Daily Bar: " + bar);
            sendUpdate(bar);
        }
        @Override
        public void onMinuteBar(CryptoBarMessage bar) {
            JATbot.botLogger.info("Minute Bar MSG: " + bar.toString());
            sendUpdate(bar);
        }
        @Override
        public void onQuote(CryptoQuoteMessage quote) {
            JATbot.botLogger.info("Quote: " + quote);
        }
        @Override
        public void onTrade(CryptoTradeMessage trade) {
            JATbot.botLogger.info("Trade: " + trade);
            
        }

        public void sendUpdate(CryptoBarMessage msgData) {
            OHLCData od = new OHLCData(msgData.getTimestamp().toLocalDateTime(), msgData.getOpen(), msgData.getHigh(), msgData.getLow(), msgData.getClose(), msgData.getVolume());
            od.symbol = msgData.getSymbol();
            System.out.println("Sending update from ["+ msgData.getSymbol()+"] data.");
            chart.updateData(od);
            
            
        }
        


    }

    public class StockListener extends StockMarketDataListenerAdapter {
        public OHLCChart chart;
        public StockListener(OHLCChart c) {
        this.chart = c;
        }
        @Override
        public void onDailyBar(StockBarMessage bar) {
            JATbot.botLogger.info("Daily Bar: " + bar);
            sendUpdate(bar);
        }
        @Override
        public void onMinuteBar(StockBarMessage bar) {
            JATbot.botLogger.info("Minute Bar MSG: " + bar.toString());
            sendUpdate(bar);
        }
        @Override
        public void onQuote(StockQuoteMessage quote) {
            JATbot.botLogger.info("Quote: " + quote);
        }
        @Override
        public void onTrade(StockTradeMessage trade) {
            JATbot.botLogger.info("Trade: " + trade);
        }
        
        public void sendUpdate(StockBarMessage msgData) {
            OHLCData od = new OHLCData(msgData.getTimestamp().toLocalDateTime(), msgData.getOpen(), msgData.getHigh(), msgData.getLow(), msgData.getClose(), msgData.getVolume());
            od.symbol = msgData.getSymbol();
            
            chart.updateData(od);
            
            
        }

    }

}