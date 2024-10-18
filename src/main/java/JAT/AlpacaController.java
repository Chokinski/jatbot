package JAT;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.openapi.trader.model.*;

import net.jacobpeterson.alpaca.model.util.apitype.MarketDataWebsocketSourceType;
import net.jacobpeterson.alpaca.model.util.apitype.TraderAPIEndpointType;
import okhttp3.OkHttpClient;

import java.io.BufferedReader;

import java.io.IOException;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZoneOffset;

import java.time.format.DateTimeFormatter;

import java.util.List;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Properties;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.jacobpeterson.alpaca.openapi.trader.ApiException;

import net.jacobpeterson.alpaca.openapi.marketdata.model.*;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;


import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import com.jat.OHLCData;


public class AlpacaController {
    public static AlpacaAPI alpaca;
    private OkHttpClient okClient;
    Path jatDir = Paths.get(System.getProperty("user.home"), "JAT");
    final Path jatConfigPath = Paths.get(System.getProperty("user.home"), "JAT", "JATconfig.properties");
    
    /**
     * Connects to the Alpaca API using the properties specified in the
     * "alpaca.properties" file.
     * If the file or path is not found, a FileNotFoundException is thrown.
     * If an IO exception occurs while loading the properties, an IOException is
     * thrown.
     */
    
    public AlpacaController() {
        this.okClient = new OkHttpClient();
        this.connect();
    }
    
    public String[] loadProperties() {
        //check if JAT directory exists, if not create it
        if (!Files.exists(jatDir)) {
            try {Files.createDirectory(jatDir);}
            catch (IOException e) {JATbot.botLogger.error("Error creating JAT directory: " + e.getMessage());}
        }
        Properties properties = new Properties();

        try (BufferedReader reader = Files.newBufferedReader(jatConfigPath)) {
            String line;
            StringBuilder sb = new StringBuilder();

            // Read the file line by line and skip the line containing "jarPath"
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("jarPath")) {
                    sb.append(line).append(System.lineSeparator());
                }
            }

            // Load properties from the filtered content
            properties.load(new java.io.StringReader(sb.toString()));
            if (!properties.containsKey("key_id") || !properties.containsKey("secret_key") || !properties.containsKey("type") || !properties.containsKey("source") || !properties.containsKey("remMe")) {
                JATbot.botLogger.error("Error loading properties: key_id, secret_key, type, or source not found");

                properties = writeProps(jatConfigPath, properties);
            }
            String keyID = properties.getProperty("key_id");
            String secretKey = properties.getProperty("secret_key");
            String type = properties.getProperty("type");
            String source = properties.getProperty("source");
            String remMe = properties.getProperty("remMe");

