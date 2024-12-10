package dev.eduardoroth.mediaplayer;

import android.Manifest;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;

import android.content.Context;
import android.util.DisplayMetrics;

import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;

import org.json.JSONException;

import java.io.File;

import dev.eduardoroth.mediaplayer.models.AndroidOptions;
import dev.eduardoroth.mediaplayer.models.ExtraOptions;
import dev.eduardoroth.mediaplayer.models.SubtitleOptions;
import dev.eduardoroth.mediaplayer.utilities.NotificationHelpers;
import dev.eduardoroth.mediaplayer.utilities.RunnableHelper;

@UnstableApi
@CapacitorPlugin(
        name = "MediaPlayer",
        permissions = {
                @Permission(alias = "mediaVideo",
                        strings = {Manifest.permission.READ_MEDIA_VIDEO}),
                @Permission(alias = "publicStorage",
                        strings = {Manifest.permission.READ_EXTERNAL_STORAGE})
        })
public class MediaPlayerPlugin extends Plugin {
    private MediaPlayer implementation;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void load() {
        addObserversToNotificationCenter();
        bridge.getActivity().getSupportFragmentManager();
        implementation = new MediaPlayer(bridge.getActivity());
    }

    @OptIn(markerClass = UnstableApi.class)
    @PluginMethod
    public void create(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "create");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        String url = call.getString("url");
        if (url == null) {
            JSObject ret = new JSObject();
            ret.put("method", "create");
            ret.put("result", false);
            ret.put("message", "Must provide a URL");
            call.resolve(ret);
            return;
        }

        JSObject androidOptions = call.getObject("android");
        JSObject extraOptions = call.getObject("extra");
        JSObject subtitleOptions = extraOptions.getJSObject("subtitles");

        DisplayMetrics metrics = bridge.getContext().getResources().getDisplayMetrics();

        Integer paramTop = androidOptions.getInteger("top", null);
        Integer paramStart = androidOptions.getInteger("start", null);
        Integer paramWidth = androidOptions.getInteger("width", null);
        Integer paramHeight = androidOptions.getInteger("height", null);

        AndroidOptions android = new AndroidOptions(
                androidOptions.optBoolean("enableChromecast", true),
                androidOptions.optBoolean("enablePiP", true),
                androidOptions.optBoolean("enableBackgroundPlay", true),
                androidOptions.optBoolean("openInFullscreen", false),
                androidOptions.optBoolean("automaticallyEnterPiP", false),
                androidOptions.optBoolean("fullscreenOnLandscape", true),
                paramTop == null ? 0 : (int) (paramTop * metrics.scaledDensity),
                paramStart == null ? 0 : (int) (paramStart * metrics.scaledDensity),
                paramWidth == null ? metrics.widthPixels : (int) (paramWidth * metrics.scaledDensity),
                paramHeight == null ? ((paramWidth == null ? metrics.widthPixels : (int) (paramWidth * metrics.scaledDensity)) * 9 / 16) : (int) (paramHeight * metrics.scaledDensity)
        );

        SubtitleOptions subtitles = null;
        if (subtitleOptions != null) {
            double fontSize = Double.parseDouble("12");
            try {
                fontSize = subtitleOptions.getDouble("fontSize");
            } catch (NullPointerException | JSONException ignored) {
            }
            subtitles = new SubtitleOptions(
                    subtitleOptions.getString("url", null),
                    subtitleOptions.getString("language", "English"),
                    subtitleOptions.getString("foregroundColor", null),
                    subtitleOptions.getString("backgroundColor", null),
                    fontSize
            );
        }

        double rate = 1;
        try {
            rate = extraOptions.getDouble("rate");
        } catch (JSONException ignored) {
        }

