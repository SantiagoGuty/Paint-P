package com.example.paintp;

import javafx.scene.image.WritableImage;

/**
 * Stores a snapshot of the canvas, used for undo/redo functionality.
 */
public class CanvasState {
    private final WritableImage snapshot;  // Snapshot of the canvas

    /**
     * Creates a CanvasState with the given snapshot.
     *
     * @param snapshot a snapshot of the canvas
     */
    public CanvasState(WritableImage snapshot) {
        this.snapshot = snapshot;
    }

    /**
     * Returns the stored canvas snapshot.
     *
     * @return the snapshot of the canvas
     */
    public WritableImage getSnapshot() {
        return snapshot;
    }
}
