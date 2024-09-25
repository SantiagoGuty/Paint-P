package com.example.paintp;

import javafx.scene.image.WritableImage;

public class CanvasState {
    private final WritableImage snapshot;  // Stores a snapshot of the canvas

    public CanvasState(WritableImage snapshot) {
        this.snapshot = snapshot;
    }

    public WritableImage getSnapshot() {
        return snapshot;
    }
}
