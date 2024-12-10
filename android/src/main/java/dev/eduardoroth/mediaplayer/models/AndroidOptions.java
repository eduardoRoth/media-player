package dev.eduardoroth.mediaplayer.models;

import java.io.Serializable;

public class AndroidOptions implements Serializable {
    public boolean enableChromecast;
    public boolean enablePiP;
    public boolean enableBackgroundPlay;
    public boolean openInFullscreen;
    public boolean automaticallyEnterPiP;
    public boolean fullscreenOnLandscape;
    public int top;
    public int start;
    public int width;
    public int height;

    public AndroidOptions(boolean enableChromecast, boolean enablePiP, boolean enableBackgroundPlay, boolean openInFullscreen, boolean automaticallyEnterPiP, boolean fullscreenOnLandscape, int top, int start, int width, int height) {
        this.enableChromecast = enableChromecast;
        this.enablePiP = enablePiP;
        this.enableBackgroundPlay = enableBackgroundPlay;
        this.openInFullscreen = openInFullscreen;
        this.automaticallyEnterPiP = automaticallyEnterPiP;
        this.fullscreenOnLandscape = fullscreenOnLandscape;

        this.top = top;
        this.start = start;
        this.width = width;
        this.height = height;
    }
}
