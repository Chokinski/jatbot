package JAT;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.model.endpoint.account.Account;
import net.jacobpeterson.alpaca.model.endpoint.account.enums.AccountStatus;
import net.jacobpeterson.alpaca.model.endpoint.orders.Order;
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderSide;
import net.jacobpeterson.alpaca.rest.AlpacaClientException;
import okhttp3.OkHttpClient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import java.util.Properties;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import net.jacobpeterson.alpaca.model.endpoint.assets.Asset;
import net.jacobpeterson.alpaca.model.endpoint.assets.enums.AssetClass;
import net.jacobpeterson.alpaca.model.endpoint.assets.enums.AssetStatus;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.bar.Bar;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.bar.enums.BarTimePeriod;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.stock.historical.bar.StockBar;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.stock.historical.bar.StockBarsResponse;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.stock.historical.bar.enums.BarAdjustment;
import net.jacobpeterson.alpaca.model.endpoint.marketdata.stock.historical.bar.enums.BarFeed;
import com.jat.OHLCData;


public class AlpacaController {
    public static AlpacaAPI alpaca;
    private OkHttpClient okClient;

    /**
     * Connects to the Alpaca API using the properties specified in the
     * "alpaca.properties" file.
     * If the file or path is not found, a FileNotFoundException is thrown.
     * If an IO exception occurs while loading the properties, an IOException is
     * thrown.
     */

    public AlpacaController() {
        this.connect();
        this.okClient = new OkHttpClient();
    }

    public String[] loadProperties() {
        try (InputStream alpacaIn = getClass().getResourceAsStream("/JAT/alpaca.properties")){
            Properties properties = new Properties();
            properties.load(alpacaIn);
            String keyID = properties.getProperty("key_id");
            String secretKey = properties.getProperty("secret_key");
            return new String[]{keyID, secretKey};
        } catch (FileNotFoundException exception) {
            JATbot.botLogger.error("Erorr loading properties, File/Path Not Found:" + exception.getMessage());
        } catch (IOException exception) {
            JATbot.botLogger.error("Erorr loading properties, IO exception:" + exception.getMessage());
    }
    return null;
}
    public AlpacaAPI connect() {
        String[] props = loadProperties();
        alpaca = new AlpacaAPI(props[0], props[1]);
            return alpaca;
            
}

    public Account getAccount() {
        try {
            // Get 'Account' information
            Account account = alpaca.account().get();
            return account;
        } catch (AlpacaClientException exception) {
            JATbot.botLogger.error("Error getting acc info: " + exception.getMessage());
            return null;
        }

    }
    public String logAccID() {
        String accID = getAccount().getId();
        JATbot.botLogger.info("Account ID: {}", accID);
        System.out.println("Account ID Logged" + "\n\n\n");
        return accID;
    }

    public String logAccCash() {
        String cash = getAccount().getCash();
        JATbot.botLogger.info("Account Cash: {}", cash);
        System.out.println("Cash Logged" + "\n\n\n");
        return cash;
    }

    public String logPortValue() {
        String portValue = getAccount().getPortfolioValue();
        JATbot.botLogger.info("Portfolio Value: {}", portValue);
        System.out.println("Portfolio Value Logged" + "\n\n\n");
        return portValue;

    }

    public String logAccStatus() {
        AccountStatus status = getAccount().getStatus();
        JATbot.botLogger.info("Account Status: {}", status);
        System.out.println("Account Status Logged" + "\n\n\n");

        return status.toString();

    }

    public String logCreateDate() {
        ZonedDateTime creation = getAccount().getCreatedAt();
        JATbot.botLogger.info("Date Created: {}", creation);
        System.out.println("Date Created" + "\n\n\n");
        String creationString = creation.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
        return creationString;

    }

    public String logBuyingPower() {
        String tradeableFunds = getAccount().getBuyingPower();
        JATbot.botLogger.info("Tradeable Funds: {}", tradeableFunds);
        System.out.println("Tradeable Funds Logged" + "\n\n\n");
        return tradeableFunds;

    }

    public String logLongMarketValue() {
        String longMarketValue = getAccount().getLongMarketValue();
        JATbot.botLogger.info("Long Market Value: {}", longMarketValue);
        System.out.println("Long Market Value Logged" + "\n\n\n");
        return longMarketValue;

    }
    public String logDayTradeLimit() {
        int dayTradeLimit = getAccount().getDaytradeCount();
        JATbot.botLogger.info("Day Trade Limit: {}", dayTradeLimit);
        System.out.println("Day Trade Limit Logged" + "\n\n\n");
        return Integer.toString(dayTradeLimit);

    }

    public String logShortMarketValue() {
        String shortMarketValue = getAccount().getShortMarketValue();
        JATbot.botLogger.info("Short Market Value: {}", shortMarketValue);
        System.out.println("Short Market Value Logged" + "\n\n\n");
        return shortMarketValue;

    }

    public String logEquity() {
        String equity = getAccount().getEquity();
        JATbot.botLogger.info("Equity: {}", equity);
        System.out.println("Equity Logged" + "\n\n\n");
        return equity;

    }

