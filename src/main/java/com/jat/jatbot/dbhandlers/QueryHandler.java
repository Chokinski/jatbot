package com.jat.jatbot.dbhandlers;
import com.jat.jatbot.ai.StockData;

import java.io.IOException;
import java.util.List;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class QueryHandler {
    private final DatabaseClient databaseClient;

    public QueryHandler(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
        databaseClient.sql("SET SESSION wait_timeout = 3000");
    }

    public Mono<Void> uploadDataSetsToDb(List<StockData> Data, String sym) throws IOException {
        System.out.println("Uploading....");
        return Flux.fromIterable(Data)
                .concatMap(data -> insertDataPointWithFeatures(data, sym)) // Use concatMap for sequential processing
                .then()
                .doOnTerminate(() -> System.out.println("Finished uploading data for: " + sym));
    }

    public Mono<Void> insertDataPointWithFeatures(StockData ohlcData, String sym) {
        if (ohlcData.getId() == 0){
            String getLastIdSQL = "SELECT MAX(id) FROM " + sym + "predset";
            databaseClient.sql(getLastIdSQL)
                    .map((row, metadata) -> row.get("max", Integer.class))
                    .one()
                    .map(id -> {
                        ohlcData.setId(id + 1);
                        return ohlcData;
                    })
                    .flatMap(data -> insertDataPointWithFeatures(data, sym));
        }
        String checkSql = "SELECT COUNT(*) FROM " + sym + "predset WHERE id = $1";

        String insertSql = "INSERT INTO " + sym + "predset " +
                "(open, high, low, close, volume) " +
                "VALUES ($1, $2, $3, $4, $5);";

        return databaseClient.sql(checkSql)
                .bind("$1", ohlcData.getId())
                .fetch()
                .one()
                .map(result -> (Long) result.values().iterator().next())
                .filter(count -> count == 0)
                .flatMap(count -> databaseClient.sql(insertSql)
                        .bind("$1", ohlcData.getOpen())
                        .bind("$2", ohlcData.getHigh())
                        .bind("$3", ohlcData.getLow())
                        .bind("$4", ohlcData.getClose())
                        .bind("$5", ohlcData.getVolume())
                        .fetch()
                        .rowsUpdated())
                .then();

    }

    public Flux<StockData> getStockData(String dt) {
        return databaseClient.sql("SELECT timestamp, id, open, high, low, close, volume, vwap, rsi, macd, roc, ema_close, atr, tema, sma_tr, nw_smooth, nw_smooth_one, nw_smooth_two, nw_derivative FROM "+dt+"dataset"+
         " ORDER BY timestamp")
                .map((row, metadata) -> new StockData(
                        row.get("open", Float.class),
                        row.get("high", Float.class),
                        row.get("low", Float.class),
                        row.get("close", Float.class),
                        row.get("volume", Long.class),
                        row.get("vwap", Float.class),
                        row.get("rsi", Float.class),
                        row.get("macd", Float.class),
                        row.get("roc", Float.class),
                        row.get("ema_close", Float.class),
                        row.get("atr", Float.class),
                        row.get("tema", Float.class),
                        row.get("sma_tr", Float.class),
                        row.get("nw_smooth", Float.class),
                        row.get("nw_smooth_one", Float.class),
                        row.get("nw_smooth_two", Float.class),
                        row.get("nw_derivative", Float.class)
                ))
                .all();
    }
    public Flux<StockData> getStockDataWithTime(String dt) {
        return databaseClient.sql("SELECT id, open, high, low, close, volume, vwap, rsi, macd, roc, ema_close, atr, tema, sma_tr, nw_smooth, nw_smooth_one, nw_smooth_two, nw_derivative, timestamp FROM "+dt+"dataset"+
         " ORDER BY timestamp")
                .map((row, metadata) -> new StockData(
                        row.get("open", Float.class),
                        row.get("high", Float.class),
                        row.get("low", Float.class),
                        row.get("close", Float.class),
                        row.get("volume", Long.class),
                        row.get("vwap", Float.class),
                        row.get("rsi", Float.class),
                        row.get("macd", Float.class),
                        row.get("roc", Float.class),
                        row.get("ema_close", Float.class),
                        row.get("atr", Float.class),
                        row.get("tema", Float.class),
                        row.get("sma_tr", Float.class),
                        row.get("nw_smooth", Float.class),
                        row.get("nw_smooth_one", Float.class),
                        row.get("nw_smooth_two", Float.class),
                        row.get("nw_derivative", Float.class),
                        row.get("timestamp", String.class)
                ))
                .all();
    }
    public Mono<Void> deleteTable(String sym) {
        String deleteTableSql = "DROP TABLE IF EXISTS " + sym + "unbiasedpredset";
        return databaseClient.sql(deleteTableSql)
                .fetch()
                .rowsUpdated()
                .then();
    }


    public Flux<StockData> getPredData(String dt) {
return databaseClient.sql(
    "SELECT * FROM (" +
    "   SELECT id, open, high, low, close, volume, vwap, rsi, macd, roc, ema_close, atr, tema, sma_tr, " +
    "   nw_smooth, nw_smooth_one, nw_smooth_two, nw_derivative" +
    "   FROM " + dt + "predset " +
    "   ORDER BY id DESC " +
    "   LIMIT 100" +
    ") subquery " +
    "ORDER BY id ASC" // Reverse back to ascending order
)
.map((row, metadata) -> new StockData(
    row.get("open", Float.class),
    row.get("high", Float.class),
    row.get("low", Float.class),
    row.get("close", Float.class),
    row.get("volume", Long.class),
    row.get("vwap", Float.class),
    row.get("rsi", Float.class),
    row.get("macd", Float.class),
    row.get("roc", Float.class),
    row.get("ema_close", Float.class),
    row.get("atr", Float.class),
    row.get("tema", Float.class),
    row.get("sma_tr", Float.class),
    row.get("nw_smooth", Float.class),
    row.get("nw_smooth_one", Float.class),
    row.get("nw_smooth_two", Float.class),
    row.get("nw_derivative", Float.class)
))
.all();
    }
    public void ensurePredSet(String sym, List<StockData> dataset) {
        try {
            checkDataSetTableExists(sym)
                .then(uploadDataSetsToDb(dataset, sym)) // Chain uploadDataSetsToDb
                .then(checkAndRecalculateMissingFeatures(sym)) // Chain checkAndRecalculateMissingFeatures
                .doOnSubscribe(s -> System.out.println("Upload for " + sym + " started."))
                .doOnSuccess(s -> System.out.println("Upload for " + sym + " finished."))
                .block(); // Block until the entire chain completes
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
public Mono<Void> updatePredSet(String sym, List<StockData> dataset) {
    try {
        return uploadDataSetsToDb(dataset, sym)  // Step 1: Upload data
            .then(checkAndRecalculateMissingFeatures(sym))  // Step 2: Ensure features are updated
            .doOnSubscribe(s -> System.out.println("Update for " + sym + " started."))
            .doOnSuccess(s -> System.out.println("Update for " + sym + " finished."));
    } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    return null;
}



    public Mono<Void> checkDataSetTableExists(String sym) {
        String createIndexSql = "CREATE INDEX IF NOT EXISTS idx_" + sym + "predset_id ON " + sym
        + "predset(id);";
        String createDataSetTableSql = "CREATE TABLE IF NOT EXISTS " + sym +
                "predset (id BIGSERIAL PRIMARY KEY,\n" + //
                "    open DOUBLE PRECISION,\n" + //
                "    high DOUBLE PRECISION,\n" + //
                "    low DOUBLE PRECISION,\n" + //
                "    close DOUBLE PRECISION,\n" + //
                "    volume DOUBLE PRECISION,\n" + //
                "    vwap DOUBLE PRECISION,\n" + //
                "    rsi DOUBLE PRECISION,\n" + //
                "    macd DOUBLE PRECISION,\n" + //
                "    roc DOUBLE PRECISION,\n" + //
                "    ema_close DOUBLE PRECISION,\n" + //
                "    atr DOUBLE PRECISION,\n" + //
                "    tema DOUBLE PRECISION,\n" + //
                "    sma_tr DOUBLE PRECISION,\n" +
                "    nw_smooth DOUBLE PRECISION,\n" +
                "    nw_smooth_one DOUBLE PRECISION,\n" +
                "    nw_smooth_two DOUBLE PRECISION,\n" +
                "    nw_derivative DOUBLE PRECISION,\n" +
                "timestamp TIMESTAMP\n"+
                ");";

        return databaseClient.sql(createDataSetTableSql)
                .fetch()
                .rowsUpdated()
                .then(
                (Mono.fromRunnable(() -> {

        // Execute index creation
        databaseClient.sql(createIndexSql)
                .fetch()
                .rowsUpdated()
                .subscribe();}))).then();

    }
    public Mono<Void> checkAndRecalculateMissingFeatures(String sym) {
        return checkAndCalculate(sym, "vwap", this::calculateVWAP)
        .doOnSubscribe(s ->  log("Starting VWAP check and recalculation ", sym))
        .doOnSuccess(v ->  log("Completed VWAP check and recalculation ", sym))
            .then(checkAndCalculate(sym, "rsi", this::calculateRSI)
                .doOnSubscribe(s ->  log("Starting RSI check and recalculation ", sym))
                .doOnSuccess(v ->  log("Completed RSI check and recalculation ", sym)))
            .then(checkAndCalculate(sym, "macd", this::calculateMACD)
                .doOnSubscribe(s ->  log("Starting MACD check and recalculation ", sym))
                .doOnSuccess(v ->  log("Completed MACD check and recalculation ", sym)))
            .then(checkAndCalculate(sym, "roc", this::calculateROC)
                .doOnSubscribe(s ->  log("Starting ROC check and recalculation ", sym))
                .doOnSuccess(v ->  log("Completed ROC check and recalculation ", sym)))
            .then(checkAndCalculate(sym, "ema_close", this::calculateEMA)
                .doOnSubscribe(s ->  log("Starting EMA check and recalculation ", sym))
                .doOnSuccess(v ->  log("Completed EMA check and recalculation ", sym)))
            .then(checkAndCalculate(sym, "atr", this::calculateATR)
                .doOnSubscribe(s ->  log("Starting ATR check and recalculation ", sym))
                .doOnSuccess(v ->  log("Completed ATR check and recalculation ", sym)))
            .then(checkAndCalculate(sym, "tema", this::calculateTEMA)
                .doOnSubscribe(s ->  log("Starting TEMA check and recalculation ", sym))
                .doOnSuccess(v ->  log("Completed TEMA check and recalculation ", sym)))
            .then(checkAndCalculate(sym, "sma_tr", this::calculateSMA)
                .doOnSubscribe(s -> log("Starting SMA check and recalculation ", sym))
                .doOnSuccess(v ->  log("Completed SMA check and recalculation ", sym)))
            .then(checkAndCalculate(sym, "nw_smooth", this::calculateNWSmooth)
                .doOnSubscribe(s ->  log("Starting NW Smooth check and recalculation ", sym))
                .doOnSuccess(v ->  log("Completed NW Smooth check and recalculation ", sym)))
            .then(checkAndCalculate(sym, "nw_smooth_one", this::calculateNWSmoothOne)
                .doOnSubscribe(s ->  log("Starting NW Smooth One check and recalculation ", sym))
                .doOnSuccess(v ->  log("Completed NW Smooth One check and recalculation ", sym)))
            .then(checkAndCalculate(sym, "nw_smooth_two", this::calculateNWSmoothTwo)
                .doOnSubscribe(s ->  log("Starting NW Smooth Two check and recalculation ", sym))
                .doOnSuccess(v ->  log("Completed NW Smooth Two check and recalculation ", sym)))
            .then(checkAndCalculate(sym, "nw_derivative", this::calculateNWDerivative)
                .doOnSubscribe(s ->  log("Starting NW Derivative check and recalculation ", sym))
                .doOnSuccess(v ->  log("Completed NW Derivative check and recalculation ", sym)));
    }
    private void log(String message, String sym) {
        System.out.println(message + " for symbol: " + sym);
    }
    private Mono<Void> checkAndCalculate(String sym, String column, Function<String, Mono<Void>> calculationFunction) {
        String query = "SELECT 1 FROM " + sym + "predset WHERE " + column + " IS NULL LIMIT 1";
    
        return databaseClient.sql(query)
                .fetch()
                .one()
                .map(result -> true)  // If a row is found, return true
                .defaultIfEmpty(false) // If no rows are found, return false
                .onErrorResume(e -> {
                    System.err.println("Error checking " + column + " for " + sym + ": " + e.getMessage());
                    return Mono.just(false); // Ensures failure doesn't stop the process
                })
                .flatMap(needsCalculation -> needsCalculation ? calculationFunction.apply(sym) : Mono.empty());
    }
    public Mono<Void> calculateVWAP(String sym) {
        String sql = "UPDATE " + sym + "predset AS d " +
                "SET vwap = COALESCE((" +
                "SELECT SUM(volume * close) / NULLIF(SUM(volume), 0) " +
                "FROM " + sym + "predset AS sub " +
                "WHERE sub.id <= d.id" +
                "), d.close) " +
                "WHERE vwap IS NULL OR vwap <= 0;";

        return databaseClient.sql(sql).fetch().rowsUpdated().then();
    }

    public Mono<Void> calculateRSI(String sym) {
        String sql = "CREATE TEMP TABLE price_changes AS " +
                     "SELECT id, " +
                     "       close - LAG(close) OVER (ORDER BY id) AS price_change " +
                     "FROM " + sym + "predset; " +
                     
                     "CREATE TEMP TABLE gains_losses AS " +
                     "SELECT id, " +
                     "       CASE WHEN price_change > 0 THEN price_change ELSE 0 END AS gain, " +
                     "       CASE WHEN price_change < 0 THEN -price_change ELSE 0 END AS loss " +
                     "FROM price_changes; " +
                     
                     "CREATE TEMP TABLE rolling_sums AS " +
                     "SELECT id, " +
                     "       SUM(gain) OVER (ORDER BY id ROWS BETWEEN 13 PRECEDING AND CURRENT ROW) AS sum_gains, " +
                     "       SUM(loss) OVER (ORDER BY id ROWS BETWEEN 13 PRECEDING AND CURRENT ROW) AS sum_losses " +
                     "FROM gains_losses; " +
                     
                     "UPDATE " + sym + "predset d " +
                     "SET rsi = COALESCE( " +
                     "  CASE " +
                     "    WHEN rs.sum_losses = 0 THEN 100 " +  // Prevent division by zero
                     "    ELSE 100 - (100 / (1 + rs.sum_gains / rs.sum_losses)) " +
                     "  END, " +
                     "  50) " + // Default to 50 if RSI cannot be calculated
                     "FROM rolling_sums rs " +
                     "WHERE d.id = rs.id " +
                     "AND (d.rsi IS NULL OR d.rsi <= 0); " +
                     
                     // Cleanup: Drop the temporary tables
                     "DROP TABLE price_changes; " +
                     "DROP TABLE gains_losses; " +
                     "DROP TABLE rolling_sums;";
    
        return databaseClient.sql(sql).fetch().rowsUpdated().then();
    }
public Mono<Void> calculateMACD(String sym) {
    String sql = "WITH ema12 AS ( " +
                 "  SELECT id, AVG(close) OVER (ORDER BY id ROWS BETWEEN 11 PRECEDING AND CURRENT ROW) AS ema12 " +
                 "  FROM " + sym + "predset), " +
                 "ema26 AS ( " +
                 "  SELECT id, AVG(close) OVER (ORDER BY id ROWS BETWEEN 25 PRECEDING AND CURRENT ROW) AS ema26 " +
                 "  FROM " + sym + "predset) " +
                 "UPDATE " + sym + "predset d " +
                 "SET macd = COALESCE((ema12.ema12 - ema26.ema26), 0) " +
                 "FROM ema12 " +
                 "JOIN ema26 ON ema12.id = ema26.id " +
                 "WHERE d.id = ema12.id " +
                 "AND (d.macd IS NULL OR d.macd = 0);";
    
    return databaseClient.sql(sql).fetch().rowsUpdated().then();
}

public Mono<Void> calculateROC(String sym) {
        String calculateROCSql = "WITH roc_values AS ( " +
                                 "  SELECT id, " +
                                 "         (close - LAG(close, 12) OVER (ORDER BY id)) / " +
                                 "         LAG(close, 12) OVER (ORDER BY id) * 100 AS roc " +
                                 "  FROM " + sym + "predset " +
                                 ") " +
                                 "UPDATE " + sym + "predset d " +
                                 "SET roc = COALESCE(rv.roc, 0) " +
                                 "FROM roc_values rv " +
                                 "WHERE d.id = rv.id " +
                                 "AND (d.roc IS NULL OR d.roc <= 0);";
        
        return databaseClient.sql(calculateROCSql)
                             .fetch()
                             .rowsUpdated()
                             .then();
    }

public Mono<Void> calculateEMA(String sym) {
    String sql = "WITH ema AS ( " +
                 "  SELECT id, " +
                 "         close * (2 / (12 + 1)) + " +
                 "         LAG(close) OVER (ORDER BY id) * (1 - (2 / (12 + 1))) AS ema_close " +
                 "  FROM " + sym + "predset " +
                 ") " +
                 "UPDATE " + sym + "predset d " +
                 "SET ema_close = COALESCE(ema.ema_close, d.close) " +
                 "FROM ema " +
                 "WHERE d.id = ema.id " +
                 "AND (d.ema_close IS NULL OR d.ema_close <= 0);";
    
    return databaseClient.sql(sql).fetch().rowsUpdated().then();
}

    public Mono<Void> calculateATR(String sym) {
        String sql = "WITH tr AS ( " +
                     "SELECT id, " +
                     "GREATEST(high - low, " +
                     "ABS(high - LAG(close) OVER (ORDER BY id)), " +
                     "ABS(low - LAG(close) OVER (ORDER BY id))) AS true_range " +
                     "FROM " + sym + "predset), " +
                     "atr_values AS ( " +
                     "SELECT id, " +
                     "AVG(true_range) OVER (ORDER BY id ROWS BETWEEN 13 PRECEDING AND CURRENT ROW) AS atr " +
                     "FROM tr) " +
                     "UPDATE " + sym + "predset d " +
                     "SET atr = COALESCE(atr_values.atr, 1) " +
                     "FROM atr_values " +
                     "WHERE d.id = atr_values.id " +
                     "AND (d.atr IS NULL OR d.atr <= 0);";
        
        return databaseClient.sql(sql).fetch().rowsUpdated().then();
    }

    public Mono<Void> calculateTEMA(String sym) {
        String sql = "WITH ema12 AS ( " +
                     "SELECT id, AVG(close) OVER (ORDER BY id ROWS BETWEEN 11 PRECEDING AND CURRENT ROW) AS ema12 " +
                     "FROM " + sym + "predset), " +
                     "ema26 AS ( " +
                     "SELECT id, AVG(close) OVER (ORDER BY id ROWS BETWEEN 25 PRECEDING AND CURRENT ROW) AS ema26 " +
                     "FROM " + sym + "predset) " +
                     "UPDATE " + sym + "predset d " +
                     "SET tema = COALESCE((3 * ema12.ema12 - 3 * ema26.ema26), d.close) " +
                     "FROM ema12 " +
                     "JOIN ema26 USING (id) " +
                     "WHERE d.id = ema12.id " +
                     "AND (d.tema IS NULL OR d.tema <= 0);";
        
        return databaseClient.sql(sql).fetch().rowsUpdated().then();
    }

    public Mono<Void> calculateSMA(String sym) {
        String sql = "WITH sma AS ( " +
                     "SELECT id, " +
                     "AVG(close) OVER (ORDER BY id ROWS BETWEEN 19 PRECEDING AND CURRENT ROW) AS sma_tr " +
                     "FROM " + sym + "predset) " +
                     "UPDATE " + sym + "predset d " +
                     "SET sma_tr = COALESCE(sma.sma_tr, d.close) " +
                     "FROM sma " +
                     "WHERE d.id = sma.id " +
                     "AND (d.sma_tr IS NULL OR d.sma_tr <= 0);";
        
        return databaseClient.sql(sql).fetch().rowsUpdated().then();
    }

    public Mono<Void> calculateNWSmooth(String sym) {
        String sql = "WITH nw_smooth_calc AS ( " +
                     "SELECT id, " +
                     "AVG(close) OVER (ORDER BY id ROWS BETWEEN 5 PRECEDING AND CURRENT ROW) AS nw_smooth " +
                     "FROM " + sym + "predset) " +
                     "UPDATE " + sym + "predset d " +
                     "SET nw_smooth = COALESCE(nw_smooth_calc.nw_smooth, d.close) " +
                     "FROM nw_smooth_calc " +
                     "WHERE d.id = nw_smooth_calc.id " +
                     "AND (d.nw_smooth IS NULL OR d.nw_smooth <= 0);";
        
        return databaseClient.sql(sql).fetch().rowsUpdated().then();
    }

public Mono<Void> calculateNWSmoothOne(String sym) {
        // Construct the optimized SQL query
        String sql = "WITH nw_smooth_one_calc AS ( " +
                     "SELECT id, " +
                     "AVG(close) OVER (ORDER BY id ROWS BETWEEN 10 PRECEDING AND CURRENT ROW) AS nw_smooth_one " +
                     "FROM " + sym + "predset) " +
                     "UPDATE " + sym + "predset d " +
                     "SET nw_smooth_one = COALESCE(nw_smooth_one_calc.nw_smooth_one, d.close) " +
                     "FROM nw_smooth_one_calc " +
                     "WHERE d.id = nw_smooth_one_calc.id " +
                     "AND (d.nw_smooth_one IS NULL OR d.nw_smooth_one <= 0);";
        
        // Execute the SQL query and return the result
        return databaseClient.sql(sql).fetch().rowsUpdated().then();
    }

    public Mono<Void> calculateNWSmoothTwo(String sym) {
        // Construct the optimized SQL query
        String sql = "WITH nw_smooth_two_calc AS ( " +
                     "SELECT id, " +
                     "AVG(close) OVER (ORDER BY id ROWS BETWEEN 20 PRECEDING AND CURRENT ROW) AS nw_smooth_two " +
                     "FROM " + sym + "predset) " +
                     "UPDATE " + sym + "predset d " +
                     "SET nw_smooth_two = COALESCE(nw_smooth_two_calc.nw_smooth_two, d.close) " +
                     "FROM nw_smooth_two_calc " +
                     "WHERE d.id = nw_smooth_two_calc.id " +
                     "AND (d.nw_smooth_two IS NULL OR d.nw_smooth_two <= 0);";
        
        // Execute the SQL query and return the result
        return databaseClient.sql(sql).fetch().rowsUpdated().then();
    }

public Mono<Void> calculateNWDerivative(String sym) {
    // Construct the SQL query
    String sql = "WITH nw_derivative_calc AS ( " +
                 "SELECT id, " +
                 "(close - LAG(close) OVER (ORDER BY id)) AS nw_derivative " +
                 "FROM " + sym + "predset) " +
                 "UPDATE " + sym + "predset AS d " +
                 "SET nw_derivative = COALESCE(nw_derivative_calc.nw_derivative, 0) " +
                 "FROM nw_derivative_calc " +
                 "WHERE d.id = nw_derivative_calc.id " +
                 "AND (d.nw_derivative IS NULL OR d.nw_derivative <= 0);";
    
    // Execute the SQL query and return the result
    return databaseClient.sql(sql).fetch().rowsUpdated().then();
}

}