        ExtraOptions extra = new ExtraOptions(
                extraOptions.getString("title"),
                extraOptions.getString("subtitle"),
                getFilePath(extraOptions.getString("poster", null)),
                extraOptions.getString("artist"),
                rate,
                subtitles,
                extraOptions.optBoolean("autoPlayWhenReady", false),
                extraOptions.optBoolean("loopOnEnd", false),
                extraOptions.optBoolean("showControls", true),
                extraOptions.getJSObject("headers")
        );
        implementation.create(call, playerId, url, android, extra);
    }

    @PluginMethod
    public void play(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "play");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.play(call, playerId);
                    }
                }
        );
    }

    @PluginMethod
    public void pause(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "pause");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.pause(call, playerId);
                    }
                }
        );
    }

    @PluginMethod
    public void getDuration(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "getDuration");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.getDuration(call, playerId);
                    }
                }
        );
    }

    @PluginMethod
    public void getCurrentTime(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "getCurrentTime");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.getCurrentTime(call, playerId);
                    }
                }
        );
    }

    @PluginMethod
    public void setCurrentTime(final PluginCall call) {
        String playerId = call.getString("playerId");
        Double time = call.getDouble("time");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "setCurrentTime");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        if (time == null) {
            JSObject ret = new JSObject();
            ret.put("method", "setCurrentTime");
            ret.put("result", false);
            ret.put("message", "Must provide a time");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.setCurrentTime(call, playerId, time);
                    }
                }
        );
    }

    @PluginMethod
    public void isPlaying(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "isPlaying");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.isPlaying(call, playerId);
                    }
                }
        );
    }

    @PluginMethod
    public void isMuted(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "isMuted");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.isMuted(call, playerId);
                    }
                }
        );
    }

    @PluginMethod
    public void setVisibilityBackgroundForPiP(final PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("method", "setVisibilityBackgroundForPiP");
        ret.put("result", false);
        ret.put("message", "Method setVisibilityBackgroundForPiP not implemented for Android");
        call.resolve(ret);
        return;
    }

    @PluginMethod
    public void mute(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "mute");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.mute(call, playerId);
                    }
                }
        );
    }

    @PluginMethod
    public void getVolume(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "getVolume");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.getVolume(call, playerId);
                    }
                }
        );
    }

    @PluginMethod
    public void setVolume(final PluginCall call) {
        String playerId = call.getString("playerId");
        Double volume = call.getDouble("volume");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "setVolume");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        if (volume == null) {
            JSObject ret = new JSObject();
            ret.put("method", "setVolume");
            ret.put("result", false);
            ret.put("message", "Must provide a volume");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.setVolume(call, playerId, volume);
                    }
                }
        );
    }

    @PluginMethod
    public void getRate(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "getRate");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.getRate(call, playerId);
                    }
                }
        );
    }

    @PluginMethod
    public void setRate(final PluginCall call) {
        String playerId = call.getString("playerId");
        Double rate = call.getDouble("rate");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "setRate");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        if (rate == null) {
            JSObject ret = new JSObject();
            ret.put("method", "setRate");
            ret.put("result", false);
            ret.put("message", "Must provide a rate");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.setRate(call, playerId, rate);
                    }
                }
        );
    }

    @PluginMethod
    public void remove(final PluginCall call) {
        String playerId = call.getString("playerId");
        if (playerId == null) {
            JSObject ret = new JSObject();
            ret.put("method", "remove");
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.remove(call, playerId);
                    }
                }
        );
    }

    @PluginMethod
    public void removeAll(final PluginCall call) {
        bridge.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        implementation.removeAll(call);
                    }
                }
        );
    }

    private String getFilePath(String url) {
        if (url == null) {
            return null;
        }
        if (url.startsWith("file:///")) {
            return url;
        }
        String path = null;
        String http = url.substring(0, 4);
        if (http.equals("http")) {
            path = url;
        } else {
            if (url.startsWith("application")) {
                String filesDir = getContext().getFilesDir() + "/";
                path = filesDir + url.substring(url.lastIndexOf("files/") + 6);
                File file = new File(path);
                if (!file.exists()) {
                    path = null;
                }
            } else if (url.contains("assets")) {
                path = "file:///android_asset/" + url;
            }
        }
        return path;
    }

    private void addObserversToNotificationCenter() {
        NotificationHelpers.defaultCenter()
                .addMethodForNotification("MediaPlayer:Ready", new RunnableHelper() {
                    @Override
                    public void run() {
                        JSObject data = new JSObject();
                        data.put("playerId", this.getInfo().get("playerId"));
                        notifyListeners("MediaPlayer:Ready", data);
                    }
                });
        NotificationHelpers.defaultCenter()
                .addMethodForNotification("MediaPlayer:Play", new RunnableHelper() {
                    @Override
                    public void run() {
                        JSObject data = new JSObject();
                        data.put("playerId", this.getInfo().get("playerId"));
                        notifyListeners("MediaPlayer:Play", data);
                    }
                });
        NotificationHelpers.defaultCenter()
                .addMethodForNotification("MediaPlayer:Pause", new RunnableHelper() {
                    @Override
                    public void run() {
                        JSObject data = new JSObject();
                        data.put("playerId", this.getInfo().get("playerId"));
                        notifyListeners("MediaPlayer:Pause", data);
                    }
                });
        NotificationHelpers.defaultCenter()
                .addMethodForNotification("MediaPlayer:Ended", new RunnableHelper() {
                    @Override
                    public void run() {
                        JSObject data = new JSObject();
                        data.put("playerId", this.getInfo().get("playerId"));
                        notifyListeners("MediaPlayer:Ended", data);
                    }
                });
        NotificationHelpers.defaultCenter()
                .addMethodForNotification("MediaPlayer:Removed", new RunnableHelper() {
                    @Override
                    public void run() {
                        JSObject data = new JSObject();
                        data.put("playerId", this.getInfo().get("playerId"));
                        notifyListeners("MediaPlayer:Removed", data);
                    }
                });
        NotificationHelpers.defaultCenter()
                .addMethodForNotification("MediaPlayer:Seeked", new RunnableHelper() {
                    @Override
                    public void run() {
                        JSObject data = new JSObject();
                        data.put("playerId", this.getInfo().get("playerId"));
                        data.put("previousTime", this.getInfo().get("previousTime"));
                        data.put("newTime", this.getInfo().get("newTime"));
                        notifyListeners("MediaPlayer:Seeked", data);
                    }
                });
        NotificationHelpers.defaultCenter()
                .addMethodForNotification("MediaPlayer:TimeUpdate", new RunnableHelper() {
                    @Override
                    public void run() {
                        JSObject data = new JSObject();
                        data.put("playerId", this.getInfo().get("playerId"));
                        data.put("currentTime", this.getInfo().get("currentTime"));
                        notifyListeners("MediaPlayer:TimeUpdate", data);
                    }
                });
        NotificationHelpers.defaultCenter()
                .addMethodForNotification("MediaPlayer:FullScreen", new RunnableHelper() {
                    @Override
                    public void run() {
                        JSObject data = new JSObject();
                        data.put("playerId", this.getInfo().get("playerId"));
                        data.put("isInFullScreen", this.getInfo().get("isInFullScreen"));
                        notifyListeners("MediaPlayer:FullScreen", data);
                    }
                });
        NotificationHelpers.defaultCenter()
                .addMethodForNotification("MediaPlayer:PictureInPicture", new RunnableHelper() {
                    @Override
                    public void run() {
                        JSObject data = new JSObject();
                        data.put("playerId", this.getInfo().get("playerId"));
                        data.put("isInPictureInPicture", this.getInfo().get("isInPictureInPicture"));
                        notifyListeners("MediaPlayer:PictureInPicture", data);
                    }
                });
        NotificationHelpers.defaultCenter()
                .addMethodForNotification("MediaPlayer:isPlayingInBackground", new RunnableHelper() {
                    @Override
                    public void run() {
                        JSObject data = new JSObject();
                        data.put("playerId", this.getInfo().get("playerId"));
                        data.put("isPlayingInBackground", this.getInfo().get("isPlayingInBackground"));
                        notifyListeners("MediaPlayer:isPlayingInBackground", data);
                    }
                });
    }

}
