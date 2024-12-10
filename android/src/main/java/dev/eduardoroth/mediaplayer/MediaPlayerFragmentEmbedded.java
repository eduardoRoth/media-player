package dev.eduardoroth.mediaplayer;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import dev.eduardoroth.mediaplayer.models.AndroidOptions;
import dev.eduardoroth.mediaplayer.state.MediaPlayerState;
import dev.eduardoroth.mediaplayer.state.MediaPlayerStateProvider;

@UnstableApi
public class MediaPlayerFragmentEmbedded extends Fragment {

    private final MediaPlayerFragmentPlayerView _playerFragment;
    private final AndroidOptions _android;

    public MediaPlayerFragmentEmbedded(MediaPlayerFragmentPlayerView playerFragment, AndroidOptions android) {
        _playerFragment = playerFragment;
        _android = android;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        MediaPlayerState mediaPlayerState = MediaPlayerStateProvider.getState(_playerFragment.playerId);
        mediaPlayerState.isPlayerReady.observe(state -> {
            view.findViewById(R.id.MediaPlayerFragmentPlayerLoading).setVisibility(state ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_media_player_embedded, container, false);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(_android.width, _android.height);
        params.topMargin = _android.top;
        params.setMarginStart(_android.start);
        rootView.setLayoutParams(params);

        getChildFragmentManager().beginTransaction().add(R.id.MediaPlayerFragmentEmbeddedPlayerView, _playerFragment, "embedded").addToBackStack("embedded").commit();
        return rootView;
    }
}