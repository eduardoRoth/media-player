package dev.eduardoroth.mediaplayer.models;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import androidx.media3.common.C;
import androidx.media3.common.MediaItem.SubtitleConfiguration;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.MimeTypes;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MediaItem {

    private final androidx.media3.common.MediaItem _mediaItem;
    private boolean _hasSubtitles = false;

    public MediaItem(Uri url, ExtraOptions extra) {
        MediaMetadata.Builder movieMetadataBuilder = new MediaMetadata.Builder()
            .setTitle(extra.title)
            .setSubtitle(extra.subtitle)
            .setArtist(extra.artist)
            .setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO);
        if (extra.poster != null) {
            movieMetadataBuilder.setArtworkUri(Uri.parse(extra.poster));
        }

        androidx.media3.common.MediaItem.Builder mediaItemBuilder = new androidx.media3.common.MediaItem.Builder()
            .setUri(url)
            .setMediaMetadata(movieMetadataBuilder.build());

        if (extra.subtitles != null && extra.subtitles.url != null) {
            List<SubtitleConfiguration> subtitlesConfig = new ArrayList<>();
            subtitlesConfig.add(getSubtitlesFactory(Uri.parse(extra.subtitles.url), extra.subtitles.settings.language));
            mediaItemBuilder.setSubtitleConfigurations(subtitlesConfig);
            _hasSubtitles = true;
        }

        _mediaItem = mediaItemBuilder.build();
    }

    public androidx.media3.common.MediaItem getMediaItem() {
        return _mediaItem;
    }

    public boolean hasSubtitles() {
        return _hasSubtitles;
    }

    private SubtitleConfiguration getSubtitlesFactory(Uri url, String language) {
        String mimeType = getMimeType(url);
        String languageLabel = Locale.forLanguageTag(language).getDisplayLanguage();
        SubtitleConfiguration subtitleConfiguration = new SubtitleConfiguration.Builder(url)
            .setMimeType(mimeType)
            .setUri(url)
            .setId(url.toString())
            .setLabel(languageLabel)
            .setRoleFlags(C.ROLE_FLAG_CAPTION)
            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
            .setLanguage(language)
            .build();
        Log.d("MediaPlayer", "subtitleConfiguration: " + subtitleConfiguration);
        return subtitleConfiguration;
    }

    private String getMimeType(Uri subtitlesUrl) {
        String lastSegment = subtitlesUrl.getLastPathSegment();
        
        if (lastSegment != null) {
            String extension = lastSegment.substring(lastSegment.lastIndexOf(".") + 1);
            
            String mimeType = switch (extension) {
                case "vtt", "webvtt" -> MimeTypes.TEXT_VTT;
                case "srt" -> MimeTypes.APPLICATION_SUBRIP;
                case "ssa", "ass" -> MimeTypes.TEXT_SSA;
                case "ttml", "dfxp", "xml" -> MimeTypes.APPLICATION_TTML;
                default -> "";
            };
            return mimeType;
        }
        return "";
    }
}
