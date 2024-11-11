package com.example.paintp;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import javafx.scene.image.Image;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * Handles HTTP requests to serve a single JavaFX {@code Image} as a PNG image over HTTP.
 * <p>
 * This handler is used to convert a JavaFX image into PNG format and send it as a response
 * for an HTTP request.
 * </p>
 */
public class SingleImageHandler implements HttpHandler {

    private final Image image;


    /**
     * Constructs a new {@code SingleImageHandler} for the specified image.
     *
     * @param image the {@code Image} to be served over HTTP
     */
    public SingleImageHandler(Image image) {
        this.image = image;
    }


    /**
     * Handles an HTTP exchange by converting the JavaFX {@code Image} to PNG format,
     * then writing it to the response output stream.
     *
     * @param exchange the HTTP exchange containing the request and response
     * @throws IOException if an I/O error occurs while handling the request
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Convert JavaFX Image to byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        ImageIO.write(bufferedImage, "png", outputStream);

        byte[] imageData = outputStream.toByteArray();

        // Set response headers and send image data
        exchange.getResponseHeaders().set("Content-Type", "image/png");
        exchange.sendResponseHeaders(200, imageData.length);
        OutputStream os = exchange.getResponseBody();
        os.write(imageData);
        os.close();
    }
}
