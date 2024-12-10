package dev.eduardoroth.mediaplayer.models;

import android.graphics.Color;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class SubtitleSettings implements Serializable {
    public String language;
    public int foregroundColor;
    public int backgroundColor;
    public Double fontSize;

    public SubtitleSettings(String language, String foregroundColor, String backgroundColor, Double fontSize) {
        this.language = language;
        this.foregroundColor = foregroundColor != null ? getColorFromRGBA(foregroundColor, Color.WHITE) : Color.WHITE;
        this.backgroundColor = backgroundColor != null ? getColorFromRGBA(backgroundColor, Color.BLACK) : Color.BLACK;
        this.fontSize = fontSize != null ? fontSize : Double.valueOf("12");
    }

    private int getColorFromRGBA(String rgbaColor, int defaultColor) {
        if (rgbaColor.length() > 4 && rgbaColor.startsWith("rgba")) {
            int ret = 0;
            String color = rgbaColor.substring(rgbaColor.indexOf("(") + 1, rgbaColor.indexOf(")"));
            List<String> colors = Arrays.asList(color.split(","));
            if (colors.size() == 4) {
                ret =
                        (Math.round(Float.parseFloat(colors.get(3).trim()) * 255) & 0xff) << 24 |
                                (Integer.parseInt(colors.get(0).trim()) & 0xff) << 16 |
                                (Integer.parseInt(colors.get(1).trim()) & 0xff) << 8 |
                                (Integer.parseInt(colors.get(2).trim()) & 0xff);
            }
            return ret;
        }
        return defaultColor;
    }
}
