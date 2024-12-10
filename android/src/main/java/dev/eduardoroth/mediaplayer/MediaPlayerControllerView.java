package dev.eduardoroth.mediaplayer;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.CaptionStyleCompat;
import androidx.media3.ui.PlayerView;
import androidx.mediarouter.app.MediaRouteButton;

import com.google.android.gms.cast.framework.CastButtonFactory;

import java.io.FileNotFoundException;

import dev.eduardoroth.mediaplayer.models.AndroidOptions;
import dev.eduardoroth.mediaplayer.models.ExtraOptions;
import dev.eduardoroth.mediaplayer.state.MediaPlayerState;
import dev.eduardoroth.mediaplayer.state.MediaPlayerStateProvider;

@UnstableApi
public class MediaPlayerControllerView extends Fragment {
    public final String playerId;
    private final Uri url;
    private final AndroidOptions android;
    private final ExtraOptions extra;
    private final MediaPlayerState mediaPlayerState;

    private PlayerView playerView;

    private LinearLayout rightButtons;
    private MediaRouteButton castButton;
    private ImageButton pipButton;
    private ImageButton fullscreenToggle;
    private Drawable artwork;

    private final DisplayMetrics displayMetrics = new DisplayMetrics();

    public MediaPlayerControllerView(String playerId, Uri url, AndroidOptions android, ExtraOptions extra) {
        mediaPlayerState = MediaPlayerStateProvider.getState(playerId);
        this.playerId = playerId;
        this.url = url;
        this.android = android;
        this.extra = extra;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mediaPlayerState.canUsePiP.set(android.enablePiP && requireContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE));

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
                    }*/

                    /*getParentFragmentManager().beginTransaction()
                    getChildFragmentManager().getFragment(null, playerId);
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.add(myDialogFragment, MyDialogFragment.TAG);
                    transaction.commitAllowingStateLoss();
                    fragmentManager.executePendingTransactions();*/

                    //startActivity(mediaPlayerIntent);

                    mediaPlayerState.fullscreenState.set(MediaPlayerState.UI_STATE.ACTIVE);

                }
                case WILL_EXIT -> {
                    /*DialogFragment playerView = (DialogFragment) getParentFragmentManager().findFragmentByTag(playerId);
                    if (playerView != null) {
                        playerView.setShowsDialog(true);
                        getParentFragmentManager().beginTransaction().hide(playerView).add(0, playerView, playerId).commit();
                    }*/
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
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View fragmentView = inflater.inflate(R.layout.fragment_media_view, container, false);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(android.width, android.height);
        params.topMargin = android.top;
        params.setMarginStart(android.start);
        fragmentView.setLayoutParams(params);

        playerView = fragmentView.findViewById(R.id.MediaPlayerHostPlayer);
        playerView.setFocusableInTouchMode(true);
        playerView.bringToFront();

        playerView.findViewById(androidx.media3.ui.R.id.exo_repeat_toggle).setVisibility(View.GONE);
        playerView.findViewById(androidx.media3.ui.R.id.exo_fullscreen).setVisibility(View.GONE);
        playerView.findViewById(androidx.media3.ui.R.id.exo_minimal_fullscreen).setVisibility(View.GONE);

        rightButtons = playerView.findViewById(R.id.right_buttons);

        pipButton = rightButtons.findViewById(R.id.pip_button);
        if (mediaPlayerState.canUsePiP.get()) {
            pipButton.setVisibility(View.VISIBLE);
            pipButton.setOnClickListener(view -> mediaPlayerState.pipState.set(MediaPlayerState.UI_STATE.WILL_ENTER));
        }

        fullscreenToggle = rightButtons.findViewById(R.id.toggle_fullscreen);
        fullscreenToggle.setOnClickListener(view -> {
            switch (mediaPlayerState.fullscreenState.get()) {
                case ACTIVE ->
                        mediaPlayerState.fullscreenState.set(MediaPlayerState.UI_STATE.WILL_EXIT);
                case INACTIVE ->
                        mediaPlayerState.fullscreenState.set(MediaPlayerState.UI_STATE.WILL_ENTER);
            }
        });

        castButton = rightButtons.findViewById(R.id.cast_button);
        if (android.enableChromecast) {
            CastButtonFactory.setUpMediaRouteButton(requireContext(), castButton);
        }

        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);
        playerView.setControllerAutoShow(extra.showControls);
        playerView.setControllerHideOnTouch(true);
        playerView.setControllerShowTimeoutMs(2500);

        playerView.setControllerVisibilityListener((PlayerView.ControllerVisibilityListener) visibility -> rightButtons.setVisibility(visibility));

        if (artwork != null) {
            try {
                artwork = Drawable.createFromStream(requireContext().getContentResolver().openInputStream(Uri.parse(extra.poster)), extra.poster);
            } catch (FileNotFoundException ignored) {
            }
            playerView.setDefaultArtwork(artwork);
            playerView.setArtworkDisplayMode(PlayerView.ARTWORK_DISPLAY_MODE_FILL);
        }

        playerView.setShowPreviousButton(false);
        playerView.setShowNextButton(false);
        playerView.setUseController(extra.showControls);
        playerView.setControllerAnimationEnabled(extra.showControls);
        playerView.setImageDisplayMode(PlayerView.IMAGE_DISPLAY_MODE_FIT);
        playerView.setShowPlayButtonIfPlaybackIsSuppressed(true);

        if (playerView.getSubtitleView() != null && extra.subtitles != null) {
            playerView.getSubtitleView().setStyle(new CaptionStyleCompat(extra.subtitles.settings.foregroundColor, extra.subtitles.settings.backgroundColor, Color.TRANSPARENT, CaptionStyleCompat.EDGE_TYPE_NONE, Color.WHITE, null));
            playerView.getSubtitleView().setFixedTextSize(TypedValue.COMPLEX_UNIT_DIP, extra.subtitles.settings.fontSize.floatValue());
        }

        playerView.setOnKeyListener((eventContainer, keyCode, keyEvent) -> {
            Player activePlayer = playerView.getPlayer();
            if (activePlayer != null && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                long duration = activePlayer.getDuration();
                long videoPosition = activePlayer.getCurrentPosition();
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        if (videoPosition < duration - MediaPlayer.VIDEO_STEP) {
                            activePlayer.seekTo(videoPosition + MediaPlayer.VIDEO_STEP);
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        if (videoPosition - MediaPlayer.VIDEO_STEP > 0) {
                            activePlayer.seekTo(videoPosition - MediaPlayer.VIDEO_STEP);
                        } else {
                            activePlayer.seekTo(0);
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        if (activePlayer.isPlaying()) {
                            activePlayer.pause();
                        } else {
                            activePlayer.play();
                        }
                        break;
                    case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                        if (videoPosition < duration - (MediaPlayer.VIDEO_STEP * 2)) {
                            activePlayer.seekTo(videoPosition + (MediaPlayer.VIDEO_STEP * 2));
                        }
                        break;
                    case KeyEvent.KEYCODE_MEDIA_REWIND:
                        if (videoPosition - (MediaPlayer.VIDEO_STEP * 2) > 0) {
                            activePlayer.seekTo(videoPosition - (MediaPlayer.VIDEO_STEP * 2));
                        } else {
                            activePlayer.seekTo(0);
                        }
                        break;
                }
            }
            return true;
        });
        playerView.setFocusableInTouchMode(true);

        /*
        if (mediaPlayerActivityState.hostPlayer.get() == null) {
            mediaPlayerActivityState.hostPlayer.set(new MediaPlayerController(requireContext(), playerId, extra));
            mediaPlayerActivityState.hostPlayer.get().addMediaItem(new MediaPlayerMediaItem(url, extra));
        }

        playerView.setPlayer(mediaPlayerActivityState.hostPlayer.get().activePlayer);
         */

        mediaPlayerState.isPlayerReady.set(true);

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
