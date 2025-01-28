package JAT;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jat.OHLCData;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.jacobpeterson.alpaca.openapi.trader.model.Assets;

public class JATInfoHandler {
    static Path jatDir = Paths.get(System.getProperty("user.home"), "JAT");
    final static Path jatConfigPath = Paths.get(System.getProperty("user.home"), "JAT", "JATconfig.properties");
    public AtomicInteger count = new AtomicInteger(1);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public boolean isRateLimited = false;
    private final ExecutorService executorService;
    private final Semaphore semaphore;
    public JATInfoHandler() {
        this.isRateLimited = false; // Flag to track rate limit status
        this.count = new AtomicInteger(1);

         this.executorService = Executors.newFixedThreadPool(200);
        this.semaphore = new Semaphore(200); // Semaphore for limiting concurrency
    }

    public static String[] loadProperties() {
        // check if JAT directory exists, if not create it
        if (!Files.exists(jatDir)) {
            try {
                Files.createDirectory(jatDir);
            } catch (IOException e) {
                JATbot.botLogger.error("Error creating JAT directory: " + e.getMessage());
            }
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
            if (!properties.containsKey("key_id") || !properties.containsKey("secret_key")
                    || !properties.containsKey("type") || !properties.containsKey("source")
                    || !properties.containsKey("remMe")) {
                JATbot.botLogger.error("Error loading properties: key_id, secret_key, type, or source not found");

                properties = writeProps(jatConfigPath, properties);
            }
            String keyID = properties.getProperty("key_id");
            String secretKey = properties.getProperty("secret_key");
            String type = properties.getProperty("type");
            String source = properties.getProperty("source");
            String remMe = properties.getProperty("remMe");

            return new String[] { keyID, secretKey, type, source, remMe };
        } catch (IOException e) {
            JATbot.botLogger.error("\nError loading properties, IO exception: " + e.getMessage());
        }

        return null;
    }

    // New method to modify a single property
    public void modifyProperty(Path config, String key, String value) {
        Properties props = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(config)) {
            props.load(reader);
        } catch (IOException e) {
            JATbot.botLogger.error("Error loading properties: " + e.getMessage());
        }
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

    public static CompletableFuture<Void> asyncWrite(StringBuilder sb, Path cfg) {
        ByteBuffer buffer = ByteBuffer.wrap(sb.toString().getBytes());
        buffer.rewind(); // Ensure position is reset

        try {
            AsynchronousFileChannel aChannel = AsynchronousFileChannel.open(
                    cfg, StandardOpenOption.WRITE, StandardOpenOption.CREATE);

            long position = aChannel.size(); // Start writing at the end of the file for append

            return CompletableFuture.runAsync(() -> {
                try {
                    JATbot.botLogger.info("Writing {} bytes to {}", sb.length(), cfg);
                    aChannel.write(buffer, position).get(); // Perform the async write
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException("Error during async write for " + cfg.toString(), e);
                }
            }).whenComplete((result, error) -> {
                try {
                    aChannel.close(); // Always close the channel
                    JATbot.botLogger.info("File channel closed for {}", cfg);
                } catch (IOException e) {
                    JATbot.botLogger.error("Error closing file channel: {}", e.getMessage());
                }

                if (error != null) {
                    JATbot.botLogger.error("Async write failed for {}: {}", cfg, error.getMessage());
                } else {
                    JATbot.botLogger.info("Async write completed for file: {}", cfg);
                }
            });

        } catch (IOException e) {
            JATbot.botLogger.error("Failed to open file channel for {}: {}", cfg, e.getMessage());
            return CompletableFuture.failedFuture(new RuntimeException("Failed to open file channel", e));
        }
    }

