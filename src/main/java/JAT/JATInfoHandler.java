package JAT;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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

    public ObservableList<OHLCData> parseOHLCFile(Path filePath) {
        CompletableFuture<ObservableList<OHLCData>> future = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
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
                future.complete(ohlcData);
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        });
        
        return future.join();
    }

    public Map<Path, String> assetFileFind(String sym) {
        CompletableFuture<Map<Path, String>> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
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
                future.complete(fileMap);
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        });

        return future.join();
    }


}
