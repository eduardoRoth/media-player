package dev.eduardoroth.mediaplayer.utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;

import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.dash.DashMediaSource;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.smoothstreaming.SsMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.MediaSourceFactory;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.ui.PlayerView;

import org.json.JSONException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.eduardoroth.mediaplayer.models.ExtraOptions;

@SuppressLint("UnsafeOptInUsageError")
public class MediaHelpers {

    private final String videoPath;
    private final Uri videoUri;
    private Uri internalUri;
    private final ExtraOptions extra;
    private final Context context;
    private final PlayerView playerView;
    private final List<String> supportedFormat = Arrays.asList(
            new String[] { "mp4", "webm", "ogv", "3gp", "flv", "dash", "mpd", "m3u8", "ism", "ytube", "" }
    );
    private final DataSource.Factory dataSourceFactory;

    public MediaHelpers(String videoPath, ExtraOptions extra, Context context, PlayerView playerView) {
        this.videoPath = videoPath;
        this.videoUri = Uri.parse(videoPath);
        this.extra = extra;
        this.context = context;
        this.playerView = playerView;
        this.dataSourceFactory = new DefaultDataSource.Factory(context);
    }

    public ProgressiveMediaSource.Factory getAssetFactory(){
        return new ProgressiveMediaSource.Factory(dataSourceFactory);
    }

    public MediaSource.Factory getHttpFactory(){
        DefaultHttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory();
        httpDataSourceFactory.setUserAgent("MediaPlayer-ExoPlayer");
        httpDataSourceFactory.setConnectTimeoutMs(DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS);
        httpDataSourceFactory.setReadTimeoutMs(1800000);
        httpDataSourceFactory.setAllowCrossProtocolRedirects(true);

        if (extra.headers != null && extra.headers.length() > 0) {
            Map<String, String> headersMap = new HashMap<String, String>();
            for (int i = 0; i < extra.headers.names().length(); i++) {
                try {
                    headersMap.put(extra.headers.names().getString(i), extra.headers.get(extra.headers.names().getString(i)).toString());
                } catch (JSONException | NullPointerException ignored) {
                }
            }
            httpDataSourceFactory.setDefaultRequestProperties(headersMap);
        }

        DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(context, httpDataSourceFactory);
        switch (getVideoType(videoUri)) {
            case "dash":
            case "mpd":
                return new DashMediaSource.Factory(dataSourceFactory);
            case "m3u8":
                return new HlsMediaSource.Factory(dataSourceFactory);
            case "ism":
                return new SsMediaSource.Factory(dataSourceFactory);
            case "mp4":
            case "webm":
            case "ogv":
            case "3gp":
            case "flv":
            case "":
            default:
                return new ProgressiveMediaSource.Factory(dataSourceFactory);
        }
    }

    /**
     * Get video Type from Uri
     *
     * @param uri
     * @return video type
     */
    private String getVideoType(Uri uri) {
        String ret = null;
        Object obj = uri.getLastPathSegment();
        String lastSegment = (obj == null) ? "" : uri.getLastPathSegment();
        for (String type : supportedFormat) {
            if (ret != null) break;
            if (!lastSegment.isEmpty() && lastSegment.contains(type)) ret = type;
            if (ret == null) {
                List<String> segments = uri.getPathSegments();
                if (!segments.isEmpty()) {
                    String segment;
                    if (segments.get(segments.size() - 1).equals("manifest")) {
                        segment = segments.get(segments.size() - 2);
                    } else {
                        segment = segments.get(segments.size() - 1);
                    }
                    for (String sType : supportedFormat) {
                        if (segment.contains(sType)) {
                            ret = sType;
                            break;
                        }
                    }
                }
            }
        }
        ret = (ret != null) ? ret : "";
        return ret;
    }
}
