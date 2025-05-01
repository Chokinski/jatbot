package com.jat.jatbot.dbhandlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

@Service
public class DataParser {
 
    public boolean preExists = false;
    public boolean labelsExists = false;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public CompletableFuture<List<DataPoint>> parseOHLCFile(Path filePath) {

        return CompletableFuture.supplyAsync(() -> {
            List<DataPoint> ohlcData = new ArrayList<>();
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
                    ohlcData.add(new DataPoint(timestamp, open, high, low, close, volume));
                    
                }
            } catch (IOException e) {
                throw new RuntimeException("Error parsing OHLC file: " + e.getMessage(), e);
            }
            return ohlcData;
        });
    }
        public String checkCSVExistWithSymbol(String sym) throws IOException {
        Path dir = Paths.get(System.getProperty("user.home"), "Desktop", "datatovis");
        preExists = false;
        labelsExists = false;
        // Walk the directory tree and filter files ending with "DATASET.TXT"
        try (Stream<Path> paths = Files.walk(dir)) {
            List<Path> filteredPaths = paths
                .filter(Files::isRegularFile)  // Ensure we are dealing with files
                .filter(p -> p.getFileName().toString().startsWith(sym.toUpperCase()))  // Match files ending with "DATASET.TXT"
                .collect(Collectors.toList());  // Collect to a list
            // Check if no files were found
            if (filteredPaths.isEmpty()) {
                System.out.println("No files matching csvs found.");
                return null;
            }

            // Iterate through the filtered list
            filteredPaths.forEach(p -> {
                System.out.println(p.getFileName().toString());
                if(p.getFileName().toString().endsWith("predictions.csv"))
                {
                    this.preExists = true;
                }
                if (p.getFileName().toString().endsWith("labels.csv"))
                {
                    this.labelsExists = true;
                }
                
            });
            if (this.preExists && this.labelsExists)
            {
                paths.close();
                return sym.toUpperCase();
            }
            else
            {
                System.out.println("File match found, but not both csvs found."+ "pre:"+preExists +"labels:"+ labelsExists);
                return null;
            }
            
        }
        catch (IOException e) {
            System.out.println("An error occurred while checking for files.");
            return null;
        }
    }
    public Map<String,Path> getDatasetFilesWithPrefix() throws IOException {
        Path jatDirectory = Paths.get(System.getProperty("user.home"), "JAT");
        Map<String,Path> datasetFiles = new HashMap<>();
        // Debugging: Print out the resolved directory
        System.out.println("JAT Directory: " + jatDirectory.toString());

        // Walk the directory tree and filter files ending with "DATASET.TXT"
        try (Stream<Path> paths = Files.walk(jatDirectory)) {
            List<Path> filteredPaths = paths
                .filter(Files::isRegularFile)  // Ensure we are dealing with files
                .filter(p -> p.getFileName().toString().endsWith("DATASET.txt"))  // Match files ending with "DATASET.TXT"
                .collect(Collectors.toList());  // Collect to a list

            // Check if no files were found
            if (filteredPaths.isEmpty()) {
                System.out.println("No files matching 'DATASET.TXT' found.");
            }

            // Iterate through the filtered list
            filteredPaths.forEach(p -> {
                // Extract prefix before "DATASET.TXT"
                String filename = p.getFileName().toString();
                String prefix = filename.replace("DATASET.txt", "");
                // Add the path and prefix to the map
                
                // Print both the path and the prefix
                System.out.println("Path: " + p.toString() + ", Prefix: " + prefix);
                datasetFiles.put(prefix, p);
            });

            return datasetFiles;  // Return the list of filtered paths
        }
    }

    
}
