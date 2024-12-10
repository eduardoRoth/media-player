package dev.eduardoroth.mediaplayer.models;

import android.net.Uri;

import java.io.Serializable;

public class SubtitleOptions implements Serializable {
    public String url;
    public SubtitleSettings settings;

    public SubtitleOptions(String url, String language, String foregroundColor, String backgroundColor, Double fontSize){
        this.url = url;
        this.settings = new SubtitleSettings(language, foregroundColor, backgroundColor, fontSize);
    }
    
}
