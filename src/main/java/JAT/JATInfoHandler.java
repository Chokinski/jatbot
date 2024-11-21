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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jat.OHLCData;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class JATInfoHandler {
static Path jatDir = Paths.get(System.getProperty("user.home"), "JAT");
    final Path jatConfigPath = Paths.get(System.getProperty("user.home"), "JAT", "JATconfig.properties");
    public String[] loadProperties() {
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


    public CompletableFuture<Void> asyncWrite(StringBuilder sb, Path cfg) {
        ByteBuffer buffer = ByteBuffer.wrap(sb.toString().getBytes());
    
        try {
            AsynchronousFileChannel aChannel = AsynchronousFileChannel.open(
                cfg, StandardOpenOption.WRITE, StandardOpenOption.CREATE
            );
    
            // Use CompletableFuture to encapsulate the async write logic
            return CompletableFuture.runAsync(() -> {
                try {
                    aChannel.write(buffer, 0).get(); // Wait for completion
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException("Error during async write: " + e.getMessage(), e);
                }
            }).whenComplete((result, error) -> {
                try {
                    aChannel.close(); // Ensure channel is closed after writing
                } catch (IOException e) {
                    JATbot.botLogger.error("Error closing file channel: " + e.getMessage());
                }
    
                if (error != null) {
                    JATbot.botLogger.error("Async write failed: " + error.getMessage());
                }
            });
    
        } catch (IOException e) {
            return CompletableFuture.failedFuture(new RuntimeException("Failed to open file channel", e));
        }
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

    //create a parser that takes the  filepath and returns the last OHLCData object

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
public static CompletableFuture<Boolean> isOutdated (LocalDateTime timestamp, Path filePath) {
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
        // Return a CompletableFuture that will be completed when the async read is done.
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
        // Return a CompletableFuture that will be completed when the async read is done.
        return parseLastOHLCData(filePath).thenApply(ohlcData -> {
            if (ohlcData != null) {
                return ohlcData;
            } else {
                // Handle case where no data is found (e.g., file is empty or malformed)
                throw new RuntimeException("No OHLC data found in the file.");
            }
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

    //This will test file finder, and return file paths and timeframes.
    public static void testFileFinder(String sym) {
        assetFileFind(sym).thenCompose(fileMap -> {
            if (fileMap.isEmpty()) {
                System.out.println("No files found for " + sym + ".");
                return CompletableFuture.completedFuture(null);
            } else {
                // Sequentially process each file
                CompletableFuture<Void> chain = CompletableFuture.completedFuture(null); // Start with a completed future
    
                for (Map.Entry<Path, String> entry : fileMap.entrySet()) {
                    Path filePath = entry.getKey();
                    String timeframe = entry.getValue();
    
                    // Chain the getLastTimestamp for each file, ensuring that the previous task completes first
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


}
