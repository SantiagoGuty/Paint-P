package com.example.paintp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.io.IOException;
import java.util.Objects;



/**
 * Main entry point for the Paint-P application.
 * This class extends the JavaFX Application class to launch the GUI and
 * starts an HTTP server for showing canvas screenshots and images.
 *
 * @author SantiagoGuty
 * @version 1.4.0
 *
 */
public class HelloApplication extends Application {

    private HttpServer httpServer;


    /**
     * Starts the Paint-P application and sets up the stage and scene.
     * Initializes the HTTP server to serve images and canvases.
     *
     * @param stage The primary stage for the JavaFX application.
     * @throws IOException If the FXML loading or HTTP server creation fails.
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Set up the FXMLLoader and load the FXML file
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1500, 750);
        //scene.getStylesheets().add(getClass().getResource("styling/style.css").toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styling/style.css")).toExternalForm());


        // Create the HttpServer before passing it to the controller
        startHttpServer();

        // Get the controller from the FXMLLoader
        HelloController controller = fxmlLoader.getController();

        // Pass the HttpServer and stage to the controller
        controller.setHttpServer(httpServer); // Pass the server to the controller
        controller.setPrimaryStage(stage);    // Now you have access to primaryStage in the controller

        // Now start the HTTP server in the controller
        controller.startHttpServer(); // This will create the contexts and start the server

        // Set up the main window
        stage.setTitle("Painting-P");
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/paint-P-Logo.png")));
        stage.getIcons().add(icon);
        stage.setScene(scene);
        stage.show();
    }


    /**
     * Creates and starts the HTTP server to show canvas screenshots and images on the local port 8000.
     *
     * @throws IOException If an I/O error kills the app
     */
    private void startHttpServer() throws IOException {
        // Create the HttpServer listening on port 8000 with a maximum of 0 backlog
        httpServer = HttpServer.create(new InetSocketAddress(8000), 0);
        System.out.println("Server started at http://localhost:8000");
    }


    /**
     * Stops the HTTP server when the JavaFX application exits.
     * This method is called automatically by the JavaFX runtime when the application is stopped.
     *
     * @throws Exception If an error occurs during server shutdown.
     */
    @Override
    public void stop() throws Exception {
        // Stop the server when the application exits
        if (httpServer != null) {
            httpServer.stop(0);
            System.out.println("Server stopped.");
        }
        super.stop();
    }


    /**
     * Main method to launch the JavaFX application.
     * This method is where the magic starts!
     *
     * @param args Command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        launch();
    }
}