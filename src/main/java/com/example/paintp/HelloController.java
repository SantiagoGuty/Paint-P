package com.example.paintp;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Dialog;
import javafx.scene.control.Menu;
import javafx.scene.image.*;
import javafx.scene.paint.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToggleButton;
import javafx.event.ActionEvent;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import java.awt.*;
import javafx.scene.control.TextField;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.*;
import java.util.List;
import com.sun.net.httpserver.HttpServer;
import static javax.swing.JOptionPane.showInputDialog;
import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.Toolkit;
import javafx.application.Platform;
import javafx.scene.control.Slider;




/**
 * Main Controller class, it is the bones of all the Paint-p app functionality
 * handling the canvas operations, user interactions, and HTTP server setup for serving Tabs, canvases and images.
 * @author SantiagoGuty
 * @version 1.4.0
 *
 */
public class HelloController {

    @FXML
    private Slider lineWidthSlider;
    @FXML
    private Button undoButton;
    @FXML
    private Button redoButton;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private StackPane canvasPane;
    @FXML
    private Pane rootPane;
    @FXML
    private Canvas canvas;
    @FXML
    private Label pixelSizeLabel;
    @FXML
    private ToggleButton colorGrabButton;
    @FXML
    private Label infoText;
    @FXML
    private ToggleGroup toolsToggleGroup;
    @FXML
    private ToggleButton SelectAndMoveButton;
    @FXML
    private CheckBox dashedLineCheckBox;
    @FXML
    private TabPane tabPane;
    @FXML
    private CheckMenuItem autosaveMenuItem;
    @FXML
    private CheckMenuItem notificationsToggle;
    @FXML
    private CheckBox autosaveCheckbox;
    @FXML
    private Label countdownLabel;
    @FXML
    private Canvas testCanvas;
    @FXML
    private CheckBox fillCheckMenuItem;
    @FXML
    private ToggleButton pencilButton, eraserButton, lineButton,
            rectangleButton, ellipseButton, circleButton, triangleButton,
            starButton, heartButton, imageButton, textButton, nGonButton,
            cubeButton, fillButton, spiralButton, standardBrushButton,
            calligraphyBrushButton, charcoalBrushButton, sprayPaintBrushButton,
            dashedToggleButton, fillToggleButton, pyramidButton, arrowButton,
            selectButton;

    @FXML
    private TextField lineWidthInput;

    private boolean fillShapes = false; // To track if filling is enabled
    private Color fillColor = Color.BLACK; // Default fill color
    private enum BrushType {
        STANDARD, CALLIGRAPHY, CHARCOAL, SPRAY_PAINT
    }

    private BrushType selectedBrush = BrushType.STANDARD; // Default brush

    private double startX, startY;
    private Color currentColor = Color.BLACK;
    private double currentLineWidth = 1.0; // Default value
    private String currentShape = "Pencil";
    private int currentPixelSize = 10; // Declare the variable here
    private KeyCombination saveShortCut;
    private KeyCombination exitShortCut;
    private KeyCombination clearShortCut;
    private DoubleProperty penSize = new SimpleDoubleProperty(1.0);
    private boolean isDashedLine = false;
    private Image defaultStickerImage = new Image("/images/paint-P-Logo.png"); // Ensure the path is correct
    private Image customStickerImage = defaultStickerImage;
    private String defaultText = "Painting P !!!";
    private String stringToolText = defaultText;
    private FontWeight fontWeight = FontWeight.NORMAL;
    private FontPosture fontPosture = FontPosture.REGULAR;
    private String defaultfontFamily = "Arial";  // Default font
    private String fontFamily = defaultfontFamily;
    private Boolean italic = false;
    private Boolean bold = false;
    private int nGonSides = 5;
    private int starPoints = 5; // Default number of points for the star
    private LogWriter logWriter;
    private boolean notificationsEnabled = true;  // Show notifications fault to tr
    private List<Image> selectedImages = new ArrayList<>(); // ArrayList for the web server
    private final HashMap<Tab, CanvasTab> canvasTabs = new HashMap<>();
    private final HashMap<Tab, Stack<CanvasState>> undoStacks = new HashMap<>();
    private final HashMap<Tab, Stack<CanvasState>> redoStacks = new HashMap<>();
    private Stack<WritableImage> undoStack = new Stack<>();
    private Stack<WritableImage> redoStack = new Stack<>();
    private Map<Tab, File> savedFilesMap = new HashMap<>(); //Track of the save files in the download directory
    private Map<Tab, CanvasTab> tabCanvasMap = new HashMap<>();
    private GraphicsContext gc;
    private GraphicsContext testGc;
    private String currentTool = "Pencil";
    private Timeline autosaveTimer;
    private int autosaveInterval = 60; // Default to 60 seconds
    private int countdownValue;
    private boolean autosaveEnabled = true; // Enable autosave by default
    private HttpServer httpServer;
    private Stage primaryStage;
    private TrayIcon trayIcon;
    private double prevX, prevY;
    private double endX, endY;     // End point
    private int clickCount = 0;

    private WritableImage selectedChunk;
    private double selectionStartX, selectionStartY, selectionEndX, selectionEndY;
    private double rotationAngle = 0;  // To track the rotation angle

    private boolean isSelecting = false;
    private WritableImage selectedImage;



    /**
     * Initializes the Paint-P controller by setting up the default UI configurations, canvas
     * tool buttons, shortcuts, and listeners.
     * (Order of the functions matters, A lot!)
     */
    @FXML
    public void initialize() {
        System.out.println("Initializing components...");

        // Set the log file path to the desired location
        logWriter = new LogWriter("user_actions.log");

        // Example of logging the initialization
        //logWriter.logEvent("new tab", "Application initialized");

        if (tabPane == null) {
            System.out.println("TabPane is null.");
        } else {
            System.out.println("TabPane initialized.");
            logWriter.logEvent(getCurrentTabName(), "Tab initiated");
        }

        if (colorPicker == null) {
            System.out.println("ColorPicker is null.");
        } else {
            System.out.println("ColorPicker initialized.");
        }

        colorPicker.setValue(Color.BLACK);  // Default color
        fillColor = colorPicker.getValue(); // Default color from the Color picker
        lineWidthSlider.setValue(1.0);      // Default line width


        setupToolButtons();
        setButtonIcons();
        setupListeners();
        setupLineWidthSync();

        // Clear any existing tabs to ensure a clean start
        tabPane.getTabs().clear();

        // Initialize the first tab with a default canvas size
        addNewTab("Paint-p 1", 1450, 575);

        setupShortcuts();//shortcuts set up CTRL S Safe as, CTRL L Clean canvas, CTRL E Exit.
        initializeTooltips(); // tool tips for all buttons

        setupSystemTray(); // set up notifications

        setupAutosaveTimer();
        saveCanvasState();

    }


