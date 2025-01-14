package JAT;

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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.jacobpeterson.alpaca.openapi.trader.ApiException;
import net.jacobpeterson.alpaca.openapi.broker.model.Asset;
import net.jacobpeterson.alpaca.openapi.marketdata.ApiCallback;
import net.jacobpeterson.alpaca.openapi.marketdata.model.*;


import com.jat.OHLCData;

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

public class AlpacaController extends AlpacaAPI {
    public AlpacaAPI alpaca;
    private OkHttpClient okClient;
    private static JATInfoHandler infoHandler = new JATInfoHandler();
    static String[]  props = infoHandler.loadProperties();
    public AlpacaStockHandler stockH;
    public AlpacaAssetHandler assetH;
    public AlpacaController() {
        super(props[0], props[1], TraderAPIEndpointType.valueOf(props[2]),
        MarketDataWebsocketSourceType.valueOf(props[3]),new OkHttpClient());
        this.alpaca = this;
        okClient = this.getOkHttpClient();
        stockH = new AlpacaStockHandler(this.marketData().getInternalAPIClient());
        assetH = new AlpacaAssetHandler(this.trader().getInternalAPIClient());
    }



    public AlpacaAPI connect() {
        
        alpaca = new AlpacaAPI(props[0], props[1], TraderAPIEndpointType.valueOf(props[2]),
                MarketDataWebsocketSourceType.valueOf(props[3]), okClient);
        // alpaca.getOkHttpClient();
        return alpaca;

    }

    public Account getAccount() {
        try {
            // Get 'Account' information
            Account account = alpaca.trader().accounts().getAccount();
            return account;
        } catch (net.jacobpeterson.alpaca.openapi.trader.ApiException exception) {
            JATbot.botLogger.error("Error getting acc info: " + exception.getMessage());
            return null;
        }

    }

    public String getAccID() {
        String accID = getAccount().getAccountNumber();
        // JATbot.botLogger.info("Account ID: {}", accID);
        JATbot.botLogger.info("Account ID Logged" + "\n");
        return accID;
    }

    public String getAccCash() {
        String cash = getAccount().getCash();
        // JATbot.botLogger.info("Account Cash: {}", cash);
        JATbot.botLogger.info("Cash Logged" + "\n");
        return cash;
    }

    public String getPortValue() {
        String portValue = getAccount().getPortfolioValue();
        // JATbot.botLogger.info("Portfolio Value: {}", portValue);
        JATbot.botLogger.info("Portfolio Value Logged" + "\n");
        return portValue;

    }

    public String getAccStatus() {
        AccountStatus status = getAccount().getStatus();
        // JATbot.botLogger.info("Account Status: {}", status);
        JATbot.botLogger.info("Account Status Logged" + "\n");

        return status.toString();

    }

    public String getCreateDate() {
        OffsetDateTime creation = getAccount().getCreatedAt();
        // JATbot.botLogger.info("Date Created: {}", creation);
        // JATbot.botLogger.info("Date Created" + "\n");
        String creationString = creation.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
        return creationString;

    }

    public String getBuyingPower() {
        String tradeableFunds = getAccount().getBuyingPower();
        // JATbot.botLogger.info("Tradeable Funds: {}", tradeableFunds);
        // JATbot.botLogger.info("Tradeable Funds Logged" + "\n");
        return tradeableFunds;

    }

    public String getLongMarketValue() {
        String longMarketValue = getAccount().getLongMarketValue();
        // JATbot.botLogger.info("Long Market Value: {}", longMarketValue);
        // JATbot.botLogger.info("Long Market Value Logged" + "\n");
        return longMarketValue;

    }

    public String getDayTradeLimit() {
        int dayTradeLimit = getAccount().getDaytradeCount();
        // JATbot.botLogger.info("Day Trade Limit: {}", dayTradeLimit);
        // JATbot.botLogger.info("Day Trade Limit Logged" + "\n");
        return Integer.toString(dayTradeLimit);

    }

    public String getShortMarketValue() {
        String shortMarketValue = getAccount().getShortMarketValue();
        // JATbot.botLogger.info("Short Market Value: {}", shortMarketValue);
        // JATbot.botLogger.info("Short Market Value Logged" + "\n");
        return shortMarketValue;

    }

    public String getEquity() {
        String equity = getAccount().getEquity();
        // JATbot.botLogger.info("Equity: {}", equity);
        // JATbot.botLogger.info("Equity Logged" + "\n");
        return equity;

    }

    public String getLastEquity() {
        String lastEquity = getAccount().getLastEquity();
        // JATbot.botLogger.info("Last Equity: {}", lastEquity);
        // JATbot.botLogger.info("Last Equity Logged" + "\n");
        return lastEquity;

    }

    public String getInitialMargin() {
        String initialMargin = getAccount().getInitialMargin();
        // JATbot.botLogger.info("Initial Margin: {}", initialMargin);
        // JATbot.botLogger.info("Initial Margin Logged" + "\n");
        return initialMargin;

    }

    public String getMaintenanceMargin() {
        String maintenanceMargin = getAccount().getMaintenanceMargin();
        // JATbot.botLogger.info("Maintenance Margin: {}", maintenanceMargin);
        // JATbot.botLogger.info("Maintenance Margin Logged" + "\n");
        return maintenanceMargin;

    }

    public String getLastMaintenanceMargin() {
        String lastMaintenanceMargin = getAccount().getLastMaintenanceMargin();
        // JATbot.botLogger.info("Last Maintenance Margin: {}", lastMaintenanceMargin);
        // JATbot.botLogger.info("Last Maintenance Margin Logged" + "\n");
        return lastMaintenanceMargin;

    }

    public String getDayTradeCount() {
        int dayTradeCount = getAccount().getDaytradeCount();
        // JATbot.botLogger.info("Day Trade Count: {}", dayTradeCount);
        // JATbot.botLogger.info("Day Trade Count Logged" + "\n");
        return Integer.toString(dayTradeCount);

    }

    public String getCurrency() {
        String currency = getAccount().getCurrency();
        // JATbot.botLogger.info("Currency: {}", currency);
        // JATbot.botLogger.info("Currency Logged" + "\n");
        return currency;
    }

    public Clock getMarketTime() {
        try {
            // Get the market 'Clock' and print it out
            Clock clock = alpaca.trader().clock().getClock();
            return clock;
        } catch (ApiException exception) {
            exception.printStackTrace();
            return null;
        }
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
        try {
            List<Assets> assets = alpaca.trader().assets().getV2Assets("active", null, null, null);
            //JATbot.botLogger.info("Assets: {}", assets);
            return assets;
        } catch (ApiException exception) {
            JATbot.botLogger.error("Error getting assets: " + exception.getMessage());
        }
        return null;
    }


    /*
     * public void logRecentData(String sym, String timeframe) {
     * try {
     * StockBarsResp barsResponse = alpaca.marketData().stock().stockBars(sym,
     * timeframe, OffsetDateTime.of(0, 0, 0, 0, 0, 0, 0, null), null, null,null);
     * barsResponse.getBars().forEach(bar -> {
     * String barString = bar.toString();
     * int index = barString.indexOf("StockBar@");
     * if (index != -1) {
     * String strippedBarString = barString.substring(index);
     * JATbot.botLogger.info(sym + " Bar: {}", strippedBarString);
     * }
     * });
     * } catch (ApiException exception) {
     * exception.printStackTrace();
     * }
     * }
     */

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