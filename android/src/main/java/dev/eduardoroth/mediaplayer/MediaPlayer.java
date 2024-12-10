package dev.eduardoroth.mediaplayer;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;

import java.util.HashMap;
import java.util.Map;

import dev.eduardoroth.mediaplayer.models.AndroidOptions;
import dev.eduardoroth.mediaplayer.models.ExtraOptions;
import dev.eduardoroth.mediaplayer.utilities.NotificationHelpers;

@UnstableApi
public class MediaPlayer {
    /**
     * Public Static Variables for Options
     */
    public static long VIDEO_STEP = 10000;

    private final AppCompatActivity _currentActivity;

    private final Map<String, MediaPlayerController> _playerControllers = new HashMap<>();

    MediaPlayer(AppCompatActivity currentActivity) {
        _currentActivity = currentActivity;
    }

    @UnstableApi
    public void create(PluginCall call, String playerId, String url, AndroidOptions android, ExtraOptions extra) {
        boolean controllerExists = _playerControllers.get(playerId) != null;
        if (controllerExists) {
            return;
        }
        /*MediaPlayerFragment existingPlayer = players.get(playerId);
        if (existingPlayer != null) {
            JSObject ret = new JSObject();
            ret.put("method", "create");
            ret.put("result", false);
            ret.put("message", "Player with id " + playerId + " is already created");
            call.resolve(ret);
            return;
        }*/

        //someActivityResultLauncher.launch(intent);
        /*ActivityResultLauncher<Intent> launcher = _currentActivity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        });

        launcher.launch(mediaPlayerIntent);*/

        //_currentActivity.startActivityIfNeeded(mediaPlayerIntent, playerId.chars().reduce(0, Integer::sum), null);

        //_currentActivity.finishActivity(playerId.chars().reduce(0, Integer::sum));

        _currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MediaPlayerController playerController = new MediaPlayerController(_currentActivity, url, playerId, android, extra);
                playerController.addMediaItem(new MediaPlayerMediaItem(Uri.parse(url), extra));
                _playerControllers.put(playerId, playerController);



                //_currentActivity.getSupportFragmentManager().beginTransaction().add(0, new MediaPlayerFragment(playerId, Uri.parse(url), android, extra), playerId).commit();
            }
        });

        //startActivity(currentActivity.getBaseContext(), mediaPlayerIntent, null);
        //currentActivity.startActivityFromChild(currentActivity, mediaPlayerIntent, layoutId, null);
        //currentActivity.finishActivity(layoutId);
        //players.put(playerId, player);

        JSObject ret = new JSObject();
        ret.put("method", "create");
        ret.put("result", true);
        ret.put("value", playerId);
        call.resolve(ret);
    }

    public void play(PluginCall call, String playerId) {
        /*MediaPlayerFragment player = players.get(playerId);
        if (player == null) {
            JSObject ret = new JSObject();
            ret.put("method", "play");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }
        player.player.play();*/
        JSObject ret = new JSObject();
        ret.put("method", "play");
        ret.put("result", true);
        ret.put("value", true);
        call.resolve(ret);
    }

    public void pause(PluginCall call, String playerId) {
        /*MediaPlayerFragment player = players.get(playerId);
        if (player == null) {
            JSObject ret = new JSObject();
            ret.put("method", "pause");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }
        player.player.pause();*/
        JSObject ret = new JSObject();
        ret.put("method", "pause");
        ret.put("result", true);
        ret.put("value", true);
        call.resolve(ret);
    }

    public void getDuration(PluginCall call, String playerId) {
        /*MediaPlayerFragment player = players.get(playerId);
        if (player == null) {
            JSObject ret = new JSObject();
            ret.put("method", "getDuration");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }*/
        JSObject ret = new JSObject();
        ret.put("method", "getDuration");
        ret.put("result", true);
        //ret.put("value", player.player.getDuration() == C.TIME_UNSET ? 0 : (player.player.getDuration() / 1000));
        call.resolve(ret);
    }

    public void getCurrentTime(PluginCall call, String playerId) {
        /*MediaPlayerFragment player = players.get(playerId);
        if (player == null) {
            JSObject ret = new JSObject();
            ret.put("method", "getCurrentTime");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }*/
        JSObject ret = new JSObject();
        ret.put("method", "getCurrentTime");
        ret.put("result", true);
        //ret.put("value", player.player.getCurrentPosition() == C.TIME_UNSET ? 0 : (player.player.getCurrentPosition() / 1000));
        call.resolve(ret);
    }

    public void setCurrentTime(PluginCall call, String playerId, Double time) {
        /*MediaPlayerFragment player = players.get(playerId);
        if (player == null) {
            JSObject ret = new JSObject();
            ret.put("method", "setCurrentTime");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }
        Double seekPosition = player.player.getCurrentPosition() == C.TIME_UNSET
                ? 0
                : Math.min(Math.max(0, time * 1000), player.player.getDuration() == C.TIME_UNSET ? 0 : player.player.getDuration());
        player.player.seekTo(seekPosition.longValue());*/
        JSObject ret = new JSObject();
        ret.put("method", "setCurrentTime");
        ret.put("result", true);
        //ret.put("value", seekPosition);
        call.resolve(ret);
    }

    public void isPlaying(PluginCall call, String playerId) {
        /*MediaPlayerFragment player = players.get(playerId);
        if (player == null) {
            JSObject ret = new JSObject();
            ret.put("method", "isPlaying");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }*/
        JSObject ret = new JSObject();
        ret.put("method", "isPlaying");
        ret.put("result", true);
        //ret.put("value", player.player.isPlaying());
        call.resolve(ret);
    }

    public void isMuted(PluginCall call, String playerId) {
        /*MediaPlayerFragment player = players.get(playerId);
        if (player == null) {
            JSObject ret = new JSObject();
            ret.put("method", "isMuted");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }*/
        JSObject ret = new JSObject();
        ret.put("method", "isMuted");
        ret.put("result", true);
        //ret.put("value", player.player.getVolume() == 0);
        call.resolve(ret);
    }

    public void mute(PluginCall call, String playerId) {
        /*MediaPlayerFragment player = players.get(playerId);
        if (player == null) {
            JSObject ret = new JSObject();
            ret.put("method", "mute");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }
        player.player.setVolume(0);*/
        JSObject ret = new JSObject();
        ret.put("method", "mute");
        ret.put("result", true);
        ret.put("value", true);
        call.resolve(ret);
    }

    public void getVolume(PluginCall call, String playerId) {
        /*MediaPlayerFragment player = players.get(playerId);
        if (player == null) {
            JSObject ret = new JSObject();
            ret.put("method", "getVolume");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }*/
        JSObject ret = new JSObject();
        ret.put("method", "getVolume");
        ret.put("result", true);
        //ret.put("value", player.player.getVolume());
        call.resolve(ret);
    }

    public void setVolume(PluginCall call, String playerId, Double volume) {
        /*MediaPlayerFragment player = players.get(playerId);
        if (player == null) {
            JSObject ret = new JSObject();
            ret.put("method", "setVolume");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }
        player.player.setVolume(volume.floatValue());*/
        JSObject ret = new JSObject();
        ret.put("method", "setVolume");
        ret.put("result", true);
        ret.put("value", volume);
        call.resolve(ret);
    }

    public void getRate(PluginCall call, String playerId) {
        /*MediaPlayerFragment player = players.get(playerId);
        if (player == null) {
            JSObject ret = new JSObject();
            ret.put("method", "getRate");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }*/
        JSObject ret = new JSObject();
        ret.put("method", "getRate");
        ret.put("result", true);
        //ret.put("value", player.player.getPlaybackParameters().speed);
        call.resolve(ret);
    }

    public void setRate(PluginCall call, String playerId, Double rate) {
        /*MediaPlayerFragment player = players.get(playerId);
        if (player == null) {
            JSObject ret = new JSObject();
            ret.put("method", "setRate");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }
        player.player.setPlaybackParameters(new PlaybackParameters(rate.floatValue(), player.player.getPlaybackParameters().pitch));*/
        JSObject ret = new JSObject();
        ret.put("method", "setRate");
        ret.put("result", true);
        ret.put("value", rate);
        call.resolve(ret);
    }

    public void remove(PluginCall call, String playerId) {
        /*MediaPlayerFragment player = players.get(playerId);
        if (player == null) {
            JSObject ret = new JSObject();
            ret.put("method", "remove");
            ret.put("result", false);
            ret.put("message", "Player not found");
            call.resolve(ret);
            return;
        }*/
        /*FragmentHelpers fragmentHelpers = new FragmentHelpers(bridge);
        ((ViewGroup) bridge.getWebView().getParent()).removeView(player.layout);
        fragmentHelpers.removeFragment(player);
        players.remove(playerId, player);
*/
        HashMap<String, Object> info = new HashMap<String, Object>();
        info.put("playerId", playerId);
        NotificationHelpers.defaultCenter().postNotification("MediaPlayer:Removed", info);
        JSObject ret = new JSObject();
        ret.put("method", "remove");
        ret.put("result", true);
        ret.put("value", playerId);
        call.resolve(ret);

    }

    public void removeAll(PluginCall call) {
        //FragmentHelpers fragmentHelpers = new FragmentHelpers(bridge);
        /*players.forEach((playerId, player) -> {
            //((ViewGroup) bridge.getWebView().getParent()).removeView(player.layout);
            //fragmentHelpers.removeFragment(player);
            HashMap<String, Object> info = new HashMap<String, Object>();
            info.put("playerId", playerId);
            NotificationHelpers.defaultCenter().postNotification("MediaPlayer:Removed", info);
        });
        players.clear();*/
        JSObject ret = new JSObject();
        ret.put("method", "removeAll");
        ret.put("result", true);
        ret.put("value", "[]");
        call.resolve(ret);
    }
}
