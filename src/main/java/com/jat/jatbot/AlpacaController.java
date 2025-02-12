package com.jat.jatbot;

import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.openapi.trader.model.*;

import net.jacobpeterson.alpaca.model.util.apitype.MarketDataWebsocketSourceType;
import net.jacobpeterson.alpaca.model.util.apitype.TraderAPIEndpointType;

import okhttp3.OkHttpClient;




import java.math.BigDecimal;

import java.time.ZoneId;
import java.time.ZoneOffset;

import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.jacobpeterson.alpaca.openapi.trader.ApiException;

import net.jacobpeterson.alpaca.openapi.marketdata.model.*;


import com.jat.ctfxplotsplus.OHLCData;

/**
 * The AlpacaController class provides methods to interact with the Alpaca API for trading and market data.
 * It extends the AlpacaAPI class and includes various methods to retrieve account information, place trades,
 * and get market data.
 * <p>Dependencies:
 * <p>
 * - JATInfoHandler: Handles loading properties and handles information asynchronously.
 * <p>
 * - AlpacaAPI: Used to interact with the Alpaca API.
 * <p>
 * - AlpacaController: Controller for Alpaca-specific operations.
 * <p>
 * - OkHttpClient: HTTP client for making API requests.
 * 
 * <p>Methods included in this class:
 * <ul>
 *   <li>{@link #AlpacaController()}: Constructor to initialize the AlpacaController with API credentials and endpoints.</li>
 *   <li>{@link #connect()}: Connects to the Alpaca API using the provided credentials and endpoints.</li>
 *   <li>{@link #getAccount()}: Retrieves the account information from the Alpaca API.</li>
 *   <li>{@link #getAccID()}: Retrieves the account ID.</li>
 *   <li>{@link #getAccCash()}: Retrieves the cash balance of the account.</li>
 *   <li>{@link #getPortValue()}: Retrieves the portfolio value of the account.</li>
 *   <li>{@link #getAccStatus()}: Retrieves the status of the account.</li>
 *   <li>{@link #getCreateDate()}: Retrieves the creation date of the account.</li>
 *   <li>{@link #getBuyingPower()}: Retrieves the buying power of the account.</li>
 *   <li>{@link #getLongMarketValue()}: Retrieves the long market value of the account.</li>
 *   <li>{@link #getDayTradeLimit()}: Retrieves the day trade limit of the account.</li>
 *   <li>{@link #getShortMarketValue()}: Retrieves the short market value of the account.</li>
 *   <li>{@link #getEquity()}: Retrieves the equity of the account.</li>
 *   <li>{@link #getLastEquity()}: Retrieves the last equity of the account.</li>
 *   <li>{@link #getInitialMargin()}: Retrieves the initial margin of the account.</li>
 *   <li>{@link #getMaintenanceMargin()}: Retrieves the maintenance margin of the account.</li>
 *   <li>{@link #getLastMaintenanceMargin()}: Retrieves the last maintenance margin of the account.</li>
 *   <li>{@link #getDayTradeCount()}: Retrieves the day trade count of the account.</li>
 *   <li>{@link #getCurrency()}: Retrieves the currency of the account.</li>
 *   <li>{@link #getMarketTime()}: Retrieves the current market time.</li>
 *   <li>{@link #placeTrade(String, String, OrderSide, OrderType, TimeInForce)}: Places a trade order.</li>
 *   <li>{@link #closeTrade(String, BigDecimal, BigDecimal)}: Closes an open trade position.</li>
 *   <li>{@link #getAssets()}: Retrieves a list of assets.</li>
 *   <li>{@link #getBarsData(String, int, int, int, int, int, int, String)}: Retrieves OHLC data for a specific symbol.</li>
 *   <li>{@link #getOkHttpClient()}: Retrieves the OkHttpClient instance used for API requests.</li>
 * </ul>
 * 
 * <p>Note: This class requires valid Alpaca API credentials and proper configuration to function correctly.
 */
@Service
public class AlpacaController extends AlpacaAPI {
    public AlpacaAPI alpaca;
    private OkHttpClient okClient;
    private static JATInfoHandler infoHandler = new JATInfoHandler();
    static String[]  props = infoHandler.loadProperties();
    @Autowired
    public AlpacaStockHandler stockH;
    @Autowired
    public AlpacaCryptoHandler cryptoH;
    @Autowired
    public AlpacaAssetHandler assetH;
    
    public AlpacaController(AlpacaAPI alpacaAPI,AlpacaStockHandler sH, AlpacaCryptoHandler cH, AlpacaAssetHandler aH) {
        super(props[0], props[1], TraderAPIEndpointType.valueOf(props[2]),
        MarketDataWebsocketSourceType.valueOf(props[3]),new OkHttpClient());
        this.alpaca = alpacaAPI;
        okClient = this.getOkHttpClient();
        this.assetH = aH;
        this.cryptoH = cH;
        this.stockH = sH;
        
    }



    public AlpacaAPI connect() {
        
        alpaca = new AlpacaAPI(props[0], props[1], TraderAPIEndpointType.valueOf(props[2]),
                MarketDataWebsocketSourceType.valueOf(props[3]), okClient);
        // alpaca.getOkHttpClient();
        return alpaca;

    }

    public Account getAccount() {

        return CompletableFuture.supplyAsync(()->{
            try {
                return alpaca.trader().accounts().getAccount();
            } catch (net.jacobpeterson.alpaca.openapi.trader.ApiException exception) {
                JATbot.botLogger.error("Error getting acc info: " + exception.getMessage());
                return null;
            }
        }).join();
    }

