package dev.eduardoroth.mediaplayer.state;

import androidx.lifecycle.LifecycleOwner;

import java.util.HashMap;

public class MediaPlayerStateProvider {
    private static final MediaPlayerStateProvider _provider = new MediaPlayerStateProvider();
    private final HashMap<String, MediaPlayerState> _instances = new HashMap<>();

    private MediaPlayerStateProvider() {
    }

    public static MediaPlayerState getState(String playerId) {
        if (!_provider._instances.containsKey(playerId)) {
            throw new Error("No State found for playerId " + playerId);
        }
        return _provider._instances.get(playerId);
    }
    public static MediaPlayerState getState(String playerId, LifecycleOwner owner){
        if (!_provider._instances.containsKey(playerId)) {
            MediaPlayerState playerState = new MediaPlayerState(owner);
            _provider._instances.put(playerId, playerState);
        }
        return MediaPlayerStateProvider.getState(playerId);
    }
    public static void clearState(String playerId){
        _provider._instances.remove(playerId);
    }
}
