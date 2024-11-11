package com.example.paintp;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Handles HTTP requests to serve static image files from a specified root directory.
 * <p>
 * This handler maps requests for images in a specific directory structure to files
 * in the root directory on the server, primarily serving PNG images.
 * </p>
 */
public class StaticFileHandler implements HttpHandler {


    private final String rootDirectory;


    /**
     * Constructs a new {@code StaticFileHandler} for the specified root directory.
     *
     * @param rootDirectory the root directory containing the static files to be served
     */
    public StaticFileHandler(String rootDirectory) {
        this.rootDirectory = rootDirectory;
    }


    /**
     * Handles an HTTP exchange by serving a static file if it exists in the root directory.
     * <p>
     * This method checks the requested file path, retrieves the file if it exists, and sends it
     * as a response. If the file does not exist, a 404 error response is sent.
     * </p>
     *
     * @param exchange the HTTP exchange containing the request and response
     * @throws IOException if an I/O error occurs while handling the request
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestURI = exchange.getRequestURI().getPath();

        // Strip the leading "/images" and map it to the rootDirectory
        String filePath = rootDirectory + requestURI.replace("/images", "");

        File file = new File(filePath);
        if (file.exists() && !file.isDirectory()) {
            // Set the response headers for an image (png in this case)
            exchange.getResponseHeaders().set("Content-Type", "image/png");
            exchange.sendResponseHeaders(200, file.length());

            // Send the image file
            OutputStream os = exchange.getResponseBody();
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int count;
            while ((count = fis.read(buffer)) != -1) {
                os.write(buffer, 0, count);
            }

            fis.close();
            os.close();
        } else {
            // If file not found, return 404
            String response = "File not found.";
            exchange.sendResponseHeaders(404, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