    /**
     * Sets up synchronization between the line width slider and text field.
     * <p>
     * Listens for changes in the slider value to update the text field and validates
     * the input from the text field to keep the value within a specified range
     * (minimum of 1 and maximum of 50) before updating the slider.
     * </p>
     */
    @FXML
    private void setupLineWidthSync() {
        // Set a listener to update the TextField whenever the Slider changes
        lineWidthSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            lineWidthInput.setText(String.format("%.2f", newValue.doubleValue()));
        });

        // Add a focus listener on the TextField to update the Slider when TextField loses focus
        lineWidthInput.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) { // When TextField loses focus
                try {
                    double inputValue = Double.parseDouble(lineWidthInput.getText());
                    if (inputValue > 50) {
                        inputValue = 50; // Limit to maximum of 50
                    } else if (inputValue < 1) {
                        inputValue = 1; // Ensure minimum of 1
                    }
                    lineWidthSlider.setValue(inputValue); // Update the Slider
                    lineWidthInput.setText(String.format("%.2f", inputValue)); // Format TextField
                } catch (NumberFormatException e) {
                    // Reset to getLineWidth()'s current value if input is invalid
                    lineWidthInput.setText(String.format("%.2f", getLineWidth()));
                }
            }
        });
    }

    /**
     * Retrieves the current line width value from the line width slider.
     *
     * @return the current line width as a double
     */
    public double getLineWidth() {
        return lineWidthSlider.getValue();
    }



    /**
     * Rotates the currently selected canvas by the specified angle (in degrees).
     * <p>
     * This method adjusts the canvas dimensions for 90 or 270-degree rotations, captures a
     * snapshot of the canvas's current state, clears the canvas, and applies the rotation
     * transformation before redrawing the image on the canvas. The transformation state
     * is saved and restored to prevent unintended effects.
     * </p>
     *
     * @param angle the angle of rotation in degrees (typically 90, 180, or 270)
     */
    private void rotateCanvas(int angle) {
        CanvasTab canvasTab = getSelectedCanvasTab();
        if (canvasTab != null) {
            Canvas currentCanvas = canvasTab.getCanvas();
            GraphicsContext gc = currentCanvas.getGraphicsContext2D();
            GraphicsContext tempGc = canvasTab.getTempGraphicsContext();

            // Store the original canvas size
            double originalWidth = currentCanvas.getWidth();
            double originalHeight = currentCanvas.getHeight();

            // Create a snapshot of the current canvas state
            WritableImage snapshot = captureCanvas(currentCanvas);

            // Adjust the canvas size for 90 or 270-degree rotations
            if (angle == 90 || angle == 270) {
                currentCanvas.setWidth(originalHeight);
                currentCanvas.setHeight(originalWidth);
                canvasTab.getTempCanvas().setWidth(originalHeight);
                canvasTab.getTempCanvas().setHeight(originalWidth);
            }

            // Clear the canvas before applying the rotation
            gc.clearRect(0, 0, currentCanvas.getWidth(), currentCanvas.getHeight());
            tempGc.clearRect(0, 0, canvasTab.getTempCanvas().getWidth(), canvasTab.getTempCanvas().getHeight());


            // Apply the rotation transformation
            gc.save(); // Save the current transformation state
            gc.translate(currentCanvas.getWidth() / 2, currentCanvas.getHeight() / 2);
            gc.rotate(angle); // Rotate by the specified angle
            gc.translate(-originalWidth / 2, -originalHeight / 2); // Adjust translation based on original size

            // Redraw the snapshot after rotating
            gc.drawImage(snapshot, 0, 0);

            gc.restore(); // Restore the original state
        }
    }

    /**
     * Rotates the currently selected canvas by 90 degrees.
     * <p>
     * Calls the rotateCanvas method to apply a 90-degree rotation
     * transformation to the canvas.
     * </p>
     */
    @FXML
    protected void onRotate90() {
        rotateCanvas(90);
    }

    /**
     * Rotates the currently selected canvas by 180 degrees.
     * <p>
     * Calls the rotateCanvas method to apply a 180-degree rotation
     * transformation to the canvas.
     * </p>
     */
    @FXML
    protected void onRotate180() {
        rotateCanvas(180);
    }


    /**
     * Rotates the currently selected canvas by 270 degrees.
     * <p>
     * Calls the rotateCanvas method to apply a 270-degree rotation
     * transformation to the canvas.
     * </p>
     */
    @FXML
    protected void onRotate270() {
        rotateCanvas(270);
    }

    /**
     * Toggles the fill functionality for drawing shapes.
     * <p>
     * Updates the fill mode for shapes based on the selected state of the
     * fill toggle button, allowing shapes to be drawn either filled or outlined.
     * </p>
     */
    @FXML
    private void toggleFill() {
        fillShapes = fillToggleButton.isSelected();
    }


    /**
     * Opens a dialog to select a fill color for shapes.
     * <p>
     * Displays a color picker dialog that lets the user choose a color for
     * filling shapes. If a color is selected, it updates the fill color used
     * for drawing filled shapes.
     * </p>
     */
    @FXML
    private void setFillingColor() {
        // Create a new ColorPicker with the current fill color as the default
        ColorPicker colorPicker = new ColorPicker(fillColor);

        // Create a new dialog
        Dialog<Color> dialog = new Dialog<>();
        dialog.setTitle("Choose Fill Color");
        dialog.setHeaderText("Select a color for filling shapes");

        // Add ColorPicker to the dialog content
        dialog.getDialogPane().setContent(colorPicker);

        // Add OK and Cancel buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Convert result to the selected color
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return colorPicker.getValue(); // Return the selected color
            }
            return null; // Return null if canceled
        });

        // Show the dialog and get the result
        Optional<Color> result = dialog.showAndWait();
        result.ifPresent(selectedColor -> fillColor = selectedColor); // Update fillColor if a color was chosen
    }

    /**
     * Resets the filling color to the default color.
     * <p>
     * Sets the fill color to black, reverting any custom color
     * selected by the user for filling shapes.
     * </p>
     */
    @FXML
    private void resetFillingColor() {
        fillColor = Color.BLACK; // Set to default color
    }


    /**
     * Retrieves the currently selected canvas in the active tab.
     * <p>
     * This method checks the selected tab, verifies that it contains a ScrollPane, then
     * further checks if that ScrollPane contains a StackPane with a Canvas as its child.
     * If such a Canvas is found, it is returned; otherwise, an error message is logged.
     * </p>
     *
     * @return the selected Canvas if found, or null if no canvas is present in the selected tab
     */
    private Canvas getSelectedCanvas() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null && selectedTab.getContent() instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) selectedTab.getContent();
            if (scrollPane.getContent() instanceof StackPane) {
                StackPane canvasPane = (StackPane) scrollPane.getContent();
                if (!canvasPane.getChildren().isEmpty() && canvasPane.getChildren().get(0) instanceof Canvas) {
                    return (Canvas) canvasPane.getChildren().get(0);
                }
            }
        }
        System.err.println("No canvas found in the selected tab.");
        return null;
    }


    /**
     * Updates the specified canvas with a new image.
     * <p>
     * Clears the existing contents of the canvas and redraws the provided image to fit
     * the canvas size.
     * </p>
     *
     * @param canvas the Canvas to be updated with the new image
     * @param newImage the WritableImage to draw onto the canvas
     */
    private void updateCanvasWithImage(Canvas canvas, WritableImage newImage) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.drawImage(newImage, 0, 0, canvas.getWidth(), canvas.getHeight());
    }


    /**
     * Flips the contents of the canvas either horizontally or vertically.
     * <p>
     * This method captures the current content of the canvas, applies a transformation
     * to flip the image either horizontally or vertically, and returns a new flipped image.
     * </p>
     *
     * @param canvas the Canvas whose content is to be flipped
     * @param horizontal if true, flips horizontally; if false, flips vertically
     * @return a WritableImage containing the flipped content of the canvas
     */
    private WritableImage flipCanvas(Canvas canvas, boolean horizontal) {
        WritableImage snapshot = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.snapshot(null, snapshot);

        // Create a new canvas for the flipped image
        Canvas flippedCanvas = new Canvas(canvas.getWidth(), canvas.getHeight());
        GraphicsContext gc = flippedCanvas.getGraphicsContext2D();

        if (horizontal) {
            gc.translate(snapshot.getWidth(), 0);
            gc.scale(-1, 1); // Flip horizontally
        } else {
            gc.translate(0, snapshot.getHeight());
            gc.scale(1, -1); // Flip vertically
        }

        gc.drawImage(snapshot, 0, 0);

        WritableImage flippedImage = new WritableImage((int) flippedCanvas.getWidth(), (int) flippedCanvas.getHeight());
        flippedCanvas.snapshot(null, flippedImage);

        return flippedImage;
    }

    /**
     * Handles the action of flipping the currently selected canvas horizontally.
     * <p>
     * This method flips the selected canvas horizontally and updates the canvas with
     * the new flipped image.
     * </p>
     */
    @FXML
    private void onFlipHorizontalClick() {
        Canvas currentCanvas = getSelectedCanvas();
        if (currentCanvas != null) {
            WritableImage flippedImage = flipCanvas(currentCanvas, true);
            updateCanvasWithImage(currentCanvas, flippedImage);
        }
    }

    /**
     * Handles the action of flipping the currently selected canvas vertically.
     * <p>
     * This method flips the selected canvas vertically and updates the canvas with
     * the new flipped image.
     * </p>
     */
    @FXML
    private void onFlipVerticalClick() {
        Canvas currentCanvas = getSelectedCanvas();
        if (currentCanvas != null) {
            WritableImage flippedImage = flipCanvas(currentCanvas, false);
            updateCanvasWithImage(currentCanvas, flippedImage);
        }
    }


    /**
     * Retrieves the name of the currently selected tab in the tab pane.
     * <p>
     * If a tab is selected, this method returns the text (name) of the tab.
     * If no tab is selected, it returns "unknown tab".
     * </p>
     *
     * @return the name of the current tab or "unknown tab" if no tab is selected
     */
    private String getCurrentTabName() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        return selectedTab != null ? selectedTab.getText() : "unknown tab";
    }

    /**
     * Displays a test notification in the system tray.
     * <p>
     * If the system supports the system tray, this method displays a test message using
     * the tray icon. It logs a message to the console when the notification is displayed.
     * </p>
     */
    @FXML
    private void testNotification() {
        if (SystemTray.isSupported()) {
            trayIcon.displayMessage("Test Notification", "This is a test message from Paint-P!", TrayIcon.MessageType.INFO);
            System.out.println("Notification displayed."); // Debug log
        }
    }


    /**
     * Sets up the system tray for displaying notifications.
     * <p>
     * This method checks if the system supports the system tray. If supported, it adds
     * a tray icon to the system tray and prepares it for displaying notifications.
     * </p>
     */
    private void setupSystemTray() {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();

            // Load the AWT image from a file
            java.awt.Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/paint-P-Logo.png")); // Ensure you use the correct path

            trayIcon = new TrayIcon(image, "Paint-P Autosave Notification");
            trayIcon.setImageAutoSize(true);

            try {
                tray.add(trayIcon);
                System.out.println("System tray setup completed."); // Debug log
            } catch (AWTException e) {
                e.printStackTrace();
                System.out.println("System tray setup failed."); // Debug log
            }
        } else {
            System.out.println("System tray is not supported on this system.");
        }
    }


    /**
     * Displays a notification message in the system tray.
     * <p>
     * If notifications are enabled and the system supports the system tray, this method
     * displays the specified message using the tray icon.
     * </p>
     *
     * @param message the message to be displayed in the notification
     */
    private void showNotification(String message) {
        if (notificationsEnabled && SystemTray.isSupported()) {
            trayIcon.displayMessage("Notification", message, TrayIcon.MessageType.INFO);
        }
    }

    /**
     * Initializes tooltips for various tool buttons in the application.
     * <p>
     * This method assigns descriptive tooltips to each tool button (e.g., Pencil, Eraser, Line)
     * in the toolbar, providing users with a brief description of each tool.
     * </p>
     */
    private void initializeTooltips() {
        setToggleTooltip(pencilButton, "Pencil Tool");
        setToggleTooltip(eraserButton, "Eraser Tool");
        setToggleTooltip(lineButton, "Line Tool");
        setToggleTooltip(rectangleButton, "Rectangle Tool");
        setToggleTooltip(ellipseButton, "Ellipse Tool");
        setToggleTooltip(circleButton, "Circle Tool");
        setToggleTooltip(triangleButton, "Triangle Tool");
        setToggleTooltip(imageButton, "Image Tool");
        setToggleTooltip(starButton, "Star Tool");
        setToggleTooltip(heartButton, "Heart Tool");
        setToggleTooltip(spiralButton, "Spiral Tool");
        setToggleTooltip(cubeButton, "Cube tool");
        setToggleTooltip(nGonButton, "Polygon Tool");
        setToggleTooltip(textButton, "Text Tool");
        setToggleTooltip(colorGrabButton, "Grab color Tool");
        setToggleTooltip(pyramidButton, "Pyramid Tool");
        setToggleTooltip(arrowButton, "Arrow Tool");
        setToggleTooltip(charcoalBrushButton, "Bubbles button");
        setToggleTooltip(sprayPaintBrushButton, "Spray paint button");
        setToggleTooltip(selectButton, "Select button");
    }

    // Helper method to set tooltip with no delay
    /**
     * Sets a tooltip with a specified text for a ToggleButton.
     * <p>
     * This helper method creates a tooltip for the given ToggleButton with the specified text.
     * </p>
     *
     * @param button the ToggleButton to which the tooltip is set
     * @param tooltipText the text to display in the tooltip
     */
    private void setToggleTooltip(ToggleButton button, String tooltipText) {
        Tooltip tooltip = new Tooltip(tooltipText);
       // tooltip.setShowDelay(Duration.ZERO);  // Set delay to zero
        button.setTooltip(tooltip);
    }

    /**
     * Sets a tooltip with a specified text for a Button.
     * <p>
     * This helper method creates a tooltip for the given Button with the specified text.
     * </p>
     *
     * @param button the Button to which the tooltip is set
     * @param tooltipText the text to display in the tooltip
     */
    private void setButtonTooltip( Button button, String tooltipText) {
        Tooltip tooltip = new Tooltip(tooltipText);
        // tooltip.setShowDelay(Duration.ZERO);  // Set delay to zero
        button.setTooltip(tooltip);
    }


    /**
     * Captures the currently selected canvas as a snapshot, stores it, and makes it accessible via HTTP.
     * <p>
     * This method creates a snapshot of the selected canvas, assigns it a unique context path,
     * and sets up HTTP endpoints for accessing the captured canvas image.
     * </p>
     */
    @FXML
    public void onCaptureCanvasClick() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();

        if (selectedTab != null && selectedTab.getContent() instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) selectedTab.getContent();
            if (scrollPane.getContent() instanceof StackPane) {
                StackPane canvasPane = (StackPane) scrollPane.getContent();
                if (!canvasPane.getChildren().isEmpty() && canvasPane.getChildren().get(0) instanceof Canvas) {
                    Canvas currentCanvas = (Canvas) canvasPane.getChildren().get(0);

                    WritableImage canvasSnapshot = new WritableImage((int) currentCanvas.getWidth(), (int) currentCanvas.getHeight());
                    currentCanvas.snapshot(null, canvasSnapshot);

                    String contextPath = "/canvas " + (canvasSnapshots.size() + 1);
                    canvasSnapshots.put(contextPath, canvasSnapshot);

                    // Remove previous context if it exists
                    try {
                        httpServer.removeContext(contextPath);
                    } catch (IllegalArgumentException ignored) { }

                    // Serve the individual snapshot
                    httpServer.createContext(contextPath, new SingleImageHandler(canvasSnapshot));

                    // Re-create the root context to refresh the main page
                    httpServer.removeContext("/");
                    httpServer.createContext("/", new RootHandler());

                    System.out.println("Canvas snapshot served at: http://localhost:8000" + contextPath);
                }
            }
        } else {
            System.out.println("No canvas found to capture.");
        }
    }






    private Map<String, WritableImage> canvasImages = new HashMap<>();

    /**
     * Updates the stored canvas image for a selected tab.
     * <p>
     * Associates the given WritableImage with the specified tab by storing it in a map,
     * which enables easy retrieval and access of the canvas images.
     * </p>
     *
     * @param image the WritableImage to be updated
     * @param tab the Tab associated with the canvas image
     */
    private void updateCanvasImageForContext(WritableImage image, Tab tab) {
        String contextPath = "/canvas" + tabPane.getTabs().indexOf(tab);
        canvasImages.put(contextPath, image);  // Store the latest image for this context
        System.out.println("Updated image for " + contextPath);
    }


    /**
     * Captures the current canvas as a WritableImage, for making it accessible via HTTP.
     * <p>
     * Takes a snapshot of the provided canvas and returns it as a WritableImage.
     * Returns null if the canvas dimensions are invalid.
     * </p>
     *
     * @param canvas the Canvas to be captured
     * @return a WritableImage containing the canvas snapshot, or null if the canvas is invalid
     */
    public WritableImage captureCanvas(Canvas canvas) {
        if (canvas.getWidth() > 0 && canvas.getHeight() > 0) {
            System.out.println("Capturing canvas: " + canvas.getWidth() + "x" + canvas.getHeight());
            WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
            canvas.snapshot(null, writableImage);  // Capture the canvas
            return writableImage;
        } else {
            System.err.println("Canvas size is invalid.");
            return null;  // Return null if the canvas size is invalid
        }
    }


    /**
     * Updates the selected images list when images are selected or deselected.
     * <p>
     * Adds the specified image to the selected images list if it is selected, or
     * removes it if it is deselected.
     * </p>
     *
     * @param image the Image object that was selected or deselected
     * @param isSelected a boolean indicating if the image was selected (true) or deselected (false)
     */
    public void onImageSelectionChanged(Image image, boolean isSelected) {
        if (isSelected) {
            selectedImages.add(image);
        } else {
            selectedImages.remove(image);
        }
    }


    /**
     * Sets the HTTP server for serving canvas and images over localhost.
     *
     * @param httpServer The HttpServer object for managing the HTTP server.
     */

    public void setHttpServer(HttpServer httpServer) {
        this.httpServer = httpServer;
    }

    /**
     * Sets the primary stage of the application.
     *
     * @param primaryStage The main stage of the JavaFX application.
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private List<String> imageContexts = new ArrayList<>();


    /**
     * Starts the HTTP server for the application.
     * <p>
     * Serves canvas snapshots and uploaded images on localhost, making them accessible
     * through specific HTTP endpoints. Checks if there are available canvases to serve.
     * </p>
     */
    public void startHttpServer() {
        if (httpServer != null) {
            if (tabPane.getTabs().isEmpty()) {
                System.out.println("No canvases available. Server functionality is offline.");
                // Optionally, you can disable the server start by returning early.
                return;
            }

            System.out.println("Starting HTTP server...");

            // Create context for the root handler
            httpServer.createContext("/", new RootHandler());

            // Serve static images from the local filesystem
            String imagePath = "C:/Users/sangu/OneDrive/Escritorio/CS250/Paint-P/src/main/resources/images";
            httpServer.createContext("/images", new StaticFileHandler(imagePath));

            httpServer.setExecutor(null); // Use default executor
            httpServer.start();
            System.out.println("Server started at http://localhost:8000. Number of images: " + selectedImages.size());
        } else {
            System.err.println("HttpServer is null. Cannot start the server.");
        }
    }


    // Map to store snapshots for each canvas (key: tab index, value: WritableImage)
    //private Map<Integer, WritableImage> canvasSnapshots = new HashMap<>();
    private Map<String, WritableImage> canvasSnapshots = new HashMap<>();

    /**
     * Handles HTTP requests for the root context, displaying available resources.
     * <p>
     * This class generates an HTML page listing available canvas snapshots and
     * uploaded images, with links to view each item. It includes inline CSS
     * for styling the HTML output.
     * </p>
     */
    public class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder response = new StringBuilder();

            response.append("<html>");
            response.append("<head>");
            response.append("<title>Painting-P - Available Resources</title>");

            // CSS for enhanced layout and styling
            response.append("<style>");
            response.append("body { font-family: Arial, sans-serif; background-color: #f4f4f4; color: #333; margin: 0; padding: 0; }");
            response.append("h1 { background-color: #3498db; color: #fff; padding: 20px; text-align: center; margin: 0; }");
            response.append("h3 { color: #2980b9; text-align: center; margin: 20px 0; }");
            response.append("ul { list-style-type: none; padding: 0; display: flex; flex-wrap: wrap; justify-content: center; }");
            response.append("li { margin: 10px; }");
            response.append("a { text-decoration: none; color: white; background-color: #2980b9; padding: 10px 20px; border-radius: 5px; font-size: 18px; }");
            response.append("a:hover { background-color: #1abc9c; }");
            response.append("p { text-align: center; color: #7f8c8d; }");
            response.append(".logo-image { display: block; margin: 20px auto; width: 100px; height: auto; border-radius: 5px; }"); // Adjust logo size here
           // response.append("img { display: block; margin: 20px auto; max-width: 100%; height: auto; border: 1px solid #ddd; border-radius: 5px; }");
            response.append("</style>");

            response.append("</head><body>");
            response.append("<h1>Painting-P</h1>");
            response.append("<img src='/images/paint-P-logo.png' alt='Paint-P Logo' class='logo-image'>");

            response.append("<h3>Available Canvas Snapshots</h3>");
            if (canvasSnapshots.isEmpty()) {
                response.append("<p>No canvas snapshots available at the moment.</p>");
            } else {
                response.append("<ul>");
                for (String contextPath : canvasSnapshots.keySet()) {
                    response.append("<li><a href='").append(contextPath).append("'>").append(contextPath).append("</a></li>");
                }
                response.append("</ul>");
            }

            response.append("<h3>Available Uploaded Images</h3>");
            if (selectedImages.isEmpty()) {
                response.append("<p>No images uploaded at the moment.</p>");
            } else {
                response.append("<ul>");
                for (int i = 0; i < selectedImages.size(); i++) {
                    String contextPath = "/image" + (i + 1);
                    response.append("<li><a href='").append(contextPath).append("'>Image ").append(i + 1).append("</a></li>");
                }
                response.append("</ul>");
            }


            response.append("</body></html>");

            byte[] responseBytes = response.toString().getBytes("UTF-8");
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        }
    }


    /**
     * Opens the HTTP server's root URL in the default browser.
     * <p>
     * Attempts to open the localhost URL where the server is running, allowing
     * users to view available resources served by the application.
     * </p>
     */
    @FXML
    private void openServerInBrowser() {
        try {
            Desktop.getDesktop().browse(new URI("http://localhost:8000"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the selection of an image file by the user.
     * <p>
     * Opens a file chooser dialog to select an image file. If an image is selected,
     * it is added to the list of selected images, a new HTTP context path is created
     * for serving the image, and the root handler is refreshed to include the new image link.
     * </p>
     */
    @FXML
    public void onSelectImageClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            Image image = new Image(selectedFile.toURI().toString());
            selectedImages.add(image);

            String contextPath = "/image" + selectedImages.size();

            try {
                httpServer.removeContext(contextPath);  // Clean up if the context path exists
            } catch (IllegalArgumentException ignored) { }

            httpServer.createContext(contextPath, new SingleImageHandler(image));

            // Refresh the root handler to include this new image link
            httpServer.removeContext("/");
            httpServer.createContext("/", new RootHandler());

            System.out.println("Selected image: " + selectedFile.getName());
            System.out.println("Image served at: http://localhost:8000" + contextPath);
        } else {
            System.out.println("No image selected.");
        }
    }


    /**
     * Handles HTTP requests for displaying a list of selected images.
     * <p>
     * This class generates an HTML page listing available images, providing links to view
     * each image in a new browser tab.
     * </p>
     */
    public class ImageHandler implements HttpHandler {
        private List<Image> selectedImages;

        public ImageHandler(List<Image> selectedImages) {
            this.selectedImages = selectedImages;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response;

            if (selectedImages.isEmpty()) {
                response = "<html><body><h1>No images available.</h1></body></html>";
            } else {
                response = "<html><body><h1>Available Images:</h1><ul>";
                for (int i = 0; i < selectedImages.size(); i++) {
                    response += "<li><a href=\"/image" + (i + 1) + "\" target=\"_blank\">Image " + (i + 1) + "</a></li>";
                }
                response += "</ul></body></html>";
            }

            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }


    /**
     * Generates HTML for displaying a list of images with a tab interface.
     * <p>
     * This method creates HTML content that lists images with a tabbed layout, allowing users
     * to view each image by selecting the corresponding tab.
     * </p>
     *
     * @param images the list of images to include in the HTML
     * @return a String containing the generated HTML
     */
    private String generateHtmlForImages(List<Image> images) {
        StringBuilder html = new StringBuilder("<html><head>");
        html.append("<style>");
        html.append("body { font-family: Arial; }");
        html.append(".tab { overflow: hidden; border: 1px solid #ccc; background-color: #f1f1f1; }");
        html.append(".tab button { background-color: inherit; float: left; border: none; outline: none; cursor: pointer;");
        html.append("padding: 14px 16px; transition: 0.3s; }");
        html.append(".tabcontent { display: none; padding: 6px 12px; border: 1px solid #ccc; }");
        html.append("</style></head><body>");

        html.append("<h1>Selected Images</h1>");

        // Create tab buttons
        html.append("<div class=\"tab\">");
        for (int i = 0; i < images.size(); i++) {
            html.append("<button class=\"tablinks\" onclick=\"openTab(event, 'Image").append(i).append("')\">Image ")
                    .append(i + 1).append("</button>");
        }
        html.append("</div>");

        // Create tab content
        for (int i = 0; i < images.size(); i++) {
            html.append("<div id=\"Image").append(i).append("\" class=\"tabcontent\">");
            html.append("<img src=\"/image").append(i).append("\" style=\"max-width:100%;height:auto;\"/>");
            html.append("</div>");
        }

        // JavaScript for tabs
        html.append("<script>");
        html.append("function openTab(evt, tabName) {");
        html.append("var i, tabcontent, tablinks;");
        html.append("tabcontent = document.getElementsByClassName('tabcontent');");
        html.append("for (i = 0; i < tabcontent.length; i++) {");
        html.append("tabcontent[i].style.display = 'none'; }");
        html.append("tablinks = document.getElementsByClassName('tablinks');");
        html.append("for (i = 0; i < tablinks.length; i++) {");
        html.append("tablinks[i].className = tablinks[i].className.replace(' active', ''); }");
        html.append("document.getElementById(tabName).style.display = 'block';");
        html.append("evt.currentTarget.className += ' active'; }");
        html.append("</script>");

        html.append("</body></html>");
        return html.toString();
    }


    /**
     * Toggles the notification settings based on the user’s selection.
     * <p>
     * Updates the notificationsEnabled flag and displays a message indicating whether
     * notifications have been enabled or disabled.
     * </p>
     */
    @FXML
    public void toggleNotifications() {
        notificationsEnabled = notificationsToggle.isSelected();  // Update based on CheckMenuItem state
        if (notificationsEnabled) {
            System.out.println("Notifications enabled");
        } else {
            System.out.println("Notifications disabled");
        }
    }


    /**
     * Toggles the autosave functionality in the application.
     * <p>
     * Enables or disables the autosave feature based on the user's selection. If enabled,
     * resets the countdown to the autosave interval and starts the autosave timer. If
     * disabled, stops the autosave timer and displays a notification about the change.
     * </p>
     */
    @FXML
    private void toggleAutosave() {
        autosaveEnabled = autosaveMenuItem.isSelected();

        if (autosaveEnabled) {
            // Reset the countdown to the autosave interval
            countdownValue = autosaveInterval;

            // Show notification for autosave enabled
            showNotification("Autosave Enabled");

            // Start or reset the autosave timer
            setupAutosaveTimer();
        } else {
            // Show notification for autosave disabled
            showNotification("Autosave Disabled");

            // Stop the autosave timer by setting autosaveEnabled to false
            autosaveTimer.stop();
        }

        // Update the info text to reflect the current state of autosave
        updateLabel(0, 0);  // This will refresh any status in the UI
    }


    /**
     * Sets up the autosave timer with the previously specified interval.
     * <p>
     * Initializes a timeline that triggers the autosave action every specified interval
     * when autosave is enabled. The countdown resets after each autosave, and updates
     * the UI with the countdown value.
     * </p>
     */
    private void setupAutosaveTimer() {
        countdownValue = autosaveInterval;
        autosaveTimer = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    if (autosaveEnabled) {
                        countdownValue--;
                        Platform.runLater(() -> updateLabel(0, 0)); // Trigger updateLabel to refresh autosave info

                        if (countdownValue <= 0) {
                            onAutoSave();
                            countdownValue = autosaveInterval; // Reset the countdown
                        }
                    }
                })
        );
        autosaveTimer.setCycleCount(Timeline.INDEFINITE);
        autosaveTimer.play();
    }


    /**
     * Automatically saves the current canvas to a specified location.
     * <p>
     * This method captures the canvas in the currently selected tab, saves it as a PNG file
     * to the user's Downloads directory, and logs the autosave event. If autosave fails,
     * an error message is displayed.
     * </p>
     */
    private void onAutoSave() {
        System.out.println("Autosaving...");

        // Get the currently selected tab
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null && selectedTab.getContent() instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) selectedTab.getContent();
            if (scrollPane.getContent() instanceof StackPane) {
                StackPane canvasPane = (StackPane) scrollPane.getContent();
                if (!canvasPane.getChildren().isEmpty() && canvasPane.getChildren().get(0) instanceof Canvas) {
                    Canvas currentCanvas = (Canvas) canvasPane.getChildren().get(0);

                    // Get the tab name
                    String tabName = selectedTab.getText();

                    // Set the autosave location to Downloads directory
                    String downloadDir = "C:\\Users\\sangu\\Downloads\\";
                    File file = new File(downloadDir + tabName + ".png");

                    // Save the canvas snapshot as a PNG file
                    WritableImage writableImage = new WritableImage((int) currentCanvas.getWidth(), (int) currentCanvas.getHeight());
                    currentCanvas.snapshot(null, writableImage);
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);

                    try {
                        ImageIO.write(bufferedImage, "png", file);
                        System.out.println("Autosaved to " + file.getAbsolutePath());
                        Platform.runLater(() -> infoText.setText("Autosaved at " + new Date().toString() + " to " + file.getAbsolutePath()));
                        logWriter.logEvent(getCurrentTabName(), "Autosave");
                    } catch (IOException e) {
                        e.printStackTrace();
                        Platform.runLater(() -> infoText.setText("Failed to autosave."));
                    }
                }
            }
        }
    }


    /**
     * Sets up the components and event handlers for a newly created tab.
     * <p>
     * Retrieves the canvas from the tab, creates a CanvasTab instance for storing canvas
     * metadata, and registers event handlers for mouse interactions.
     * </p>
     *
     * @param tab the Tab object to initialize
     */
    private void setupExistingTab(Tab tab) {
        // Retrieve components from FXML
        ScrollPane scrollPane = (ScrollPane) tab.getContent();
        StackPane canvasPane = (StackPane) scrollPane.getContent();
        Canvas canvas = (Canvas) canvasPane.getChildren().get(0);

        // Create CanvasTab and store it
        CanvasTab canvasTab = new CanvasTab(tab.getText(), canvas, canvas.getGraphicsContext2D(), canvasPane);
        tabCanvasMap.put(tab, canvasTab);

        // Register event handlers to the canvas
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);
    }


    /**
     * Sets up keyboard shortcuts for common actions within the application.
     * <p>
     * Binds key combinations to specific actions such as Save, Exit, and Clear Canvas
     * once the scene is available.
     * </p>
     */
    private void setupShortcuts() {
        Platform.runLater(() -> {
            Scene scene = tabPane.getScene();  // Get the Scene object
            if (scene != null) {
                System.out.println("Scene is now available. Setting up shortcuts.");

                // Define the key combinations for Save, Exit, and Clear
                KeyCombination saveShortCut = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
                KeyCombination exitShortCut = new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN);
                KeyCombination clearShortCut = new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN);

                // Bind the shortcuts to the corresponding actions
                scene.getAccelerators().put(saveShortCut, this::onSaveAsClick);  // Save As functionality
                scene.getAccelerators().put(exitShortCut, this::safetyExit);     // Exit functionality
                scene.getAccelerators().put(clearShortCut, this::onCanvaClearCanva);  // Clear Canvas functionality
            } else {
                System.err.println("Scene is still not available.");
            }
        });
    }


    /**
     * Saves the current state of the canvas to the undo stack.
     * <p>
     * Captures a snapshot of the current canvas and pushes it to the undo stack.
     * Clears the redo stack to maintain a consistent undo/redo history.
     * </p>
     */
    private void saveCanvasState() {
        CanvasTab canvasTab = getSelectedCanvasTab();
        if (canvasTab != null) {
            Canvas currentCanvas = canvasTab.getCanvas();

            // Take a snapshot of the current canvas state
            WritableImage snapshot = new WritableImage((int) currentCanvas.getWidth(), (int) currentCanvas.getHeight());
            currentCanvas.snapshot(null, snapshot);

            // Push the snapshot to the undo stack
            undoStack.push(snapshot);

            // Clear the redo stack since new action is performed
            redoStack.clear();
        }
    }


    /**
     * Retrieves the CanvasTab associated with the currently selected tab.
     *
     * @return the CanvasTab object corresponding to the selected tab, or null if no tab is selected
     */
    private CanvasTab getSelectedCanvasTab() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        return tabCanvasMap.get(selectedTab);
    }


    /**
     * Performs an undo action on the current canvas.
     * <p>
     * Restores the previous state of the canvas from the undo stack, and pushes the current
     * state to the redo stack to allow for a redo operation. If the undo stack is empty,
     * no action is performed.
     * </p>
     */
    @FXML
    public void onUndoClick() {
        CanvasTab canvasTab = getSelectedCanvasTab();
        if (canvasTab != null) {
            Canvas currentCanvas = canvasTab.getCanvas();
            GraphicsContext gc = currentCanvas.getGraphicsContext2D();

            if (!undoStack.isEmpty()) {
                // Save the current state to the redo stack
                WritableImage currentSnapshot = new WritableImage((int) currentCanvas.getWidth(), (int) currentCanvas.getHeight());
                currentCanvas.snapshot(null, currentSnapshot);
                redoStack.push(currentSnapshot);

                // Pop the last state from the undo stack and restore it
                WritableImage previousSnapshot = undoStack.pop();
                gc.clearRect(0, 0, currentCanvas.getWidth(), currentCanvas.getHeight());
                gc.drawImage(previousSnapshot, 0, 0);
            }
        }
    }


    /**
     * Performs a redo action on the current canvas.
     * <p>
     * Restores the next state of the canvas from the redo stack, and pushes the current
     * state to the undo stack to allow for further undo operations. If the redo stack
     * is empty, no action is performed.
     * </p>
     */
    @FXML
    public void onRedoClick() {
        CanvasTab canvasTab = getSelectedCanvasTab();
        if (canvasTab != null) {
            Canvas currentCanvas = canvasTab.getCanvas();
            GraphicsContext gc = currentCanvas.getGraphicsContext2D();

            if (!redoStack.isEmpty()) {
                // Save the current state to the undo stack
                WritableImage currentSnapshot = new WritableImage((int) currentCanvas.getWidth(), (int) currentCanvas.getHeight());
                currentCanvas.snapshot(null, currentSnapshot);
                undoStack.push(currentSnapshot);

                // Pop the last state from the redo stack and restore it
                WritableImage nextSnapshot = redoStack.pop();
                gc.clearRect(0, 0, currentCanvas.getWidth(), currentCanvas.getHeight());
                gc.drawImage(nextSnapshot, 0, 0);
            }
        }
    }


    /**
     * Clears the undo and redo stacks.
     * <p>
     * This method is typically called when initializing a new canvas or resetting the
     * canvas history.
     * </p>
     */
    private void clearUndoRedoStacks() {
        undoStack.clear();
        redoStack.clear();
    }


    /**
     * Retrieves the canvas from the specified tab.
     * <p>
     * This helper method checks if the tab content is a ScrollPane containing a StackPane
     * with a Canvas. If so, it returns the Canvas; otherwise, returns null.
     * </p>
     *
     * @param tab the Tab from which to retrieve the Canvas
     * @return the Canvas object if found, or null if no canvas is present
     */
    private Canvas getCanvasFromTab(Tab tab) {
        if (tab.getContent() instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) tab.getContent();
            StackPane canvasPane = (StackPane) scrollPane.getContent();
            return (Canvas) canvasPane.getChildren().get(0);
        }
        return null;
    }


    /**
     * Adds a new tab with a specified title, width, and height.
     * <p>
     * Creates a new CanvasTab, sets up a ScrollPane for it, and registers the canvas
     * with event handlers for drawing. The new tab is then added to the TabPane and selected.
     * </p>
     *
     * @param title the title of the new tab
     * @param width the width of the canvas in the new tab
     * @param height the height of the canvas in the new tab
     */
    private void addNewTab(String title, double width, double height) {
        // Create a new Tab instance with the provided title
        Tab newTab = new Tab(title);

        // Create a new CanvasTab instance to manage the canvas and its container
        CanvasTab canvasTab = new CanvasTab(title, width, height);

        // Wrap the canvasPane (which contains the canvas) in a ScrollPane for scrolling capability
        ScrollPane scrollPane = new ScrollPane(canvasTab.getCanvasPane());
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Set the ScrollPane as the content of the new tab
        newTab.setContent(scrollPane);

        // Map the new tab to its corresponding CanvasTab for easy retrieval
        tabCanvasMap.put(newTab, canvasTab);

        // Retrieve the canvas from the CanvasTab
        Canvas canvas = canvasTab.getCanvas();

        // Register event handlers for drawing on the canvas
        setupCanvasDrawing(canvasTab);

        //tring contextPath = "/canvas" + tabPane.getTabs().size();
        //httpServer.createContext(contextPath, new SingleImageHandler(contextPath));

        // Add the new tab to the TabPane and select it
        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
    }


    /**
     * Sets up a new tab with default canvas dimensions and settings.
     * <p>
     * Creates a CanvasTab with a default title and dimensions, wraps it in a ScrollPane,
     * and registers the canvas for drawing. The tab is then added to the TabPane and selected.
     * </p>
     */
    private void setupNewTab() {
        // Create a new CanvasTab with default settings
        CanvasTab canvasTab = new CanvasTab("Canvas " + (tabPane.getTabs().size() + 1), 800, 600);

        // Create a Tab and set its content to the StackPane containing the canvas
        Tab newTab = new Tab(canvasTab.getTitle());

        // Wrap the canvas in a ScrollPane
        ScrollPane scrollPane = new ScrollPane(canvasTab.getCanvasPane());
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        newTab.setContent(scrollPane);

        // Add to TabPane and select the new tab
        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);

        // Save initial state
        saveCanvasState();

        // Store the CanvasTab instance for this Tab
        canvasTabs.put(newTab, canvasTab);

        // Set up drawing on the canvas
        setupCanvasDrawing(canvasTab);  // Pass the entire CanvasTab object
    }


    /**
     * Configures event handlers for drawing on the specified CanvasTab.
     * <p>
     * Sets up mouse event handlers on the temporary canvas for live drawing, and commits
     * the drawing to the main canvas on mouse release.
     * </p>
     *
     * @param canvasTab the CanvasTab to set up for drawing
     */
    private void setupCanvasDrawing(CanvasTab canvasTab) {
        Canvas tempCanvas = canvasTab.getTempCanvas(); // Temporary canvas for live drawing
        Canvas mainCanvas = canvasTab.getCanvas(); // Main canvas for final drawing

        // Mouse pressed event to capture the start point
        tempCanvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            onMousePressed(event); // Use your existing onMousePressed logic
        });

        // Mouse dragged event for live drawing (draw on the temp canvas)
        tempCanvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            onMouseDragged(event); // Use your existing onMouseDragged logic (draw on tempCanvas)
        });

        // Mouse released event to finalize the drawing (commit to the main canvas)
        tempCanvas.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            onMouseReleased(event); // Use your existing onMouseReleased logic (commit drawing to mainCanvas)
        });
        // Add the MOUSE_MOVED event handler to update the label with the coordinates
        tempCanvas.addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
            updateLabel(event.getX(), event.getY());
        });
    }


    /**
     * Initializes and groups tool buttons in a ToggleGroup.
     * <p>
     * Assigns each tool button to a ToggleGroup and sets their actions to activate specific
     * drawing tools when selected.
     * </p>
     */
    private void setupToolButtons() {

        ToggleGroup toolsToggleGroup = new ToggleGroup();
        ToggleGroup editToolsToggleGroup = new ToggleGroup();
        pencilButton.setToggleGroup(toolsToggleGroup);
        eraserButton.setToggleGroup(toolsToggleGroup);
        lineButton.setToggleGroup(toolsToggleGroup);
        rectangleButton.setToggleGroup(toolsToggleGroup);
        ellipseButton.setToggleGroup(toolsToggleGroup);
        circleButton.setToggleGroup(toolsToggleGroup);
        triangleButton.setToggleGroup(toolsToggleGroup);
        imageButton.setToggleGroup(toolsToggleGroup);
        starButton.setToggleGroup(toolsToggleGroup);
        heartButton.setToggleGroup(toolsToggleGroup);
        selectButton.setToggleGroup(toolsToggleGroup);
        spiralButton.setToggleGroup(toolsToggleGroup);
        cubeButton.setToggleGroup(toolsToggleGroup);
        nGonButton.setToggleGroup(toolsToggleGroup);
        textButton.setToggleGroup(toolsToggleGroup);
        colorGrabButton.setToggleGroup(toolsToggleGroup);
        charcoalBrushButton.setToggleGroup(toolsToggleGroup);
        sprayPaintBrushButton.setToggleGroup(toolsToggleGroup);
        pyramidButton.setToggleGroup(toolsToggleGroup);
        arrowButton.setToggleGroup(toolsToggleGroup);

        //edit buttons are the buttons that alter the nature of the rest of the drawing tools
        fillToggleButton.setToggleGroup(editToolsToggleGroup);
        dashedToggleButton.setToggleGroup(editToolsToggleGroup);
        selectButton.setToggleGroup(editToolsToggleGroup);

        // Set the onAction for the tool buttons (example for pencil)
        pencilButton.setOnAction(event -> setToolDrawing("Pencil"));
        eraserButton.setOnAction(event -> setToolDrawing("Eraser"));
        lineButton.setOnAction(event -> setLineTool());
        rectangleButton.setOnAction(event -> setRectangleTool());
        ellipseButton.setOnAction(event -> setEllipseTool());
        circleButton.setOnAction(event -> setCircleTool());
        triangleButton.setOnAction(event -> setTriangleTool());
        imageButton.setOnAction(event -> setImageTool());
        starButton.setOnAction(event -> setStarTool());
        heartButton.setOnAction(event -> setHeartTool());
        selectButton.setOnAction(event -> setSelectTool());
        spiralButton.setOnAction(event -> setSpiralTool());
        cubeButton.setOnAction(event -> setCubeTool());
        textButton.setOnAction(event -> setTextTool());
        nGonButton.setOnAction(event -> setNgonTool());
        pyramidButton.setOnAction(event -> setPyramidTool());
        arrowButton.setOnAction(event -> setArrowTool());
        colorGrabButton.setOnAction(event -> setImageTool());
        charcoalBrushButton.setOnAction(event -> setCharcoalBrushTool());
        sprayPaintBrushButton.setOnAction(event -> setSprayPaintBrushTool());


    }

    /**
     * Configures event listeners for selecting areas on the canvas.
     * <p>
     * Sets mouse event listeners on the currently selected canvas to enable area selection
     * functionality.
     * </p>
     */
    private void setToolSelection() {
        // Set the listeners for selecting areas
        CanvasTab canvasTab = getSelectedCanvasTab();
        if (canvasTab != null) {
            Canvas currentCanvas = canvasTab.getCanvas();

            // Clear any previous listeners
            currentCanvas.setOnMousePressed(this::onMousePressed);
            currentCanvas.setOnMouseDragged(this::onMouseDragged);
            currentCanvas.setOnMouseReleased(this::onMouseReleased);

        }
    }


    /**
     * Sets the current tool for drawing and configures its listeners.
     * <p>
     * Updates the current drawing tool and assigns mouse event listeners to the canvas
     * to handle drawing actions specific to the selected tool.
     * </p>
     *
     * @param tool the name of the drawing tool to activate
     */
    private void setToolDrawing(String tool) {
        // Set the current tool and its listeners
        currentTool = tool;
        CanvasTab canvasTab = getSelectedCanvasTab();
        if (canvasTab != null) {
            Canvas currentCanvas = canvasTab.getCanvas();

            // Clear any previous listeners
            currentCanvas.setOnMousePressed(this::onMousePressed);
            currentCanvas.setOnMouseDragged(this::onMouseDragged);
            currentCanvas.setOnMouseReleased(this::onMouseReleased);

        }
    }


    /**
     * Updates the information label with the canvas size and pen size.
     * <p>
     * Retrieves the size of the currently selected canvas and the current pen size, and
     * displays this information in the info text label.
     * </p>
     */
    private void updateLabel() { // TS
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null && selectedTab.getContent() instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) selectedTab.getContent();
            if (scrollPane.getContent() instanceof StackPane) {
                StackPane canvasPane = (StackPane) scrollPane.getContent();
                if (!canvasPane.getChildren().isEmpty() && canvasPane.getChildren().get(0) instanceof Canvas) {
                    Canvas currentCanvas = (Canvas) canvasPane.getChildren().get(0);

                    double width = currentCanvas.getWidth();
                    double height = currentCanvas.getHeight();
                    double currentPenSize = getLineWidth(); // Get the current pen size from the slider

                    infoText.setText(String.format("Canvas size: %.0f x %.0f | Pen Size: %.0f px", width, height, currentPenSize));
                } else {
                    System.err.println("No canvas found in the StackPane.");
                }
            } else {
                System.err.println("The content inside ScrollPane is not a StackPane.");
            }
        } else {
            System.err.println("The content of the tab is not a ScrollPane.");
        }
    }


    /**
     * Updates the line width for drawing on the selected canvas.
     * <p>
     * Sets the current line width on the graphics context of the canvas in the selected tab.
     * </p>
     */
    @FXML
    public void setLineWidth() {
        // Update the current line width for drawing on the selected canvas
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            CanvasTab canvasTab = canvasTabs.get(selectedTab);
            if (canvasTab != null) {
                canvasTab.getGraphicsContext().setLineWidth(getLineWidth());
            }
        }
    }


    /**
     * Activates color grabbing functionality when the color grab button is selected.
     * <p>
     * Enables or disables the color grabbing mode based on the button's selected state.
     * When enabled, allows picking a color from the canvas; when disabled, reverts to the
     * default tool.
     * </p>
     */
    @FXML
    private void onColorGrabClick() {
        // Toggle the color grab button state
        if (colorGrabButton.isSelected()) {
            // Enable color grabbing functionality
            enableColorGrabMode();
        } else {
            // Reset to default if deselected
            resetToDefaultTool();
        }
    }


    /**
     * Enables the color grab mode, allowing the user to pick a color from the canvas.
     * <p>
     * Sets up an informational message and configures the canvas for color picking
     * by adding a mouse click event for color grabbing.
     * </p>
     */
    private void enableColorGrabMode() {
        infoText.setText("Click on the canvas to grab a color.");

        // Get the currently selected canvas tab
        CanvasTab canvasTab = getSelectedCanvasTab();
        if (canvasTab != null) {
            Canvas currentCanvas = canvasTab.getCanvas();

            // Clear any existing mouse click handlers before adding the color grab handler
            currentCanvas.setOnMouseClicked(null);  // Remove any previous click handlers

            // Set up color grabbing on mouse click
            //currentCanvas.setOnMouseClicked(event -> grabColor(event, currentCanvas));
        }
    }


    /**
     * Toggles the color grabbing functionality.
     * <p>
     * Activates or deactivates the color grabbing mode based on the button's selected state.
     * When activated, the color picker is enabled; otherwise, color grabbing is disabled.
     * </p>
     */
    @FXML
    private void toggleColorGrab() {
        if (colorGrabButton.isSelected()) {
            // Enable color picking mode
            useEyedrop(colorPicker);
        } else {
            // Disable color picking mode by removing the event handler
            CanvasTab canvasTab = getSelectedCanvasTab();
            if (canvasTab != null) {
                canvasTab.getCanvas().setOnMousePressed(null); // Clear the eyedrop event handler
            }
        }
    }


    /**
     * Activates color picking functionality on the canvas.
     * <p>
     * When the user clicks on the canvas, captures the color at the clicked coordinates
     * and updates the ColorPicker with the selected color.
     * </p>
     *
     * @param colorPicker the ColorPicker control to update with the selected color
     */
    public void useEyedrop(ColorPicker colorPicker) {
        CanvasTab canvasTab = getSelectedCanvasTab();
        if (canvasTab != null) {
            Canvas mainCanvas = canvasTab.getCanvas();

            // Set up a mouse pressed event on the main canvas for color picking
            mainCanvas.setOnMousePressed(event -> {
                try {
                    double x = event.getX();
                    double y = event.getY();

                    // Capture a snapshot of the canvas and get the color at the clicked coordinates
                    WritableImage snapshot = mainCanvas.snapshot(null, null);
                    PixelReader pixelReader = snapshot.getPixelReader();

                    // Ensure coordinates are within bounds before trying to access them
                    if (x >= 0 && x < snapshot.getWidth() && y >= 0 && y < snapshot.getHeight()) {
                        Color color = pixelReader.getColor((int) x, (int) y);
                        colorPicker.setValue(color);
                    } else {
                        System.err.println("Coordinates out of bounds for the snapshot.");
                    }
                } catch (Exception e) {
                    System.err.println("Error capturing color: " + e.getMessage());
                }
            });
        } else {
            System.err.println("No CanvasTab found for the selected tab.");
        }
    }


    /**
     * Rotates the selected chunk on the canvas by a specified angle.
     * <p>
     * Applies a rotation transformation to the selected chunk and draws the
     * rotated image on a temporary canvas.
     * </p>
     *
     * @param angle the rotation angle in degrees
     */
    @FXML
    private void onRotateChunk(double angle) {
        if (selectedChunk != null) {
            CanvasTab canvasTab = getSelectedCanvasTab();
            if (canvasTab != null) {
                GraphicsContext tempGc = canvasTab.getTempGraphicsContext();

                // Clear the temp canvas before drawing the rotated image
                tempGc.clearRect(0, 0, canvasTab.getTempCanvas().getWidth(), canvasTab.getTempCanvas().getHeight());

                // Save the current transformation state
                tempGc.save();

                // Move the canvas origin to the center of the selected chunk
                tempGc.translate(selectionStartX + selectedChunk.getWidth() / 2, selectionStartY + selectedChunk.getHeight() / 2);

                // Apply the rotation
                tempGc.rotate(angle);

                // Draw the selected chunk centered
                tempGc.drawImage(selectedChunk, -selectedChunk.getWidth() / 2, -selectedChunk.getHeight() / 2);

                // Restore the transformation state
                tempGc.restore();
            }
        }
    }


    /**
     * Moves the selected chunk on the canvas by the specified delta values.
     * <p>
     * Translates the selected chunk by the given x and y offsets and redraws
     * it on the temporary canvas.
     * </p>
     *
     * @param deltaX the change in the x-coordinate
     * @param deltaY the change in the y-coordinate
     */
    @FXML
    private void onMoveChunk(double deltaX, double deltaY) {
        if (selectedChunk != null) {
            CanvasTab canvasTab = getSelectedCanvasTab();
            if (canvasTab != null) {
                GraphicsContext tempGc = canvasTab.getTempGraphicsContext();

                // Clear the temporary canvas
                tempGc.clearRect(0, 0, canvasTab.getTempCanvas().getWidth(), canvasTab.getTempCanvas().getHeight());

                // Move the selected chunk by deltaX and deltaY
                tempGc.drawImage(selectedChunk, selectionStartX + deltaX, selectionStartY + deltaY);
            }
        }
    }


    /**
     * Commits changes made to the selected chunk, applying it permanently to the main canvas.
     * <p>
     * Clears the temporary canvas and draws the selected chunk onto the main canvas, finalizing
     * any transformations or movements applied to it.
     * </p>
     */
    @FXML
    private void onCommitChunkChanges() {
        if (selectedChunk != null) {
            CanvasTab canvasTab = getSelectedCanvasTab();
            if (canvasTab != null) {
                GraphicsContext gc = canvasTab.getGraphicsContext();

                // Clear the selection highlight
                canvasTab.getTempGraphicsContext().clearRect(0, 0, canvasTab.getTempCanvas().getWidth(), canvasTab.getTempCanvas().getHeight());

                // Draw the selected chunk on the main canvas
                gc.drawImage(selectedChunk, selectionStartX, selectionStartY);

                // Clear the selected chunk
                selectedChunk = null;
            }
        }
    }


    /**
     * Resets the active tool to the default tool, typically the pencil tool.
     * <p>
     * Deselects the color grab button, resets the informational text, and removes
     * any click listeners from the canvas to disable color grabbing.
     * </p>
     */
    private void resetToDefaultTool() {
        // Reset tool to the default tool, e.g., Pencil
        currentTool = "Pencil";
        colorGrabButton.setSelected(false);  // Deselect the color grab button
        infoText.setText("Back to pencil tool.");

        // Remove mouse click listener from the canvas to stop grabbing colors
        CanvasTab canvasTab = getSelectedCanvasTab();
        if (canvasTab != null) {
            Canvas currentCanvas = canvasTab.getCanvas();
            currentCanvas.setOnMouseClicked(null);  // Remove the click listener
        }
    }


    /**
     * Sets the eraser tool as the active drawing tool.
     * <p>
     * Updates the active tool to the eraser and logs the selection event.
     * </p>
     */
    @FXML
    private void setPencilTool() {
        currentTool = "Pencil";
        CanvasTab canvasTab = getSelectedCanvasTab();
        if (canvasTab != null) {
            Canvas currentCanvas = canvasTab.getCanvas();

            // Clear selection mode and set new mouse handlers for drawing
            isSelecting = false;
            currentCanvas.setOnMousePressed(this::onMousePressed);
            currentCanvas.setOnMouseDragged(this::onMouseDragged);
            currentCanvas.setOnMouseReleased(this::onMouseReleased);
        }
    }


    /**
     * Sets the eraser tool as the active drawing tool.
     * <p>
     * Updates the active tool to the eraser and logs the selection event.
     * </p>
     */
    @FXML
    private void setEraserTool() {
        currentTool = "Eraser";
        logWriter.logEvent(getCurrentTabName(), "Eraser tool selected");
    }


    /**
     * Sets the line tool as the active drawing tool.
     * <p>
     * Updates the active tool to the line tool and logs the selection event.
     * </p>
     */
    @FXML
    private void setLineTool() {
        currentTool = "Line";
        logWriter.logEvent(getCurrentTabName(), "Pencil tool selected");
    }


    /**
     * Sets the rectangle tool as the active drawing tool.
     * <p>
     * Updates the active tool to the rectangle tool and logs the selection event.
     * </p>
     */
    @FXML
    private void setRectangleTool() {
        currentTool = "Rectangle";
        logWriter.logEvent(getCurrentTabName(), "Rectangle tool selected");
    }


    /**
     * Sets the ellipse tool as the active drawing tool.
     * <p>
     * Updates the active tool to the ellipse tool and logs the selection event.
     * </p>
     */
    @FXML
    private void setEllipseTool() {
        currentTool = "Ellipse";
        logWriter.logEvent(getCurrentTabName(), "Ellipse tool selected");
    }


    /**
     * Sets the circle tool as the active drawing tool.
     * <p>
     * Updates the active tool to the circle tool and logs the selection event.
     * </p>
     */
    @FXML
    private void setCircleTool() {
        currentTool = "Circle";
        logWriter.logEvent(getCurrentTabName(), "Circle tool selected");
    }


    /**
     * Sets the triangle tool as the active drawing tool.
     * <p>
     * Updates the active tool to the triangle tool and logs the selection event.
     * </p>
     */
    @FXML
    private void setTriangleTool() {
        currentTool = "Triangle";
        logWriter.logEvent(getCurrentTabName(), "Triangle tool selected");
    }


    /**
     * Sets the image tool as the active drawing tool.
     * <p>
     * Updates the active tool to the image tool for applying stickers and logs the selection event.
     * </p>
     */
    @FXML
    private void setImageTool() {
        currentTool = "Image";
        logWriter.logEvent(getCurrentTabName(), "Sticker tool selected");
    }


    /**
     * Sets the star tool as the active drawing tool.
     * <p>
     * Updates the active tool to the star tool and logs the selection event.
     * </p>
     */
    @FXML
    private void setStarTool() {
        currentTool = "Star";
        logWriter.logEvent(getCurrentTabName(), "Star tool selected");
    }


    /**
     * Sets the heart tool as the active drawing tool.
     * <p>
     * Updates the active tool to the heart tool and logs the selection event.
     * </p>
     */
    @FXML
    private void setHeartTool() {
        currentTool = "Heart";
        logWriter.logEvent(getCurrentTabName(), "Heart tool selected");
    }


    /**
     * Sets the pyramid tool as the active drawing tool.
     * <p>
     * Updates the active tool to the pyramid tool and logs the selection event.
     * </p>
     */
    @FXML
    private void setPyramidTool() {
        currentTool = "pyramid";
        logWriter.logEvent(getCurrentTabName(), "Pyramid tool selected");
    }


    /**
     * Sets the arrow tool as the active drawing tool.
     * <p>
     * Updates the active tool to the arrow tool and logs the selection event.
     * </p>
     */
    @FXML
    private void setArrowTool() {
        currentTool = "Arrow";
        logWriter.logEvent(getCurrentTabName(), "Arrow tool selected");
    }

    /**
     * Sets the spiral tool as the active drawing tool.
     * <p>
     * Updates the active tool to the spiral tool and logs the selection event.
     * </p>
     */
    @FXML
    private void setSpiralTool() {
        currentTool = "Spiral";
        logWriter.logEvent(getCurrentTabName(), "Spiral tool selected");
    }


    /**
     * Sets the cube tool as the active drawing tool.
     * <p>
     * Updates the active tool to the cube tool and logs the selection event.
     * </p>
     */
    @FXML
    private void setCubeTool() {
        currentTool = "Cube";
        logWriter.logEvent(getCurrentTabName(), "Cube tool selected");
    }


    /**
     * Sets the text tool as the active drawing tool.
     * <p>
     * Updates the active tool to the text tool and logs the selection event.
     * </p>
     */
    @FXML
    private void setTextTool() {
        currentTool = "Text";
        logWriter.logEvent(getCurrentTabName(), "Text tool selected");
    }


    /**
     * Sets the n-gon (polygon) tool as the active drawing tool.
     * <p>
     * Updates the active tool to the n-gon tool and logs the selection event.
     * </p>
     */
    @FXML
    private void setNgonTool() {
        currentTool = "nGon";
        logWriter.logEvent(getCurrentTabName(), "Polygon tool selected");
    }


    /**
     * Sets the selection tool for selecting areas on the canvas.
     * <p>
     * Updates the active tool to the selection tool and logs the selection event.
     * </p>
     */
    @FXML
    private void setSelectTool() {
        currentTool = "Select";
        logWriter.logEvent(getCurrentTabName(), "Select tool selected");
    }


    /**
     * Sets the charcoal brush tool as the active drawing tool.
     * <p>
     * Updates the active tool to the charcoal brush tool (bubbles effect) and logs the selection event.
     * </p>
     */
    @FXML
    private void setCharcoalBrushTool() {
        currentTool = "Bubbles";
        logWriter.logEvent(getCurrentTabName(), "Bubbles tool selected");
    }


    /**
     * Sets the spray paint brush tool as the active drawing tool.
     * <p>
     * Updates the active tool to the spray paint tool and logs the selection event.
     * </p>
     */
    @FXML
    private void setSprayPaintBrushTool() {
        currentTool = "Spray";
        logWriter.logEvent(getCurrentTabName(), "Spray tool selected");
    }


    /**
     * Handles mouse press events on the canvas.
     * <p>
     * Initializes the starting coordinates for drawing, sets color and line properties,
     * and enables eyedropper functionality if the color grab mode is active.
     * </p>
     *
     * @param event the MouseEvent containing the mouse click coordinates
     */
    @FXML
    private void onMousePressed(MouseEvent event) {
        prevX = event.getX();
        prevY = event.getY();
        CanvasTab canvasTab = getSelectedCanvasTab();
        if (canvasTab != null) {
            GraphicsContext gc = canvasTab.getGraphicsContext();
            GraphicsContext tempGc = canvasTab.getTempGraphicsContext();  // Temporary canvas GC

            if (colorGrabButton.isSelected()) {
                // Eyedropper mode: pick color from canvas
                WritableImage snapshot = canvasTab.getCanvas().snapshot(null, null);
                PixelReader pixelReader = snapshot.getPixelReader();

                double x = event.getX();
                double y = event.getY();

                // Ensure coordinates are within bounds
                if (x >= 0 && x < snapshot.getWidth() && y >= 0 && y < snapshot.getHeight()) {
                    Color color = pixelReader.getColor((int) x, (int) y);
                    colorPicker.setValue(color);  // Set picked color in ColorPicker
                }
                // Exit after picking the color to avoid further processing in draw mode
                return;
            }

            startX = event.getX();
            startY = event.getY();

            gc.setStroke(colorPicker.getValue());
            gc.setLineWidth(getLineWidth());
            gc.setLineCap(StrokeLineCap.BUTT); //soft edges

            if (fillShapes) {
                gc.setFill(colorPicker.getValue()); // Set the selected fill color
            }
            if (selectButton.isSelected()) {
                isSelecting = true;
                startX = event.getX();
                startY = event.getY();
            }


            if (dashedToggleButton.isSelected()) {
                gc.setLineDashes((2 * getLineWidth() + 10));
                tempGc.setLineDashes((2 * getLineWidth() + 10));
            } else {
                gc.setLineDashes(0);
                tempGc.setLineDashes(0);
            }

            if (pencilButton.isSelected() || eraserButton.isSelected()) {
                if (eraserButton.isSelected()) {
                    gc.setStroke(Color.WHITE);
                }
                gc.beginPath();
                gc.moveTo(startX, startY);
                gc.stroke();
            }
        }
    }


    /**
     * Handles mouse drag events on the canvas.
     * <p>
     * Provides real-time drawing feedback by updating the temporary canvas with
     * the selected shape or effect (e.g., line, rectangle, spray paint).
     * </p>
     *
     * @param event the MouseEvent containing the drag coordinates
     */
    @FXML
    private void onMouseDragged(MouseEvent event) {
        CanvasTab canvasTab = getSelectedCanvasTab();
        if (canvasTab != null) {
            GraphicsContext gc = canvasTab.getGraphicsContext(); // Main canvas GC
            GraphicsContext tempGc = canvasTab.getTempGraphicsContext();  // Temporary canvas GC
            double endX = event.getX();
            double endY = event.getY();


           if (charcoalBrushButton.isSelected()) {
               drawCharcoalBrush(gc, endX, endY);
            } else if (sprayPaintBrushButton.isSelected()) {
               drawSprayPaintBrush(gc, endX, endY);
            }

            prevX = endX;
            prevY = endY;

            if (isSelecting) {
                if (canvasTab != null) {
                    // Draw a dashed rectangle to visualize the selection
                    tempGc.setLineDashes(10);
                    tempGc.setStroke(Color.BLUE);
                    tempGc.strokeRect(
                            Math.min(startX, endX),
                            Math.min(startY, endY),
                            Math.abs(endX - startX),
                            Math.abs(endY - startY)
                    );
                }
            }


            if (pencilButton.isSelected() || eraserButton.isSelected()) {
                // Set stroke and line width for gc
                if (eraserButton.isSelected()) {
                    gc.setStroke(Color.WHITE);
                } else {
                    gc.setStroke(colorPicker.getValue());
                }
                gc.setLineWidth(getLineWidth());

                gc.lineTo(endX, endY);
                gc.stroke();
            } else {
                // Clear the temporary canvas for redrawing
                tempGc.clearRect(0, 0, canvasTab.getTempCanvas().getWidth(), canvasTab.getTempCanvas().getHeight());

                tempGc.setStroke(colorPicker.getValue()); // Use the color picker value
                tempGc.setFill(colorPicker.getValue());
                tempGc.setLineWidth(getLineWidth());

                double controlX = event.getX();
                double controlY = event.getY();

                // Draw the selected shape on the temporary canvas
                if (lineButton.isSelected()) {
                    tempGc.strokeLine(startX, startY, endX, endY);
                }


                else if (rectangleButton.isSelected()) {
                    double width = Math.abs(endX - startX);
                    double height = Math.abs(endY - startY);
                    if (fillShapes) {
                        tempGc.fillRect(Math.min(startX, endX), Math.min(startY, endY), width, height);
                    } else {
                        tempGc.strokeRect(Math.min(startX, endX), Math.min(startY, endY), width, height);
                    }
                } else if (ellipseButton.isSelected()) {
                    double width = Math.abs(endX - startX);
                    double height = Math.abs(endY - startY);
                    if (fillShapes) {
                        tempGc.fillOval(Math.min(startX, endX), Math.min(startY, endY), width, height);
                    } else {
                        tempGc.strokeOval(Math.min(startX, endX), Math.min(startY, endY), width, height);
                    }
                } else if (circleButton.isSelected()) {
                    double radius = Math.hypot(endX - startX, endY - startY);
                    if (fillShapes) {
                        tempGc.fillOval(startX - radius, startY - radius, radius * 2, radius * 2);
                    } else {
                        tempGc.strokeOval(startX - radius, startY - radius, radius * 2, radius * 2);
                    }
                } else if (triangleButton.isSelected()) {
                    tempGc.clearRect(0, 0, canvasTab.getTempCanvas().getWidth(), canvasTab.getTempCanvas().getHeight());
                    drawTriangle(tempGc, startX, startY, event.getX(), event.getY(), fillShapes);
                }else if (starButton.isSelected()) {
                    drawStar(tempGc, startX, startY, endX, endY, starPoints, fillShapes); // Preview on temporary canvas
                } else if (cubeButton.isSelected()) {
                    double centerX = (startX + endX) / 2;
                    double centerY = (startY + endY) / 2;
                    double cubeSize = Math.min(Math.abs(endX - startX), Math.abs(endY - startY));

                    // Draw preview of the cube on the temp canvas
                    drawCube(tempGc, centerX, centerY, cubeSize);
                } else if (heartButton.isSelected()) {
                    double centerX = (startX + endX) / 2;
                    double centerY = (startY + endY) / 2;
                    double size = Math.abs(endX - startX) / 16;
                    drawHeart(tempGc, centerX, centerY, size, fillShapes);
                } else if (imageButton.isSelected()) {
                    double width = Math.abs(endX - startX);
                    double height = Math.abs(endY - startY);
                    tempGc.drawImage(customStickerImage, Math.min(startX, endX), Math.min(startY, endY), width, height);
                } else if (nGonButton.isSelected()) {
                    int numberOfSides = nGonSides;
                    drawNgon(tempGc, startX, startY, endX, endY, numberOfSides, fillShapes);
                } else if (textButton.isSelected()) {
                    double deltaX = endX - startX;
                    double deltaY = endY - startY;
                    double distance = Math.hypot(deltaX, deltaY);
                    double fontSize = distance / 8;  // Calculate font size based on distance
                    Font font = createFontWithStyle(fontFamily, fontSize, italic, bold);
                    drawText(tempGc, startX, startY, endX, endY, stringToolText, font, colorPicker.getValue());
                } else if (spiralButton.isSelected()) {

                    double radius = Math.hypot(endX - startX, endY - startY);

                    double turns = (endX - startX + endY - startY) / 8;


                    // Draw the spiral preview on the temporary canvas
                    drawSpiral(tempGc, startX, startY, radius, 5, 0.5, turns);
                }else if (arrowButton.isSelected()) {
                    tempGc.clearRect(0, 0, canvasTab.getTempCanvas().getWidth(), canvasTab.getTempCanvas().getHeight());
                    drawArrow(tempGc, startX, startY, event.getX(), event.getY(), false);
                } else if (pyramidButton.isSelected()) {
                    tempGc.clearRect(0, 0, canvasTab.getTempCanvas().getWidth(), canvasTab.getTempCanvas().getHeight());
                    drawPyramid(tempGc, startX, startY, event.getX(), event.getY(), fillShapes);
                }
            }
        }
    }

    private WritableImage copySelection() {
        if (selectedImage != null) {
            return new WritableImage(selectedImage.getPixelReader(),
                    (int) selectedImage.getWidth(),
                    (int) selectedImage.getHeight()
            );
        }
        return null;
    }

    private void cutSelection() {
        if (selectedImage != null) {
            GraphicsContext gc = getSelectedCanvasTab().getGraphicsContext();
            gc.clearRect(
                    Math.min(startX, endX),
                    Math.min(startY, endY),
                    Math.abs(endX - startX),
                    Math.abs(endY - startY)
            );
        }
    }

    private void pasteSelection(double pasteX, double pasteY) {
        if (selectedImage != null) {
            GraphicsContext gc = getSelectedCanvasTab().getGraphicsContext();
            gc.drawImage(selectedImage, pasteX, pasteY);
        }
    }


    @FXML
    private void onCopyClick() {
        WritableImage copiedImage = copySelection();
        if (copiedImage != null) {
            selectedImage = copiedImage;
        }
    }

    @FXML
    private void onCutClick() {
        cutSelection();
    }

    @FXML
    private void onPasteClick(MouseEvent event) {
        pasteSelection(event.getX(), event.getY());
    }



    /**
     * Handles mouse release events on the canvas.
     * <p>
     * Commits the final shape or drawing to the main canvas, clears the temporary canvas,
     * and saves the current canvas state for undo functionality.
     * </p>
     *
     * @param event the MouseEvent containing the release coordinates
     */
    @FXML
    private void onMouseReleased(MouseEvent event) {
        CanvasTab canvasTab = getSelectedCanvasTab();
        if (canvasTab != null) {
            GraphicsContext gc = canvasTab.getGraphicsContext();  // Main canvas GC
            GraphicsContext tempGc = canvasTab.getTempGraphicsContext();  // Temporary canvas GC
            double endX = event.getX();
            double endY = event.getY();
            double controlX = event.getX();
            double controlY = event.getY();

            // Clear the temporary canvas after releasing the mouse
            tempGc.clearRect(0, 0, canvasTab.getTempCanvas().getWidth(), canvasTab.getTempCanvas().getHeight());

            // Now commit the final shape to the main canvas
            gc.setStroke(colorPicker.getValue());
            gc.setLineWidth(getLineWidth());

            if (pencilButton.isSelected() || eraserButton.isSelected()) {
                // Nothing to do here; drawing is handled during drag
            }  if (isSelecting) {
                isSelecting = false;
                if (canvasTab != null) {

                    // Capture the selected area as an image
                    SnapshotParameters params = new SnapshotParameters();
                    params.setFill(Color.TRANSPARENT);
                    params.setViewport(new Rectangle2D(
                            Math.min(startX, endX),
                            Math.min(startY, endY),
                            Math.abs(endX - startX),
                            Math.abs(endY - startY)
                    ));

                    selectedImage = canvasTab.getCanvas().snapshot(params, null);

                    // Clear the temporary selection rectangle
                    canvasTab.getTempGraphicsContext().clearRect(
                            0, 0, canvasTab.getTempCanvas().getWidth(), canvasTab.getTempCanvas().getHeight()
                    );

                    // Optional: Highlight the selected area
                    gc.setStroke(Color.BLUE);
                    gc.setLineDashes(10);
                    gc.strokeRect(
                            Math.min(startX, endX),
                            Math.min(startY, endY),
                            Math.abs(endX - startX),
                            Math.abs(endY - startY)
                    );
                }
            } else if (lineButton.isSelected()) {
                gc.strokeLine(startX, startY, endX, endY);
            } else if (rectangleButton.isSelected()) {

                double width = Math.abs(endX - startX);
                double height = Math.abs(endY - startY);

                if (fillShapes) {
                    gc.fillRect(Math.min(startX, endX), Math.min(startY, endY), width, height);
                }else {
                    gc.strokeRect(Math.min(startX, endX), Math.min(startY, endY), width, height);
                }

            } else if (ellipseButton.isSelected()) {
                double width = Math.abs(endX - startX);
                double height = Math.abs(endY - startY);
                if (fillShapes) {
                    gc.fillOval(Math.min(startX, endX), Math.min(startY, endY), width, height);
                } else {
                    gc.strokeOval(Math.min(startX, endX), Math.min(startY, endY), width, height);
                }
            } else if (circleButton.isSelected()) {
                double radius = Math.hypot(endX - startX, endY - startY);
                if (fillShapes) {
                    gc.fillOval(startX - radius, startY - radius, radius * 2, radius * 2);
                } else {
                    gc.strokeOval(startX - radius, startY - radius, radius * 2, radius * 2);
                }
            } else if (triangleButton.isSelected()) {
                drawTriangle(gc, startX, startY, event.getX(), event.getY(), fillShapes);
            } else if (starButton.isSelected()) {
                drawStar(gc, startX, startY, endX, endY, starPoints, fillShapes);
            } else if (cubeButton.isSelected()) {
                double centerX = (startX + endX) / 2;
                double centerY = (startY + endY) / 2;
                double cubeSize = Math.min(Math.abs(endX - startX), Math.abs(endY - startY));

                // Draw preview of the cube on the temp canvas
                drawCube(gc, centerX, centerY, cubeSize);
            } else if (heartButton.isSelected()) {
                double centerX = (startX + endX) / 2;
                double centerY = (startY + endY) / 2;
                double size = Math.abs(endX - startX) / 16;
                drawHeart(gc, centerX, centerY, size, fillShapes);
            } else if (imageButton.isSelected()) {
                double width = Math.abs(endX - startX);
                double height = Math.abs(endY - startY);
                gc.drawImage(customStickerImage, Math.min(startX, endX), Math.min(startY, endY), width, height);
            } else if (nGonButton.isSelected()) {
                int numberOfSides = nGonSides;
                drawNgon(gc, startX, startY, endX, endY, numberOfSides, fillShapes);
            }else if (textButton.isSelected()) {
                double deltaX = endX - startX;
                double deltaY = endY - startY;
                double distance = Math.hypot(deltaX, deltaY);
                double fontSize = distance / 8;  // Calculate font size based on distance
                Font font = createFontWithStyle(fontFamily, fontSize, italic, bold);
                drawText(gc, startX, startY, endX, endY, stringToolText, font, colorPicker.getValue());
            }else if (spiralButton.isSelected()) {

                double radius = Math.hypot(endX - startX, endY - startY);

                double turns =  (endX - startX + endY - startY) / 8;

                // Draw the spiral preview on the temporary canvas
                drawSpiral(gc, startX, startY, radius, 5, 0.5, turns);
            } else if (arrowButton.isSelected()) {
                drawArrow(gc, startX, startY, event.getX(), event.getY(), false);
            } else if (pyramidButton.isSelected()) {
                drawPyramid(gc, startX, startY, event.getX(), event.getY(), fillShapes);
            }

            // Save the canvas state for undo functionality
            //saveCanvasState(tabPane.getSelectionModel().getSelectedItem());
            saveCanvasState();
        }
    }


    /**
     * Draws a charcoal brush effect at the specified coordinates.
     * <p>
     * Simulates a charcoal effect by randomly scattering small, semi-transparent dots.
     * </p>
     *
     * @param gc the GraphicsContext to draw on
     * @param x the x-coordinate of the brush stroke
     * @param y the y-coordinate of the brush stroke
     */
    private void drawCharcoalBrush(GraphicsContext gc, double x, double y) {
        double brushSize = getLineWidth() + 10;
        gc.setFill(colorPicker.getValue());

        for (int i = 0; i < 10; i++) { // Draw multiple points to create rough texture
            double offsetX = (Math.random() - 0.5) * brushSize;
            double offsetY = (Math.random() - 0.5) * brushSize;
            gc.setGlobalAlpha(0.2 + Math.random() * 0.3); // Vary opacity for texture
            gc.fillOval(x + offsetX, y + offsetY, brushSize / 4, brushSize / 4);
        }

        gc.setGlobalAlpha(1.0); // Reset opacity
    }



    /**
     * Draws a triangle on the canvas with a straight horizontal base.
     * <p>
     * The triangle is dynamically calculated based on the starting and ending
     * coordinates, ensuring the base corners are aligned on the same y-coordinate.
     * The top vertex is positioned at the midpoint between the starting and
     * ending x-coordinates, with the y-coordinate being the smallest of the two.
     * </p>
     *
     * @param gc     the GraphicsContext used to draw on the canvas
     * @param startX the x-coordinate of the starting point of the triangle's base
     * @param startY the y-coordinate of the starting point of the triangle's base
     * @param endX   the x-coordinate of the ending point of the triangle's base
     * @param endY   the y-coordinate of the ending point of the triangle's base
     * @param fill   if true, fills the triangle; otherwise, outlines it
     */
    private void drawTriangle(GraphicsContext gc, double startX, double startY, double endX, double endY, boolean fill) {
        // Calculate the base y-coordinate (ensuring both corners are aligned)
        double baseY = Math.max(startY, endY);

        // Points for the triangle
        double[] xPoints = {startX, endX, (startX + endX) / 2};
        double[] yPoints = {baseY, baseY, Math.min(startY, endY)};

        // Draw the triangle (fill or stroke)
        if (fill) {
            gc.fillPolygon(xPoints, yPoints, 3);
        } else {
            gc.strokePolygon(xPoints, yPoints, 3);
        }
    }



    /**
     * Draws an arrow on the canvas.
     * <p>
     * This method calculates the position of the arrowhead and draws a line
     * from the starting point to the endpoint with an optional arrowhead
     * at the end. The arrow can be filled or outlined based on the `fill` parameter.
     * </p>
     *
     * @param gc    the GraphicsContext used to draw on the canvas
     * @param startX the x-coordinate of the starting point of the arrow
     * @param startY the y-coordinate of the starting point of the arrow
     * @param endX  the x-coordinate of the endpoint of the arrow
     * @param endY  the y-coordinate of the endpoint of the arrow
     * @param fill  if true, fills the arrowhead; otherwise, outlines it
     */
    private void drawArrow(GraphicsContext gc, double startX, double startY, double endX, double endY, boolean fill) {
        double strokeWidth = gc.getLineWidth(); // Get the current stroke width
        double arrowLength = strokeWidth * 6;   // Scale arrowhead length based on stroke width
        double arrowWidth = strokeWidth * 3;    // Scale arrowhead width based on stroke width

        double angle = Math.atan2(endY - startY, endX - startX);

        // Points for the arrowhead triangle
        double tipX = endX;
        double tipY = endY;
        double baseX1 = endX - arrowLength * Math.cos(angle) - arrowWidth * Math.sin(angle);
        double baseY1 = endY - arrowLength * Math.sin(angle) + arrowWidth * Math.cos(angle);
        double baseX2 = endX - arrowLength * Math.cos(angle) + arrowWidth * Math.sin(angle);
        double baseY2 = endY - arrowLength * Math.sin(angle) - arrowWidth * Math.cos(angle);

        // Draw the main line (stopping at the base of the triangle)
        double lineEndX = endX - arrowLength * Math.cos(angle);
        double lineEndY = endY - arrowLength * Math.sin(angle);
        gc.strokeLine(startX, startY, lineEndX, lineEndY);

        // Draw the arrowhead (triangle)
        if (fill) {
            gc.setFill(gc.getStroke()); // Match the fill color to the stroke color
            gc.fillPolygon(new double[]{tipX, baseX1, baseX2}, new double[]{tipY, baseY1, baseY2}, 3);
        } else {
            gc.strokePolygon(new double[]{tipX, baseX1, baseX2}, new double[]{tipY, baseY1, baseY2}, 3);
        }
    }


    /**
     * Draws a 3D pyramid on the canvas.
     * <p>
     * This method creates a triangular base and connects the base vertices
     * to a peak point, forming a 3D pyramid. The pyramid can be filled or outlined
     * based on the `fill` parameter.
     * </p>
     *
     * @param gc    the GraphicsContext used to draw on the canvas
     * @param startX the x-coordinate of the top-left corner of the pyramid's base
     * @param startY the y-coordinate of the top-left corner of the pyramid's base
     * @param endX  the x-coordinate of the bottom-right corner of the pyramid's base
     * @param endY  the y-coordinate of the bottom-right corner of the pyramid's base
     * @param fill  if true, fills the base triangle; otherwise, outlines it
     */
    private void drawPyramid(GraphicsContext gc, double startX, double startY, double endX, double endY, boolean fill) {
        double midX = (startX + endX) / 2; // Horizontal midpoint
        double midY = (startY + endY) / 2; // Vertical midpoint

        // Four points of the rhombus
        double[] xPoints = {startX, midX, endX, midX};
        double[] yPoints = {midY, startY, midY, endY};

        // Draw the rhombus
        if (fill) {
            gc.fillPolygon(xPoints, yPoints, 4);
        } else {
            gc.strokePolygon(xPoints, yPoints, 4);
        }
    }



    /**
     * Draws a spray paint brush effect at the specified coordinates.
     * <p>
     * Simulates spray paint by scattering small dots with varied opacity around the center point.
     * </p>
     *
     * @param gc the GraphicsContext to draw on
     * @param x the x-coordinate of the brush stroke
     * @param y the y-coordinate of the brush stroke
     */
    private void drawSprayPaintBrush(GraphicsContext gc, double x, double y) {
        double brushSize = getLineWidth() + 10;
        gc.setFill(colorPicker.getValue());

        for (int i = 0; i < 30; i++) { // Increase number for denser spray
            double offsetX = (Math.random() - 0.5) * brushSize * 2;
            double offsetY = (Math.random() - 0.5) * brushSize * 2;
            gc.setGlobalAlpha(0.1 + Math.random() * 0.4); // Vary opacity for spray effect
            gc.fillOval(x + offsetX, y + offsetY, 2, 2); // Small dots for spray effect
        }

        gc.setGlobalAlpha(1.0); // Reset opacity
    }


    /**
     * Draws a star with the specified parameters on the canvas.
     * <p>
     * Calculates the vertices based on the number of points and draws the star
     * as either filled or outlined.
     * </p>
     *
     * @param gc the GraphicsContext to draw on
     * @param x1 the x-coordinate of the center point
     * @param y1 the y-coordinate of the center point
     * @param x2 the x-coordinate of the outer radius point
     * @param y2 the y-coordinate of the outer radius point
     * @param points the number of points in the star
     * @param fill if true, fills the star; otherwise, outlines it
     */
    public void drawStar(GraphicsContext gc, double x1, double y1, double x2, double y2, int points, boolean fill) {
        if (points < 4) {
            throw new IllegalArgumentException("Number of points must be 4 or more to draw a valid star.");
        }

        double[] xPoints = new double[points * 2];
        double[] yPoints = new double[points * 2];

        // Calculate the radius of the outer and inner points
        double outerRadius = Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
        double innerRadius = outerRadius / 2.5; // Adjust the ratio between the outer and inner points
        double startAngle = Math.atan2(y2 - y1, x2 - x1); // Start angle based on the direction of drag

        // Calculate the vertices for the star
        for (int i = 0; i < points * 2; i++) {
            double angle = ((Math.PI * i) / points) + startAngle;
            double radius = (i % 2 == 0) ? outerRadius : innerRadius; // Alternate between outer and inner points
            xPoints[i] = x1 + radius * Math.cos(angle);
            yPoints[i] = y1 + radius * Math.sin(angle);
        }

        // Draw the star as either filled or outlined based on the fill parameter
        if (fill) {
            gc.fillPolygon(xPoints, yPoints, points * 2);
        } else {
            gc.strokePolygon(xPoints, yPoints, points * 2);
        }
    }


    /**
     * Opens a dialog to set the number of points for the star tool.
     * <p>
     * Displays a dialog allowing the user to specify the number of points, with a minimum
     * of 4 points required for a valid star shape.
     * </p>
     */
    public void setStarPoints() {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(starPoints));
        dialog.setTitle("Set Star Points");
        dialog.setHeaderText("Star Tool");
        dialog.setContentText("Enter the number of points for the star:");

        // Set the icon for the dialog
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/paint-P-Logo.png")));
        stage.getIcons().add(icon);

        // Get user input
        dialog.showAndWait().ifPresent(input -> {
            try {
                int points = Integer.parseInt(input);
                if (points >= 4) {  // Minimum 4 points for a valid star
                    starPoints = points;
                } else {
                    // Show error message if input is invalid
                    System.out.println("Please enter a number greater than or equal to 4.");
                }
            } catch (NumberFormatException e) {
                // Handle invalid input (not a number)
                System.out.println("Invalid input! Please enter a valid number.");
            }
        });
    }


    /**
     * Resets the number of star points to the default value.
     * <p>
     * Sets the default number of points to 5 for the star tool.
     * </p>
     */
    @FXML
    private void resetStarPoints() {
        setDefaultStarPoints();
    }


    /**
     * Sets the number of star points to the default value.
     * <p>
     * Sets the default number of points to 5 for the star tool.
     * </p>
     */
    public void setDefaultStarPoints() {
        starPoints = 5; // Default to 5 points
    }


    /**
     * Draws an n-sided polygon (n-gon) with the specified parameters.
     * <p>
     * Calculates vertices based on the number of sides and radius, and draws
     * the n-gon as either filled or outlined.
     * </p>
     *
     * @param gc the GraphicsContext to draw on
     * @param x1 the x-coordinate of the center point
     * @param y1 the y-coordinate of the center point
     * @param x2 the x-coordinate of the radius point
     * @param y2 the y-coordinate of the radius point
     * @param n the number of sides for the n-gon
     * @param fill if true, fills the n-gon; otherwise, outlines it
     */
    public void drawNgon(GraphicsContext gc, double x1, double y1, double x2, double y2, int n, boolean fill) {
        double[] xPoints = new double[n];
        double[] yPoints = new double[n];

        // Calculate the radius of the n-gon (distance between start and end points)
        double radius = Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
        double startAngle = Math.atan2(y2 - y1, x2 - x1); // Starting angle based on the direction of drag

        // Calculate the vertices of the n-gon
        for (int i = 0; i < n; i++) {
            double angle = ((2 * Math.PI * i) / n) + startAngle;
            xPoints[i] = x1 + radius * Math.cos(angle);
            yPoints[i] = y1 + radius * Math.sin(angle);
        }

        // Draw the n-gon
        //gc.fillPolygon(xPoints, yPoints, n);
        if (fill) {
            gc.fillPolygon(xPoints, yPoints, n);
        } else {
            gc.strokePolygon(xPoints, yPoints, n);
        }

    }


    /**
     * Draws text on the canvas with specified start and end coordinates.
     * <p>
     * Sets font size based on the distance between start and end points and applies
     * both fill and stroke for text styling.
     * </p>
     *
     * @param gc the GraphicsContext to draw on
     * @param startX the x-coordinate of the starting point
     * @param startY the y-coordinate of the starting point
     * @param endX the x-coordinate of the end point (used to calculate font size)
     * @param endY the y-coordinate of the end point (used to calculate font size)
     * @param text the text to be drawn
     * @param font the font style for the text
     * @param color the color to be used for the text
     */
    public void drawText(GraphicsContext gc, double startX, double startY, double endX, double endY, String text, Font font, Color color) {
        gc.setLineWidth(1); // More than 1 is unreadable for text

        // Set the font directly without changing it
        gc.setFont(font);

        // Set both stroke and fill colors to the selected color from the ColorPicker
        gc.setStroke(color);
        gc.setFill(color);

        // Set a max width for the text to ensure it fits within the drawing area
        double maxWidth = Math.abs(endX - startX);

        // Ensure the custom text is not null or empty
        if (text != null && !text.isEmpty()) {
            gc.fillText(text, startX, startY, maxWidth);  // Fill the text first
            gc.strokeText(text, startX, startY, maxWidth);  // Outline the text
        }
    }


    /**
     * Draws a heart shape on the canvas centered at the specified coordinates.
     * <p>
     * Uses parametric equations to plot points in the shape of a heart, scaling
     * and positioning it based on the specified size.
     * </p>
     *
     * @param gc the GraphicsContext to draw on
     * @param centerX the x-coordinate of the heart's center
     * @param centerY the y-coordinate of the heart's center
     * @param size the scaling factor for the heart size
     * @param fill if true, fills the heart; otherwise, outlines it
     */
    private void drawHeart(GraphicsContext gc, double centerX, double centerY, double size, boolean fill) {
        int points = 100; // Number of points to plot
        double[] xPoints = new double[points];
        double[] yPoints = new double[points];

        // Loop through and calculate the heart shape points
        for (int i = 0; i < points; i++) {
            double t = Math.PI * 2 * i / points;

            // Parametric equations for the heart
            double x = 16 * Math.pow(Math.sin(t), 3);
            double y = 13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t);

            // Scale and translate the points to the desired position and size
            xPoints[i] = centerX + x * size;
            yPoints[i] = centerY - y * size; // Inverting y-axis because JavaFX y grows downwards
        }


        if (fill) {
            gc.fillPolygon(xPoints, yPoints, points);
        } else {
            gc.strokePolygon(xPoints, yPoints, points);
        }
        // Draw the heart shape using a polygon

    }


    /**
     * Draws a 3D-like cube shape on the canvas centered at the specified coordinates.
     * <p>
     * Renders the cube by drawing two squares and connecting their corners to create
     * a depth effect.
     * </p>
     *
     * @param gc the GraphicsContext to draw on
     * @param centerX the x-coordinate of the cube's center
     * @param centerY the y-coordinate of the cube's center
     * @param cubeSize the size of the cube
     */
    private void drawCube(GraphicsContext gc, double centerX, double centerY, double cubeSize) {
        double offset = cubeSize / 2.5; // Offset for depth based on cube size

        double x1 = centerX - cubeSize / 2;
        double y1 = centerY - cubeSize / 2;
        double x2 = centerX + cubeSize / 2;
        double y2 = centerY + cubeSize / 2;

        double x1Offset = x1 + offset;
        double y1Offset = y1 - offset;
        double x2Offset = x2 + offset;
        double y2Offset = y2 - offset;

        gc.strokeRect(x1, y1, cubeSize, cubeSize);
        gc.strokeRect(x1Offset, y1Offset, cubeSize, cubeSize);

        gc.strokeLine(x1, y1, x1Offset, y1Offset);
        gc.strokeLine(x2, y1, x2Offset, y1Offset);
        gc.strokeLine(x2, y2, x2Offset, y2Offset);
        gc.strokeLine(x1, y2, x1Offset, y2Offset);
    }


    /**
     * Draws a spiral shape on the canvas, expanding outward from the center point.
     * <p>
     * Iteratively increases the angle and radius to create a spiral with the specified
     * number of turns and scaling increments.
     * </p>
     *
     * @param gc the GraphicsContext to draw on
     * @param centerX the x-coordinate of the spiral's center
     * @param centerY the y-coordinate of the spiral's center
     * @param initialRadius the starting radius of the spiral
     * @param angleIncrement the angle increment for each step, in degrees
     * @param scaleIncrement the radius increment for each step
     * @param turns the number of spiral turns
     */
    private void drawSpiral(GraphicsContext gc, double centerX, double centerY, double initialRadius, double angleIncrement, double scaleIncrement, double turns) {
        gc.beginPath();

        double angle = 0; // Starting angle in radians
        double radius = initialRadius; // Initial radius

        // Move to the starting point of the spiral
        double x = centerX + radius * Math.cos(angle);
        double y = centerY + radius * Math.sin(angle);
        gc.moveTo(x, y);

        // Draw the spiral
        for (int i = 0; i < turns * 360 / angleIncrement; i++) {
            // Increase the angle and radius for each point
            angle += Math.toRadians(angleIncrement);
            radius += scaleIncrement;

            // Calculate new x and y positions
            x = centerX + radius * Math.cos(angle);
            y = centerY + radius * Math.sin(angle);

            // Draw the line to the next point
            gc.lineTo(x, y);
        }

        gc.stroke(); // Draw the spiral outline
    }


    /**
     * Opens a file chooser to select a custom sticker image for drawing on the canvas.
     * <p>
     * Allows the user to upload an image file to be used as a sticker tool.
     * </p>
     */
    @FXML
    public void setCustomSticker() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        // Open file chooser dialog
        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            // Set the uploaded image as the custom sticker
            customStickerImage = new Image(file.toURI().toString());
        }
    }


    /**
     * Sets the default sticker image for the sticker tool.
     * <p>
     * Resets the sticker tool to use the application's default image.
     * </p>
     */
    @FXML
    public void setDefaultSticker(){
        customStickerImage = new Image("/images/paint-P-Logo.png");
    }


    /**
     * Resets the text tool to use the default text.
     * <p>
     * Sets the text displayed by the text tool to a predefined default string.
     * </p>
     */
    @FXML
    public void setDefaultText(){
        stringToolText = defaultText;
    }


    /**
     * Opens a dialog for setting the number of sides for the polygon tool.
     * <p>
     * Prompts the user to enter the desired number of sides for polygons created
     * with the n-gon tool, with a minimum of 3 sides required.
     * </p>
     */
    public void setPolygonSides() {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(nGonSides));
        dialog.setTitle("Set Polygon Sides");
        dialog.setHeaderText("Polygon Tool");
        dialog.setContentText("Enter the number of sides for the polygon:");

        // Set the icon for the dialog
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();  // Fix this line by using 'dialog'
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/paint-P-Logo.png")));
        stage.getIcons().add(icon);

        // Get user input
        dialog.showAndWait().ifPresent(input -> {
            try {
                int sides = Integer.parseInt(input);
                if (sides >= 3) {  // Minimum 3 sides for a valid polygon
                    nGonSides = sides;
                } else {
                    // Show error message if input is invalid
                    System.out.println("Please enter a number greater than or equal to 3.");
                }
            } catch (NumberFormatException e) {
                // Handle invalid input (not a number)
                System.out.println("Invalid input! Please enter a valid number.");
            }
        });
    }


    /**
     * Resets the polygon tool to use the default number of sides.
     * <p>
     * Sets the number of sides for polygons drawn with the n-gon tool to a predefined
     * default value.
     * </p>
     */
    public void setDefaultPolygonSides(){
        nGonSides = 5;
    }


    /**
     * Retrieves the current number of sides for the n-gon (polygon) tool.
     *
     * @return the number of sides for the polygon
     */
    public int getNGonSides() {
        return nGonSides;
    }


    /**
     * Creates a custom font with specified style properties.
     * <p>
     * Configures a font with the specified family, size, and style attributes (bold and/or italic).
     * </p>
     *
     * @param fontFamily the name of the font family
     * @param fontSize the size of the font
     * @param italic if true, applies italic style; otherwise, uses regular style
     * @param bold if true, applies bold style; otherwise, uses normal weight
     * @return the configured Font object
     */
    @FXML
    private Font createFontWithStyle(String fontFamily, double fontSize, boolean italic, boolean bold) {
        FontWeight fontWeight = bold ? FontWeight.BOLD : FontWeight.NORMAL;
        FontPosture fontPosture = italic ? FontPosture.ITALIC : FontPosture.REGULAR;

        return Font.font(fontFamily, fontWeight, fontPosture, fontSize);
    }


    /**
     * Opens a dialog to set a custom text string for the text tool.
     * <p>
     * Prompts the user to input custom text, which will then be used when drawing text
     * on the canvas with the text tool.
     * </p>
     */
    @FXML
    public void setCustomText() {
        // Create the TextInputDialog
        TextInputDialog textInputDialog = new TextInputDialog();
        textInputDialog.setTitle("Set Custom Text");
        textInputDialog.setHeaderText("Enter the text you want to use for the text tool:");
        textInputDialog.setContentText("Text:");

        // Set the custom icon for the dialog
        Stage stage = (Stage) textInputDialog.getDialogPane().getScene().getWindow();
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/paint-P-Logo.png")));
        stage.getIcons().add(icon);

        // Show the dialog and capture the result
        Optional<String> result = textInputDialog.showAndWait();

        // If the user provides input, set the custom text
        result.ifPresent(this::setCustomTextTool);
    }


    /**
     * Sets a custom text string for the text tool.
     * <p>
     * Updates the text that will be drawn on the canvas when using the text tool.
     * </p>
     *
     * @param inputText the custom text to set for the text tool
     */
    @FXML
    public void setCustomTextTool(String inputText) {
        stringToolText = inputText;
    }


    /**
     * Sets the font family for the text tool to Arial.
     * <p>
     * Updates the font family to Arial for text drawn on the canvas.
     * </p>
     */
    @FXML
    public void setArialFont() {
        fontFamily = "Arial";
    }


    /**
     * Sets the font family for the text tool to Verdana.
     * <p>
     * Updates the font family to Verdana for text drawn on the canvas.
     * </p>
     */
    @FXML
    public void setVerdanaFont() {
        fontFamily = "Verdana";
        //updateFont();
    }


    /**
     * Sets the font family for the text tool to Tahoma.
     * <p>
     * Updates the font family to Tahoma for text drawn on the canvas.
     * </p>
     */
    @FXML
    public void setTahomaFont() {
        fontFamily = "Tahoma";
    }


    /**
     * Sets the font family for the text tool to Times New Roman.
     * <p>
     * Updates the font family to Times New Roman for text drawn on the canvas.
     * </p>
     */
    @FXML
    public void setTimesFont() {
        fontFamily = "Times New Roman";
    }


    /**
     * Toggles the bold style for the text tool.
     * <p>
     * Enables or disables the bold style for text drawn on the canvas.
     * </p>
     */
    @FXML
    public void toggleBold() {
        if (bold)
            bold = false;
        else{
            bold = true;
        }
    }


    /**
     * Toggles the italic style for the text tool.
     * <p>
     * Enables or disables the italic style for text drawn on the canvas.
     * </p>
     */
    @FXML
    public void toggleItalic() {
        if (italic)
            italic = false;
        else{
            italic = true;
        }
    }


    /**
     * Updates the font used by the text tool with the specified family, weight, and posture.
     * <p>
     * Configures the font settings for drawing text on the canvas, including font family,
     * size, weight, and posture (italic or regular).
     * </p>
     */
    private void updateFont() {
        double fontSize = 24;  // Adjust the size as needed, or make dynamic
        Font customFont = Font.font(fontFamily, fontWeight, fontPosture, fontSize);
        gc.setFont(customFont);
    }


    /**
     * Updates the informational label with canvas details and mouse coordinates.
     * <p>
     * Displays the current canvas size, pen size, mouse coordinates, and autosave countdown if enabled.
     * </p>
     *
     * @param mouseX the x-coordinate of the mouse
     * @param mouseY the y-coordinate of the mouse
     */
    private void updateLabel(double mouseX, double mouseY) {
        // Get the selected tab
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            // Retrieve the corresponding CanvasTab for the selected tab
            CanvasTab canvasTab = tabCanvasMap.get(selectedTab);
            if (canvasTab != null) {
                // Get the canvas from the selected CanvasTab
                Canvas currentCanvas = canvasTab.getCanvas();

                // Get the width, height, and pen size for the current canvas
                double width = currentCanvas.getWidth();
                double height = currentCanvas.getHeight();
                double currentPenSize = getLineWidth(); // Assuming the pen size is controlled by the slider

                // Prepare the canvas information
                String canvasInfo = String.format("Canvas size: %.0f x %.0f | Pen Size: %.0f px | Coordinates X: %.0f, Y: %.0f",
                        width, height, currentPenSize, mouseX, mouseY);

                // Append the autosave message only if autosave is enabled
                if (autosaveEnabled) {
                    String autosaveMessage = " | Autosave in: " + countdownValue + "s";
                    canvasInfo += autosaveMessage;
                }

                // Update the infoText label with the complete message
                infoText.setText(canvasInfo);
            } else {
                System.err.println("No CanvasTab found for the selected tab.");
            }
        } else {
            System.err.println("No tab is selected.");
        }
    }


    /**
     * Handles canvas click events for filling an area with color.
     * <p>
     * When the fill button is selected, performs a flood fill at the clicked coordinates
     * using the color from the color picker.
     * </p>
     *
     * @param event the MouseEvent containing the click coordinates
     */
    @FXML
    private void onCanvasClickForFill(MouseEvent event) {
        if (fillButton.isSelected()) {
            CanvasTab canvasTab = getSelectedCanvasTab();
            if (canvasTab != null) {
                double x = event.getX();
                double y = event.getY();
                Color fillColor = colorPicker.getValue(); // Get the color from the color picker

                floodFill(canvasTab, x, y, fillColor);
            }
        }
    }


    /**
     * Performs a flood fill operation on the specified canvas area.
     * <p>
     * Replaces the target color at the specified coordinates with the fill color,
     * and continues filling neighboring pixels that match the target color.
     * </p>
     *
     * @param canvasTab the CanvasTab containing the canvas to fill
     * @param x the x-coordinate of the starting point
     * @param y the y-coordinate of the starting point
     * @param fillColor the color to use for filling the area
     */
    private void floodFill(CanvasTab canvasTab, double x, double y, Color fillColor) {
        // Get image from the canvas
        WritableImage writableImage = new WritableImage((int) canvasTab.getCanvas().getWidth(), (int) canvasTab.getCanvas().getHeight());
        canvasTab.getCanvas().snapshot(null, writableImage);

        PixelReader pixelReader = writableImage.getPixelReader();
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        Color targetColor = pixelReader.getColor((int) x, (int) y);

        if (fillColor.equals(targetColor)) {
            return; // Avoid unnecessary fill
        }

        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{(int) x, (int) y});

        while (!queue.isEmpty()) {
            int[] point = queue.poll();
            int px = point[0];
            int py = point[1];

            // Check boundaries and color match
            if (px < 0 || px >= writableImage.getWidth() || py < 0 || py >= writableImage.getHeight()) continue;
            if (!pixelReader.getColor(px, py).equals(targetColor)) continue;

            // Set pixel to fill color
            pixelWriter.setColor(px, py, fillColor);

            // Add neighboring pixels
            queue.add(new int[]{px + 1, py});
            queue.add(new int[]{px - 1, py});
            queue.add(new int[]{px, py + 1});
            queue.add(new int[]{px, py - 1});
        }

        // Draw the updated image back onto the canvas
        canvasTab.getGraphicsContext().drawImage(writableImage, 0, 0);
    }


    /**
     * Sets icons for various tool buttons using predefined images.
     * <p>
     * Loads images from the resources folder and assigns them as icons to corresponding buttons
     * in the toolbar.
     *
     * FUN FACT
     *
     *         Most icons in the buttons were created with this app!
     *         Painting P!!!!
     * </p>
     */
    private void setButtonIcons() {
        Image pencilIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/pencil_icon.png")));
        Image lineIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/line_icon1.png")));
        Image rectangleIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/rectangle_icon.png")));
        Image ellipseIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/ellipse_icon.png")));
        Image circleIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/circle_icon.png")));
        Image triangleIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/triangle_icon.png")));
        Image eraserIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/eraser_icon.png")));
        Image stickerIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/sticker_icon.png")));
        Image starIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/star_icon.png")));
        Image heartIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/heart_icon.png")));
        Image textIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/text_icon.png")));
        Image nGonIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/polygon_icon.png")));
        //Image undoIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/undo_icon.png")));
        //Image redoIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/redo_icon.png")));
        Image cubeIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/cube_icon.png")));
        Image spiralIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/spiral_icon.png")));
        Image bubblesIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/bubles_icon.png")));
        Image sprayIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/spraypaint_icon.png")));
        Image colorGrabIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/color_picker_icon.png")));
        Image dashedIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/dashed_icon.png")));
        Image fillIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/fill_icon.png")));
        Image pyramidIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/rhombus_icon.png")));
        Image arrowIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/arrow_icon.png")));
        Image selectIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/select_icon.png")));


        int size = 30;
        int sizew = 30;
        ImageView imageView1 = new ImageView(pencilIcon);
        imageView1.setFitHeight(size); // Set the image height
        imageView1.setFitWidth(sizew);  // Set the image width
        ImageView imageView2 = new ImageView(lineIcon);
        imageView2.setFitHeight(size); // Set the image height
        imageView2.setFitWidth(sizew);  // Set the image width
        ImageView imageView3 = new ImageView(rectangleIcon);
        imageView3.setFitHeight(size); // Set the image height
        imageView3.setFitWidth(sizew);  // Set the image width
        ImageView imageView4 = new ImageView(ellipseIcon);
        imageView4.setFitHeight(size); // Set the image height
        imageView4.setFitWidth(sizew);  // Set the image width
        ImageView imageView5 = new ImageView(circleIcon);
        imageView5.setFitHeight(size); // Set the image height
        imageView5.setFitWidth(sizew);  // Set the image width
        ImageView imageView6 = new ImageView(triangleIcon);
        imageView6.setFitHeight(size); // Set the image height
        imageView6.setFitWidth(sizew);  // Set the image width
        ImageView imageView7 = new ImageView(eraserIcon);
        imageView7.setFitHeight(size); // Set the image height
        imageView7.setFitWidth(sizew);
        ImageView imageView8 = new ImageView(starIcon);
        imageView8.setFitHeight(size); // Set the image height
        imageView8.setFitWidth(sizew);
        ImageView imageView9 = new ImageView(stickerIcon);
        imageView9.setFitHeight(size); // Set the image height
        imageView9.setFitWidth(sizew);
        ImageView imageView10 = new ImageView(heartIcon);
        imageView10.setFitHeight(size); // Set the image height
        imageView10.setFitWidth(sizew);
        ImageView imageView11 = new ImageView(textIcon);
        imageView11.setFitHeight(size); // Set the image height
        imageView11.setFitWidth(sizew);
        ImageView imageView12 = new ImageView(nGonIcon);
        imageView12.setFitHeight(size); // Set the image height
        imageView12.setFitWidth(sizew);

        ImageView imageView15 = new ImageView(cubeIcon);
        imageView15.setFitHeight(size); // Set the image height
        imageView15.setFitWidth(sizew);
        ImageView imageView16 = new ImageView(spiralIcon);
        imageView16.setFitHeight(size); // Set the image height
        imageView16.setFitWidth(sizew);
        ImageView imageView17 = new ImageView(bubblesIcon);
        imageView17.setFitHeight(size); // Set the image height
        imageView17.setFitWidth(sizew);
        ImageView imageView18 = new ImageView(sprayIcon);
        imageView18.setFitHeight(size); // Set the image height
        imageView18.setFitWidth(sizew);
        ImageView imageView19 = new ImageView(colorGrabIcon);
        imageView19.setFitHeight(size); // Set the image height
        imageView19.setFitWidth(sizew);
        ImageView imageView20 = new ImageView(dashedIcon);
        imageView20.setFitHeight(size); // Set the image height
        imageView20.setFitWidth(sizew);
        ImageView imageView22 = new ImageView(fillIcon);
        imageView22.setFitHeight(size); // Set the image height
        imageView22.setFitWidth(sizew);
        ImageView imageView23 = new ImageView(pyramidIcon);
        imageView23.setFitHeight(size); // Set the image height
        imageView23.setFitWidth(sizew);
        ImageView imageView24 = new ImageView(arrowIcon);
        imageView24.setFitHeight(size); // Set the image height
        imageView24.setFitWidth(sizew);
        ImageView imageView25 = new ImageView(selectIcon);
        imageView25.setFitHeight(size); // Set the image height
        imageView25.setFitWidth(sizew);

        pencilButton.setGraphic(imageView1);
        lineButton.setGraphic(imageView2);
        rectangleButton.setGraphic(imageView3);
        ellipseButton.setGraphic(imageView4);
        circleButton.setGraphic(imageView5);
        triangleButton.setGraphic(imageView6);
        eraserButton.setGraphic(imageView7);
        starButton.setGraphic(imageView8);
        imageButton.setGraphic(imageView9);
        heartButton.setGraphic(imageView10);
        textButton.setGraphic(imageView11);
        nGonButton.setGraphic(imageView12);
        //undoButton.setGraphic(imageView13);
       // redoButton.setGraphic(imageView14);
        cubeButton.setGraphic(imageView15);
        spiralButton.setGraphic(imageView16);
        charcoalBrushButton.setGraphic(imageView17);
        sprayPaintBrushButton.setGraphic(imageView18);
        colorGrabButton.setGraphic(imageView19);
        dashedToggleButton.setGraphic(imageView20);
        fillToggleButton.setGraphic(imageView22);
        pyramidButton.setGraphic(imageView23);
        arrowButton.setGraphic(imageView24);
        arrowButton.setGraphic(imageView24);
        selectButton.setGraphic(imageView25);

        Image undoIcon = new Image(getClass().getResourceAsStream("/images/undo_icon.png"));
        ImageView imageViewUndo = new ImageView(undoIcon);
        imageViewUndo.setFitHeight(15.0); // Set the image height
        imageViewUndo.setFitWidth(15.0);
        undoButton.setGraphic(imageViewUndo);
        undoButton.setOnAction(event -> onUndoClick());

        // Set up Redo button
        Image redoIcon = new Image(getClass().getResourceAsStream("/images/redo_icon.png"));
        ImageView imageViewRedo = new ImageView(redoIcon);
        imageViewRedo.setFitHeight(15.0); // Set the image height
        imageViewRedo.setFitWidth(15.0);
        redoButton.setGraphic(imageViewRedo);
        redoButton.setOnAction(event -> onRedoClick());

        // Optional: Adjust button styles to match the menu bar
        //undoButton.setStyle("-fx-background-color: transparent; -fx-padding: 5;");
        //redoButton.setStyle("-fx-background-color: transparent; -fx-padding: 5;");
    }


    /**
     * Retrieves the current pen size.
     *
     * @return the current pen size
     */
    public double getPenSize() {
        return penSize.get();
    }


    /**
     * Sets the pen size to a specified value.
     *
     * @param size the new pen size
     */
    public void setPenSize(double size) {
        penSize.set(size);
    }


    /**
     * Provides access to the pen size property.
     * <p>
     * Useful for binding the pen size property to other UI components.
     * </p>
     *
     * @return the DoubleProperty representing the pen size
     */
    public DoubleProperty penSizeProperty() {
        return penSize;
    }


    /**
     * Prompts the user to confirm exit when there are unsaved changes.
     * <p>
     * Displays a confirmation dialog offering options to save, not save, or cancel exit.
     * </p>
     */
    @FXML
    private void safetyExit() {
        // Create an alert of type confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Paint-p");
        alert.setHeaderText("Unsaved Changes");
        alert.setContentText("Do you want to save your file before exiting?");

        // Add Save and Don't Save buttons
        ButtonType saveButton = new ButtonType("Save");
        ButtonType dontSaveButton = new ButtonType("Don't Save");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(saveButton, dontSaveButton, cancelButton);

        // Set the icon for the popup
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/paint-P-Logo.png")));
        stage.getIcons().add(icon);

        // Show the alert and wait for the user response
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == saveButton) {
            // Handle saving the file here
            System.out.println("Save the file");
            onSafeClick();
            onExitMenuClick(); // Assuming this exits after saving
        } else if (result.isPresent() && result.get() == dontSaveButton) {
            // Just exit without saving
            System.out.println("Don't save, just exit");
            onExitMenuClick();
        } else {
            // Cancel: do nothing, stay in the application
            System.out.println("Exit cancelled");
        }
    }


    /**
     * Prompts the user before making structural changes to the canvas.
     * <p>
     * Warns the user that editing the canvas structure may result in data loss, providing
     * options to proceed or cancel.
     * </p>
     *
     * @return true if the user chooses to continue editing, false if canceled
     */
    private boolean safetyCanvasEdit() {
        // Create an alert of type confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Paint-p");
        alert.setHeaderText("You are about to edit the structure of the Canvas");
        alert.setContentText("Some changes might get lost, do you want to continue?");

        // Add Save and Don't Save buttons
        ButtonType editCanvaButton = new ButtonType("Yes");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(editCanvaButton, cancelButton);

        // Set the icon for the popup
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/paint-P-Logo.png")));
        stage.getIcons().add(icon);

        // Show the alert and wait for the user response
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == editCanvaButton) {
            // Handle saving the file here
            return true;// Assuming this exits after
        }else {

            // Cancel: do nothing, stay in the application
            System.out.println("Canvas edit cancelled");
            return false;
        }
    }


    /**
     * Updates the current drawing color on the selected canvas based on the color picker.
     * <p>
     * Sets the stroke color for the selected canvas to the value chosen in the color picker.
     * </p>
     */
    @FXML
    public void onColorChange() {
        // Update the current color for drawing on the selected canvas
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            CanvasTab canvasTab = canvasTabs.get(selectedTab);
            if (canvasTab != null) {
                canvasTab.getGraphicsContext().setStroke(colorPicker.getValue());
            }
        }
    }


    /**
     * Sets the canvas size to a wide format.
     * <p>
     * Adjusts the canvas dimensions to 1450 x 575 pixels, clearing and filling
     * the background to ensure the new size is visible.
     * </p>
     */
    @FXML
    protected void setWideCanvas() {
        boolean safe = safetyCanvasEdit();
        if (safe) {
            CanvasTab canvasTab = getSelectedCanvasTab();
            if (canvasTab != null) {
                Canvas currentCanvas = canvasTab.getCanvas();
                Canvas tempCanvas = canvasTab.getTempCanvas(); // Temporary canvas

                double newWidth = 1450;
                double newHeight = 575;

                // Update size for both canvases
                currentCanvas.setWidth(newWidth);
                currentCanvas.setHeight(newHeight);
                tempCanvas.setWidth(newWidth);
                tempCanvas.setHeight(newHeight);

                // Clear and set background for both canvases
                GraphicsContext gc = currentCanvas.getGraphicsContext2D();
                GraphicsContext tempGc = tempCanvas.getGraphicsContext2D();

                gc.clearRect(0, 0, newWidth, newHeight);
                gc.setFill(Color.WHITE);
                gc.fillRect(0, 0, newWidth, newHeight);

                tempGc.clearRect(0, 0, newWidth, newHeight);
                tempGc.setFill(Color.WHITE);
                tempGc.fillRect(0, 0, newWidth, newHeight);

                updateLabel(); // Update any UI elements or labels reflecting the new size
            } else {
                System.err.println("No CanvasTab found for the selected tab.");
            }
        }
    }


    /**
     * Prompts the user to set a custom canvas size through input dialogs.
     * <p>
     * Allows the user to input width and height values, and resizes the canvas accordingly.
     * Ensures valid input and updates UI elements to reflect the new canvas size.
     * </p>
     *
     * @param event the ActionEvent triggering the size change
     */
    @FXML
    protected void setCustomSizeCanvas(ActionEvent event) {
        boolean safe = safetyCanvasEdit();
        if (safe) {
            CanvasTab canvasTab = getSelectedCanvasTab();
            if (canvasTab != null) {
                Canvas currentCanvas = canvasTab.getCanvas();
                Canvas tempCanvas = canvasTab.getTempCanvas();

                // Load the icon image for dialogs
                Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/paint-P-Logo.png")));

                // Create dialog for width input
                TextInputDialog widthDialog = new TextInputDialog();
                widthDialog.setTitle("Custom Canvas Size");
                widthDialog.setHeaderText("Set Custom Canvas Size");
                widthDialog.setContentText("Enter canvas width (in pixels):");

                // Set the icon for the width dialog
                Stage widthStage = (Stage) widthDialog.getDialogPane().getScene().getWindow();
                widthStage.getIcons().add(icon);

                Optional<String> widthInput = widthDialog.showAndWait();
                if (!widthInput.isPresent()) {
                    return; // Exit if user cancels the width input dialog
                }

                // Create dialog for height input
                TextInputDialog heightDialog = new TextInputDialog();
                heightDialog.setTitle("Custom Canvas Size");
                heightDialog.setHeaderText("Set Custom Canvas Size");
                heightDialog.setContentText("Enter canvas height (in pixels):");

                // Set the icon for the height dialog
                Stage heightStage = (Stage) heightDialog.getDialogPane().getScene().getWindow();
                heightStage.getIcons().add(icon);

                Optional<String> heightInput = heightDialog.showAndWait();
                if (!heightInput.isPresent()) {
                    return; // Exit if user cancels the height input dialog
                }

                try {
                    // Parse the width and height values
                    double newWidth = Double.parseDouble(widthInput.get());
                    double newHeight = Double.parseDouble(heightInput.get());

                    // Validate the size values
                    if (newWidth <= 0 || newHeight <= 0) {
                        throw new IllegalArgumentException("Canvas size must be positive.");
                    }

                    // Set the new canvas size
                    currentCanvas.setWidth(newWidth);
                    currentCanvas.setHeight(newHeight);
                    tempCanvas.setWidth(newWidth);
                    tempCanvas.setHeight(newHeight);

                    // Re-fill the canvas background to ensure the new size is visible
                    GraphicsContext gc = currentCanvas.getGraphicsContext2D();
                    GraphicsContext tempGc = tempCanvas.getGraphicsContext2D();

                    gc.setFill(Color.WHITE);  // Set default background color (white)
                    gc.fillRect(0, 0, newWidth, newHeight);

                    //tempGc.clearRect(0, 0, newWidth, newHeight);
                    tempGc.setFill(Color.WHITE);
                    tempGc.fillRect(0, 0, newWidth, newHeight);

                    // Update any UI elements or labels reflecting the new size
                    updateLabel();  // Refresh the label to show the updated canvas size

                } catch (NumberFormatException e) {
                    // Handle invalid input (non-numeric values)
                    showErrorDialog("Invalid input", "Please enter valid numeric values for the canvas size.");
                } catch (IllegalArgumentException e) {
                    // Handle size out of bounds
                    showErrorDialog("Invalid size", e.getMessage());
                }
            } else {
                System.err.println("No canvas found in the selected tab.");
            }
        }
    }


    /**
     * Resizes the canvas to match the current window dimensions.
     * <p>
     * Adjusts the canvas width and height to fit within the main window,
     * providing some padding around the edges.
     * </p>
     */
    @FXML
    protected void setWindowSizeCanvas() {
        boolean safe = safetyCanvasEdit();
        if (safe) {
            CanvasTab canvasTab = getSelectedCanvasTab();
            if (canvasTab != null) {
                Canvas currentCanvas = canvasTab.getCanvas();
                Canvas tempCanvas = canvasTab.getTempCanvas();

                // Get the current stage's dimensions
                Stage stage = (Stage) currentCanvas.getScene().getWindow();
                double width = stage.getWidth();
                double height = stage.getHeight();

                // Adjust the canvas size according to the window size (with some padding)
                currentCanvas.setWidth(width - 30);
                currentCanvas.setHeight(height - 50);
                tempCanvas.setWidth(width - 30);
                tempCanvas.setHeight(height - 50);

                // Re-fill the canvas background to ensure the new size is visible
                GraphicsContext gc = currentCanvas.getGraphicsContext2D();
                gc.setFill(Color.WHITE);
                gc.fillRect(0, 0, currentCanvas.getWidth(), currentCanvas.getHeight());

                // Update any UI elements or labels reflecting the new size
                updateLabel();  // Refresh the label to show the updated canvas size
            } else {
                System.err.println("No CanvasTab found for the selected tab.");
            }
        }
    }


    /**
     * Sets the canvas to a tall format.
     * <p>
     * Adjusts the canvas dimensions to 600 x 650 pixels, clearing and filling
     * the background to ensure the new size is visible.
     * </p>
     */
    @FXML
    protected void setTallCanvas() {
        boolean safe = safetyCanvasEdit();
        if (safe) {
            CanvasTab canvasTab = getSelectedCanvasTab();
            if (canvasTab != null) {
                Canvas currentCanvas = canvasTab.getCanvas();
                Canvas tempCanvas = canvasTab.getTempCanvas(); // Temporary canvas

                double newWidth = 600;
                double newHeight = 650;

                // Update size for both canvases
                currentCanvas.setWidth(newWidth);
                currentCanvas.setHeight(newHeight);
                tempCanvas.setWidth(newWidth);
                tempCanvas.setHeight(newHeight);

                // Clear and set background for both canvases
                GraphicsContext gc = currentCanvas.getGraphicsContext2D();
                GraphicsContext tempGc = tempCanvas.getGraphicsContext2D();

                gc.clearRect(0, 0, newWidth, newHeight);
                gc.setFill(Color.WHITE);
                gc.fillRect(0, 0, newWidth, newHeight);

                tempGc.clearRect(0, 0, newWidth, newHeight);
                tempGc.setFill(Color.WHITE);
                tempGc.fillRect(0, 0, newWidth, newHeight);

                updateLabel(); // Update any UI elements or labels reflecting the new size
            } else {
                System.err.println("No CanvasTab found for the selected tab.");
            }
        }
    }


    /**
     * Sets up listeners to monitor changes to canvas properties.
     * <p>
     * Adds listeners to update UI elements whenever a tab selection changes
     * or the canvas width/height is modified.
     * </p>
     */
    private void setupListeners() {
        // Add listener for tab selection
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab != null) {
                CanvasTab canvasTab = tabCanvasMap.get(newTab);
                if (canvasTab != null) {
                    Canvas currentCanvas = canvasTab.getCanvas();

                    // Add listeners to the width and height properties of the Canvas
                    currentCanvas.widthProperty().addListener((obs, oldValue, newValue) -> updateLabel());
                    currentCanvas.heightProperty().addListener((obs, oldValue, newValue) -> updateLabel());
                } else {
                    System.err.println("CanvasTab not found for the new tab.");
                }
            }
        });
    }


    /**
     * Clears the canvas by filling it with the background color.
     * <p>
     * Resets the entire canvas area to white, effectively clearing all drawn content.
     * </p>
     */
    @FXML
    protected void onCanvaClearCanva() {
        boolean safe = safetyCanvasEdit();
        if (safe) {
            CanvasTab canvasTab = getSelectedCanvasTab();
            if (canvasTab != null) {
                Canvas currentCanvas = canvasTab.getCanvas();
                GraphicsContext gc = currentCanvas.getGraphicsContext2D();

                gc.setFill(Color.WHITE);
                gc.fillRect(0, 0, currentCanvas.getWidth(), currentCanvas.getHeight());
                saveCanvasState();
            } else {
                System.err.println("No CanvasTab found for the selected tab.");
            }
        }
    }


    /**
     * Opens an image file and resizes it to match the canvas size.
     *
     * @param event the ActionEvent triggering this method.
     */
    @FXML
    protected void onOpenImageClickCanvasSize(ActionEvent event) {
        onOpenImageClick(false);
    }


    /**
     * Opens an image file in its original size, adjusting the canvas size to match the image dimensions.
     *
     * @param event the ActionEvent triggering this method.
     */
    @FXML
    protected void onOpenImageClickOriginalSize(ActionEvent event) {
        onOpenImageClick(true);
    }


    /**
     * Opens an image file and draws it on the canvas, resizing to fit canvas size or keeping the original size.
     * <p>
     * Prompts the user to choose an image file to open, and draws it on the currently selected canvas.
     * If originalSize is true, the canvas size will adjust to the image's dimensions;
     * otherwise, the image will be resized to fit the current canvas dimensions.
     * </p>
     *
     * @param originalSize if true, resizes the canvas to match the image size; if false, resizes the image to fit the canvas.
     */
    @FXML
    protected void onOpenImageClick(boolean originalSize) {  //TS
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null && selectedTab.getContent() instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) selectedTab.getContent();
            if (scrollPane.getContent() instanceof StackPane) {
                StackPane canvasPane = (StackPane) scrollPane.getContent();
                if (!canvasPane.getChildren().isEmpty() && canvasPane.getChildren().get(0) instanceof Canvas) {
                    Canvas currentCanvas = (Canvas) canvasPane.getChildren().get(0);

                    FileChooser fileChooser = new FileChooser();
                    fileChooser.getExtensionFilters().add(
                            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
                    );

                    File selectedFile = fileChooser.showOpenDialog(currentCanvas.getScene().getWindow());

                    if (selectedFile != null) {
                        Image image = new Image(selectedFile.toURI().toString());

                        double imageWidth = image.getWidth();
                        double imageHeight = image.getHeight();

                        GraphicsContext gc = currentCanvas.getGraphicsContext2D();
                        CanvasTab canvasTab = getSelectedCanvasTab();

                        Canvas canvass = canvasTab.getCanvas();
                        Canvas tempCanvas = canvasTab.getTempCanvas();

                        if (originalSize) {
                            currentCanvas.setWidth(imageWidth);
                            currentCanvas.setHeight(imageHeight);
                            tempCanvas.setWidth(imageWidth);
                            tempCanvas.setWidth(imageWidth);
                            tempCanvas.setHeight(imageHeight);
                            gc.drawImage(image, 0, 0, imageWidth, imageHeight);
                        } else {
                            gc.drawImage(image, 0, 0, currentCanvas.getWidth(), currentCanvas.getHeight());
                        }
                    }
                }
            }
        } else {
            System.err.println("No canvas is selected.");
        }
    }


    /**
     * Saves the current canvas content to a file.
     * <p>
     * Checks if a file for the selected canvas tab already exists; if not, saves it to the Downloads folder with
     * the tab name as the file name. If autosave is enabled, resets the countdown timer after saving.
     * </p>
     */
    @FXML
    protected void onSafeClick() {
        // Get the currently selected tab
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();

        if (selectedTab != null && selectedTab.getContent() instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) selectedTab.getContent();
            if (scrollPane.getContent() instanceof StackPane) {
                StackPane canvasPane = (StackPane) scrollPane.getContent();
                if (!canvasPane.getChildren().isEmpty() && canvasPane.getChildren().get(0) instanceof Canvas) {
                    Canvas currentCanvas = (Canvas) canvasPane.getChildren().get(0);

                    // Get the tab name to use as the file name
                    String tabName = selectedTab.getText();

                    // Check if a file for this tab already exists in the map
                    File savedFile = savedFilesMap.get(selectedTab);

                    if (savedFile != null) {
                        // If file already exists, save it directly without prompting
                        saveCanvasToFile(currentCanvas, savedFile);
                        System.out.println("File saved: " + savedFile.getAbsolutePath());
                    } else {
                        // Define the default location for saving files in the Downloads folder
                        File defaultSaveLocation = new File(System.getProperty("user.home") + "/Downloads/" + tabName + ".png");

                        if (defaultSaveLocation.exists()) {
                            // If file exists in Downloads, overwrite it directly
                            saveCanvasToFile(currentCanvas, defaultSaveLocation);
                            savedFilesMap.put(selectedTab, defaultSaveLocation); // Update the saved file location in the map
                            System.out.println("File overwritten in Downloads: " + defaultSaveLocation.getAbsolutePath());
                        } else {
                            // Save the canvas to a new file in Downloads
                            saveCanvasToFile(currentCanvas, defaultSaveLocation);
                            savedFilesMap.put(selectedTab, defaultSaveLocation); // Track the saved file
                            System.out.println("New file saved in Downloads: " + defaultSaveLocation.getAbsolutePath());
                        }
                    }

                    // Reset the autosave countdown after manual save
                    countdownValue = autosaveInterval;
                    updateLabel(0, 0);  // Update the label to show the reset timer
                    logWriter.logEvent(getCurrentTabName(),"File saved");
                }
            }
        } else {
            System.err.println("No canvas is selected or available for saving.");
        }
    }


    /**
     * Saves the current canvas to a specified file.
     * <p>
     * Takes a snapshot of the canvas content and writes it to the specified file in PNG format.
     * </p>
     *
     * @param canvas the Canvas to save.
     * @param file   the file to save the Canvas content to.
     */
    private void saveCanvasToFile(Canvas canvas, File file) {
        try {
            WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
            canvas.snapshot(null, writableImage);
            RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
            ImageIO.write(renderedImage, "png", file);
        } catch (IOException e) {
            System.err.println("Error saving canvas to file: " + e.getMessage());
        }
    }


    /**
     * Prompts the user to select a file format and save location for the current canvas.
     * <p>
     * Provides options for saving in PNG, JPEG, GIF, or BMP formats and alerts the user of potential data loss
     * when saving in a lossy format. Resets the autosave countdown after saving.
     * </p>
     */
    @FXML
    protected void onSaveAsClick() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null && selectedTab.getContent() instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) selectedTab.getContent();
            if (scrollPane.getContent() instanceof StackPane) {
                StackPane canvasPane = (StackPane) scrollPane.getContent();
                if (!canvasPane.getChildren().isEmpty() && canvasPane.getChildren().get(0) instanceof Canvas) {
                    Canvas currentCanvas = (Canvas) canvasPane.getChildren().get(0);

                    // Get the tab name
                    String tabName = selectedTab.getText();

                    // FileChooser for saving the image
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Save As");
                    fileChooser.getExtensionFilters().addAll(
                            new FileChooser.ExtensionFilter("PNG Files", "*.png"),
                            new FileChooser.ExtensionFilter("JPEG Files", "*.jpg", "*.jpeg"),
                            new FileChooser.ExtensionFilter("GIF Files", "*.gif"),
                            new FileChooser.ExtensionFilter("BMP Files", "*.bmp")
                    );
                    fileChooser.setInitialFileName(tabName); // Set the default name

                    File selectedFile = fileChooser.showSaveDialog(currentCanvas.getScene().getWindow());

                    if (selectedFile != null) {
                        String format = selectedFile.getName().substring(selectedFile.getName().lastIndexOf('.') + 1).toLowerCase();

                        // Check if the file already exists, prompt the user if they want to overwrite
                        if (selectedFile.exists()) {
                            boolean overwrite = confirmOverwrite(selectedFile);
                            if (!overwrite) {
                                // If the user doesn't want to overwrite, stop the process
                                return;
                            }
                        }

                        WritableImage writableImage = new WritableImage((int) currentCanvas.getWidth(), (int) currentCanvas.getHeight());
                        currentCanvas.snapshot(null, writableImage);
                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);

                        // Check if the format conversion may result in data loss (e.g., from PNG to JPEG)
                        if (format.equals("jpg") || format.equals("jpeg")) {
                            showDataLossWarning("Warning: JPEG format may result in loss of transparency.");
                        } else if (format.equals("bmp")) {
                            showDataLossWarning("Warning: BMP format may not support all features.");
                        }

                        try {
                            // Handle saving to various formats and overwriting if needed
                            switch (format) {
                                case "png":
                                    ImageIO.write(bufferedImage, "png", selectedFile);
                                    break;
                                case "jpg":
                                case "jpeg":
                                    saveAsJPEG(bufferedImage, selectedFile);
                                    break;
                                case "bmp":
                                    saveAsBMP(bufferedImage, selectedFile);
                                    break;
                                case "gif":
                                    saveAsGIF(bufferedImage, selectedFile);
                                    break;
                                default:
                                    System.err.println("Unsupported format: " + format);
                            }
                            System.out.println("Saved as " + selectedFile.getAbsolutePath());

                            // Reset the autosave countdown after manual save
                            countdownValue = autosaveInterval;
                            updateLabel(0, 0);
                            logWriter.logEvent(getCurrentTabName(), "file saved as " + selectedFile.getAbsolutePath());

                        } catch (IOException e) {
                            e.printStackTrace();
                            showAlert("Error", "An error occurred while saving the file.");
                        }
                    }
                }
            }
        } else {
            System.err.println("No canvas is selected.");
        }
    }


    /**
     * Confirms if the user wants to overwrite an existing file.
     *
     * @param file the file that already exists.
     * @return true if the user confirms overwrite; false otherwise.
     */
    private boolean confirmOverwrite(File file) {
        // Create an alert of type CONFIRMATION
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Overwrite");
        alert.setHeaderText("File already exists");
        alert.setContentText("The file " + file.getName() + " already exists. Do you want to overwrite it?");

        // Define the buttons for the alert
        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

        // Add buttons to the alert dialog
        alert.getButtonTypes().setAll(yesButton, noButton);

        // Ensure the alert is executed on the JavaFX thread
        Optional<ButtonType> result = alert.showAndWait();

        // Return true if "Yes" is clicked, otherwise false
        return result.isPresent() && result.get() == yesButton;
    }


    /**
     * Displays a warning about potential data loss when saving in certain formats.
     *
     * @param message the warning message to display.
     */
    private void showDataLossWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Data Loss Warning");
        alert.setHeaderText("Potential Data Loss");
        alert.setContentText(message);

        // Add a warning icon (optional)
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/paint-P-Logo.png")));
        stage.getIcons().add(icon);

        alert.showAndWait();
    }


    /**
     * Opens a dialog to change the autosave interval.
     * <p>
     * Prompts the user to enter a new autosave time interval in seconds, validates the input, and updates
     * the autosave interval. The countdown is reset to the new interval, and the change is logged.
     * </p>
     */
    @FXML
    private void onChangeAutosaveTimeClick() {
        // Create a TextInputDialog for setting the autosave time
        TextInputDialog dialog = new TextInputDialog(String.valueOf(autosaveInterval));
        dialog.setTitle("Change Autosave Time");
        dialog.setHeaderText("Set Autosave Time Interval");
        dialog.setContentText("Enter autosave time (in seconds):");

        // Load the icon for the dialog
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/paint-P-Logo.png")));
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(icon);

        // Show the dialog and capture the input
        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            try {
                // Parse the input as an integer
                int newInterval = Integer.parseInt(result.get());

                // Validate that the input is positive
                if (newInterval <= 0) {
                    throw new IllegalArgumentException("Autosave time must be greater than 0.");
                }

                // Update the autosave interval and restart the timer
                autosaveInterval = newInterval;
                countdownValue = autosaveInterval;  // Reset the countdown
                updateLabel(0, 0);  // Update the label to reflect the new time

                System.out.println("Autosave interval updated to: " + autosaveInterval + " seconds");
                logWriter.logEvent(getCurrentTabName(), "Autosave time interval changed to "+ autosaveInterval );

            } catch (NumberFormatException e) {
                // Handle invalid input (non-numeric values)
                showErrorDialog("Invalid input", "Please enter a valid number for the autosave time.");
            } catch (IllegalArgumentException e) {
                // Handle negative or zero input
                showErrorDialog("Invalid input", e.getMessage());
            }
        }
    }


    /**
     * Displays an error dialog with a specified title and message.
     *
     * @param title   the title of the error dialog.
     * @param message the message displayed in the error dialog.
     */
    private void showErrorDialog(String title, String message) {
        // Create an error alert
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Set the logo for the error alert
        try {
            Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/paint-P-Logo.png")));
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(icon);
        } catch (NullPointerException e) {
            System.err.println("Error icon not found. Proceeding without an icon.");
        }

        alert.showAndWait();
    }


    //DO NOT TOUCH BMP IS WORKING
    /**
     * Saves a BufferedImage as a BMP file.
     * <p>
     * Converts the image to 24-bit BMP format if necessary and writes it to the specified file.
     * </p>
     *
     * @param bufferedImage the image to save.
     * @param selectedFile  the destination file.
     */
    private void saveAsBMP(BufferedImage bufferedImage, File selectedFile) {
        // Ensure the file has a .bmp extension
        if (!selectedFile.getName().toLowerCase().endsWith(".bmp")) {
            selectedFile = new File(selectedFile.getAbsolutePath() + ".bmp");
        }

        // Convert the image to 24-bit BMP format
        BufferedImage bmpImage = convertToBmp(bufferedImage);

        try {
            ImageIO.write(bmpImage, "bmp", selectedFile);
        } catch (IOException e) {
            e.printStackTrace();
            // Show error alert if saving fails
            System.out.println("Error Failed to save the BMP image.");
        }
    }



    /**
     * Converts a BufferedImage to 24-bit BMP format if it is not already in that format.
     *
     * @param image the original image.
     * @return the image in 24-bit BMP format.
     */
    private static BufferedImage convertToBmp(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            return image; // Already in BMP format
        }

        BufferedImage bmpImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR);

        // Draw a white background and put the original image on it
        bmpImage.createGraphics().drawImage(image, 0, 0, java.awt.Color.WHITE, null);
        return bmpImage;
    }


    /**
     * Saves a BufferedImage as a JPEG file, removing any transparency.
     * <p>
     * Converts the image to RGB format, filling any transparent areas with white, and writes it to the specified file.
     * </p>
     *
     * @param bufferedImage the image to save.
     * @param file          the destination file.
     * @throws IOException if an error occurs during saving.
     */
    private void saveAsJPEG(BufferedImage bufferedImage, File file) throws IOException {
        // Convert the image to RGB, which removes the alpha channel (transparency)
        BufferedImage rgbImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);

        // Draw the original image onto the new RGB image (without alpha)
        Graphics2D g = rgbImage.createGraphics();
        g.drawImage(bufferedImage, 0, 0, java.awt.Color.WHITE, null);  // Fills transparent areas with white
        g.dispose();

        // Save the image as JPEG
        ImageIO.write(rgbImage, "jpg", file);
    }


    /**
     * Saves a BufferedImage as a GIF file.
     *
     * @param bufferedImage the image to save.
     * @param file          the destination file.
     * @throws IOException if GIF format is not supported or another error occurs.
     */
    private void saveAsGIF(BufferedImage bufferedImage, File file) throws IOException {
        try {
            // Save as GIF using ImageIO with TwelveMonkeys
            ImageIO.write(bufferedImage, "gif", file);
        } catch (IOException e) {
            throw new IOException("GIF format is not supported by ImageIO in your setup.", e);
        }
    }


    /**
     * Displays a general alert with a specified title and message.
     *
     * @param title   the title of the alert.
     * @param message the message displayed in the alert.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    /**
     * Displays the "About" window, loading and showing the release notes from a file.
     */
    @FXML
    protected void onAboutMenuClick() {

        TextArea releaseNotesTextArea = new TextArea();

        Stage popupStage = new Stage();
        popupStage.setTitle("About");

        releaseNotesTextArea.setEditable(false);
        releaseNotesTextArea.setWrapText(true);

        try (BufferedReader reader = new BufferedReader(new FileReader("paint-p_ReleaseNotes.txt"))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            releaseNotesTextArea.setText(content.toString());
        } catch (IOException e) {
            releaseNotesTextArea.setText("Error loading release notes.");
        }

        VBox vbox = new VBox(releaseNotesTextArea);
        vbox.setPadding(new Insets(10));
        VBox.setVgrow(releaseNotesTextArea, javafx.scene.layout.Priority.ALWAYS);

        Scene scene = new Scene(vbox, 600, 400);

        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/paint-P-Logo.png")));
        popupStage.getIcons().add(icon);

        popupStage.setScene(scene);
        popupStage.show();
        logWriter.logEvent(getCurrentTabName(), "About menu clicked");
    }


    /**
     * Updates the pixel size label with the current pixel size.
     */
    @FXML
    private void updatePixelSizeLabel() {
        pixelSizeLabel.setText("Pixel size: " + currentPixelSize);
    }


    /**
     * Handles the selection of a shape button, updating the current shape for drawing.
     *
     * @param event the ActionEvent triggered by clicking a shape button.
     */
    @FXML
    protected void onShapeButtonClick(ActionEvent event) {
        ToggleButton source = (ToggleButton) event.getSource();
        currentShape = source.getText();
    }


    /**
     * Creates a new canvas in a new tab with a user-specified name.
     * <p>
     * Prompts the user to enter a name for the new canvas, then adds a new tab with the specified
     * name and a default canvas size.
     * </p>
     */
    @FXML
    private void onNewMenuClick() {
        // Create a TextInputDialog to ask for the new file name
        String title = "Paint-p " + (tabPane.getTabs().size() + 1);
        TextInputDialog dialog = new TextInputDialog(title);
        dialog.setTitle("Create New Canvas");
        dialog.setHeaderText("Enter a name for the new canvas:");
        dialog.setContentText("Canvas name:");

        // Set the icon for the dialog
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/paint-P-Logo.png")));
        stage.getIcons().add(icon);

        // Show the dialog and capture the result
        Optional<String> result = dialog.showAndWait();

        // If the user provides input, use that input as the new tab name
        result.ifPresent(canvasName -> {
            // Add a new tab with the specified name and default size
            addNewTab(canvasName, 1450, 575);
            logWriter.logEvent(getCurrentTabName(), "New tab created");
        });

        // If no input is provided (user cancels), do nothing
    }


    /**
     * Exits the application, logging the exit event and shutting down the logging thread.
     */
    @FXML
    protected void onExitMenuClick() {
        logWriter.logEvent(getCurrentTabName(), "Application exit");
        logWriter.shutdown();  // shut down the logging thread
        System.exit(0);
    }
}