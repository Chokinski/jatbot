package com.jat.jatbot.alpaca;

import net.jacobpeterson.alpaca.AlpacaAPI;



import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.bar.CryptoBarMessage;
import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.quote.CryptoQuoteMessage;
import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.crypto.model.trade.CryptoTradeMessage;

import net.jacobpeterson.alpaca.websocket.marketdata.streams.crypto.CryptoMarketDataListenerAdapter;

import net.jacobpeterson.alpaca.websocket.marketdata.streams.stock.StockMarketDataListenerAdapter;

import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model.bar.StockBarMessage;
import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model.quote.StockQuoteMessage;
import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model.trade.StockTradeMessage;
import net.jacobpeterson.alpaca.openapi.trader.model.Position;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.jat.ctfxplotsplus.OHLCChart;
import com.jat.ctfxplotsplus.OHLCData;
import com.jat.jatbot.JATbot;
import com.jat.jatbot.ai.robot;
import com.jat.jatbot.datahandlers.JATInfoHandler;

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
    protected StockListenerForBot slfb;
    protected CryptoListenerForBot clfb;
    public OHLCChart passchart;
    public robot r;
    @Autowired
    public AlpacaAPI ac;
    @Autowired
    public JATInfoHandler jai;
    public StreamListener() {
        if (this.passchart == null) {
            sml = new StockListener();
            cml = new CryptoListener();
            slfb = new StockListenerForBot(r);
            clfb = new CryptoListenerForBot(r);          
        }
        else{
        sml = new StockListener(passchart);
        cml = new CryptoListener(passchart);
        slfb = new StockListenerForBot(r);
        clfb = new CryptoListenerForBot(r);}
    }

    public void giveCharts(OHLCChart chart) {
    this.passchart = chart;
    sml.chart = chart;
    cml.chart = chart;
    
    }
