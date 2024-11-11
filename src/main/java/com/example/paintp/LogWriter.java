package com.example.paintp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * The {@code LogWriter} class handles asynchronous logging of events to a specified log file.
 * It uses a single-threaded executor to queue log writing tasks, allowing non-blocking logging
 * while the application continues running.
 */
public class LogWriter {
    private final ExecutorService executorService;
    private final String logFilePath;


    /**
     * Constructs a {@code LogWriter} with the specified file path for the log file.
     *
     * @param logFilePath the path to the log file where events will be written.
     */
    public LogWriter(String logFilePath) {
        this.logFilePath = logFilePath;
        this.executorService = Executors.newSingleThreadExecutor(); // Single-threaded executor for writing
    }


    /**
     * Logs an event with a timestamp, filename, and description asynchronously.
     *
     * @param filename          the name of the file or resource associated with the event.
     * @param eventDescription  a description of the event being logged.
     */
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


    /**
     * Shuts down the executor service, stopping any pending or future logging tasks.
     */
    public void shutdown() {
        executorService.shutdown();
    }
}
