package com.example.paintp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogWriter {
    private final ExecutorService executorService;
    private final String logFilePath;

    // Constructor
    public LogWriter(String logFilePath) {
        this.logFilePath = logFilePath;
        this.executorService = Executors.newSingleThreadExecutor(); // Single-threaded executor for writing
    }

    // Method to log events
    public void logEvent(String filename, String eventDescription) {
        executorService.submit(() -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath, true))) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"));
                writer.write(String.format("%s [%s] %s%n", timestamp, filename, eventDescription));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    // Method to gracefully shut down the executor service
    public void shutdown() {
        executorService.shutdown();
    }
}
