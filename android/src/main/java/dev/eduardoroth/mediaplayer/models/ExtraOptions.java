package dev.eduardoroth.mediaplayer.models;
import android.graphics.drawable.Drawable;

import com.getcapacitor.JSObject;

import java.io.Serializable;

public class ExtraOptions implements Serializable {
    public String title;
    public String subtitle;
    public String poster;
    public String artist;
    public double rate;
    public SubtitleOptions subtitles;
    public boolean autoPlayWhenReady;
    public boolean loopOnEnd;
    public boolean showControls;
    public JSObject headers;

    public ExtraOptions(String title, String subtitle, String poster, String artist, double rate, SubtitleOptions subtitles, boolean autoPlayWhenReady, boolean loopOnEnd, boolean showControls, JSObject headers) {
        this.title = title;
        this.subtitle = subtitle;
        this.poster = poster;
        this.artist = artist;
        this.rate = rate;
        this.subtitles = subtitles;
        this.autoPlayWhenReady = autoPlayWhenReady;
        this.loopOnEnd = loopOnEnd;
        this.showControls = showControls;
        this.headers = headers;
    }
}
