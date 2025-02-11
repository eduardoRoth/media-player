package dev.eduardoroth.mediaplayer;

import android.content.ComponentName;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.media3.common.C;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.google.common.util.concurrent.ListenableFuture;
import dev.eduardoroth.mediaplayer.models.AndroidOptions;
import dev.eduardoroth.mediaplayer.models.ExtraOptions;
import dev.eduardoroth.mediaplayer.models.MediaPlayerNotification;
import dev.eduardoroth.mediaplayer.models.PlacementOptions;
import dev.eduardoroth.mediaplayer.state.MediaPlayerState;
import dev.eduardoroth.mediaplayer.state.MediaPlayerStateProvider;
import java.io.File;

public class MediaPlayer {

    public static long VIDEO_STEP = 10000;
    private final AppCompatActivity _currentActivity;

    MediaPlayer(AppCompatActivity currentActivity) {
        _currentActivity = currentActivity;
    }

    @OptIn(markerClass = UnstableApi.class)
    public void create(
        PluginCall call,
        String playerId,
        String url,
        PlacementOptions placement,
        AndroidOptions android,
        ExtraOptions extra
    ) {
        Bundle connectionHints = new Bundle();
        connectionHints.putString("playerId", playerId);
        connectionHints.putString("videoUrl", url);
        connectionHints.putSerializable("placement", placement);
        connectionHints.putSerializable("android", android);
        extra.poster = extra.poster != null ? getFinalPath(extra.poster) : null;
        connectionHints.putSerializable("extra", extra);

        SessionToken sessionToken = new SessionToken(
            _currentActivity.getApplicationContext(),
            new ComponentName(_currentActivity.getApplicationContext(), MediaPlayerService.class)
        );
        ListenableFuture<MediaController> futureController = new MediaController.Builder(
            _currentActivity.getApplicationContext(),
            sessionToken
        )
            .setConnectionHints(connectionHints)
            .buildAsync();

        futureController.addListener(
            () -> {
                JSObject ret = new JSObject();
                ret.put("method", "create");
                try {
                    _currentActivity
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.MediaPlayerFragmentContainerView, new MediaPlayerContainer(futureController.get(), playerId), playerId)
                        .commit();
                    ret.put("result", true);
                    ret.put("value", playerId);
                    call.resolve(ret);
                } catch (Exception | Error futureError) {
                    ret.put("result", false);
                    ret.put("message", "An error occurred while creating player with id " + playerId + ".\n" + futureError.getMessage());
                    call.resolve(ret);
                }
            },
            _currentActivity.getMainExecutor()
        );
    }

    public void play(PluginCall call, String playerId) {
        JSObject ret = new JSObject();
        ret.put("method", "play");
        try {
            MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
            playerState.mediaController.get().play();
            ret.put("result", true);
            ret.put("value", true);
        } catch (Error | Exception err) {
            ret.put("result", false);
            ret.put("message", "Player not found. " + err.getMessage());
        }
        call.resolve(ret);
    }

    public void pause(PluginCall call, String playerId) {
        JSObject ret = new JSObject();
        ret.put("method", "pause");
        try {
            MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
            playerState.mediaController.get().pause();
            ret.put("result", true);
            ret.put("value", true);
        } catch (Error | Exception err) {
            ret.put("result", false);
            ret.put("message", "Player not found. " + err.getMessage());
        }
        call.resolve(ret);
    }

    public void getDuration(PluginCall call, String playerId) {
        JSObject ret = new JSObject();
        ret.put("method", "getDuration");
        try {
            MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
            long duration = playerState.mediaController.get().getDuration();
            ret.put("result", true);
            ret.put("value", duration == C.TIME_UNSET ? 0 : (duration / 1000));
        } catch (Error | Exception err) {
            ret.put("result", false);
            ret.put("message", "Player not found. " + err.getMessage());
        }
        call.resolve(ret);
    }

    public void getCurrentTime(PluginCall call, String playerId) {
        JSObject ret = new JSObject();
        ret.put("method", "getCurrentTime");
        try {
            MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
            long currentTime = playerState.mediaController.get().getCurrentPosition();
            ret.put("result", true);
            ret.put("value", currentTime == C.TIME_UNSET ? 0 : (currentTime / 1000));
        } catch (Error | Exception err) {
            ret.put("result", false);
            ret.put("message", "Player not found. " + err.getMessage());
        }
        call.resolve(ret);
    }

    public void setCurrentTime(PluginCall call, String playerId, long time) {
        JSObject ret = new JSObject();
        ret.put("method", "setCurrentTime");
        try {
            MediaController controller = MediaPlayerStateProvider.getState(playerId).mediaController.get();

            long duration = controller.getDuration();
            long currentTime = controller.getCurrentPosition();
            long seekPosition = currentTime == C.TIME_UNSET
                ? 0
                : Math.min(Math.max(0, time * 1000), duration == C.TIME_UNSET ? 0 : duration);
            controller.seekTo(seekPosition);
            ret.put("result", true);
            ret.put("value", seekPosition);
        } catch (Error | Exception err) {
            ret.put("result", false);
            ret.put("message", "Player not found. " + err.getMessage());
        }
        call.resolve(ret);
    }

    public void isPlaying(PluginCall call, String playerId) {
        JSObject ret = new JSObject();
        ret.put("method", "isPlaying");
        try {
            MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
            ret.put("result", true);
            ret.put("value", playerState.mediaController.get().isPlaying());
        } catch (Error | Exception err) {
            ret.put("result", false);
            ret.put("message", "Player not found. " + err.getMessage());
        }
        call.resolve(ret);
    }

    public void isMuted(PluginCall call, String playerId) {
        JSObject ret = new JSObject();
        ret.put("method", "isMuted");
        try {
            MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
            ret.put("result", true);
            ret.put("value", playerState.mediaController.get().getVolume() == 0);
        } catch (Error | Exception err) {
            ret.put("result", false);
            ret.put("message", "Player not found. " + err.getMessage());
        }
        call.resolve(ret);
    }

    public void mute(PluginCall call, String playerId) {
        JSObject ret = new JSObject();
        ret.put("method", "mute");
        try {
            MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
            playerState.mediaController.get().setVolume(0);
            ret.put("result", true);
            ret.put("value", true);
        } catch (Error | Exception err) {
            ret.put("result", false);
            ret.put("message", "Player not found. " + err.getMessage());
        }
        call.resolve(ret);
    }

    public void getVolume(PluginCall call, String playerId) {
        JSObject ret = new JSObject();
        ret.put("method", "getVolume");
        try {
            MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
            ret.put("result", true);
            ret.put("value", playerState.mediaController.get().getVolume());
        } catch (Error | Exception err) {
            ret.put("result", false);
            ret.put("message", "Player not found. " + err.getMessage());
        }
        call.resolve(ret);
    }

    public void setVolume(PluginCall call, String playerId, Double volume) {
        JSObject ret = new JSObject();
        ret.put("method", "setVolume");
        try {
            MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
            playerState.mediaController.get().setVolume(volume.floatValue());
            ret.put("result", true);
            ret.put("value", volume);
        } catch (Error | Exception err) {
            ret.put("result", false);
            ret.put("message", "Player not found. " + err.getMessage());
        }
        call.resolve(ret);
    }

    public void getRate(PluginCall call, String playerId) {
        JSObject ret = new JSObject();
        ret.put("method", "getRate");
        try {
            MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
            ret.put("result", true);
            ret.put("value", playerState.mediaController.get().getPlaybackParameters().speed);
        } catch (Error | Exception err) {
            ret.put("result", false);
            ret.put("message", "Player not found. " + err.getMessage());
        }
        call.resolve(ret);
    }

    public void setRate(PluginCall call, String playerId, Double rate) {
        JSObject ret = new JSObject();
        ret.put("method", "setRate");
        try {
            MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
            playerState.mediaController.get().setPlaybackSpeed(rate.floatValue());
            ret.put("result", true);
            ret.put("value", rate);
        } catch (Error | Exception err) {
            ret.put("result", false);
            ret.put("message", "Player not found. " + err.getMessage());
        }
        call.resolve(ret);
    }

    public void remove(PluginCall call, String playerId) {
        JSObject ret = new JSObject();
        ret.put("method", "remove");
        try {
            MediaPlayerState state = MediaPlayerStateProvider.getState(playerId);
            state.mediaController.get().stop();

            // Find and remove the fragment.
            Fragment playerFragment = _currentActivity.getSupportFragmentManager().findFragmentByTag(playerId);
            if (playerFragment != null) {
                _currentActivity.getSupportFragmentManager()
                    .beginTransaction()
                    .remove(playerFragment)
                    .commitNow();
            }
    
            // Clear the stored state so that a new creation doesn't reuse the old state.
            MediaPlayerStateProvider.removeState(playerId);
    
            // Post a notification that the player was removed.
            MediaPlayerNotificationCenter.post(
                MediaPlayerNotification.create(playerId, MediaPlayerNotificationCenter.NOTIFICATION_TYPE.MEDIA_PLAYER_REMOVED)
                    .build()
            );
            ret.put("result", true);
            ret.put("value", playerId);
        } catch (Error | Exception err) {
            ret.put("result", false);
            ret.put("message", "Player not found. " + err.getMessage());
        }
        call.resolve(ret);
    }




    public void removeAll(PluginCall call) {
        List<Fragment> fragments = new ArrayList<>(_currentActivity.getSupportFragmentManager().getFragments());
        for (Fragment fragment : fragments) {
            String playerId = fragment.getTag();
            _currentActivity.getSupportFragmentManager()
                .beginTransaction()
                .remove(fragment)
                .commitNow();
            try {
                MediaPlayerState playerState = MediaPlayerStateProvider.getState(playerId);
                playerState.mediaController.get().stop();
            } catch (Error | Exception ignored) {
                // Ignore errors for missing state.
            }
            MediaPlayerNotificationCenter.post(
                MediaPlayerNotification.create(playerId, MediaPlayerNotificationCenter.NOTIFICATION_TYPE.MEDIA_PLAYER_REMOVED)
                    .build()
            );
            MediaPlayerStateProvider.removeState(playerId);
        }
        JSObject ret = new JSObject();
        ret.put("method", "removeAll");
        ret.put("result", true);
        ret.put("value", "[]");
        call.resolve(ret);
    }

    private String getFinalPath(String url) {
        if (url == null) {
            return null;
        }
        String path = null;
        if (url.startsWith("file:///")) {
            return url;
        } else if (url.startsWith("application") || url.contains("_capacitor_file_")) {
            String filesDir = _currentActivity.getFilesDir() + "/";
            path = filesDir + url.substring(url.lastIndexOf("files/") + "files/".length());
            File file = new File(path);
            if (!file.exists()) {
                Log.e("Media Player", "File not found");
                path = null;
            }
        } else if (url.contains("public/assets")) {
            path = "/android_asset/" + url;
        } else if (url.startsWith("http")) {
            path = url;
        }
        return path;
    }
}
