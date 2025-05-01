package com.jat.jatbot.dbhandlers;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CsvUploadService {
    private final DatabaseClient databaseClient;

    public CsvUploadService(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Mono<Void> uploadDataSetsToDb(List<DataPoint> Data, String sym) throws IOException {
        System.out.println("Uploading....");
        return Flux.fromIterable(Data)
                .flatMap(data -> insertDataPointWithFeatures(data, sym))
                .then()
                .doOnTerminate(() -> System.out.println("Finished uploading data for: " + sym));
    }

    public Mono<Void> insertDataPointWithFeatures(DataPoint ohlcData, String sym) {
        String checkSql = "SELECT COUNT(*) FROM " + sym + "dataset WHERE timestamp = $1";

        String insertSql = "INSERT INTO " + sym + "dataset " +
                "(timestamp, open, high, low, close, volume) " +
                "VALUES ($1, $2, $3, $4, $5, $6);";

        return databaseClient.sql(checkSql)
                .bind("$1", ohlcData.getDateTime())
                .fetch()
                .one()
                .map(result -> (Long) result.values().iterator().next())
                .filter(count -> count == 0)
                .flatMap(count -> databaseClient.sql(insertSql)
                        .bind("$1", ohlcData.getDateTime())
                        .bind("$2", ohlcData.getOpen())
                        .bind("$3", ohlcData.getHigh())
                        .bind("$4", ohlcData.getLow())
                        .bind("$5", ohlcData.getClose())
                        .bind("$6", ohlcData.getVolume())
                        .fetch()
                        .rowsUpdated())
                .then();

    }

    private void log(String message, String sym) {
        System.out.println(message + " for symbol: " + sym);
    }
    public Mono<Void> insertComputableFeatures(String sym){
        return calculateVWAP(sym)
        .doOnSubscribe(s ->  log("Starting VWAP calculation ", sym))
        .doOnSuccess(v ->  log("Completed VWAP calculation ", sym))
        .then(calculateRSI(sym).doOnSubscribe(s ->  log("Starting RSI calculation ", sym))
        .doOnSuccess(v ->  log("Completed RSI calculation ", sym)))
        .then(calculateMACD(sym).doOnSubscribe(s ->  log("Starting MACD calculation ", sym))
        .doOnSuccess(v ->  log("Completed MACD calculation ", sym)))
        .then(calculateROC(sym).doOnSubscribe(s ->  log("Starting ROC calculation ", sym))
        .doOnSuccess(v ->  log("Completed ROC calculation ", sym)))
        .then(calculateTEMA(sym).doOnSubscribe(s ->  log("Starting TEMA calculation ", sym))
        .doOnSuccess(v ->  log("Completed TEMA calculation ", sym)))
        .then(calculateATR(sym).doOnSubscribe(s ->  log("Starting ATR calculation ", sym))
        .doOnSuccess(v ->  log("Completed ATR calculation ", sym)))
        .then(calculateEMA(sym).doOnSubscribe(s ->  log("Starting EMA calculation ", sym))
        .doOnSuccess(v ->  log("Completed EMA calculation ", sym)))
        .then(calculateSMA(sym).doOnSubscribe(s ->  log("Starting SMA calculation ", sym))
        .doOnSuccess(v ->  log("Completed SMA calculation ", sym)))
        .then(calculateNWSmooth(sym).doOnSubscribe(s ->  log("Starting NW Smooth calculation ", sym))
        .doOnSuccess(v ->  log("Completed NW Smooth calculation ", sym)))
        .then(calculateNWSmoothOne(sym).doOnSubscribe(s ->  log("Starting NW Smooth One calculation ", sym))
        .doOnSuccess(v ->  log("Completed NW Smooth One calculation ", sym)))
        .then(calculateNWSmoothTwo(sym).doOnSubscribe(s ->  log("Starting NW Smooth Two calculation ", sym))
        .doOnSuccess(v ->  log("Completed NW Smooth Two calculation ", sym)))
        .then(calculateNWDerivative(sym).doOnSubscribe(s ->  log("Starting NW Derivative calculation ", sym))
        .doOnSuccess(v ->  log("Completed NW Derivative calculation ", sym)));
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
    
    private Mono<Void> checkAndCalculate(String sym, String column, Function<String, Mono<Void>> calculationFunction) {
        String query = "SELECT 1 FROM " + sym + "dataset WHERE " + column + " IS NULL LIMIT 1";
    
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


    public Mono<Void> checkDataSetTableExists(String sym) {
        String createIndexSql = "CREATE INDEX IF NOT EXISTS idx_" + sym + "_timestamp ON " + sym
        + "dataset(timestamp);";
        String createDataSetTableSql = "CREATE TABLE IF NOT EXISTS " + sym +
                "dataset (id BIGSERIAL PRIMARY KEY,\n" + //
                "    timestamp TIMESTAMP NOT NULL,\n" + //
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
                "    nw_derivative DOUBLE PRECISION\n" +
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

    public Mono<Void> calculateVWAP(String sym) {
        String sql = "UPDATE " + sym + "dataset AS d " +
                "SET vwap = COALESCE((" +
                "SELECT SUM(volume * close) / NULLIF(SUM(volume), 0) " +
                "FROM " + sym + "dataset AS sub " +
                "WHERE sub.timestamp <= d.timestamp" +
                "), d.close) " +
                "WHERE vwap IS NULL OR vwap <= 0;";

        return databaseClient.sql(sql).fetch().rowsUpdated().then();
    }

    public Mono<Void> calculateRSI(String sym) {
        String sql = "CREATE TEMP TABLE price_changes AS " +
                     "SELECT timestamp, " +
                     "       close - LAG(close) OVER (ORDER BY timestamp) AS price_change " +
                     "FROM " + sym + "dataset; " +
                     
                     "CREATE TEMP TABLE gains_losses AS " +
                     "SELECT timestamp, " +
                     "       CASE WHEN price_change > 0 THEN price_change ELSE 0 END AS gain, " +
                     "       CASE WHEN price_change < 0 THEN -price_change ELSE 0 END AS loss " +
                     "FROM price_changes; " +
                     
                     "CREATE TEMP TABLE rolling_sums AS " +
                     "SELECT timestamp, " +
                     "       SUM(gain) OVER (ORDER BY timestamp ROWS BETWEEN 13 PRECEDING AND CURRENT ROW) AS sum_gains, " +
                     "       SUM(loss) OVER (ORDER BY timestamp ROWS BETWEEN 13 PRECEDING AND CURRENT ROW) AS sum_losses " +
                     "FROM gains_losses; " +
                     
                     "UPDATE " + sym + "dataset d " +
                     "SET rsi = COALESCE( " +
                     "  CASE " +
                     "    WHEN rs.sum_losses = 0 THEN 100 " +  // Prevent division by zero
                     "    ELSE 100 - (100 / (1 + rs.sum_gains / rs.sum_losses)) " +
                     "  END, " +
                     "  50) " + // Default to 50 if RSI cannot be calculated
                     "FROM rolling_sums rs " +
                     "WHERE d.timestamp = rs.timestamp " +
                     "AND (d.rsi IS NULL OR d.rsi <= 0); " +
                     
                     // Cleanup: Drop the temporary tables
                     "DROP TABLE price_changes; " +
                     "DROP TABLE gains_losses; " +
                     "DROP TABLE rolling_sums;";
    
        return databaseClient.sql(sql).fetch().rowsUpdated().then();
    }
public Mono<Void> calculateMACD(String sym) {
    String sql = "WITH ema12 AS ( " +
                 "  SELECT timestamp, AVG(close) OVER (ORDER BY timestamp ROWS BETWEEN 11 PRECEDING AND CURRENT ROW) AS ema12 " +
                 "  FROM " + sym + "dataset), " +
                 "ema26 AS ( " +
                 "  SELECT timestamp, AVG(close) OVER (ORDER BY timestamp ROWS BETWEEN 25 PRECEDING AND CURRENT ROW) AS ema26 " +
                 "  FROM " + sym + "dataset) " +
                 "UPDATE " + sym + "dataset d " +
                 "SET macd = COALESCE((ema12.ema12 - ema26.ema26), 0) " +
                 "FROM ema12 " +
                 "JOIN ema26 ON ema12.timestamp = ema26.timestamp " +
                 "WHERE d.timestamp = ema12.timestamp " +
                 "AND (d.macd IS NULL OR d.macd = 0);";
    
    return databaseClient.sql(sql).fetch().rowsUpdated().then();
}

public Mono<Void> calculateROC(String sym) {
        String calculateROCSql = "WITH roc_values AS ( " +
                                 "  SELECT timestamp, " +
                                 "         (close - LAG(close, 12) OVER (ORDER BY timestamp)) / " +
                                 "         LAG(close, 12) OVER (ORDER BY timestamp) * 100 AS roc " +
                                 "  FROM " + sym + "dataset " +
                                 ") " +
                                 "UPDATE " + sym + "dataset d " +
                                 "SET roc = COALESCE(rv.roc, 0) " +
                                 "FROM roc_values rv " +
                                 "WHERE d.timestamp = rv.timestamp " +
                                 "AND (d.roc IS NULL OR d.roc <= 0);";
        
        return databaseClient.sql(calculateROCSql)
                             .fetch()
                             .rowsUpdated()
                             .then();
    }

public Mono<Void> calculateEMA(String sym) {
    String sql = "WITH ema AS ( " +
                 "  SELECT timestamp, " +
                 "         close * (2 / (12 + 1)) + " +
                 "         LAG(close) OVER (ORDER BY timestamp) * (1 - (2 / (12 + 1))) AS ema_close " +
                 "  FROM " + sym + "dataset " +
                 ") " +
                 "UPDATE " + sym + "dataset d " +
                 "SET ema_close = COALESCE(ema.ema_close, d.close) " +
                 "FROM ema " +
                 "WHERE d.timestamp = ema.timestamp " +
                 "AND (d.ema_close IS NULL OR d.ema_close <= 0);";
    
    return databaseClient.sql(sql).fetch().rowsUpdated().then();
}

    public Mono<Void> calculateATR(String sym) {
        String sql = "WITH tr AS ( " +
                     "SELECT timestamp, " +
                     "GREATEST(high - low, " +
                     "ABS(high - LAG(close) OVER (ORDER BY timestamp)), " +
                     "ABS(low - LAG(close) OVER (ORDER BY timestamp))) AS true_range " +
                     "FROM " + sym + "dataset), " +
                     "atr_values AS ( " +
                     "SELECT timestamp, " +
                     "AVG(true_range) OVER (ORDER BY timestamp ROWS BETWEEN 13 PRECEDING AND CURRENT ROW) AS atr " +
                     "FROM tr) " +
                     "UPDATE " + sym + "dataset d " +
                     "SET atr = COALESCE(atr_values.atr, 1) " +
                     "FROM atr_values " +
                     "WHERE d.timestamp = atr_values.timestamp " +
                     "AND (d.atr IS NULL OR d.atr <= 0);";
        
        return databaseClient.sql(sql).fetch().rowsUpdated().then();
    }

    public Mono<Void> calculateTEMA(String sym) {
        String sql = "WITH ema12 AS ( " +
                     "SELECT timestamp, AVG(close) OVER (ORDER BY timestamp ROWS BETWEEN 11 PRECEDING AND CURRENT ROW) AS ema12 " +
                     "FROM " + sym + "dataset), " +
                     "ema26 AS ( " +
                     "SELECT timestamp, AVG(close) OVER (ORDER BY timestamp ROWS BETWEEN 25 PRECEDING AND CURRENT ROW) AS ema26 " +
                     "FROM " + sym + "dataset) " +
                     "UPDATE " + sym + "dataset d " +
                     "SET tema = COALESCE((3 * ema12.ema12 - 3 * ema26.ema26), d.close) " +
                     "FROM ema12 " +
                     "JOIN ema26 USING (timestamp) " +
                     "WHERE d.timestamp = ema12.timestamp " +
                     "AND (d.tema IS NULL OR d.tema <= 0);";
        
        return databaseClient.sql(sql).fetch().rowsUpdated().then();
    }

    public Mono<Void> calculateSMA(String sym) {
        String sql = "WITH sma AS ( " +
                     "SELECT timestamp, " +
                     "AVG(close) OVER (ORDER BY timestamp ROWS BETWEEN 19 PRECEDING AND CURRENT ROW) AS sma_tr " +
                     "FROM " + sym + "dataset) " +
                     "UPDATE " + sym + "dataset d " +
                     "SET sma_tr = COALESCE(sma.sma_tr, d.close) " +
                     "FROM sma " +
                     "WHERE d.timestamp = sma.timestamp " +
                     "AND (d.sma_tr IS NULL OR d.sma_tr <= 0);";
        
        return databaseClient.sql(sql).fetch().rowsUpdated().then();
    }

    public Mono<Void> calculateNWSmooth(String sym) {
        String sql = "WITH nw_smooth_calc AS ( " +
                     "SELECT timestamp, " +
                     "AVG(close) OVER (ORDER BY timestamp ROWS BETWEEN 5 PRECEDING AND CURRENT ROW) AS nw_smooth " +
                     "FROM " + sym + "dataset) " +
                     "UPDATE " + sym + "dataset d " +
                     "SET nw_smooth = COALESCE(nw_smooth_calc.nw_smooth, d.close) " +
                     "FROM nw_smooth_calc " +
                     "WHERE d.timestamp = nw_smooth_calc.timestamp " +
                     "AND (d.nw_smooth IS NULL OR d.nw_smooth <= 0);";
        
        return databaseClient.sql(sql).fetch().rowsUpdated().then();
    }

public Mono<Void> calculateNWSmoothOne(String sym) {
        // Construct the optimized SQL query
        String sql = "WITH nw_smooth_one_calc AS ( " +
                     "SELECT timestamp, " +
                     "AVG(close) OVER (ORDER BY timestamp ROWS BETWEEN 10 PRECEDING AND CURRENT ROW) AS nw_smooth_one " +
                     "FROM " + sym + "dataset) " +
                     "UPDATE " + sym + "dataset d " +
                     "SET nw_smooth_one = COALESCE(nw_smooth_one_calc.nw_smooth_one, d.close) " +
                     "FROM nw_smooth_one_calc " +
                     "WHERE d.timestamp = nw_smooth_one_calc.timestamp " +
                     "AND (d.nw_smooth_one IS NULL OR d.nw_smooth_one <= 0);";
        
        // Execute the SQL query and return the result
        return databaseClient.sql(sql).fetch().rowsUpdated().then();
    }

    public Mono<Void> calculateNWSmoothTwo(String sym) {
        // Construct the optimized SQL query
        String sql = "WITH nw_smooth_two_calc AS ( " +
                     "SELECT timestamp, " +
                     "AVG(close) OVER (ORDER BY timestamp ROWS BETWEEN 20 PRECEDING AND CURRENT ROW) AS nw_smooth_two " +
                     "FROM " + sym + "dataset) " +
                     "UPDATE " + sym + "dataset d " +
                     "SET nw_smooth_two = COALESCE(nw_smooth_two_calc.nw_smooth_two, d.close) " +
                     "FROM nw_smooth_two_calc " +
                     "WHERE d.timestamp = nw_smooth_two_calc.timestamp " +
                     "AND (d.nw_smooth_two IS NULL OR d.nw_smooth_two <= 0);";
        
        // Execute the SQL query and return the result
        return databaseClient.sql(sql).fetch().rowsUpdated().then();
    }

public Mono<Void> calculateNWDerivative(String sym) {
    // Construct the SQL query
    String sql = "WITH nw_derivative_calc AS ( " +
                 "SELECT timestamp, " +
                 "(close - LAG(close) OVER (ORDER BY timestamp)) AS nw_derivative " +
                 "FROM " + sym + "dataset) " +
                 "UPDATE " + sym + "dataset AS d " +
                 "SET nw_derivative = COALESCE(nw_derivative_calc.nw_derivative, 0) " +
                 "FROM nw_derivative_calc " +
                 "WHERE d.timestamp = nw_derivative_calc.timestamp " +
                 "AND (d.nw_derivative IS NULL OR d.nw_derivative <= 0);";
    
    // Execute the SQL query and return the result
    return databaseClient.sql(sql).fetch().rowsUpdated().then();
}

}
