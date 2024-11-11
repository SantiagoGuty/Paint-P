package com.example.paintp;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;


/**
 * Manages a canvas and its graphical context, including a temporary canvas
 * for live drawing, within a stack pane.
 * <p>
 * This class provides methods for initializing, clearing, and resetting the canvas,
 * as well as accessing its graphical context and temporary drawing layer.
 * </p>
 */
public class CanvasTab {

    private Canvas canvas;
    private GraphicsContext gc;
    private StackPane canvasPane;
    private String title;
    private Canvas tempCanvas; //Canvas for live draw
    private GraphicsContext tempGc; // Graphics context for live draw


    /**
     * Constructs a new {@code CanvasTab} with a specified title, width, and height.
     *
     * @param title  the title of the canvas tab
     * @param width  the width of the canvas
     * @param height the height of the canvas
     */
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
    /**
     * Constructs a new {@code CanvasTab} with an existing canvas and stack pane.
     * <p>
     * This constructor can be used when loading a CanvasTab from FXML.
     * </p>
     *
     * @param title      the title of the canvas tab
     * @param canvas     the main canvas to be used in this tab
     * @param gc         the graphics context of the main canvas
     * @param canvasPane the stack pane containing the canvas and temporary layer
     */
    public CanvasTab(String title, Canvas canvas, GraphicsContext gc, StackPane canvasPane) {
        this.title = title;
        this.canvas = canvas;
        this.gc = gc;
        this.canvasPane = canvasPane;

        // Initialize Canvas with white background if it's not already set
        initializeCanvas(canvas.getWidth(), canvas.getHeight());
    }

    // Method to initialize the canvas and set default properties
    /**
     * Initializes the canvas with default properties such as background color
     * and default stroke settings.
     *
     * @param width  the width of the canvas area
     * @param height the height of the canvas area
     */
    private void initializeCanvas(double width, double height) {
        gc.setFill(Color.WHITE);  // Background color set to white
        gc.fillRect(0, 0, width, height);

        // Initialize default drawing settings
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.0);
    }

    // Getter for Canvas object
    /**
     * Gets the main canvas for drawing.
     *
     * @return the main canvas
     */
    public Canvas getCanvas() {
        return canvas;
    }


    // Getter for GraphicsContext object
    /**
     * Gets the graphics context of the main canvas.
     *
     * @return the graphics context for the main canvas
     */
    public GraphicsContext getGraphicsContext() {
        return gc;
    }


    // Getter for StackPane containing the Canvas
    /**
     * Gets the stack pane that contains both the main and temporary canvases.
     *
     * @return the stack pane with the main and temporary canvases
     */
    public StackPane getCanvasPane() {
        return canvasPane;
    }


    /**
     * Gets the temporary canvas used for live drawing.
     *
     * @return the temporary canvas
     */
    public Canvas getTempCanvas() {
        return tempCanvas;
    }


    /**
     * Gets the graphics context of the temporary canvas.
     *
     * @return the graphics context for the temporary canvas
     */
    public GraphicsContext getTempGraphicsContext() {
        return tempGc;
    }


    // Getter for title (can be used for Tab titles)
    /**
     * Gets the title of the canvas tab, typically used as a tab label.
     *
     * @return the title of the canvas tab
     */
    public String getTitle() {
        return title;
    }


    // Method to clear the canvas
    /**
     * Clears the main canvas by filling it with a white background.
     */
    public void clearCanvas() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }


    // Method to reset the canvas with a specific color
    /**
     * Resets the main canvas with a specified background color, and sets the
     * default stroke color and line width.
     *
     * @param color the background color to set
     */
    public void resetCanvas(Color color) {
        gc.setFill(color);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.0);
    }

}