            return new String[]{keyID, secretKey, type, source,remMe};
        } catch (IOException e) {
            JATbot.botLogger.error("\nError loading properties, IO exception: " + e.getMessage());
        }

        return null;
    }

    public Properties writeProps(Path config, Properties props) {
// Default values for the properties
String defaultKeyID = "PKIUYM93AMQV3N07CB98";
String defaultSecretKey = "6BZJEPXFWxcldcpXazine3Kewsj1hyklumDriqJa";
String defaultType = "PAPER";
String defaultSource = "IEX";
String defaultRemMe = "true"; // Replace with the actual default value if any

Map<String, String> defaultValues = new HashMap<>();
defaultValues.put("key_id", defaultKeyID);
defaultValues.put("secret_key", defaultSecretKey);
defaultValues.put("type", defaultType);
defaultValues.put("source", defaultSource);
defaultValues.put("remMe", defaultRemMe);

// Set the properties if they don't already exist
for (Map.Entry<String, String> entry : defaultValues.entrySet()) {
    props.putIfAbsent(entry.getKey(), entry.getValue());
}
        try {
            // Read the existing content of the file
            StringBuilder sb = new StringBuilder();
            if (Files.exists(config)) {
                try (BufferedReader reader = Files.newBufferedReader(config)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("jarPath")) {
                            sb.append(line).append(System.lineSeparator());
                        }
                    }
                }
            }

            // Append the new properties to the StringBuilder
            for (String key : props.stringPropertyNames()) {
                sb.append(key).append("=").append(props.getProperty(key)).append(System.lineSeparator());
            }
            asyncWrite(sb, config);

            return props;
        } catch (IOException e) {
            JATbot.botLogger.error("Error writing properties: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // New method to modify a single property
    public void modifyProperty(Path config, String key, String value) {
        Properties props = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(config))
            {props.load(reader);}
        catch (IOException e) 
            {JATbot.botLogger.error("Error loading properties: " + e.getMessage());}
        props.setProperty(key, value);

        try {
            // Convert the properties to a StringBuilder
            StringBuilder sb = new StringBuilder();
            for (String propKey : props.stringPropertyNames()) {
                sb.append(propKey).append("=").append(props.getProperty(propKey)).append(System.lineSeparator());
            }
            asyncWrite(sb, config);

        } catch (Exception e) {
            JATbot.botLogger.error("Error modifying property: " + e.getMessage());
        }
    }

    public void asyncWrite(StringBuilder sb, Path cfg) {

        // Convert the StringBuilder content to a ByteBuffer
        ByteBuffer buffer = ByteBuffer.wrap(sb.toString().getBytes());

        // Write the buffer to the file asynchronously
        try (AsynchronousFileChannel aChannel = AsynchronousFileChannel.open(cfg, StandardOpenOption.WRITE,
                StandardOpenOption.CREATE)) {
            CompletableFuture<Void> writeFuture = new CompletableFuture<>();
            aChannel.write(buffer, 0, null, new CompletionHandler<Integer, Void>() {
                @Override
                public void completed(Integer result, Void attachment) {
                    writeFuture.complete(null);
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    writeFuture.completeExceptionally(exc);
                }
            });
            writeFuture.get();
        } catch (IOException | InterruptedException | ExecutionException e) {
            JATbot.botLogger.error("Error writing properties: " + e.getMessage());
            e.printStackTrace();
        }
    } 

    public AlpacaAPI connect() {
        String[] props = loadProperties();
        alpaca = new AlpacaAPI(props[0], props[1], TraderAPIEndpointType.valueOf(props[2]), MarketDataWebsocketSourceType.valueOf(props[3]));
        return alpaca;
            
}

    public Account getAccount() {
        try {
            // Get 'Account' information
            Account account = alpaca.trader().accounts().getAccount();
            return account;
        } catch (ApiException exception) {
            JATbot.botLogger.error("Error getting acc info: " + exception.getMessage());
            return null;
        }

    }
    public String getAccID() {
        String accID = getAccount().getAccountNumber();
        //JATbot.botLogger.info("Account ID: {}", accID);
        JATbot.botLogger.info("Account ID Logged" + "\n");
        return accID;
    }

    public String getAccCash() {
        String cash = getAccount().getCash();
        //JATbot.botLogger.info("Account Cash: {}", cash);
        JATbot.botLogger.info("Cash Logged" + "\n");
        return cash;
    }

    public String getPortValue() {
        String portValue = getAccount().getPortfolioValue();
        //JATbot.botLogger.info("Portfolio Value: {}", portValue);
        JATbot.botLogger.info("Portfolio Value Logged" + "\n");
        return portValue;

    }

    public String getAccStatus() {
        AccountStatus status = getAccount().getStatus();
        //JATbot.botLogger.info("Account Status: {}", status);
        JATbot.botLogger.info("Account Status Logged" + "\n");

        return status.toString();

    }

    public String getCreateDate() {
        OffsetDateTime creation = getAccount().getCreatedAt();
        //JATbot.botLogger.info("Date Created: {}", creation);
        //JATbot.botLogger.info("Date Created" + "\n");
        String creationString = creation.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
        return creationString;

    }

    public String getBuyingPower() {
        String tradeableFunds = getAccount().getBuyingPower();
        //JATbot.botLogger.info("Tradeable Funds: {}", tradeableFunds);
        //JATbot.botLogger.info("Tradeable Funds Logged" + "\n");
        return tradeableFunds;

    }

    public String getLongMarketValue() {
        String longMarketValue = getAccount().getLongMarketValue();
        //JATbot.botLogger.info("Long Market Value: {}", longMarketValue);
        //JATbot.botLogger.info("Long Market Value Logged" + "\n");
        return longMarketValue;

    }
    public String getDayTradeLimit() {
        int dayTradeLimit = getAccount().getDaytradeCount();
        //JATbot.botLogger.info("Day Trade Limit: {}", dayTradeLimit);
        //JATbot.botLogger.info("Day Trade Limit Logged" + "\n");
        return Integer.toString(dayTradeLimit);

    }

    public String getShortMarketValue() {
        String shortMarketValue = getAccount().getShortMarketValue();
        //JATbot.botLogger.info("Short Market Value: {}", shortMarketValue);
        //JATbot.botLogger.info("Short Market Value Logged" + "\n");
        return shortMarketValue;

    }

    public String getEquity() {
        String equity = getAccount().getEquity();
        //JATbot.botLogger.info("Equity: {}", equity);
        //JATbot.botLogger.info("Equity Logged" + "\n");
        return equity;

    }

    public String getLastEquity() {
        String lastEquity = getAccount().getLastEquity();
        //JATbot.botLogger.info("Last Equity: {}", lastEquity);
        //JATbot.botLogger.info("Last Equity Logged" + "\n");
        return lastEquity;

    }

    public String getInitialMargin() {
        String initialMargin = getAccount().getInitialMargin();
        //JATbot.botLogger.info("Initial Margin: {}", initialMargin);
        //JATbot.botLogger.info("Initial Margin Logged" + "\n");
        return initialMargin;

    }

    public String getMaintenanceMargin() {
        String maintenanceMargin = getAccount().getMaintenanceMargin();
        //JATbot.botLogger.info("Maintenance Margin: {}", maintenanceMargin);
        //JATbot.botLogger.info("Maintenance Margin Logged" + "\n");
        return maintenanceMargin;

    }

    public String getLastMaintenanceMargin() {
        String lastMaintenanceMargin = getAccount().getLastMaintenanceMargin();
        //JATbot.botLogger.info("Last Maintenance Margin: {}", lastMaintenanceMargin);
        //JATbot.botLogger.info("Last Maintenance Margin Logged" + "\n");
        return lastMaintenanceMargin;

    }

    public String getDayTradeCount() {
        int dayTradeCount = getAccount().getDaytradeCount();
        //JATbot.botLogger.info("Day Trade Count: {}", dayTradeCount);
        //JATbot.botLogger.info("Day Trade Count Logged" + "\n");
        return Integer.toString(dayTradeCount);

    }

    public String getCurrency() {
        String currency = getAccount().getCurrency();
        //JATbot.botLogger.info("Currency: {}", currency);
        //JATbot.botLogger.info("Currency Logged" + "\n");
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
            Order newOrder = alpaca.trader().orders().postOrder(new PostOrderRequest().symbol(sym).qty(vol).side(side).type(OrderType.MARKET).timeInForce(TimeInForce.DAY));
            JATbot.botLogger.info("Order placed: {}", newOrder);
        } catch (ApiException exception) {
            JATbot.botLogger.error("Error placing order: " + exception.getMessage());
        }
    }

    public void closeTrade(String sym, BigDecimal qty, BigDecimal perc){

        try {
            Order closingorder = alpaca.trader().positions().deleteOpenPosition(sym, qty, perc);
            JATbot.botLogger.info("Order closed: {}", closingorder);
        } catch (ApiException exception) {
            JATbot.botLogger.error("Error closing order: " + exception.getMessage());
        }


    }

    public void getAssets() {

        try {
            List<Assets> assets = alpaca.trader().assets().getV2Assets("active", null, null, null);
            JATbot.botLogger.info("Assets: {}", assets);
        } catch (ApiException exception) {
            JATbot.botLogger.error("Error getting assets: " + exception.getMessage());
        }
    }
    /*public void logRecentData(String sym, String timeframe) {
        try {
            StockBarsResp barsResponse = alpaca.marketData().stock().stockBars(sym, timeframe, OffsetDateTime.of(0, 0, 0, 0, 0, 0, 0, null), null, null,null);
            barsResponse.getBars().forEach(bar -> {
                String barString = bar.toString();
                int index = barString.indexOf("StockBar@");
                if (index != -1) {
                    String strippedBarString = barString.substring(index);
                    JATbot.botLogger.info(sym + " Bar: {}", strippedBarString);
                }
            });
        } catch (ApiException exception) {
            exception.printStackTrace();
        }
    }*/

    /**
     * Represents a series of Open, High, Low, Close (OHLC) data for a specific symbol.
     * @throws net.jacobpeterson.alpaca.openapi.marketdata.ApiException 
     */
public ObservableList<OHLCData> getBarsData(String sym, int stYr, int stMo, int stDay,
                                            int endYr, int endMo, int endDay, String timeframe) throws net.jacobpeterson.alpaca.openapi.marketdata.ApiException {
    ObservableList<OHLCData> ohlcDataList = FXCollections.observableArrayList();
    StockBarsResp barsResponse;

    try {
        LocalDateTime now = LocalDateTime.now();
        ZoneId zid = ZoneId.of("America/New_York");
        ZoneOffset zoffset = zid.getRules().getOffset(now);
        OffsetDateTime startDateTime = OffsetDateTime.of(stYr, stMo, stDay, 9, 30, 0, 0, zoffset);
        OffsetDateTime endDateTime = OffsetDateTime.of(endYr, endMo, endDay, 16, 0, 0, 0,zoffset);

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