    public String logLastEquity() {
        String lastEquity = getAccount().getLastEquity();
        JATbot.botLogger.info("Last Equity: {}", lastEquity);
        System.out.println("Last Equity Logged" + "\n\n\n");
        return lastEquity;

    }

    public String logInitialMargin() {
        String initialMargin = getAccount().getInitialMargin();
        JATbot.botLogger.info("Initial Margin: {}", initialMargin);
        System.out.println("Initial Margin Logged" + "\n\n\n");
        return initialMargin;

    }

    public String logMaintenanceMargin() {
        String maintenanceMargin = getAccount().getMaintenanceMargin();
        JATbot.botLogger.info("Maintenance Margin: {}", maintenanceMargin);
        System.out.println("Maintenance Margin Logged" + "\n\n\n");
        return maintenanceMargin;

    }

    public String logLastMaintenanceMargin() {
        String lastMaintenanceMargin = getAccount().getLastMaintenanceMargin();
        JATbot.botLogger.info("Last Maintenance Margin: {}", lastMaintenanceMargin);
        System.out.println("Last Maintenance Margin Logged" + "\n\n\n");
        return lastMaintenanceMargin;

    }

    public String logDayTradeCount() {
        int dayTradeCount = getAccount().getDaytradeCount();
        JATbot.botLogger.info("Day Trade Count: {}", dayTradeCount);
        System.out.println("Day Trade Count Logged" + "\n\n\n");
        return Integer.toString(dayTradeCount);

    }

    public String logCurrency() {
        String currency = getAccount().getCurrency();
        JATbot.botLogger.info("Currency: {}", currency);
        System.out.println("Currency Logged" + "\n\n\n");
        return currency;
    }

    public Order placeTrade() {
        try {
            Order newOrder = alpaca.orders().requestMarketOrder(null, null, OrderSide.BUY, null);
            return newOrder;
        } catch (AlpacaClientException exception) {
            JATbot.botLogger.error("Error placing order: " + exception.getMessage());
        }
        return null;
    }

    public void getAssets() {

        try {
            List<Asset> assets = alpaca.assets().get(AssetStatus.ACTIVE, AssetClass.US_EQUITY);
            JATbot.botLogger.info("Assets: {}", assets);
        } catch (AlpacaClientException exception) {
            JATbot.botLogger.error("Error getting assets: " + exception.getMessage());
        }
    }
    public void logRecentData(String sym) {
        try {
            // Get 'sym' one hour, split-adjusted bars from 1/11/2023 market open
            // to 12/1/2023 market close from the SIP feed and print them out
            StockBarsResponse barsResponse = alpaca.stockMarketData().getBars(
                    sym,
                    ZonedDateTime.of(2023, 11, 1, 9, 30, 0, 0, ZoneId.of("America/New_York")),
                    ZonedDateTime.of(2023, 12, 1,12+4, 0, 0, 0, ZoneId.of("America/New_York")),
                    null,
                    null,
                    1,
                    BarTimePeriod.HOUR,
                    BarAdjustment.SPLIT,
                    BarFeed.SIP);
            barsResponse.getBars().forEach((bar) -> {
                String barString = bar.toString();
                int index = barString.indexOf("StockBar@");
                if (index != -1) {
                    String strippedBarString = barString.substring(index);
                    JATbot.botLogger.info(sym+" Bar: {}", strippedBarString);
                }
            });
        } catch (AlpacaClientException exception) {
            exception.printStackTrace();
        }
    }

/**
 * Represents a series of Open, High, Low, Close (OHLC) data for a specific symbol.
 */
public ObservableList<OHLCData> getBarsData(String sym, int stYr, int stMo, int stDay,
                                            int endYr, int endMo, int endDay, BarTimePeriod barPeriod, int duration) {
    ObservableList<OHLCData> ohlcDataList = FXCollections.observableArrayList();

    try {
        StockBarsResponse barsResponses = alpaca.stockMarketData().getBars(sym,
            ZonedDateTime.of(stYr, stMo, stDay, 9, 30, 0, 0, ZoneId.of("America/New_York")),
            ZonedDateTime.of(endYr, endMo, endDay, 12 + 4, 0, 0, 0, ZoneId.of("America/New_York")), 10000, null,
            duration, barPeriod, BarAdjustment.SPLIT, BarFeed.IEX);

        barsResponses.getBars().forEach(bar -> {
            ZonedDateTime timestamp = bar.getTimestamp();
            LocalDate date = timestamp.toLocalDate();

            double open = bar.getOpen().doubleValue();
            double high = bar.getHigh().doubleValue();
            double low = bar.getLow().doubleValue();
            double close = bar.getClose().doubleValue();
            long volume = bar.getVolume();

            OHLCData ohlcData = new OHLCData(date, open, high, low, close, volume);
            ohlcDataList.add(ohlcData);
        });
    } catch (AlpacaClientException e) {
        JATbot.botLogger.info("Error getting bars data: {}", e.getMessage());
    }

    return ohlcDataList;
}

        

    public OkHttpClient getOkHttpClient() {
        return okClient;
    }

}