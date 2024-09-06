package com.example.paintp;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
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

import javafx.scene.control.ColorPicker;
import javafx.scene.control.ToggleButton;

import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Objects;

import javax.imageio.ImageIO;



public class HelloController {

    @FXML
    private ToggleButton pincel_button;

    @FXML
    private Slider lineWidthSlider;

    @FXML
    private Button undoButton;

    @FXML
    private Button redoButton;

    @FXML
    private ColorPicker colorPicker;

    @FXML
    public ScrollPane scrollPanel;

    @FXML
    public StackPane canvasPane;

    @FXML
    private Canvas canvas;

    @FXML
    private Label welcomeText;


    @FXML
    public void initialize() {

        pincel_button.setPrefHeight(20.0);
        pincel_button.setPrefWidth(10.0);

        Image pencilIcon = new Image(getClass().getResourceAsStream("/images/pencil_icon.png"));

        // Create an ImageView and set the image
        ImageView imageView = new ImageView(pencilIcon);
        imageView.setFitHeight(25.0); // Set the image height
        imageView.setFitWidth(25.0);  // Set the image width
        pincel_button.setGraphic(imageView);

        colorPicker.setOnAction(event -> onColorChange());


        // 1240 x 620 default REMEMBER!!  1240  620
        canvas.setWidth(1240);
        canvas.setHeight(620);

        // Drawing default values
        colorPicker.setValue(Color.BLACK);
        canvas.setOnMousePressed(this::startDrawing);
        canvas.setOnMouseDragged(this::drawLine);
        canvas.setOnMouseReleased(this::endDrawing);


        // default canvas initialize
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private boolean drawing = false;
    private double startX, startY;

    private void startDrawing(MouseEvent event) {
        if (pincel_button.isSelected() && event.getButton() == MouseButton.PRIMARY) {
            drawing = true;
            startX = event.getX();
            startY = event.getY();
        }
    }

    private void drawLine(MouseEvent event) {
        if (drawing && pincel_button.isSelected()) {
            GraphicsContext gc = canvas.getGraphicsContext2D();

            gc.setStroke(colorPicker.getValue()); // Uses the color from the ColorPicker
            gc.setLineWidth(lineWidthSlider.getValue()); // Uses the line width from the Slider


            double x = event.getX();
            double y = event.getY();

            gc.strokeLine(startX, startY, x, y);
            startX = x;
            startY = y;
        }
    }

    private void endDrawing(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            drawing = false;
        }
    }


@FXML
    protected void onColorChange() {
        Color selectedColor = colorPicker.getValue();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(selectedColor);
    }

    @FXML
    protected void onUndoClick() {
        // Coming soonnn
    }

    @FXML
    protected void onRedoClick() {
        // Coming sooon
    }

    @FXML
    protected void set_wide_canva(){
        canvas.setWidth(1240);
        canvas.setHeight(620);
    }

    @FXML
    protected void set_tall_canva(){
        canvas.setWidth(600);
        canvas.setHeight(650);
    }

    @FXML
    protected void set_custom_size_canva(int width, int height){
        canvas.setHeight(height);
        canvas.setWidth(width);
    }

    @FXML
    protected void set_windows_size_canva(){

        Stage stage = (Stage) canvas.getScene().getWindow();

        double height = stage.getHeight();
        double width = stage.getWidth();
        System.out.println(height);
        System.out.println(width);
        //Screen size width -30 and height -50 because of the menu
        canvas.setWidth(width - 30);
        canvas.setHeight(height - 50);
    }

    @FXML
    protected void onOpenImageClickCanvaSize() {
        onOpenImageClick(false); // Stretched to canvas size
    }

    @FXML
    protected void onOpenImageClickOriginalSIze() {
        onOpenImageClick(true); // Original size
    }


    @FXML
    protected void onOpenImageClick(boolean originalSize) {

        // Making sure canvas is initialized
        if (canvas == null) {
            System.err.println("Canvas is not initialized.");
            return;
        }

        // Open a file chooser to select an image
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        // Show open file menu dialog
        File selectedFile = fileChooser.showOpenDialog(canvas.getScene().getWindow());

        // If a file was selected, display it on the Canvas
        if (selectedFile != null) {
            Image image = new Image(selectedFile.toURI().toString());

            // Get image width and height
            double imageWidth = image.getWidth();
            double imageHeight = image.getHeight();

            // Print image dimensions for reference
            System.out.println("Image Width: " + imageWidth);
            System.out.println("Image Height: " + imageHeight);

            GraphicsContext gc = canvas.getGraphicsContext2D();

            // Draw image based on the 'originalSize' parameter
            if (originalSize) {
                // Draw image at its original size
                canvas.setWidth(imageWidth);
                canvas.setHeight(imageHeight);
                gc.drawImage(image, 0, 0, imageWidth, imageHeight);
            } else {
                // Draw image stretched to canvas size
                gc.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());
            }
        }
    }


    @FXML
    protected void onSafeAsClick() {

        // Open a file chooser to select an image
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

            // Handle different formats with different methods
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
                showErrorAlert("Error", "Failed to save the image.");
            }
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
            showErrorAlert("Error", "Failed to save the BMP image.");
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


    //Show what is going on with the error, why is it not working
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
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
    protected void onCanvaClearCanva() {
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

    //Create a new pop-up window which has a Text area that connects the release notes txt file
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
    protected void onExitMenuClick() {
        System.exit(0);
    }


    // Default menu items


    @FXML
    protected void onNewMenuClick() {
        welcomeText.setText("New Menu Item Clicked!");
    }

    @FXML
    protected void onCutMenuClick() {
        welcomeText.setText("Cut Menu Item Clicked!");
    }

    @FXML
    protected void onCopyMenuClick() {
        welcomeText.setText("Copy Menu Item Clicked!");
    }

    @FXML
    protected void onPasteMenuClick() {
        welcomeText.setText("Paste Menu Item Clicked!");
    }
}