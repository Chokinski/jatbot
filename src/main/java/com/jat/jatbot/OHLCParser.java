package com.jat.jatbot;
import com.jat.ctfxplotsplus.OHLCData;

import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.List;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class OHLCParser {


    public static long startTime =0;
    public static long readTime = 0;
    public static long startPrintTime = 0;
    public static long endPrintTime = 0;
    public static long totalTime = 0;


    public static void parseFile(Path file) throws IOException{
        if (file == null) {file = Paths.get(System.getProperty("user.home"), "JAT", "data.txt");}
        
        long datalength = 0;
        List<String> data;
        try {
            // Start a timer to see how fast this is.
            startTime = System.nanoTime();

            data = Files.readAllLines(file);
            readTime = System.nanoTime();
            //get time to read all lines
            startPrintTime = System.nanoTime();
            int count = 0;
            for (String line : data) {
                count++;
            //    System.out.println("printing line # "+ count);
            //    System.out.println(line);
                
            }
            endPrintTime = System.nanoTime();
            totalTime = endPrintTime - startTime;
            //output all info in milliseconds
            coutData(count);



        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }




    public static void writeResultsUsingStandardOutput(ObservableList<OHLCData> data) throws IOException,ExecutionException,InterruptedException {
        /* Overview of method.
        Get path of data.txt file residing in user's home directory under JAT folder and check with conditions. 
        Add data to a string builder, then convert to a bytebuffer.
        Convert lines to a single ByteBuffer - (what is bytebuffer, why is it used, benefits, cons) 
        Bytebuffer is a container for data of a specific primitive type (byte, int, long, etc)
        It is used to read and write data in a buffer. It is a mutable buffer for bytes.

        Benefits:faster and more flexible than using byte arrays.  
        Cons: additional complexity, it is also not thread safe by default.
        
        */   
        Path file = Paths.get(System.getProperty("user.home"), "JAT", "data.txt");
        if (Files.exists(file)) {
            //If datafile exists, delete for rewrite purpose
            Files.delete(file);
        }
        Files.createFile(file);
        
        //Build content
        StringBuilder sb = new StringBuilder();
        for (OHLCData l : data) {
            sb.append(l.toString()+"\n");
        }
        ByteBuffer buffer = ByteBuffer.wrap(sb.toString().getBytes());
        // Now lets write to the file asynchronously
        try (AsynchronousFileChannel aChannel = AsynchronousFileChannel.open(file, StandardOpenOption.WRITE)) {
            CompletableFuture<Void> writeFu = new CompletableFuture<>();
            aChannel.write(buffer, 0, null, new CompletionHandler<Integer,Void>() {
                @Override
                public void completed(Integer result, Void attachment) {
                    writeFu.complete(null);
                }
                @Override
                public void failed(Throwable exc, Void attachment) {
                    writeFu.completeExceptionally(exc);
                }
            });
            writeFu.get();
            } catch (IOException e) {
                System.err.println("IOException occurred while writing to the file: " + e.getMessage());
                e.printStackTrace();
            } catch (InterruptedException e) {
                System.err.println("Write operation was interrupted: " + e.getMessage());
                Thread.currentThread().interrupt(); // Restore the interrupted status
            } catch (ExecutionException e) {
                System.err.println("ExecutionException occurred during the write operation: " + e.getMessage());
                e.printStackTrace();
            }


        JATbot.botLogger.info("Results written to filepath :" + file.toString());
    }
    public static void coutData(int count) {

        JATbot.botLogger.info(
            //"\nTime to read all lines: " + (readTime - startTime) / 1_000_000 + " milliseconds"+
            "\nTime to print all lines: " + (endPrintTime - startPrintTime) / 1_000_000 + " milliseconds"+
            "\nTotal time: " + totalTime / 1_000_000 + " milliseconds"+
            "\nTotal lines read: " + count
        );

    }

    

}