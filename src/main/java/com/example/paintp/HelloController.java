package com.example.paintp;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Objects;
import java.util.Optional;

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
    private CheckBox dashedLineCheckBox;


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

    @FXML
    private Canvas testCanvas;
    private GraphicsContext gc;
    private GraphicsContext testGc;
    private String currentTool = "Pencil";


    @FXML
    public void initialize() {

        // Initialize default canvas size 1240 X 620
        canvas.setWidth(1240);
        canvas.setHeight(620);

        // Initialize KeyCombination instances
        saveShortCut = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
        exitShortCut = new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN);
        clearShortCut = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);

        // Set default color and line width
        colorPicker.setValue(currentColor);
        lineWidthSlider.setValue(currentLineWidth);

        // Set default tool (pencil) default color (Black) Line width size (1)
        gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);

        //Toggle Buttons menu
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
        textButton.setToggleGroup(toolsToggleGroup);

        // Assign ToggleButton methods
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

        //Event for color grabbing
        colorGrabButton.setOnAction(event -> onColorGrabClick());

        // Set default button icons
        setButtonIcons();

        // Handle color picker changes
        colorPicker.setOnAction(event -> onColorChange());
        colorPicker.setValue(Color.BLACK);

        //Pen Size management
        penSize.bind(lineWidthSlider.valueProperty());

        //Debugging info
        System.out.println("Canvas: " + canvas);
        System.out.println("ColorPicker: " + colorPicker);
        System.out.println("LineWidthSlider: " + lineWidthSlider);

        // Ensure canvasPane has focus
        canvasPane.requestFocus();
        dashedLineCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            isDashedLine = newValue;
        });

        // Listener to the Scene to set up shortcuts
        canvasPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                System.out.println("Scene is initialized. Setting up shortcuts.");
                setupShortcuts();
            }
        });

        // Label that shows the canvas size/ pixel location / Pencil size
        updateLabel();

        // Optionally, you can listen for changes and perform additional actions when the slider value changes
        penSize.addListener((observable, oldValue, newValue) -> {
            System.out.println("Pen size updated: " + newValue);
            updateLabel(); // Update the label to reflect the new pen size, if necessary
        });


        // Canvas

        // Set initial canvas, with a white background
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Add listeners to track changes in the canvas size and mouse movement
        canvas.widthProperty().addListener((observable, oldValue, newValue) -> updateLabel());
        canvas.heightProperty().addListener((observable, oldValue, newValue) -> updateLabel());
        canvas.setOnMouseMoved(event -> updateLabel(event.getX(), event.getY()));
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);

    }

    @FXML
    private void onColorGrabClick() {
        currentShape = "ColorGrabber";
        canvas.setOnMouseClicked(this::grabColor);
    }

    // grabColor method
    private void grabColor(MouseEvent event) {
        if ("ColorGrabber".equals(currentShape)) {
            // Get the coordinates of the click
            double x = event.getX();
            double y = event.getY();

            // Capture the snapshot of the canvas to read pixel data
            WritableImage snapshot = canvas.snapshot(null, null);
            PixelReader pixelReader = snapshot.getPixelReader();

            // Ensure x and y are within canvas bounds
            if (x >= 0 && x < snapshot.getWidth() && y >= 0 && y < snapshot.getHeight()) {
                Color color = pixelReader.getColor((int) x, (int) y);

                // Update the color picker with the grabbed color
                colorPicker.setValue(color);

                // Optional: Display the color value somewhere
                System.out.println("Color grabbed: " + color.toString());
            } else {
                System.out.println("Click out of canvas bounds.");
            }

            // Reset the tool back to default
            currentShape = "Pencil";  // Or whatever the default tool is
            canvas.setOnMouseClicked(null); // Remove the color grabber handler
        }
    }

    @FXML
    private ToggleButton pencilButton, eraserButton, lineButton, rectangleButton, ellipseButton, circleButton, triangleButton, starButton, heartButton, imageButton, textButton;

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
    private void setHeartTool() { currentTool = "Heart";}

    @FXML
    private void setTextTool() {
        currentTool = "Text";
    }


    private void onMousePressed(MouseEvent event) {
        startX = event.getX();
        startY = event.getY();

        gc.setStroke(colorPicker.getValue());
        gc.setLineWidth(lineWidthSlider.getValue());

        if (pencilButton.isSelected()) {
            gc.beginPath();
            gc.moveTo(startX, startY);
        } else if (eraserButton.isSelected()) {
            //gc.clearRect(startX, startY, lineWidthSlider.getValue(), lineWidthSlider.getValue());
            gc.setStroke(Color.WHITE); // Set eraser stroke color to white
            gc.setLineWidth(lineWidthSlider.getValue()); // Use lineWidth for eraser as well
            gc.beginPath();
            gc.moveTo(startX, startY);
        }
    }

    private void onMouseDragged(MouseEvent event) {
        if (pencilButton.isSelected()) {
            gc.lineTo(event.getX(), event.getY());
            gc.stroke();
        } else if (eraserButton.isSelected()) {
            //gc.clearRect(event.getX(), event.getY(), lineWidthSlider.getValue(), lineWidthSlider.getValue());
            gc.lineTo(event.getX(), event.getY());
            gc.stroke(); // Use stroke with white color for eraser
        }
    }

    private void onMouseReleased(MouseEvent event) {
        // Check if the dashed lines checkbox is selected
        if (dashedLineCheckBox.isSelected()) {
            gc.setLineDashes(10);  // Set dashes (length of dashes)
            gc.setLineDashOffset(5);  // Distance between dashes
        } else {
            gc.setLineDashes(0);  // Solid line (no dashes)
        }

        // Drawing the selected shapes
        if (lineButton.isSelected()) {
            gc.strokeLine(startX, startY, event.getX(), event.getY());
        } else if (rectangleButton.isSelected()) {
            double width = Math.abs(event.getX() - startX);
            double height = Math.abs(event.getY() - startY);
            gc.strokeRect(Math.min(startX, event.getX()), Math.min(startY, event.getY()), width, height);
        } else if (ellipseButton.isSelected()) {
            double width = Math.abs(event.getX() - startX);
            double height = Math.abs(event.getY() - startY);
            gc.strokeOval(Math.min(startX, event.getX()), Math.min(startY, event.getY()), width, height);
        } else if (circleButton.isSelected()) {
            double radius = Math.abs(event.getX() - startX);
            gc.strokeOval(Math.min(startX, event.getX()), Math.min(startY, event.getY()), radius, radius);
        } else if (triangleButton.isSelected()) {
            gc.strokePolygon(new double[]{startX, event.getX(), (startX + event.getX()) / 2},
                    new double[]{startY, event.getY(), startY - Math.abs(event.getY() - startY)}, 3);
        }else if (starButton.isSelected()) {
            drawStar(startX, startY, event.getX(), event.getY());
        }else if (heartButton.isSelected()) {
            double centerX = (startX + event.getX()) / 2;
            double centerY = (startY + event.getY()) / 2;
            double size = Math.abs(event.getX() - startX) / 16;  // Scale based on the width of the drawn area

            drawHeart(centerX, centerY, size);
        }else if (imageButton.isSelected()) {
            // Load and draw the image
            //Image image = new Image("/images/paint-P-Logo.png");  // Replace with the correct image path or load dynamically
            double x = startX;
            double y = startY;
            double width = Math.abs(event.getX() - startX);
            double height = Math.abs(event.getY() - startY);

            gc.drawImage(customStickerImage, Math.min(startX, event.getX()), Math.min(startY, event.getY()), width, height);
        }else if (textButton.isSelected()) {
            double endX = event.getX();
            double endY = event.getY();

            // Calculate the distance between the start and end points (as a basis for the font size)
            double deltaX = endX - startX;
            double deltaY = endY - startY;
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

            // Set the font size relative to the distance (you can adjust the scaling factor)
            double fontSize = distance / 10;  // Adjust the divisor for smaller or larger fonts
            Font font = createFontWithStyle(fontFamily, fontSize, italic);
            gc.setFont(font);

            // Set a max width for the text to ensure it fits within the drawing area
            double maxWidth = Math.abs(endX - startX);  // Use the width between the start and end points as the max width

            // Ensure the custom text is not null or empty
            if (stringToolText != null && !stringToolText.isEmpty()) {
                gc.strokeText(stringToolText, startX, startY, maxWidth);
                gc.fillText(stringToolText, startX, startY, maxWidth);
            }
        }
        // Reset dashes back to solid for future drawings if needed
        //gc.setLineDashes(0);
    }

    private void drawHeart(double centerX, double centerY, double size) {
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

    @FXML
    private Font createFontWithStyle(String fontFamily, double fontSize, boolean italic) {
        FontWeight weight = FontWeight.NORMAL;
        FontPosture posture = italic ? FontPosture.ITALIC : FontPosture.REGULAR;
        return Font.font(fontFamily, weight, posture, fontSize);
    }

    private void drawStar(double startX, double startY, double endX, double endY) {
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
        updateFont();
    }

    @FXML
    public void setVerdanaFont() {
        fontFamily = "Verdana";
        updateFont();
    }

    @FXML
    public void setTahomaFont() {
        fontFamily = "Tahoma";
        updateFont();
    }

    @FXML
    public void setTimesFont() {
        fontFamily = "Times New Roman";
        updateFont();
    }

    @FXML
    public void toggleBold() {
        fontWeight = FontWeight.BOLD;
        updateFont();
    }

    @FXML
    public void toggleItalic() {
        fontPosture = FontPosture.ITALIC;
        updateFont();
    }

    private void updateFont() {
        double fontSize = 24;  // Adjust the size as needed, or make dynamic
        Font customFont = Font.font(fontFamily, fontWeight, fontPosture, fontSize);
        gc.setFont(customFont);
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

    private void updateLabel(double mouseX, double mouseY) {

        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double currentPenSize = getPenSize(); // Get the current pen size (from the slider)

        // Set label text with width, height, mouse coordinates, and pen size
        infoText.setText(String.format("Canvas size: %.0f x %.0f | Pen Size: %.0f px | Coordinates X: %.0f, Y: %.0f", width, height, currentPenSize, mouseX, mouseY));
    }

    // Overloaded method to just update the size without mouse coordinates
    private void updateLabel() {
        updateLabel(0, 0);  // Default mouse coordinates to (0, 0)
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



    private void setupShortcuts() {
        Scene scene = canvasPane.getScene();
        if (scene != null) {
            System.out.println("Scene is initialized. Setting up shortcuts.");
            scene.getAccelerators().put(saveShortCut, this::onSaveAsClick);
            scene.getAccelerators().put(exitShortCut, this::safetyExit);
            scene.getAccelerators().put(clearShortCut, this::onCanvaClearCanva);
        } else {
            System.out.println("Scene is null. Cannot set up shortcuts.");
        }
    }




    @FXML
    protected void onColorChange() {
        currentColor = colorPicker.getValue();
    }

    @FXML
    public void onUndoClick() {
        // Your undo logic here
    }

    @FXML
    protected void onRedoClick() {
        // Implement redo functionality here
    }

    @FXML
    protected void setWideCanvas() {
        boolean safe = safetyCanvasEdit();
        if (safe) {
            canvas.setWidth(1240);
            canvas.setHeight(620);
        }
    }

    @FXML
    protected void setTallCanvas() {
        boolean safe = safetyCanvasEdit();
        if (safe) {
            canvas.setWidth(600);
            canvas.setHeight(650);
        }
    }

    @FXML
    protected void setCustomSizeCanvas() {
        boolean safe = safetyCanvasEdit();
        if (safe) {
            // Load the icon image
            Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/paint-P-Logo.png")));

            // Create two dialogs for input: one for width, one for height
            TextInputDialog widthDialog = new TextInputDialog();
            widthDialog.setTitle("Custom Canvas Size");
            widthDialog.setHeaderText("Set Custom Canvas Size");
            widthDialog.setContentText("Enter canvas width (in pixels):");

            // Set the icon for the width dialog
            Stage widthStage = (Stage) widthDialog.getDialogPane().getScene().getWindow();
            widthStage.getIcons().add(icon);

            Optional<String> widthInput = widthDialog.showAndWait();
            if (!widthInput.isPresent()) {
                return; // If the user cancels the dialog, exit the method
            }

            TextInputDialog heightDialog = new TextInputDialog();
            heightDialog.setTitle("Custom Canvas Size");
            heightDialog.setHeaderText("Set Custom Canvas Size");
            heightDialog.setContentText("Enter canvas height (in pixels):");

            // Set the icon for the height dialog
            Stage heightStage = (Stage) heightDialog.getDialogPane().getScene().getWindow();
            heightStage.getIcons().add(icon);

            Optional<String> heightInput = heightDialog.showAndWait();
            if (!heightInput.isPresent()) {
                return; // If the user cancels the dialog, exit the method
            }

            // Try to parse the input values and apply them to the canvas
            try {
                double newWidth = Double.parseDouble(widthInput.get());
                double newHeight = Double.parseDouble(heightInput.get());

                // Validate the size values (e.g., minimum and maximum size)
                if (newWidth <= 0 || newHeight <= 0) {
                    throw new IllegalArgumentException("Canvas size must be positive.");
                }

                // Set the new canvas size
                canvas.setWidth(newWidth);
                canvas.setHeight(newHeight);

                // Re-fill the canvas background to ensure the new size is visible
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.setFill(Color.WHITE); // Set default background color (if any)
                gc.fillRect(0, 0, newWidth, newHeight);

                // Update any UI elements or labels reflecting the new size
                updateLabel(); // Refresh the label to show the updated canvas size

            } catch (NumberFormatException e) {
                // Handle invalid input (non-numeric values)
                showErrorDialog("Invalid input", "Please enter valid numeric values for the canvas size.");
            } catch (IllegalArgumentException e) {
                // Handle size out of bounds
                showErrorDialog("Invalid size", e.getMessage());
            }
        }
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
    protected void setWindowSizeCanvas() {
        boolean safe = safetyCanvasEdit();
        if (safe) {
            Stage stage = (Stage) canvas.getScene().getWindow();
            double height = stage.getHeight();
            double width = stage.getWidth();
            canvas.setWidth(width - 30);
            canvas.setHeight(height - 50);
        }
    }



    @FXML
    protected void onCanvaClearCanva() {
        boolean safe = safetyCanvasEdit();
        if (safe) {
            if (canvas != null) {

                GraphicsContext gc = canvas.getGraphicsContext2D();
                if (gc != null) {

                    gc.setFill(Color.WHITE);
                    gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());


                } else {

                    System.err.println("GraphicsContext is not initialized.");

                }
            } else {

                System.err.println("Canvas is not initialized.");

            }
        }
    }

    @FXML
    protected void onOpenImageClickCanvasSize() {
        onOpenImageClick(false);
    }

    @FXML
    protected void onOpenImageClickOriginalSize() {
        onOpenImageClick(true);
    }

    @FXML
    protected void onOpenImageClick(boolean originalSize) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(canvas.getScene().getWindow());

        if (selectedFile != null) {
            Image image = new Image(selectedFile.toURI().toString());

            double imageWidth = image.getWidth();
            double imageHeight = image.getHeight();

            GraphicsContext gc = canvas.getGraphicsContext2D();

            if (originalSize) {
                canvas.setWidth(imageWidth);
                canvas.setHeight(imageHeight);
                gc.drawImage(image, 0, 0, imageWidth, imageHeight);
            } else {
                gc.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());
            }
        }
    }

    @FXML
    protected void onSafeClick() {

        // Make sure the canvas is initialized
        if (canvas == null) {
            System.err.println("Canvas is not initialized bro.");
            return;
        }

        // FileChooser to select the save location
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Canvas As");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JPEG Files", "*.jpg", "*.jpeg")
        );

        // Set default paint_p name and extension
        fileChooser.setInitialFileName("paint_p.jpg");

        File selectedFile = fileChooser.showSaveDialog(canvas.getScene().getWindow());

        if (selectedFile != null) {

            // Ensure the file extension is ".jpg" if not correct it
            if (!selectedFile.getName().toLowerCase().endsWith(".jpg")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".jpg");
            }

            // Create a WritableImage from the Canvas
            WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
            canvas.snapshot(null, writableImage);

            // Convert WritableImage to BufferedImage
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);

            // Save BufferedImage as JPEG
            try {
                ImageIO.write(bufferedImage, "jpg", selectedFile);
            } catch (IOException e) {
                e.printStackTrace();

                // Error alert if the fails
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save the image.");
                alert.setTitle("Error");
                alert.showAndWait();
            }
        }
    }

    @FXML
    protected void onSaveAsClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save As");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG Files", "*.png"),
                new FileChooser.ExtensionFilter("JPEG Files", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("GIF Files", "*.gif"),
                new FileChooser.ExtensionFilter("BMP Files", "*.bmp")
        );
        File selectedFile = fileChooser.showSaveDialog(canvas.getScene().getWindow());

        if (selectedFile != null) {
            String format = selectedFile.getName().substring(selectedFile.getName().lastIndexOf('.') + 1).toLowerCase();
            WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
            canvas.snapshot(null, writableImage);
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

    private void saveAsJPEG(BufferedImage bufferedImage, File file) throws IOException {
        ImageIO.write(bufferedImage, "jpg", file);
    }

    private void saveAsBMP(BufferedImage bufferedImage, File file) throws IOException {
        ImageIO.write(bufferedImage, "bmp", file);
    }

    private void saveAsGIF(BufferedImage bufferedImage, File file) throws IOException {
        ImageIO.write(bufferedImage, "gif", file);
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
    protected void onNewMenuClick() {
    }

    @FXML
    protected void onExitMenuClick() {
        System.exit(0);
    }
}