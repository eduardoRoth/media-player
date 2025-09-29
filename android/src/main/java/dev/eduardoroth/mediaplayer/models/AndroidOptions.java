package dev.eduardoroth.mediaplayer.models;

import java.io.Serializable;

public class AndroidOptions implements Serializable {

    public boolean enableChromecast;
    public boolean enablePiP;
    public boolean enableBackgroundPlay;
    public boolean openInFullscreen;
    public boolean automaticallyEnterPiP;
    public boolean fullscreenOnLandscape;
    public boolean stopOnTaskRemoved;

    // Default constructor with sensible defaults for Capacitor plugin
    public AndroidOptions() {
        this.enableChromecast = false;
        this.enablePiP = false;
        this.enableBackgroundPlay = false;
        this.openInFullscreen = false;
        this.automaticallyEnterPiP = false;
        this.fullscreenOnLandscape = false;
        this.stopOnTaskRemoved = true; // Default to true for better memory management
    }

    public AndroidOptions(
        boolean enableChromecast,
        boolean enablePiP,
        boolean enableBackgroundPlay,
        boolean openInFullscreen,
        boolean automaticallyEnterPiP,
        boolean fullscreenOnLandscape,
        boolean stopOnTaskRemoved
    ) {
        this.enableChromecast = enableChromecast;
        this.enablePiP = enablePiP;
        this.enableBackgroundPlay = enableBackgroundPlay;
        this.openInFullscreen = openInFullscreen;
        this.automaticallyEnterPiP = automaticallyEnterPiP;
        this.fullscreenOnLandscape = fullscreenOnLandscape;
        this.stopOnTaskRemoved = stopOnTaskRemoved;
    }
}
