package dev.eduardoroth.mediaplayer.models;

import java.io.Serializable;

public class PlacementOptions implements Serializable {

    public String videoOrientation;
    public String horizontalAlignment;
    public String verticalAlignment;

    public int height;
    public int width;

    public int horizontalMargin;
    public int verticalMargin;

    // Default constructor with sensible defaults
    public PlacementOptions() {
        this.height = 0; // Will use default height
        this.width = 0;  // Will use default width
        this.videoOrientation = "portrait";
        this.horizontalAlignment = "center";
        this.verticalAlignment = "center";
        this.horizontalMargin = 0;
        this.verticalMargin = 0;
    }

    public PlacementOptions(
        int height,
        int width,
        String videoOrientation,
        String horizontalAlignment,
        String verticalAlignment,
        int horizontalMargin,
        int verticalMargin
    ) {
        this.height = height;
        this.width = width;
        this.videoOrientation = videoOrientation;
        this.horizontalAlignment = horizontalAlignment;
        this.verticalAlignment = verticalAlignment;
        this.horizontalMargin = horizontalMargin;
        this.verticalMargin = verticalMargin;
    }
}
