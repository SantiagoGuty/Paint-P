package com.example.paintp;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class CanvasTab {
    private Canvas canvas;
    private GraphicsContext gc;
    private StackPane canvasPane;
    private String title;
    private Canvas tempCanvas; //Canvas for live draw
    private GraphicsContext tempGc; // Graphics context for live draw

    // Constructor to create a new CanvasTab with specified width and height
    public CanvasTab(String title, double width, double height) {
        this.title = title;  // Don't forget to set the title

        this.canvas = new Canvas(width, height);
        this.tempCanvas = new Canvas(width, height);  // Temp canvas of the same size

        this.gc = canvas.getGraphicsContext2D();
        this.tempGc = tempCanvas.getGraphicsContext2D();

        // Set up the StackPane to overlay the tempCanvas on top of the canvas
        this.canvasPane = new StackPane(canvas, tempCanvas);
        canvasPane.setPrefSize(width, height);

        // Initialize the canvas with default settings
        initializeCanvas(width, height);
    }


    // Constructor to initialize CanvasTab with an existing Canvas and StackPane (from FXML)
    public CanvasTab(String title, Canvas canvas, GraphicsContext gc, StackPane canvasPane) {
        this.title = title;
        this.canvas = canvas;
        this.gc = gc;
        this.canvasPane = canvasPane;

        // Initialize Canvas with white background if it's not already set
        initializeCanvas(canvas.getWidth(), canvas.getHeight());
    }

    // Method to initialize the canvas and set default properties
    private void initializeCanvas(double width, double height) {
        gc.setFill(Color.WHITE);  // Background color set to white
        gc.fillRect(0, 0, width, height);

        // Initialize default drawing settings
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.0);
    }

    // Getter for Canvas object
    public Canvas getCanvas() {
        return canvas;
    }

    // Getter for GraphicsContext object
    public GraphicsContext getGraphicsContext() {
        return gc;
    }

    // Getter for StackPane containing the Canvas
    public StackPane getCanvasPane() {
        return canvasPane;
    }

    public Canvas getTempCanvas() {
        return tempCanvas;
    }

    public GraphicsContext getTempGraphicsContext() {
        return tempGc;
    }

    // Getter for title (can be used for Tab titles)
    public String getTitle() {
        return title;
    }

    // Method to clear the canvas
    public void clearCanvas() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    // Method to reset the canvas with a specific color
    public void resetCanvas(Color color) {
        gc.setFill(color);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.0);
    }

    // Additional methods for undo/redo can be added here in future
}
