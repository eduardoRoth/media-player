package dev.eduardoroth.mediaplayer;

import android.net.Uri;

import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaItem.SubtitleConfiguration;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.MimeTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dev.eduardoroth.mediaplayer.models.ExtraOptions;

public class MediaPlayerMediaItem {

    private final MediaItem _mediaItem;
    private boolean _hasSubtitles = false;

    public MediaPlayerMediaItem(Uri url, ExtraOptions extra) {
        MediaMetadata.Builder movieMetadataBuilder = new MediaMetadata.Builder()
                .setTitle(extra.title)
                .setSubtitle(extra.subtitle)
                .setArtist(extra.artist)
                .setMediaType(MediaMetadata.MEDIA_TYPE_MOVIE);
        if (extra.poster != null) {
            movieMetadataBuilder.setArtworkUri(Uri.parse(extra.poster));
        }

        MediaItem.Builder mediaItemBuilder = new MediaItem.Builder()
                .setUri(url)
                .setMediaMetadata(movieMetadataBuilder.build());

        if (extra.subtitles != null && extra.subtitles.url != null) {
            List<SubtitleConfiguration> subtitlesConfig = new ArrayList<>();
            subtitlesConfig.add(getSubtitlesFactory(url, extra.subtitles.settings.language));
            mediaItemBuilder.setSubtitleConfigurations(subtitlesConfig);
            _hasSubtitles = true;
        }

        _mediaItem = mediaItemBuilder.build();
    }

    public MediaItem getMediaItem() {
        return _mediaItem;
    }

    public boolean hasSubtitles() {
        return _hasSubtitles;
    }

    private SubtitleConfiguration getSubtitlesFactory(Uri url, String language) {
        String mimeType = getMimeType(url);
        String languageLabel = Locale.forLanguageTag(language).getDisplayLanguage();
        return new SubtitleConfiguration.Builder(url)
                .setMimeType(mimeType)
                .setUri(url)
                .setId(url.toString())
                .setLabel(languageLabel)
                .setRoleFlags(C.ROLE_FLAG_CAPTION)
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                .setLanguage(language)
                .build();
    }

    private String getMimeType(Uri subtitlesUrl) {
        String lastSegment = subtitlesUrl.getLastPathSegment();
        if (lastSegment != null) {
            String extension = lastSegment.substring(lastSegment.lastIndexOf(".") + 1);
            return switch (extension) {
                case "vtt" -> MimeTypes.TEXT_VTT;
                case "srt" -> MimeTypes.APPLICATION_SUBRIP;
                case "ssa", "ass" -> MimeTypes.TEXT_SSA;
                case "ttml", "dfxp", "xml" -> MimeTypes.APPLICATION_TTML;
                default -> "";
            };
        }
        return "";
    }
}