    public static Properties writeProps(Path config, Properties props) {
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

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public CompletableFuture<ObservableList<OHLCData>> parseOHLCFile(Path filePath) {

        return CompletableFuture.supplyAsync(() -> {
            ObservableList<OHLCData> ohlcData = FXCollections.observableArrayList();
            try (BufferedReader reader = Files.newBufferedReader(filePath)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.replace("OHLCData{", "").replace("}", "").trim();
                    String[] parts = line.split(", ");
                    String timestampStr = parts[0].substring(parts[0].indexOf('=') + 1).trim();
                    LocalDateTime timestamp = LocalDateTime.parse(timestampStr, formatter);
                    double open = Double.parseDouble(parts[1].substring(parts[1].indexOf('=') + 1));
                    double high = Double.parseDouble(parts[2].substring(parts[2].indexOf('=') + 1));
                    double low = Double.parseDouble(parts[3].substring(parts[3].indexOf('=') + 1));
                    double close = Double.parseDouble(parts[4].substring(parts[4].indexOf('=') + 1));
                    double volume = Double.parseDouble(parts[5].substring(parts[5].indexOf('=') + 1));
                    OHLCData ohlc = new OHLCData(timestamp, open, high, low, close, volume);
                    ohlcData.add(ohlc);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error parsing OHLC file: " + e.getMessage(), e);
            }
            return ohlcData;
        });
    }

    // create a parser that takes the filepath and returns the last OHLCData object

    public static CompletableFuture<OHLCData> parseLastOHLCData(Path filePath) {
        return CompletableFuture.supplyAsync(() -> {
            OHLCData ohlcData = null;
            try (BufferedReader reader = Files.newBufferedReader(filePath)) {
                String line;
                String lastLine = null;

                // Read through the file and store the last line
                while ((line = reader.readLine()) != null) {
                    lastLine = line;
                }

                // If we have a last line, process it
                if (lastLine != null) {
                    lastLine = lastLine.replace("OHLCData{", "").replace("}", "").trim();
                    String[] parts = lastLine.split(", ");

                    // Extract and parse the components from the last line
                    String timestampStr = parts[0].substring(parts[0].indexOf('=') + 1).trim();
                    LocalDateTime timestamp = LocalDateTime.parse(timestampStr, formatter);
                    double open = Double.parseDouble(parts[1].substring(parts[1].indexOf('=') + 1));
                    double high = Double.parseDouble(parts[2].substring(parts[2].indexOf('=') + 1));
                    double low = Double.parseDouble(parts[3].substring(parts[3].indexOf('=') + 1));
                    double close = Double.parseDouble(parts[4].substring(parts[4].indexOf('=') + 1));
                    double volume = Double.parseDouble(parts[5].substring(parts[5].indexOf('=') + 1));

                    // Create the OHLCData object from the parsed values
                    ohlcData = new OHLCData(timestamp, open, high, low, close, volume);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error parsing OHLC file: " + e.getMessage(), e);
            }
            return ohlcData;
        });
    }

    public static CompletableFuture<Void> removeLastOhlcData(Path filePath) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Read all lines from the file
                List<String> lines = Files.readAllLines(filePath);

                // If the file has content, remove the last line
                if (!lines.isEmpty()) {
                    lines.remove(lines.size() - 1);
                }

                // Rewrite the file with the remaining lines (if any)
                Files.write(filePath, lines, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Error processing the file: " + e.getMessage(), e);
            }
        });
    }

    public static CompletableFuture<Boolean> isOutdated(LocalDateTime timestamp, Path filePath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Read the last OHLCData object from the file
                OHLCData lastData = parseLastOHLCData(filePath).get();

                // Check if the timestamp is older than the last data timestamp
                boolean outdated = timestamp.isBefore(lastData.getDateTime());
                return outdated;
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Error checking timestamp: " + e.getMessage(), e);
            }
        });
    }

    public static CompletableFuture<String> getLastTimestamp(Path filePath) {
        // Return a CompletableFuture that will be completed when the async read is
        // done.
        return parseLastOHLCData(filePath).thenApply(ohlcData -> {
            if (ohlcData != null) {
                // If we have an OHLCData object, return its timestamp as a string
                return ohlcData.getDateTime().toString();
            } else {
                // Handle case where no data is found (e.g., file is empty or malformed)
                throw new RuntimeException("No OHLC data found in the file.");
            }
        });
    }

    public static CompletableFuture<OHLCData> returnLastOHLCData(Path filePath) {
        // Return a CompletableFuture that will be completed when the async read is
        // done.
        return parseLastOHLCData(filePath).thenApply(ohlcData -> {
            if (ohlcData != null) {
                return ohlcData;
            } else {
                // Handle case where no data is found (e.g., file is empty or malformed)
                throw new RuntimeException("No OHLC data found in the file.");
            }
        });
    }

    public static CompletableFuture<Boolean> doesFileExist(String sym, String timeframe) {
        return CompletableFuture.supplyAsync(() -> {
            // Construct the file path
            Path filePath = Paths.get(System.getProperty("user.home"), "JAT", sym + "_" + timeframe + ".txt");

            // Check if the file exists
            return Files.exists(filePath);
        });
    }

    public static CompletableFuture<Map<Path, String>> assetFileFind(String sym) {
        return CompletableFuture.supplyAsync(() -> {
            Map<Path, String> fileMap = new HashMap<>();
            Pattern pattern = Pattern.compile(Pattern.quote(sym) + "_(\\w+)\\.txt");

            try {
                Path searchDir = Paths.get(System.getProperty("user.home"), "JAT");
                Files.walkFileTree(searchDir, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        Matcher matcher = pattern.matcher(file.getFileName().toString());
                        if (matcher.matches()) {
                            String timeframe = matcher.group(1);
                            fileMap.put(file, timeframe);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException("Error searching files: " + e.getMessage(), e);
            }
            return fileMap;
        });
    }

    public static CompletableFuture<Path> assetFindWithTF(String sym, String tf) {
        return CompletableFuture.supplyAsync(() -> {
            // Construct the file path
            Path filePath = Paths.get(System.getProperty("user.home"), "JAT", sym + "_" + tf + ".txt");

            // Check if the file exists
            return filePath;
        });
    }

    public static CompletableFuture<Void> deleteAssetFile(String sym, String timeframe) {
        return CompletableFuture.runAsync(() -> {
            // Construct the file path
            Path filePath = Paths.get(System.getProperty("user.home"), "JAT", sym + "_" + timeframe + ".txt");

            try {
                if (Files.exists(filePath)) {
                    Files.delete(filePath); // Delete the file
                    JATbot.botLogger.info("Successfully deleted file: {}", filePath);
                } else {
                    JATbot.botLogger.info("File does not exist: {}", filePath);
                }
            } catch (IOException e) {
                JATbot.botLogger.error("Failed to delete file {}: {}", filePath, e.getMessage());
            }
        });
    }

    public static CompletableFuture<Void> deleteAssetFiles(Map<Path, String> fileMap) {
        // Perform file deletion asynchronously
        return CompletableFuture.runAsync(() -> {
            for (Map.Entry<Path, String> entry : fileMap.entrySet()) {
                Path filePath = entry.getKey();
                try {
                    Files.delete(filePath); // Delete the file
                    JATbot.botLogger.info("Successfully deleted file: {}", filePath);
                } catch (IOException e) {
                    JATbot.botLogger.error("Failed to delete file {}: {}", filePath, e.getMessage());
                }
            }
        });
    }

    public static CompletableFuture<Void> findAndDelete(String sym) {
        // Step 1: Find the existing asset files for the symbol
        return assetFileFind(sym)
                .thenCompose(fileMap -> {
                    // Step 2: Delete the existing asset files
                    return deleteAssetFiles(fileMap);
                });
    }

    // This will test file finder, and return file paths and timeframes.
    public static void testFileFinder(String sym) {
        assetFileFind(sym).thenCompose(fileMap -> {
            if (fileMap.isEmpty()) {
                System.out.println("No files found for " + sym + ".");
                return CompletableFuture.completedFuture(null);
            } else {
                // Sequentially process each file
                CompletableFuture<Void> chain = CompletableFuture.completedFuture(null); // Start with a completed
                                                                                         // future

                for (Map.Entry<Path, String> entry : fileMap.entrySet()) {
                    Path filePath = entry.getKey();
                    String timeframe = entry.getValue();

                    // Chain the getLastTimestamp for each file, ensuring that the previous task
                    // completes first
                    chain = chain.thenCompose(aVoid -> {
                        // Print the file and timeframe first
                        System.out.println("File: " + filePath + " Timeframe: " + timeframe);

                        // Now call getLastTimestamp and wait for its completion
                        return JATInfoHandler.getLastTimestamp(filePath).thenAccept(tstamp -> {
                            // Print the timestamp after the file path and timeframe
                            System.out.println("Timestamp printed for file: " + tstamp);
                        }).exceptionally(e -> {
                            // Handle any error that occurs in getLastTimestamp for a file
                            System.err.println("Error getting timestamp for file " + filePath + ": " + e.getMessage());
                            return null;
                        });
                    });
                }

                // Wait for all futures to complete sequentially
                return chain;
            }
        }).exceptionally(e -> {
            System.err.println("Error during file search: " + e.getMessage());
            e.printStackTrace();
            return null;
        }).join(); // Ensure the test waits for completion during debugging
    }

    /*
     * Currently this will take a list of assets and fetch then write stock data to
     * a file.
     * We're focusing on getting the list of asset symbols from our watchlist(w) to
     * prioritize fetching data for the
     * assets in the watchlist, then after we've checked if we already have, checked
     * if outdated, we will remove it
     * from the asset list(a) and continue for the next asset in the assets list.
     * 
     * 
     * 
     * 
     * 
     */
    public void dataFromList(
        List<Assets> aL, 
        List<String> w, 
        AlpacaStockHandler ash, 
        AlpacaAssetHandler aah, 
        List<String> timeframes) {
    
        List<Assets> assetsToRemove = new ArrayList<>(); // Store assets to remove
        List<String> symbolsProcessed = new ArrayList<>(); // Store processed symbols

        // Check if we are rate-limited before continuing
        checkRateLimit();
        if (this.isRateLimited) {
            // If rate-limited, wait for reset before proceeding
            waitForRateLimit();
        }

        // Now that rate limit is checked and potentially reset, proceed with fetching data
        for (String symbol : w) {
            if (!this.isRateLimited) { // Proceed if not rate-limited
                try {
                    // Acquire permit before starting a task
                    semaphore.acquire();

                    executorService.submit(() -> {
                        try {
                            aah.getAssetAsync(symbol, getCount()).thenAccept(asset -> {
                                if (asset != null) {
                                    addToRate(); // Increment count after processing
                                    processAssetData(asset, timeframes, ash);
                                    assetsToRemove.add(asset); // Add processed asset to remove list
                                }
                            }).exceptionally(e -> {
                                JATbot.botLogger.error("Error fetching asset {}: {}", symbol, e.getMessage());
                                return null;
                            });
                        } finally {
                            // Release the semaphore permit after task completion
                            semaphore.release();
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    JATbot.botLogger.error("Error acquiring semaphore permit: {}", e.getMessage());
                }
            }
        }

        // Process remaining assets in the list
        for (Assets asset : aL) {
            if (!symbolsProcessed.contains(asset.getSymbol()) && !this.isRateLimited) { // Avoid reprocessing and check rate limit
                addToRate(); // Increment counter after waiting

                try {
                    // Acquire permit before starting a task
                    semaphore.acquire();

                    executorService.submit(() -> {
                        try {
                            aah.getAssetAsync(asset.getSymbol(), getCount()).thenAccept(assetFetched -> {
                                if (assetFetched != null) {
                                    processAssetData(assetFetched, timeframes, ash);
                                    assetsToRemove.add(assetFetched); // Add processed asset to remove list
                                }
                            }).exceptionally(e -> {
                                JATbot.botLogger.error("Error fetching asset {}: {}", asset.getSymbol(), e.getMessage());
                                return null;
                            });
                        } finally {
                            // Release the semaphore permit after task completion
                            semaphore.release();
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    JATbot.botLogger.error("Error acquiring semaphore permit: {}", e.getMessage());
                }
            }
        }

        // Remove processed assets from the list after finishing all tasks
        aL.removeAll(assetsToRemove);
    }

    private void processAssetData(
        Assets asset,
        List<String> timeframes,
        AlpacaStockHandler ash) {
        for (String timeframe : timeframes) {
            ash.fetchAndWriteStockData(asset.getSymbol(), timeframe, getCount())
                .thenAccept(result -> {
                    JATbot.botLogger.info("Successfully fetched and wrote data for {} in timeframe {}", asset.getSymbol(), timeframe);
                })
                .exceptionally(e -> {
                    JATbot.botLogger.error("Error fetching/writing data for {} in timeframe {}: {}", asset.getSymbol(), timeframe, e.getMessage());
                    return null;
                });
        }
    }
    public boolean checkRateLimit() {
        if (this.count.get() >= 199) {
            this.isRateLimited = true;
            return true;
        } else {
            this.isRateLimited = false;
            return false;
        }


    }
    // Non-blocking rate limit handler with reset
// Non-blocking rate limit handler with reset
// Non-blocking rate limit handler with reset
public void waitForRateLimit() {
    if (checkRateLimit()) { // Trigger wait only when the limit is reached
        // Schedule a task to reset the count after the rate limit duration
        JATbot.botLogger.info("Rate limit reached. Waiting for {} seconds...", 70);

        // Schedule the reset task without blocking the current thread
        scheduler.schedule(() -> {
            synchronized (this) {
                resetRate(); // Reset counter after waiting
                JATbot.botLogger.info("Rate limit reset. Resuming requests...");
            }
        }, 70000, TimeUnit.MILLISECONDS);
        
        // Optionally, pause the thread that is calling waitForRateLimit() for a while
        // to prevent it from making additional requests while waiting.
        try {
            Thread.sleep(70000); // Sleep this thread until the reset completes
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            JATbot.botLogger.error("Rate limit wait interrupted: {}", e.getMessage());
        }
    }
}

    public void resetRate(){
        this.count.set(1);
    }
    public void addToRate() {

        this.count.incrementAndGet();
    }
    public AtomicInteger getCount() {

        return this.count;
    }
    public void shutdown() {
        executorService.shutdown();
    }
    
}
