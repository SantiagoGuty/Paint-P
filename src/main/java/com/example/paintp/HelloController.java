package com.example.paintp;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;

import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

import javax.imageio.ImageIO;


public class HelloController {

    @FXML
    private Canvas canvas;

    @FXML
    private Label welcomeText;


    @FXML
    public void initialize() {
        // 800 x 600 default REMEMBER!!
        canvas.setWidth(800);
        canvas.setHeight(600);
    }

    @FXML
    protected void onOpenImageClick() {

        // Make sure canvas is initialized
        if (canvas == null) {
            System.err.println("Canvas is not initialized.");
            return;
        }

        // Open a file chooser to select an image
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(

                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")

        );

        // Show open file dialog
        File selectedFile = fileChooser.showOpenDialog(canvas.getScene().getWindow());

        // If a file was selected, display it on the Canvas
        if (selectedFile != null) {

            Image image = new Image(selectedFile.toURI().toString());
            // Get GraphicsContext and draw image
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());

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

            // Handle stupid formats (JPEG, BMP)
            if (format.equals("jpg") || format.equals("jpeg") || format.equals("bmp")) {
                ButtonType yes = new ButtonType("Yes, Save");
                ButtonType no = new ButtonType("No, Don't Save");
                Alert alert = new Alert(Alert.AlertType.WARNING, "Potential data loss may occur.\nAre you sure you want to continue?", yes, no);
                alert.setTitle("Potential Data Loss");
                ButtonType response = alert.showAndWait().orElse(no);

                if (response == no) {
                    return;
                }
            }

            try {
                ImageIO.write(bufferedImage, format, selectedFile);
            } catch (IOException e) {
                e.printStackTrace();
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

        // Show save file dialog
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

                // Clear the entire canvas, Reset to default
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

            } else {

                System.err.println("GraphicsContext is not initialized.");

            }
        } else {

            System.err.println("Canvas is not initialized.");

        }
    }


    @FXML
    protected void onAboutMenuClick() { //Create a new pop-up window which has a Text area that connects the release notes txt file

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
        vbox.setPadding(new Insets(1));
        VBox.setVgrow(releaseNotesTextArea, javafx.scene.layout.Priority.ALWAYS);

        Scene scene = new Scene(vbox, 600, 400);

        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/paint-P-Logo.png")));
        popupStage.getIcons().add(icon);

        popupStage.setScene(scene);
        popupStage.show();
    }


    // Default menu items


    @FXML
    protected void onNewMenuClick() {
        welcomeText.setText("New Menu Item Clicked!");
    }

    @FXML
    protected void onExitMenuClick() {
        System.exit(0);
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