public void giveRobot(robot r,AlpacaController ac) {
        this.r = r;
        sml.robot = r;
        cml.robot = r;
        slfb.robot = r;
        clfb.robot = r;
        slfb.ac = ac;
        clfb.ac = ac;
    }
    public void connectStockStream() {
        alpacaAPIStocks.stockMarketDataStream().connect();
        if (!alpacaAPIStocks.stockMarketDataStream().waitForAuthorization(5, TimeUnit.SECONDS)) 
        {JATbot.botLogger.error("Failed to authorize stock stream");}
        alpacaAPIStocks.stockMarketDataStream().setListener(sml);                                                            
    }
    public void connectStockRobotStream() {
        alpacaAPIStocks.stockMarketDataStream().connect();
        if (!alpacaAPIStocks.stockMarketDataStream().waitForAuthorization(5, TimeUnit.SECONDS)) 
        {JATbot.botLogger.error("Failed to authorize stock stream");}
        alpacaAPIStocks.stockMarketDataStream().setListener(slfb);                                                            
    }
    public void connectCryptoStream() {

        alpacaAPICrypto.cryptoMarketDataStream().connect();
        if (!alpacaAPICrypto.cryptoMarketDataStream().waitForAuthorization(3, TimeUnit.SECONDS))
        {JATbot.botLogger.error("Failed to authorize crypto stream");}
        alpacaAPICrypto.cryptoMarketDataStream().setListener(cml);                                                                                                                      
    }

    public void connectCryptoRobotStream() {

        alpacaAPICrypto.cryptoMarketDataStream().connect();
        if (!alpacaAPICrypto.cryptoMarketDataStream().waitForAuthorization(3, TimeUnit.SECONDS))
        {JATbot.botLogger.error("Failed to authorize crypto stream");}
        alpacaAPICrypto.cryptoMarketDataStream().setListener(clfb);                                                                                                                      
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
        JATbot.botLogger.info("Listening to stock trades: " + symbols);
        System.out.println("Listening to stock tradesss: " + symbols);
        alpacaAPIStocks.stockMarketDataStream().setMinuteBarSubscriptions(symbols);
    }
    public void listenToStockData(Set<String> symbols) {
        alpacaAPIStocks.stockMarketDataStream().setMinuteBarSubscriptions(symbols);
    }

    public void listenToCoinTrades(Set<String> symbols) {
        JATbot.botLogger.info("Listening to coin trades: " + symbols);
        System.out.println("Listening to coin tradesss: " + symbols);
        alpacaAPICrypto.cryptoMarketDataStream().setMinuteBarSubscriptions(symbols);
        
    }
    public void listenToCoinData(Set<String> symbols) {
        alpacaAPICrypto.cryptoMarketDataStream().setMinuteBarSubscriptions(symbols);
    }
    public void listenToQuotes(Set<String> symbols) {
        alpacaAPIStocks.stockMarketDataStream().setQuoteSubscriptions(symbols);
    }
    public class CryptoListener extends CryptoMarketDataListenerAdapter {
        public OHLCChart chart;
        @Autowired
        public robot robot;
        @Autowired
        public AlpacaController ac;
        public CryptoListener(OHLCChart c) {
        this.chart = c;
        }
        public CryptoListener() {
            
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
    public class CryptoListenerForBot extends CryptoMarketDataListenerAdapter {
        public OHLCChart chart;
        @Autowired
        public robot robot;
        @Autowired
        public AlpacaController ac;
        public CryptoListenerForBot(OHLCChart c) {
        this.chart = c;
        }
        public CryptoListenerForBot(robot robot) {
            this.robot = robot;  
            }
        @Override
        public void onDailyBar(CryptoBarMessage bar) {
            JATbot.botLogger.info("Daily Bar: " + bar);
            sendUpdate(bar);
        }
        @Override
        public void onMinuteBar(CryptoBarMessage bar) {
            JATbot.botLogger.info("Minute Bar MSG: " + bar.toString());
            robot.setCurrentMarketDate(bar.getTimestamp().toLocalDateTime());
            robot.setCurrentMarketPrice(bar.getClose());
            manageOrders(bar.getSymbol());
        }
        @Override
        public void onQuote(CryptoQuoteMessage quote) {
            JATbot.botLogger.info("Quote: " + quote);
            
        }
        @Override
        public void onTrade(CryptoTradeMessage trade) {
            JATbot.botLogger.info("Trade: " + trade);

        }
        public void manageOrders(String sym) {

            List<Position> positions = ac.getAllPositions();

            if (robot.getCurrentMarketPrice() < robot.getCurrentPredPrice()) {
                // Check if the order is already placed
                boolean orderExists = false;
                for (Position position : positions) {
                    if (position.getSymbol().equalsIgnoreCase(sym) && position.getSide().equalsIgnoreCase("long")) {
                        orderExists = true;
                        if (orderExists && Double.parseDouble(position.getUnrealizedPl()) < (Double.parseDouble(position.getCurrentPrice()) * 0.01)) {
                            ac.closePosition(position.getSymbol(),BigDecimal.valueOf(Double.parseDouble(position.getQty())));
                        }
                        break;
                    }
                
                }
                // If the order does not exist, place a new order
                if (!orderExists) {
                    ac.postCryptoOrder(sym, "400","buy");
                } else {
                    JATbot.botLogger.info("Order already exists for " + sym);
                }


            }
             else {
                JATbot.botLogger.info("No action needed for " + sym);

            }



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
        @Autowired
        public robot robot;
        @Autowired
        public AlpacaController ac;
        public StockListener(OHLCChart c) {
        this.chart = c;
        }
        public StockListener() {
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
    public class StockListenerForBot extends StockMarketDataListenerAdapter {
        public OHLCChart chart;
        @Autowired
        public robot robot;
        @Autowired
        public AlpacaController ac;
        public StockListenerForBot(OHLCChart c) {
        this.chart = c;
        }
        public StockListenerForBot(robot robot) {
        this.robot = robot;    
        }
        @Override
        public void onDailyBar(StockBarMessage bar) {
            JATbot.botLogger.info("Daily Bar: " + bar);
            sendUpdate(bar);
        }
        @Override
        public void onMinuteBar(StockBarMessage bar) {
            JATbot.botLogger.info("Minute Bar MSG: " + bar.toString());
            robot.setCurrentMarketDate(bar.getTimestamp().toLocalDateTime());
            robot.setCurrentMarketPrice(bar.getClose());
            manageOrders(bar.getSymbol());
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
        
        public void manageOrders(String sym) {

            List<Position> positions = ac.getAllPositions();

            if (robot.getCurrentMarketPrice() < robot.getCurrentPredPrice()) {
                // Check if the order is already placed
                boolean orderExists = false;
                for (Position position : positions) {
                    if (position.getSymbol().equalsIgnoreCase(sym) && position.getSide().equalsIgnoreCase("long")) {
                        orderExists = true;
                        if (orderExists && Double.parseDouble(position.getUnrealizedPl()) < (Double.parseDouble(position.getCurrentPrice()) * 0.01)) {
                            ac.closePosition(position.getSymbol(),BigDecimal.valueOf(Double.parseDouble(position.getQty())));
                        }
                    }
                }
                // If the order does not exist, place a new order
                if (!orderExists) {
                    ac.postOrder(sym, "400","buy");
                } else {
                    JATbot.botLogger.info("Order already exists for " + sym);
                }


            }
            
            if (robot.getCurrentMarketPrice() > robot.getCurrentPredPrice()) {
                // Check if the order is already placed
                boolean orderExists = false;
                for (Position position : positions) {
                    if (position.getSymbol().equalsIgnoreCase(sym) && position.getSide().equalsIgnoreCase("short")) {
                        orderExists = true;
                        if (orderExists && Double.parseDouble(position.getUnrealizedPl()) < (Double.parseDouble(position.getCurrentPrice()) * 0.01)) {
                            ac.closePosition(position.getSymbol(),BigDecimal.valueOf(Double.parseDouble(position.getQty())));
                        }
                    }
                }
                // If the order does not exist, place a new order
                if (!orderExists) {
                    ac.postOrder(sym, "400","sell");
                } else {
                    JATbot.botLogger.info("Order already exists for " + sym);
                }
            } else {
                JATbot.botLogger.info("No action needed for " + sym);





            }



        }

        public void checkIfPriceIsNear(StockTradeMessage msgData) {
            double thresholdPercentage = 0.01;  // 1% margin
            double actualPrice = msgData.getPrice();
            String symbol = msgData.getSymbol();
            LocalDateTime timestamp = msgData.getTimestamp().toLocalDateTime();
            
            // Iterate through the predictions
            for (Map.Entry<LocalDateTime, Double> data : jai.dataFromPredictionFile(symbol).entrySet()) {
                // Check if the predicted date matches the actual date
                if (data.getKey().getDayOfMonth() == timestamp.getDayOfMonth() &&
                    data.getKey().getMonth() == timestamp.getMonth() &&  // Also check for the same month
                    data.getKey().getYear() == timestamp.getYear()) {  // Check for the same year
                    double predictedPrice = data.getValue();
                    // Calculate the 1% margin
                    double priceDifference = Math.abs(predictedPrice - actualPrice);
                    double allowedDifference = actualPrice * thresholdPercentage;
        
                    // Check if the predicted price is within 1% of the actual price
                    if (priceDifference <= allowedDifference) {
                        // Output the result if the price is near
                        JATbot.botLogger.info("Actual price: " + actualPrice +
                                              " is near predicted price: " + predictedPrice +
                                              " on " + timestamp);
                    }
                }
            }
        }

    }
    
 }
