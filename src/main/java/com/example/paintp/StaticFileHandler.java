package com.example.paintp;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class StaticFileHandler implements HttpHandler {
    private final String rootDirectory;

    public StaticFileHandler(String rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

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
