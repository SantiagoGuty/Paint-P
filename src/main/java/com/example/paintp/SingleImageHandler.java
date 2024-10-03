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

public class SingleImageHandler implements HttpHandler {
    private final Image image;

    public SingleImageHandler(Image image) {
        this.image = image;
    }

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
