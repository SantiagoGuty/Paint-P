package com.example.paintp;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Node;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToggleButton;
import javafx.event.ActionEvent;


import javax.imageio.ImageIO;
import java.awt.*;
//import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import static javax.swing.JOptionPane.showInputDialog;

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
    private ToggleButton selectButton;
    @FXML
    private ToggleButton SelectAndMoveButton;
    @FXML
    private CheckBox dashedLineCheckBox;
    @FXML
    private TabPane tabPane;


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
    // Undo and Redo stacks
    // private Stack<CanvasState> undoStack = new Stack<>();
    //private Stack<CanvasState> redoStack = new Stack<>();

    private final HashMap<Tab, CanvasTab> canvasTabs = new HashMap<>();
    private final HashMap<Tab, Stack<CanvasState>> undoStacks = new HashMap<>();
    private final HashMap<Tab, Stack<CanvasState>> redoStacks = new HashMap<>();
    private Stack<WritableImage> undoStack = new Stack<>();
    private Stack<WritableImage> redoStack = new Stack<>();


    private Map<Tab, CanvasTab> tabCanvasMap = new HashMap<>();

    @FXML
    private Canvas testCanvas;
    private GraphicsContext gc;
    private GraphicsContext testGc;
    private String currentTool = "Pencil";


    @FXML
    private ToggleButton pencilButton, eraserButton, lineButton, rectangleButton, ellipseButton, circleButton, triangleButton, starButton, heartButton, imageButton, textButton, nGonButton;

    @FXML
    public void initialize() {
        System.out.println("Initializing components...");

        if (tabPane == null) {
            System.out.println("TabPane is null.");
        } else {
            System.out.println("TabPane initialized.");
        }

        if (colorPicker == null) {
            System.out.println("ColorPicker is null.");
        } else {
            System.out.println("ColorPicker initialized.");
        }

        colorPicker.setValue(Color.BLACK);  // Default color
        lineWidthSlider.setValue(1.0);      // Default line width

        setupToolButtons();
        setButtonIcons();
        setupListeners();

        // Clear any existing tabs to ensure a clean start
        tabPane.getTabs().clear();

        // Initialize the first tab with a default canvas size
        addNewTab("Paint-p 1", 1100, 525); // 1100 and 525 default

        setupShortcuts();//shortcuts set up CTRL S Safe as, CTRL L Clean canvas, CTRL E Exit.
    }



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


    //SA
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



    private CanvasTab getSelectedCanvasTab() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        return tabCanvasMap.get(selectedTab);
    }


    // Undo action
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

    private void clearUndoRedoStacks() {
        undoStack.clear();
        redoStack.clear();
    }


    // Helper method to get the canvas from the selected tab
    private Canvas getCanvasFromTab(Tab tab) {
        if (tab.getContent() instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) tab.getContent();
            StackPane canvasPane = (StackPane) scrollPane.getContent();
            return (Canvas) canvasPane.getChildren().get(0);
        }
        return null;
    }

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

        // Add the new tab to the TabPane and select it
        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
    }



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



    private void setupToolButtons() {

        ToggleGroup toolsToggleGroup = new ToggleGroup();
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
        nGonButton.setToggleGroup(toolsToggleGroup);
        textButton.setToggleGroup(toolsToggleGroup);
        // Set the onAction for the tool buttons (example for pencil)
        pencilButton.setOnAction(event -> setPencilTool());
        eraserButton.setOnAction(event -> setEraserTool());
        lineButton.setOnAction(event -> setLineTool());
        rectangleButton.setOnAction(event -> setRectangleTool());
        ellipseButton.setOnAction(event -> setEllipseTool());
        circleButton.setOnAction(event -> setCircleTool());
        triangleButton.setOnAction(event -> setTriangleTool());
        imageButton.setOnAction(event -> setImageTool());
        starButton.setOnAction(event -> setStarTool());
        heartButton.setOnAction(event -> setHeartTool());
        textButton.setOnAction(event -> setTextTool());
        nGonButton.setOnAction(event -> setNgonTool());
        // Add similar actions for other tools...
    }




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
                    double currentPenSize = lineWidthSlider.getValue(); // Get the current pen size from the slider

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

    @FXML
    public void setLineWidth() {
        // Update the current line width for drawing on the selected canvas
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            CanvasTab canvasTab = canvasTabs.get(selectedTab);
            if (canvasTab != null) {
                canvasTab.getGraphicsContext().setLineWidth(lineWidthSlider.getValue());
            }
        }
    }


    // Method to set up drawing on a given canvas
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

    private void enableColorGrabMode() {
        infoText.setText("Click on the canvas to grab a color.");

        // Get the currently selected canvas tab
        CanvasTab canvasTab = getSelectedCanvasTab();
        if (canvasTab != null) {
            Canvas currentCanvas = canvasTab.getCanvas();

            // Clear any existing mouse click handlers before adding the color grab handler
            currentCanvas.setOnMouseClicked(null);  // Remove any previous click handlers

            // Set up color grabbing on mouse click
            currentCanvas.setOnMouseClicked(event -> grabColor(event, currentCanvas));
        }
    }


    private void grabColor(MouseEvent event, Canvas currentCanvas) {
        // Ensure the snapshot dimensions match the canvas size
        WritableImage snapshot = new WritableImage((int) currentCanvas.getWidth(), (int) currentCanvas.getHeight());
        currentCanvas.snapshot(null, snapshot);

        // Debugging: print snapshot size and click coordinates
        System.out.println("Snapshot dimensions: " + snapshot.getWidth() + "x" + snapshot.getHeight());
        System.out.println("Mouse click at: " + event.getX() + ", " + event.getY());

        // Get the pixel reader from the snapshot
        PixelReader pixelReader = snapshot.getPixelReader();
        double x = event.getX();
        double y = event.getY();

        // Ensure the click is within canvas bounds
        if (x >= 0 && x < snapshot.getWidth() && y >= 0 && y < snapshot.getHeight()) {
            // Grab the color at the clicked position
            Color grabbedColor = pixelReader.getColor((int) x, (int) y);
            System.out.println("Grabbed color: " + grabbedColor.toString());

            // Set the grabbed color to the ColorPicker
            colorPicker.setValue(grabbedColor);

            // Update the info text
            infoText.setText("Color grabbed: " + grabbedColor.toString());

            // Reset to the default tool after grabbing the color
            resetToDefaultTool();
        } else {
            infoText.setText("Clicked outside canvas bounds.");
            System.out.println("Click outside canvas bounds.");
        }
    }



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

    // Variable to track the current tool
    //private String currentTool = "Pencil"; default

    @FXML
    private void setPencilTool() {
        currentTool = "Pencil";
    }

    @FXML
    private void setEraserTool() {
        currentTool = "Eraser";
    }

    @FXML
    private void setLineTool() {
        currentTool = "Line";
    }

    @FXML
    private void setRectangleTool() {
        currentTool = "Rectangle";
    }

    @FXML
    private void setEllipseTool() {
        currentTool = "Ellipse";
    }

    @FXML
    private void setCircleTool() {
        currentTool = "Circle";
    }

    @FXML
    private void setTriangleTool() {
        currentTool = "Triangle";
    }

    @FXML
    private void setImageTool() {
        currentTool = "Image";
    }

    @FXML
    private void setStarTool() {
        currentTool = "Star";
    }

    @FXML
    private void setHeartTool() {
        currentTool = "Heart";
    }

    @FXML
    private void setTextTool() {
        currentTool = "Text";
    }

    @FXML
    private void setNgonTool() {
        currentTool = "nGon";
    }

    @FXML
    private void onMousePressed(MouseEvent event) {
        CanvasTab canvasTab = getSelectedCanvasTab();
        if (canvasTab != null) {
            GraphicsContext gc = canvasTab.getGraphicsContext();
            startX = event.getX();
            startY = event.getY();

            gc.setStroke(colorPicker.getValue());
            gc.setLineWidth(lineWidthSlider.getValue());

            if (dashedLineCheckBox.isSelected()) {
                gc.setLineDashes((2 * lineWidthSlider.getValue()) + 10);
            } else {
                gc.setLineDashes(0);
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



    @FXML
    private void onMouseDragged(MouseEvent event) {
        CanvasTab canvasTab = getSelectedCanvasTab();
        if (canvasTab != null) {
            GraphicsContext gc = canvasTab.getGraphicsContext(); // Main canvas GC
            GraphicsContext tempGc = canvasTab.getTempGraphicsContext();  // Temporary canvas GC
            double endX = event.getX();
            double endY = event.getY();

            if (pencilButton.isSelected() || eraserButton.isSelected()) {
                // Set stroke and line width for gc
                if (eraserButton.isSelected()) {
                    gc.setStroke(Color.WHITE);
                } else {
                    gc.setStroke(colorPicker.getValue());
                }
                gc.setLineWidth(lineWidthSlider.getValue());

                gc.lineTo(endX, endY);
                gc.stroke();
            } else {
                // Clear the temporary canvas for redrawing
                tempGc.clearRect(0, 0, canvasTab.getTempCanvas().getWidth(), canvasTab.getTempCanvas().getHeight());

                tempGc.setStroke(colorPicker.getValue()); // Use the color picker value
                tempGc.setLineWidth(lineWidthSlider.getValue());

                // Draw the selected shape on the temporary canvas
                if (lineButton.isSelected()) {
                    tempGc.strokeLine(startX, startY, endX, endY);
                } else if (rectangleButton.isSelected()) {
                    double width = Math.abs(endX - startX);
                    double height = Math.abs(endY - startY);
                    tempGc.strokeRect(Math.min(startX, endX), Math.min(startY, endY), width, height);
                } else if (ellipseButton.isSelected()) {
                    double width = Math.abs(endX - startX);
                    double height = Math.abs(endY - startY);
                    tempGc.strokeOval(Math.min(startX, endX), Math.min(startY, endY), width, height);
                } else if (circleButton.isSelected()) {
                    double radius = Math.hypot(endX - startX, endY - startY);
                    tempGc.strokeOval(startX - radius, startY - radius, radius * 2, radius * 2);
                } else if (triangleButton.isSelected()) {
                    tempGc.strokePolygon(
                            new double[]{startX, endX, (startX + endX) / 2},
                            new double[]{startY, endY, startY - Math.abs(endY - startY)}, 3);
                } else if (starButton.isSelected()) {
                    drawStar(tempGc, startX, startY, endX, endY);
                } else if (heartButton.isSelected()) {
                    double centerX = (startX + endX) / 2;
                    double centerY = (startY + endY) / 2;
                    double size = Math.abs(endX - startX) / 16;
                    drawHeart(tempGc, centerX, centerY, size);
                } else if (imageButton.isSelected()) {
                    double width = Math.abs(endX - startX);
                    double height = Math.abs(endY - startY);
                    tempGc.drawImage(customStickerImage, Math.min(startX, endX), Math.min(startY, endY), width, height);
                } else if (nGonButton.isSelected()) {
                    int numberOfSides = nGonSides;
                    drawNgon(tempGc, startX, startY, endX, endY, numberOfSides);
                } else if (textButton.isSelected()) {
                    double deltaX = endX - startX;
                    double deltaY = endY - startY;
                    double distance = Math.hypot(deltaX, deltaY);
                    double fontSize = distance / 8;  // Calculate font size based on distance
                    Font font = createFontWithStyle(fontFamily, fontSize, italic, bold);
                    drawText(tempGc, startX, startY, endX, endY, stringToolText, font, colorPicker.getValue());
                }
            }
            // Save the canvas state for undo functionality
            // You can uncomment the following line if needed
            // saveCanvasState();
        }
    }


    @FXML
    private void onMouseReleased(MouseEvent event) {
        CanvasTab canvasTab = getSelectedCanvasTab();
        if (canvasTab != null) {
            GraphicsContext gc = canvasTab.getGraphicsContext();  // Main canvas GC
            GraphicsContext tempGc = canvasTab.getTempGraphicsContext();  // Temporary canvas GC
            double endX = event.getX();
            double endY = event.getY();

            // Clear the temporary canvas after releasing the mouse
            tempGc.clearRect(0, 0, canvasTab.getTempCanvas().getWidth(), canvasTab.getTempCanvas().getHeight());

            // Now commit the final shape to the main canvas
            gc.setStroke(colorPicker.getValue());
            gc.setLineWidth(lineWidthSlider.getValue());

            if (pencilButton.isSelected() || eraserButton.isSelected()) {
                // Nothing to do here; drawing is handled during drag
            } else if (lineButton.isSelected()) {
                gc.strokeLine(startX, startY, endX, endY);
            } else if (rectangleButton.isSelected()) {
                double width = Math.abs(endX - startX);
                double height = Math.abs(endY - startY);
                gc.strokeRect(Math.min(startX, endX), Math.min(startY, endY), width, height);
            } else if (ellipseButton.isSelected()) {
                double width = Math.abs(endX - startX);
                double height = Math.abs(endY - startY);
                gc.strokeOval(Math.min(startX, endX), Math.min(startY, endY), width, height);
            } else if (circleButton.isSelected()) {
                double radius = Math.hypot(endX - startX, endY - startY);
                gc.strokeOval(startX - radius, startY - radius, radius * 2, radius * 2);
            } else if (triangleButton.isSelected()) {
                gc.strokePolygon(
                        new double[]{startX, endX, (startX + endX) / 2},
                        new double[]{startY, endY, startY - Math.abs(endY - startY)}, 3);
            } else if (starButton.isSelected()) {
                drawStar(gc, startX, startY, endX, endY);
            } else if (heartButton.isSelected()) {
                double centerX = (startX + endX) / 2;
                double centerY = (startY + endY) / 2;
                double size = Math.abs(endX - startX) / 16;
                drawHeart(gc, centerX, centerY, size);
            } else if (imageButton.isSelected()) {
                double width = Math.abs(endX - startX);
                double height = Math.abs(endY - startY);
                gc.drawImage(customStickerImage, Math.min(startX, endX), Math.min(startY, endY), width, height);
            } else if (nGonButton.isSelected()) {
                int numberOfSides = nGonSides;
                drawNgon(gc, startX, startY, endX, endY, numberOfSides);
            } else if (textButton.isSelected()) {
                double deltaX = endX - startX;
                double deltaY = endY - startY;
                double distance = Math.hypot(deltaX, deltaY);
                double fontSize = distance / 8;  // Calculate font size based on distance
                Font font = createFontWithStyle(fontFamily, fontSize, italic, bold);
                drawText(gc, startX, startY, endX, endY, stringToolText, font, colorPicker.getValue());
            }
            // Save the canvas state for undo functionality
            //saveCanvasState(tabPane.getSelectionModel().getSelectedItem());
            saveCanvasState();
        }
    }





    public void drawNgon(GraphicsContext gc, double x1, double y1, double x2, double y2, int n) {
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
        gc.strokePolygon(xPoints, yPoints, n);
    }

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



    private void drawHeart(GraphicsContext gc, double centerX, double centerY, double size) {
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

        // Draw the heart shape using a polygon
        gc.strokePolygon(xPoints, yPoints, points);
    }

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


    @FXML
    public void setDefaultSticker(){
        customStickerImage = new Image("/images/paint-P-Logo.png");
    }

    @FXML
    public void setDefaultText(){
        stringToolText = defaultText;
    }

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

    public void setDefaultPolygonSides(){
        nGonSides = 5;
    }

    public int getNGonSides() {
        return nGonSides;
    }

    @FXML
    private Font createFontWithStyle(String fontFamily, double fontSize, boolean italic, boolean bold) {
        FontWeight fontWeight = bold ? FontWeight.BOLD : FontWeight.NORMAL;
        FontPosture fontPosture = italic ? FontPosture.ITALIC : FontPosture.REGULAR;

        return Font.font(fontFamily, fontWeight, fontPosture, fontSize);
    }


    private void drawStar(GraphicsContext gc,double startX, double startY, double endX, double endY) {

        // Debugging print statements
        System.out.println("Entered drawStar method");
        System.out.println("GraphicsContext: " + gc);
        System.out.println("StartX: " + startX + ", StartY: " + startY);
        System.out.println("EndX: " + endX + ", EndY: " + endY);

        // Check if GraphicsContext is null
        if (gc == null) {
            System.err.println("GraphicsContext is null! Exiting drawStar.");
            return;  // Exit the method if GraphicsContext is not initialized
        }
        double centerX = (startX + endX) / 2;
        double centerY = (startY + endY) / 2;
        double radius = Math.min(Math.abs(endX - startX), Math.abs(endY - startY)) / 2;
        double angle = Math.PI / 5;  // 36 degrees for star points

        double[] xPoints = new double[10];
        double[] yPoints = new double[10];

        for (int i = 0; i < 10; i++) {
            double radiusFactor = (i % 2 == 0) ? 1 : 0.5;  // Alternate between outer and inner points
            xPoints[i] = centerX + Math.cos(i * 2 * angle) * radius * radiusFactor;
            yPoints[i] = centerY - Math.sin(i * 2 * angle) * radius * radiusFactor;
        }

        gc.strokePolygon(xPoints, yPoints, 10);
    }

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

    @FXML
    public void setCustomTextTool(String inputText) {
        stringToolText = inputText;
    }
    public void setArialFont() {
        fontFamily = "Arial";
    }

    @FXML
    public void setVerdanaFont() {
        fontFamily = "Verdana";
        //updateFont();
    }

    @FXML
    public void setTahomaFont() {
        fontFamily = "Tahoma";
    }

    @FXML
    public void setTimesFont() {
        fontFamily = "Times New Roman";
    }

    @FXML
    public void toggleBold() {
        if (bold)
            bold = false;
        else{
            bold = true;
        }
    }

    @FXML
    public void toggleItalic() {
        if (italic)
            italic = false;
        else{
            italic = true;
        }
    }

    private void updateFont() {
        double fontSize = 24;  // Adjust the size as needed, or make dynamic
        Font customFont = Font.font(fontFamily, fontWeight, fontPosture, fontSize);
        gc.setFont(customFont);
    }

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
                double currentPenSize = lineWidthSlider.getValue(); // Assuming the pen size is controlled by the slider

                // Update the label with canvas size, pen size, and mouse coordinates
                infoText.setText(String.format("Canvas size: %.0f x %.0f | Pen Size: %.0f px | Coordinates X: %.0f, Y: %.0f",
                        width, height, currentPenSize, mouseX, mouseY));
            } else {
                System.err.println("No CanvasTab found for the selected tab.");
            }
        } else {
            System.err.println("No tab is selected.");
        }
    }


    // Ensure this is properly initialized

    /*

        FUN FACT

        Most icons in the buttons were created with this app!
        Painting P!!!!

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

        ImageView imageView1 = new ImageView(pencilIcon);
        imageView1.setFitHeight(25.0); // Set the image height
        imageView1.setFitWidth(25.0);  // Set the image width
        ImageView imageView2 = new ImageView(lineIcon);
        imageView2.setFitHeight(25.0); // Set the image height
        imageView2.setFitWidth(25.0);  // Set the image width
        ImageView imageView3 = new ImageView(rectangleIcon);
        imageView3.setFitHeight(25.0); // Set the image height
        imageView3.setFitWidth(25.0);  // Set the image width
        ImageView imageView4 = new ImageView(ellipseIcon);
        imageView4.setFitHeight(25.0); // Set the image height
        imageView4.setFitWidth(25.0);  // Set the image width
        ImageView imageView5 = new ImageView(circleIcon);
        imageView5.setFitHeight(25.0); // Set the image height
        imageView5.setFitWidth(25.0);  // Set the image width
        ImageView imageView6 = new ImageView(triangleIcon);
        imageView6.setFitHeight(25.0); // Set the image height
        imageView6.setFitWidth(25.0);  // Set the image width
        ImageView imageView7 = new ImageView(eraserIcon);
        imageView7.setFitHeight(25.0); // Set the image height
        imageView7.setFitWidth(25.0);
        ImageView imageView8 = new ImageView(starIcon);
        imageView8.setFitHeight(25.0); // Set the image height
        imageView8.setFitWidth(25.0);
        ImageView imageView9 = new ImageView(stickerIcon);
        imageView9.setFitHeight(25.0); // Set the image height
        imageView9.setFitWidth(25.0);
        ImageView imageView10 = new ImageView(heartIcon);
        imageView10.setFitHeight(25.0); // Set the image height
        imageView10.setFitWidth(25.0);
        ImageView imageView11 = new ImageView(textIcon);
        imageView11.setFitHeight(25.0); // Set the image height
        imageView11.setFitWidth(25.0);
        ImageView imageView12 = new ImageView(nGonIcon);
        imageView12.setFitHeight(25.0); // Set the image height
        imageView12.setFitWidth(25.0);

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
    }

    // Getter for pen size
    public double getPenSize() {
        return penSize.get();
    }

    // Setter for pen size
    public void setPenSize(double size) {
        penSize.set(size);
    }

    // Getter for the pen size property (useful for binding)
    public DoubleProperty penSizeProperty() {
        return penSize;
    }




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

    @FXML
    protected void setWideCanvas() {
        boolean safe = safetyCanvasEdit();
        if (safe) {
            Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
            if (selectedTab != null && selectedTab.getContent() instanceof ScrollPane) {
                ScrollPane scrollPane = (ScrollPane) selectedTab.getContent();
                if (scrollPane.getContent() instanceof StackPane) {
                    StackPane canvasPane = (StackPane) scrollPane.getContent();
                    if (!canvasPane.getChildren().isEmpty() && canvasPane.getChildren().get(0) instanceof Canvas) {
                        Canvas currentCanvas = (Canvas) canvasPane.getChildren().get(0);
                        currentCanvas.setWidth(1240);
                        currentCanvas.setHeight(620);
                    } else {
                        System.err.println("No canvas found in the StackPane.");
                    }
                } else {
                    System.err.println("The content inside the ScrollPane is not a StackPane.");
                }
            } else {
                System.err.println("The content of the tab is not a ScrollPane.");
            }
        }
    }


    @FXML
    protected void setCustomSizeCanvas(ActionEvent event) {
        boolean safe = safetyCanvasEdit();
        if (safe) {
            CanvasTab canvasTab = getSelectedCanvasTab();
            if (canvasTab != null) {
                Canvas currentCanvas = canvasTab.getCanvas();

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

                    // Re-fill the canvas background to ensure the new size is visible
                    GraphicsContext gc = currentCanvas.getGraphicsContext2D();
                    gc.setFill(Color.WHITE);  // Set default background color (white)
                    gc.fillRect(0, 0, newWidth, newHeight);

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

    @FXML
    protected void setWindowSizeCanvas() {
        boolean safe = safetyCanvasEdit();
        if (safe) {
            CanvasTab canvasTab = getSelectedCanvasTab();
            if (canvasTab != null) {
                Canvas currentCanvas = canvasTab.getCanvas();

                // Get the current stage's dimensions
                Stage stage = (Stage) currentCanvas.getScene().getWindow();
                double width = stage.getWidth();
                double height = stage.getHeight();

                // Adjust the canvas size according to the window size (with some padding)
                currentCanvas.setWidth(width - 30);
                currentCanvas.setHeight(height - 50);

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



    @FXML
    protected void setTallCanvas() {
        boolean safe = safetyCanvasEdit();
        if (safe) {
            Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
            if (selectedTab != null) {
                Node content = selectedTab.getContent();
                StackPane canvasPane;
                if (content instanceof ScrollPane) {
                    ScrollPane scrollPane = (ScrollPane) content;
                    canvasPane = (StackPane) scrollPane.getContent();
                } else if (content instanceof StackPane) {
                    canvasPane = (StackPane) content;
                } else {
                    System.err.println("The content is neither a ScrollPane nor a StackPane.");
                    return;
                }

                if (!canvasPane.getChildren().isEmpty() && canvasPane.getChildren().get(0) instanceof Canvas) {
                    Canvas currentCanvas = (Canvas) canvasPane.getChildren().get(0);
                    currentCanvas.setWidth(600);
                    currentCanvas.setHeight(650);

                    // Re-fill the canvas background to ensure the new size is visible
                    GraphicsContext gc = currentCanvas.getGraphicsContext2D();
                    gc.setFill(Color.WHITE);
                    gc.fillRect(0, 0, currentCanvas.getWidth(), currentCanvas.getHeight());

                    updateLabel();  // Update any UI elements or labels reflecting the new size
                } else {
                    System.err.println("No canvas found in the StackPane.");
                }
            }
        }
    }

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







    private void showErrorDialog(String title, String message) {
        // Utility method to show an error dialog
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }







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
            } else {
                System.err.println("No CanvasTab found for the selected tab.");
            }
        }
    }

    @FXML
    protected void onOpenImageClickCanvasSize(ActionEvent event) {
        onOpenImageClick(false);
    }

    @FXML
    protected void onOpenImageClickOriginalSize(ActionEvent event) {
        onOpenImageClick(true);
    }

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

                        if (originalSize) {
                            currentCanvas.setWidth(imageWidth);
                            currentCanvas.setHeight(imageHeight);
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

    @FXML
    protected void onSafeClick() { //TS
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null && selectedTab.getContent() instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) selectedTab.getContent();
            if (scrollPane.getContent() instanceof StackPane) {
                StackPane canvasPane = (StackPane) scrollPane.getContent();
                if (!canvasPane.getChildren().isEmpty() && canvasPane.getChildren().get(0) instanceof Canvas) {
                    Canvas currentCanvas = (Canvas) canvasPane.getChildren().get(0);

                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Save Canvas As");
                    fileChooser.getExtensionFilters().add(
                            new FileChooser.ExtensionFilter("JPEG Files", "*.jpg", "*.jpeg")
                    );

                    fileChooser.setInitialFileName("paint_p.jpg");

                    File selectedFile = fileChooser.showSaveDialog(currentCanvas.getScene().getWindow());

                    if (selectedFile != null) {
                        if (!selectedFile.getName().toLowerCase().endsWith(".jpg")) {
                            selectedFile = new File(selectedFile.getAbsolutePath() + ".jpg");
                        }

                        WritableImage writableImage = new WritableImage((int) currentCanvas.getWidth(), (int) currentCanvas.getHeight());
                        currentCanvas.snapshot(null, writableImage);
                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);

                        try {
                            ImageIO.write(bufferedImage, "jpg", selectedFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                            showAlert("Error", "Failed to save the image.");
                        }
                    }
                }
            }
        } else {
            System.err.println("No canvas is selected.");
        }
    }



    @FXML
    protected void onSaveAsClick() {  //TS
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null && selectedTab.getContent() instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) selectedTab.getContent();
            if (scrollPane.getContent() instanceof StackPane) {
                StackPane canvasPane = (StackPane) scrollPane.getContent();
                if (!canvasPane.getChildren().isEmpty() && canvasPane.getChildren().get(0) instanceof Canvas) {
                    Canvas currentCanvas = (Canvas) canvasPane.getChildren().get(0);

                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Save As");
                    fileChooser.getExtensionFilters().addAll(
                            new FileChooser.ExtensionFilter("PNG Files", "*.png"),
                            new FileChooser.ExtensionFilter("JPEG Files", "*.jpg", "*.jpeg"),
                            new FileChooser.ExtensionFilter("GIF Files", "*.gif"),
                            new FileChooser.ExtensionFilter("BMP Files", "*.bmp")
                    );
                    File selectedFile = fileChooser.showSaveDialog(currentCanvas.getScene().getWindow());

                    if (selectedFile != null) {
                        String format = selectedFile.getName().substring(selectedFile.getName().lastIndexOf('.') + 1).toLowerCase();
                        WritableImage writableImage = new WritableImage((int) currentCanvas.getWidth(), (int) currentCanvas.getHeight());
                        currentCanvas.snapshot(null, writableImage);
                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);

                        try {
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
                                    break;
                            }
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


    //DO NOT TOUCH BMP IS WORKING
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

    private void saveAsGIF(BufferedImage bufferedImage, File file) throws IOException {
        try {
            // Save as GIF using ImageIO with TwelveMonkeys
            ImageIO.write(bufferedImage, "gif", file);
        } catch (IOException e) {
            throw new IOException("GIF format is not supported by ImageIO in your setup.", e);
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    //private int currentPixelSize = 10; // Initialize with a default value

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
    }

    @FXML
    private void updatePixelSizeLabel() {
        pixelSizeLabel.setText("Pixel size: " + currentPixelSize);
    }

    @FXML
    protected void onShapeButtonClick(ActionEvent event) {
        ToggleButton source = (ToggleButton) event.getSource();
        currentShape = source.getText();
    }

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
            addNewTab(canvasName, 1100, 525);
        });

        // If no input is provided (user cancels), do nothing
    }


    @FXML
    protected void onExitMenuClick() {
        System.exit(0);
    }
}