    public String getAccID() {
        return CompletableFuture.supplyAsync(()->{

            return getAccount().getAccountNumber();
        }).join();
        
 
    }

    public String getAccCash() {

        return CompletableFuture.supplyAsync(()->{
  
            return getAccount().getCash();
        }).join();
    }

    public String getPortValue() {

        return CompletableFuture.supplyAsync(()->{
  
            return getAccount().getPortfolioValue();
        }).join();

    }

    public String getAccStatus() {

        
        return CompletableFuture.supplyAsync(()->{
            return getAccount().getStatus().toString();
        }).join();

    }
  
    public String getCreateDate() {


        return CompletableFuture.supplyAsync(()->{
                return getAccount().getCreatedAt().format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
        }).join();

    }

    public String getBuyingPower() {

        return CompletableFuture.supplyAsync(()->{
            return getAccount().getBuyingPower();


        }).join();

    }

    public String getLongMarketValue() {


        return CompletableFuture.supplyAsync(()->{


            return getAccount().getLongMarketValue();


        }).join();

    }

    public String getDayTradeLimit() {

        
        return CompletableFuture.supplyAsync(()->{

            return Integer.toString(getAccount().getDaytradeCount());


            
        }).join();

    }

    public String getShortMarketValue() {

        
        return CompletableFuture.supplyAsync(()->{


            return getAccount().getShortMarketValue();

            
        }).join();

    }

    public String getEquity() {

        return CompletableFuture.supplyAsync(()->{


            return getAccount().getEquity();

            
        }).join();

    }

    public String getLastEquity() {

        return CompletableFuture.supplyAsync(()->{


            return getAccount().getLastEquity();

            
        }).join();

    }


    public String getInitialMargin() {

        
        return CompletableFuture.supplyAsync(()->{


            return getAccount().getInitialMargin();

            
        }).join();

    }

    public String getMaintenanceMargin() {

        
        return CompletableFuture.supplyAsync(()->{


            return getAccount().getMaintenanceMargin();

            
        }).join();

    }

    public String getLastMaintenanceMargin() {

        
        return CompletableFuture.supplyAsync(()->{


            return getAccount().getLastMaintenanceMargin();

            
        }).join();

    }

    public String getDayTradeCount() {

        return CompletableFuture.supplyAsync(()->{


            return Integer.toString(getAccount().getDaytradeCount());

            
        }).join();

    }

    public String getCurrency() {

    
        return CompletableFuture.supplyAsync(()->{


            return getAccount().getCurrency();

            
        }).join();
    }

    public Clock getMarketTime() {


        return CompletableFuture.supplyAsync(()->{
            try {
                // Get the market 'Clock' and print it out
                Clock clock = alpaca.trader().clock().getClock();
                return clock;
            } catch (ApiException exception) {
                exception.printStackTrace();
                return null;
            }    
        }).join();
    }

    public void placeTrade(String sym, String vol, OrderSide side, OrderType type, TimeInForce tif) {
        try {
            Order newOrder = alpaca.trader().orders().postOrder(new PostOrderRequest().symbol(sym).qty(vol).side(side)
                    .type(OrderType.MARKET).timeInForce(TimeInForce.DAY));
            JATbot.botLogger.info("Order placed: {}", newOrder);
        } catch (ApiException exception) {
            JATbot.botLogger.error("Error placing order: " + exception.getMessage());
        }
    }

    public void closeTrade(String sym, BigDecimal qty, BigDecimal perc) {

        try {
            Order closingorder = alpaca.trader().positions().deleteOpenPosition(sym, qty, perc);
            JATbot.botLogger.info("Order closed: {}", closingorder);
        } catch (ApiException exception) {
            JATbot.botLogger.error("Error closing order: " + exception.getMessage());
        }

    }

    public List<Assets> getAssets() {
        return assetH.getAssets();
        
    }



    /**
     * Represents a series of Open, High, Low, Close (OHLC) data for a specific
     * symbol.
     * 
     * @throws net.jacobpeterson.alpaca.openapi.marketdata.ApiException
     */
    
    public ObservableList<OHLCData> getBarsData(String sym, int stYr, int stMo, int stDay,
            int endYr, int endMo, int endDay, String timeframe)
            throws net.jacobpeterson.alpaca.openapi.marketdata.ApiException {
        ObservableList<OHLCData> ohlcDataList = FXCollections.observableArrayList();
        StockBarsResp barsResponse;

        try {
            LocalDateTime now = LocalDateTime.now();
            ZoneId zid = ZoneId.of("America/New_York");
            ZoneOffset zoffset = zid.getRules().getOffset(now);
            OffsetDateTime startDateTime = OffsetDateTime.of(stYr, stMo, stDay, 9, 30, 0, 0, zoffset);
            OffsetDateTime endDateTime = OffsetDateTime.of(endYr, endMo, endDay, 16, 0, 0, 0, zoffset);

            barsResponse = alpaca.marketData().stock().stockBars(
                    sym,
                    timeframe,
                    startDateTime,
                    endDateTime,
                    10000L, // Limiting to 10000 bars, changed to Long
                    StockAdjustment.SPLIT,
                    null,
                    StockFeed.IEX,
                    null,
                    null,
                    Sort.ASC);

            barsResponse.getBars().forEach((symbol, barsList) -> {
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
                });
            });
        } catch (net.jacobpeterson.alpaca.openapi.marketdata.ApiException e) {
            JATbot.botLogger.error("Error getting bars data: {}", e.getMessage());
        }

        return ohlcDataList;
    }





    public OkHttpClient getOkHttpClient() {
        return okClient;
    }

}