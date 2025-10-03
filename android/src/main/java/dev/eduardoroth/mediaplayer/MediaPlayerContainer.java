package dev.eduardoroth.mediaplayer;

import static dev.eduardoroth.mediaplayer.state.MediaPlayerState.*;

import android.app.PictureInPictureParams;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.MediaController;
import androidx.media3.ui.CaptionStyleCompat;
import androidx.media3.ui.PlayerView;
import androidx.media3.ui.SubtitleView;
import androidx.mediarouter.app.MediaRouteButton;
import com.google.android.gms.cast.framework.CastButtonFactory;
import dev.eduardoroth.mediaplayer.models.AndroidOptions;
import dev.eduardoroth.mediaplayer.models.ExtraOptions;
import dev.eduardoroth.mediaplayer.models.MediaPlayerNotification;
import dev.eduardoroth.mediaplayer.models.PlacementOptions;
import dev.eduardoroth.mediaplayer.state.MediaPlayerState;
import dev.eduardoroth.mediaplayer.state.MediaPlayerStateProvider;

public class MediaPlayerContainer extends Fragment {

    private PlacementOptions _placement;
    private AndroidOptions _android;
    private ExtraOptions _extra;
    private MediaPlayerState _mediaPlayerState;
    private MediaController _playerController;
    private String _playerId;
    private final Rect _sourceRectHint = new Rect();
    private PlayerView _embeddedPlayerView;
    private PlayerView _fullscreenPlayerView;
    private RelativeLayout _embeddedView;
    private FrameLayout _fullscreenView;

		public MediaPlayerContainer() {
			// Required empty public constructor for fragment instantiation
		}

