package com.jat.jatbot.dbhandlers;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import com.jat.jatbot.ai.robot;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/data")
public class CsvUploadController {
    private final CsvUploadService csvUploadService;
    private final DataParser dataParser;
    
    public CsvUploadController(CsvUploadService csvUploadService,DataParser dataParser) {
        this.csvUploadService = csvUploadService;
        this.dataParser = dataParser;
    }

    @PostMapping("/test")
    public Mono<String> test() {
        return Mono.just("Test endpoint is working!");
    }

    @PostMapping("/upload/dataset/{sym}")
    public Mono<String> uploadDataset(@PathVariable("sym") String sym) throws IOException{
        
            List<DataPoint> dataset = dataParser.parseOHLCFile(Paths.get(System.getProperty("user.home"), "JAT", sym+"DATASET.txt")).join();
            csvUploadService.checkDataSetTableExists(sym).then(Mono.fromRunnable(() -> {try {
                csvUploadService.uploadDataSetsToDb(dataset,sym).then(
                    Mono.fromRunnable(() ->{csvUploadService.checkAndRecalculateMissingFeatures(sym).subscribe();})).doOnSubscribe((s) -> {
                        System.out.println("Upload for "+sym+" started.");
                    }).doOnSuccess((s)->{
                        System.out.println("Upload for "+sym+" finished.");
                        
                    }).subscribe();
                    
                //csvUploadService.calculateROC(sym).subscribe();
            }
            
            catch (IOException e) {
                e.printStackTrace();
                // handle the error
                
            }
        })).subscribe();
        return Mono.just("Uploading "+ sym+" Dataset beginning....");    
    }
    public Mono<String> uploadDatasetWithSym(String sym) throws IOException{
        
        List<DataPoint> dataset = dataParser.parseOHLCFile(Paths.get(System.getProperty("user.home"), "JAT", sym+"DATASET.txt")).join();
        csvUploadService.checkDataSetTableExists(sym).then(Mono.fromRunnable(() -> {try {
            csvUploadService.uploadDataSetsToDb(dataset,sym).then(
                Mono.fromRunnable(() ->{csvUploadService.checkAndRecalculateMissingFeatures(sym).subscribe();})).doOnSubscribe((s) -> {
                    System.out.println("Upload for "+sym+" started.");
                }).doOnSuccess((s)->{
                    System.out.println("Upload for "+sym+" finished.");
                    
                }).subscribe();
                
            //csvUploadService.calculateROC(sym).subscribe();
        }
        
        catch (IOException e) {
            e.printStackTrace();
            // handle the error
            
        }
    })).block();
    return Mono.just("Uploading "+ sym+" Dataset beginning....");    
}



    @PostMapping("/upload/datasets")
public Mono<String> uploadDatasets() throws IOException {
    return Flux.fromIterable(dataParser.getDatasetFilesWithPrefix().entrySet())
        .flatMap(entry -> {
            String sym = entry.getKey();
            Path data = entry.getValue();

            List<DataPoint> dataset = dataParser.parseOHLCFile(data).join();

            try {
                return csvUploadService.checkDataSetTableExists(sym)
                    .then(csvUploadService.uploadDataSetsToDb(dataset, sym))
                    .then(csvUploadService.checkAndRecalculateMissingFeatures(sym))
                    .doOnSubscribe(s -> System.out.println("Upload for " + sym + " started."))
                    .doOnSuccess(s -> System.out.println("Upload for " + sym + " finished."));
            } catch (IOException e) {
                e.printStackTrace();
                return Mono.empty(); // Return an empty Mono to satisfy the return type
            }
        })
        .then(Mono.just("Uploading all datasets and batch file execution complete."));
}


}