package dev.eduardoroth.mediaplayer;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.media3.common.Player;
import androidx.media3.ui.CaptionStyleCompat;
import androidx.media3.ui.PlayerView;

import com.google.android.gms.cast.framework.CastButtonFactory;

import java.io.FileNotFoundException;

import dev.eduardoroth.mediaplayer.state.MediaPlayerState;

public class MediaPlayerContainer extends Fragment {


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /*mediaPlayerState.canUsePiP.set(android.enablePiP && requireContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE));

        mediaPlayerState.fullscreenState.observe(state -> {
            switch (state) {
                case ACTIVE -> fullscreenToggle.setImageResource(R.drawable.ic_fullscreen_exit);
                case INACTIVE -> fullscreenToggle.setImageResource(R.drawable.ic_fullscreen_enter);
                case WILL_ENTER -> {
                    /*DialogFragment playerView = (DialogFragment) getParentFragmentManager().findFragmentByTag(playerId);
                    if (playerView != null) {
                        playerView.setShowsDialog(true);
                        getParentFragmentManager().beginTransaction().replace(0, playerView).commit();
                        //playerView.show(getParentFragmentManager(), playerId);
                    }*

                    /*getParentFragmentManager().beginTransaction()
                    getChildFragmentManager().getFragment(null, playerId);
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.add(myDialogFragment, MyDialogFragment.TAG);
                    transaction.commitAllowingStateLoss();
                    fragmentManager.executePendingTransactions();*

                    //startActivity(mediaPlayerIntent);

                    mediaPlayerState.fullscreenState.set(MediaPlayerState.UI_STATE.ACTIVE);

                }
                case WILL_EXIT -> {
                    /*DialogFragment playerView = (DialogFragment) getParentFragmentManager().findFragmentByTag(playerId);
                    if (playerView != null) {
                        playerView.setShowsDialog(true);
                        getParentFragmentManager().beginTransaction().hide(playerView).add(0, playerView, playerId).commit();
                    }*
                    mediaPlayerState.fullscreenState.set(MediaPlayerState.UI_STATE.INACTIVE);
                }
            }
        });
        mediaPlayerState.pipState.observe(state -> {
            switch (state) {
                case WILL_ENTER -> {
                    view.findViewById(R.id.MediaPlayerPipActiveImage).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.MediaPlayerPipActiveText).setVisibility(View.VISIBLE);
                }
                case WILL_EXIT -> {
                    view.findViewById(R.id.MediaPlayerPipActiveImage).setVisibility(View.GONE);
                    view.findViewById(R.id.MediaPlayerPipActiveText).setVisibility(View.GONE);
                }
                case ACTIVE -> {
                    pipButton.setVisibility(View.GONE);
                    playerView.setUseController(false);
                }
                case INACTIVE -> {
                    pipButton.setVisibility(View.VISIBLE);
                    playerView.setUseController(extra.showControls);
                }
            }
        });
        mediaPlayerState.landscapeState.observe(state -> {
            if (android.fullscreenOnLandscape) {
                switch (state) {
                    case ACTIVE -> {
                        if (mediaPlayerState.fullscreenState.get() == MediaPlayerState.UI_STATE.INACTIVE) {
                            mediaPlayerState.fullscreenState.set(MediaPlayerState.UI_STATE.WILL_ENTER);
                        }
                    }
                    case INACTIVE -> {
                        if (mediaPlayerState.fullscreenState.get() == MediaPlayerState.UI_STATE.ACTIVE) {
                            mediaPlayerState.fullscreenState.set(MediaPlayerState.UI_STATE.WILL_EXIT);
                        }
                    }
                }
            }
        });

        mediaPlayerState.canCast.observe(isCastAvailable -> {
            castButton.setVisibility(isCastAvailable ? View.VISIBLE : View.GONE);
            castButton.setEnabled(isCastAvailable);
        });
        mediaPlayerState.showSubtitles.observe(showSubtitles -> playerView.setShowSubtitleButton(showSubtitles));

        if (android.openInFullscreen) {
            mediaPlayerState.fullscreenState.set(MediaPlayerState.UI_STATE.WILL_ENTER);
        }*/
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View fragmentView = inflater.inflate(R.layout.fragment_media_view, container, false);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(android.width, android.height);
        params.topMargin = android.top;
        params.setMarginStart(android.start);
        fragmentView.setLayoutParams(params);

        return fragmentView;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        mediaPlayerState.landscapeState.set(isLandscape ? MediaPlayerState.UI_STATE.ACTIVE : MediaPlayerState.UI_STATE.INACTIVE);
        if (android.fullscreenOnLandscape && isLandscape) {
            mediaPlayerState.fullscreenState.set(MediaPlayerState.UI_STATE.WILL_ENTER);
        }
    }
}