    public MediaPlayerContainer(MediaController playerController, String playerId) {
        _playerId = playerId;
        _playerController = playerController;
    }

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        _mediaPlayerState = MediaPlayerStateProvider.getState(_playerId);
        _mediaPlayerState.mediaController.set(_playerController);
        _placement = _mediaPlayerState.placementOptions.get();
        _android = _mediaPlayerState.androidOptions.get();
        _extra = _mediaPlayerState.extraOptions.get();
        requireActivity()
            .addOnPictureInPictureModeChangedListener(state -> {
                if (getLifecycle().getCurrentState() == Lifecycle.State.CREATED) {
                    _mediaPlayerState.fullscreenState.set(UI_STATE.WILL_EXIT);
                    _mediaPlayerState.pipState.set(UI_STATE.WILL_EXIT);
                    if (!_android.enableBackgroundPlay) {
                        _playerController.pause();
                    }
                } else if (getLifecycle().getCurrentState() == Lifecycle.State.STARTED) {
                    if (state.isInPictureInPictureMode()) {
                        if (_mediaPlayerState.fullscreenState.get() != UI_STATE.ACTIVE) {
                            _mediaPlayerState.fullscreenState.set(UI_STATE.WILL_ENTER);
                        }
                    } else {
                        _mediaPlayerState.fullscreenState.set(UI_STATE.WILL_EXIT);
                        _mediaPlayerState.pipState.set(UI_STATE.WILL_EXIT);
                    }
                }
            });
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedBundleInstance) {
        super.onViewCreated(view, savedBundleInstance);

        _mediaPlayerState.pipState.observe(state -> {
            switch (state) {
                case ACTIVE -> MediaPlayerNotificationCenter.post(
                    MediaPlayerNotification.create(_playerId, MediaPlayerNotificationCenter.NOTIFICATION_TYPE.MEDIA_PLAYER_PIP)
                        .addData("isInPictureInPicture", true)
                        .build()
                );
                case INACTIVE -> MediaPlayerNotificationCenter.post(
                    MediaPlayerNotification.create(_playerId, MediaPlayerNotificationCenter.NOTIFICATION_TYPE.MEDIA_PLAYER_PIP)
                        .addData("isInPictureInPicture", false)
                        .build()
                );
                case WILL_ENTER -> {
                    _embeddedView.findViewById(R.id.MediaPlayerEmbeddedPiP).setVisibility(View.VISIBLE);

                    PictureInPictureParams.Builder pictureInPictureParams = new PictureInPictureParams.Builder()
                        .setSourceRectHint(_mediaPlayerState.sourceRectHint.get())
                        .setAspectRatio(new Rational(_placement.width, _placement.height));

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        pictureInPictureParams.setAutoEnterEnabled(_android.automaticallyEnterPiP);
                        pictureInPictureParams.setSeamlessResizeEnabled(true);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pictureInPictureParams.setTitle(_extra.title);
                        pictureInPictureParams.setSubtitle(_extra.subtitle);
                        pictureInPictureParams.setExpandedAspectRatio(new Rational(_placement.width, _placement.height));
                    }
                    requireActivity().enterPictureInPictureMode(pictureInPictureParams.build());
                    _mediaPlayerState.pipState.set(UI_STATE.ACTIVE);
                }
                case WILL_EXIT -> {
                    _embeddedView.findViewById(R.id.MediaPlayerEmbeddedPiP).setVisibility(View.GONE);
                    _mediaPlayerState.pipState.set(UI_STATE.INACTIVE);
                }
            }
        });
        View decorView = requireActivity().getWindow().getDecorView();
        int defaultUiVisibility = decorView.getSystemUiVisibility();
        int fullscreenUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LOW_PROFILE |
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        ActionBar actionBar = getSupportActionBar();
        _mediaPlayerState.fullscreenState.observe(state -> {
            switch (state) {
                case ACTIVE -> MediaPlayerNotificationCenter.post(
                    MediaPlayerNotification.create(_playerId, MediaPlayerNotificationCenter.NOTIFICATION_TYPE.MEDIA_PLAYER_FULLSCREEN)
                        .addData("isInFullScreen", true)
                        .build()
                );
                case INACTIVE -> MediaPlayerNotificationCenter.post(
                    MediaPlayerNotification.create(_playerId, MediaPlayerNotificationCenter.NOTIFICATION_TYPE.MEDIA_PLAYER_FULLSCREEN)
                        .addData("isInFullScreen", false)
                        .build()
                );
                case WILL_ENTER -> {
                    _embeddedView.findViewById(R.id.MediaPlayerEmbeddedContainer).setVisibility(View.GONE);
                    _fullscreenView.findViewById(R.id.MediaPlayerFullscreenContainer).setVisibility(View.VISIBLE);
                    _fullscreenView.findViewById(R.id.MediaPlayerFullscreenContainer).getGlobalVisibleRect(_sourceRectHint);
                    _mediaPlayerState.sourceRectHint.set(_sourceRectHint);

                    PlayerView.switchTargetView(_playerController, _embeddedPlayerView, _fullscreenPlayerView);

                    decorView.setSystemUiVisibility(fullscreenUiVisibility);

                    if (actionBar != null) {
                        actionBar.hide();
                    }

                    _mediaPlayerState.fullscreenState.set(UI_STATE.ACTIVE);
                }
                case WILL_EXIT -> {
                    _fullscreenView.findViewById(R.id.MediaPlayerFullscreenContainer).setVisibility(View.GONE);
                    _embeddedView.findViewById(R.id.MediaPlayerEmbeddedContainer).setVisibility(View.VISIBLE);
                    _embeddedView.findViewById(R.id.MediaPlayerEmbeddedContainer).getGlobalVisibleRect(_sourceRectHint);
                    _mediaPlayerState.sourceRectHint.set(_sourceRectHint);

                    PlayerView.switchTargetView(_playerController, _fullscreenPlayerView, _embeddedPlayerView);

                    decorView.setSystemUiVisibility(defaultUiVisibility);

                    if (actionBar != null) {
                        actionBar.show();
                    }

                    _mediaPlayerState.fullscreenState.set(UI_STATE.INACTIVE);
                }
            }
        });
        _mediaPlayerState.landscapeState.observe(state -> {
            if (_android.fullscreenOnLandscape && _mediaPlayerState.pipState.get() == UI_STATE.INACTIVE) {
                if (state == UI_STATE.ACTIVE && _mediaPlayerState.fullscreenState.get() == UI_STATE.INACTIVE) {
                    _mediaPlayerState.fullscreenState.set(UI_STATE.WILL_ENTER);
                }
                if (state == UI_STATE.INACTIVE && _mediaPlayerState.fullscreenState.get() == UI_STATE.ACTIVE) {
                    _mediaPlayerState.fullscreenState.set(UI_STATE.WILL_EXIT);
                }
            }
        });

        if (_android.openInFullscreen) {
            _mediaPlayerState.fullscreenState.set(UI_STATE.WILL_ENTER);
        } else {
            boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
            if (_android.fullscreenOnLandscape && isLandscape) {
                _mediaPlayerState.fullscreenState.set(UI_STATE.WILL_ENTER);
            } else {
                _mediaPlayerState.fullscreenState.set(UI_STATE.WILL_EXIT);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View containerView = inflater.inflate(R.layout.media_player_container, container, false);

        _fullscreenView = containerView.findViewById(R.id.MediaPlayerFullscreenContainer);
        _embeddedView = containerView.findViewById(R.id.MediaPlayerEmbeddedContainer);

        updateEmbeddedLayout();

        _embeddedPlayerView = createPlayerView(inflater, _embeddedView);
        _fullscreenPlayerView = createPlayerView(inflater, _fullscreenView);

        return containerView;
    }

    @OptIn(markerClass = UnstableApi.class)
    private PlayerView createPlayerView(@NonNull LayoutInflater inflater, View container) {
        View videoView = inflater.inflate(R.layout.media_player_controller_view, (ViewGroup) container, true);

        PlayerView _playerView = videoView.findViewById(R.id.MediaPlayerControllerView);

        _playerView.findViewById(androidx.media3.ui.R.id.exo_repeat_toggle).setVisibility(View.GONE);
        _playerView.findViewById(androidx.media3.ui.R.id.exo_fullscreen).setVisibility(View.GONE);
        _playerView.findViewById(androidx.media3.ui.R.id.exo_minimal_fullscreen).setVisibility(View.GONE);
        _playerView.findViewById(androidx.media3.ui.R.id.exo_extra_controls_scroll_view).setVisibility(View.VISIBLE);

        _playerView.findViewById(androidx.media3.ui.R.id.exo_bottom_bar).setVisibility(View.VISIBLE);
        _playerView.findViewById(androidx.media3.ui.R.id.exo_rew_with_amount).setVisibility(View.VISIBLE);
        _playerView.findViewById(androidx.media3.ui.R.id.exo_ffwd_with_amount).setVisibility(View.VISIBLE);
        _playerView.findViewById(androidx.media3.ui.R.id.exo_next).setVisibility(View.GONE);
        _playerView.findViewById(androidx.media3.ui.R.id.exo_prev).setVisibility(View.GONE);

        ProgressBar buffering = _playerView.findViewById(androidx.media3.ui.R.id.exo_buffering);
        buffering.setIndeterminateTintList(ColorStateList.valueOf(Color.WHITE));

        LinearLayout basicControls = _playerView.findViewById(androidx.media3.ui.R.id.exo_basic_controls);
        View extraControls = inflater.inflate(R.layout.media_player_controller_view_extra_buttons, basicControls, true);

        MediaRouteButton _castButton = extraControls.findViewById(R.id.cast_button);
        if (_android.enableChromecast) {
            CastButtonFactory.setUpMediaRouteButton(requireContext(), _castButton);
        }

        ImageButton pipButton = extraControls.findViewById(R.id.pip_button);
        if (_android.enablePiP) {
            pipButton.setVisibility(View.VISIBLE);
            pipButton.setOnClickListener(view -> _mediaPlayerState.pipState.set(MediaPlayerState.UI_STATE.WILL_ENTER));
        }

        ImageButton _fullscreenToggle = extraControls.findViewById(R.id.toggle_fullscreen);
        _fullscreenToggle.setOnClickListener(view -> {
            switch (_mediaPlayerState.fullscreenState.get()) {
                case ACTIVE -> _mediaPlayerState.fullscreenState.set(MediaPlayerState.UI_STATE.WILL_EXIT);
                case INACTIVE -> _mediaPlayerState.fullscreenState.set(MediaPlayerState.UI_STATE.WILL_ENTER);
            }
        });

        if (!_extra.showControls) {
            _playerView.setUseController(false);
        } else {
            _playerView.setControllerShowTimeoutMs(1250);
            _playerView.setControllerHideOnTouch(true);
        }

        SubtitleView subtitleView = _playerView.findViewById(androidx.media3.ui.R.id.exo_subtitles);

        if (subtitleView != null && _extra.subtitles != null) {
            subtitleView.setStyle(
                new CaptionStyleCompat(
                    _extra.subtitles.settings.foregroundColor,
                    _extra.subtitles.settings.backgroundColor,
                    Color.TRANSPARENT,
                    CaptionStyleCompat.EDGE_TYPE_NONE,
                    Color.WHITE,
                    null
                )
            );
            subtitleView.setFixedTextSize(TypedValue.COMPLEX_UNIT_DIP, _extra.subtitles.settings.fontSize.floatValue());
        }

        _playerView.setOnKeyListener((eventContainer, keyCode, keyEvent) -> {
            if (_playerController != null && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                long duration = _playerController.getDuration();
                long videoPosition = _playerController.getCurrentPosition();
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        if (videoPosition < duration - MediaPlayer.VIDEO_STEP) {
                            _playerController.seekTo(videoPosition + MediaPlayer.VIDEO_STEP);
                        }
                        return true;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        if (videoPosition - MediaPlayer.VIDEO_STEP > 0) {
                            _playerController.seekTo(videoPosition - MediaPlayer.VIDEO_STEP);
                        } else {
                            _playerController.seekTo(0);
                        }
                        return true;
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        if (_playerController.isPlaying()) {
                            _playerController.pause();
                        } else {
                            _playerController.play();
                        }
                        return true;
                    case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                        if (videoPosition < duration - (MediaPlayer.VIDEO_STEP * 2)) {
                            _playerController.seekTo(videoPosition + (MediaPlayer.VIDEO_STEP * 2));
                        }
                        return true;
                    case KeyEvent.KEYCODE_MEDIA_REWIND:
                        if (videoPosition - (MediaPlayer.VIDEO_STEP * 2) > 0) {
                            _playerController.seekTo(videoPosition - (MediaPlayer.VIDEO_STEP * 2));
                        } else {
                            _playerController.seekTo(0);
                        }
                        return true;
                }
            }
            return false;
        });

        _mediaPlayerState.fullscreenState.observe(state ->
            ((ImageButton) _playerView.findViewById(R.id.toggle_fullscreen)).setImageResource(
                    state == UI_STATE.ACTIVE ? R.drawable.ic_fullscreen_exit : R.drawable.ic_fullscreen_enter
                )
        );
        _mediaPlayerState.pipState.observe(state -> {
            switch (state) {
                case WILL_ENTER -> _playerView.setUseController(false);
                case WILL_EXIT -> _playerView.setUseController(_extra.showControls);
            }
        });
        _mediaPlayerState.canCast.observe(isCastAvailable -> {
            _playerView.findViewById(R.id.cast_button).setVisibility(isCastAvailable ? View.VISIBLE : View.GONE);
            _playerView.findViewById(R.id.cast_button).setEnabled(isCastAvailable);
        });
        _mediaPlayerState.showSubtitles.observe(showSubtitles ->
            _playerView.findViewById(androidx.media3.ui.R.id.exo_subtitle).setVisibility(showSubtitles ? View.VISIBLE : View.GONE)
        );

        return _playerView;
    }

    private void updateEmbeddedLayout() {
        // Create layout parameters with the desired width and height.
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(_placement.width, _placement.height);
        // Apply horizontal alignment and margins.
        switch (_placement.horizontalAlignment) {
            case "START":
                params.addRule(RelativeLayout.ALIGN_PARENT_START);
                params.setMarginStart(_placement.horizontalMargin);
                break;
            case "END":
                params.addRule(RelativeLayout.ALIGN_PARENT_END);
                params.setMarginEnd(_placement.horizontalMargin);
                break;
            case "CENTER":
            default:
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                // For center alignment, apply left and right margins
                params.leftMargin = _placement.horizontalMargin;
                params.rightMargin = _placement.horizontalMargin;
                break;
        }
        // Apply vertical alignment and margins.
        switch (_placement.verticalAlignment) {
            case "BOTTOM":
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                params.bottomMargin = _placement.verticalMargin;
                break;
            case "CENTER":
                params.addRule(RelativeLayout.CENTER_VERTICAL);
                // Optionally, apply top and bottom margins for center vertical alignment.
                params.topMargin = _placement.verticalMargin;
                params.bottomMargin = _placement.verticalMargin;
                break;
            case "TOP":
            default:
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.topMargin = _placement.verticalMargin;
                break;
        }
        // Ensure the width and height are correctly set.
        params.width = _placement.width;
        params.height = _placement.height;
        // Apply the updated layout parameters to the embedded view.
        _embeddedView.setLayoutParams(params);
    }

    @Nullable
    private ActionBar getSupportActionBar() {
        ActionBar actionBar = null;
        if (requireActivity() instanceof AppCompatActivity activity) {
            actionBar = activity.getSupportActionBar();
        }
        return actionBar;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        _mediaPlayerState.landscapeState.set(isLandscape ? UI_STATE.ACTIVE : UI_STATE.INACTIVE);
    }

    @Override
    public void onPause() {
        if (_android.enableBackgroundPlay) {
            MediaPlayerNotificationCenter.post(
                MediaPlayerNotification.create(_playerId, MediaPlayerNotificationCenter.NOTIFICATION_TYPE.MEDIA_PLAYER_BACKGROUND_PLAYING)
                    .addData("isPlayingInBackground", true)
                    .build()
            );
        } else {
            if (_mediaPlayerState.pipState.get() != UI_STATE.ACTIVE) {
                _playerController.pause();
            }
        }

        super.onPause();
    }

    @Override
    public void onResume() {
        if (_android.enableBackgroundPlay) {
            MediaPlayerNotificationCenter.post(
                MediaPlayerNotification.create(_playerId, MediaPlayerNotificationCenter.NOTIFICATION_TYPE.MEDIA_PLAYER_BACKGROUND_PLAYING)
                    .addData("isPlayingInBackground", false)
                    .build()
            );
        }

        super.onResume();
    }
}
