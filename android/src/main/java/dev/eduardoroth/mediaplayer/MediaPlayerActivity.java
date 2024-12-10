package dev.eduardoroth.mediaplayer;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.OptIn;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.media3.common.util.UnstableApi;

import android.app.PictureInPictureParams;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Rational;
import android.widget.FrameLayout;

import dev.eduardoroth.mediaplayer.models.AndroidOptions;
import dev.eduardoroth.mediaplayer.models.ExtraOptions;
import dev.eduardoroth.mediaplayer.state.MediaPlayerState;
import dev.eduardoroth.mediaplayer.state.MediaPlayerState.UI_STATE;
import dev.eduardoroth.mediaplayer.state.MediaPlayerStateProvider;

public class MediaPlayerActivity extends FragmentActivity {
    public String playerId;

    private MediaPlayerState mediaPlayerState;

    private AndroidOptions android;
    private ExtraOptions extra;

    private final Rect sourceRectHint = new Rect();

    private FrameLayout frameLayout;

    private final DisplayMetrics displayMetrics = new DisplayMetrics();


    @OptIn(markerClass = UnstableApi.class)
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_player_activity);

        playerId = getIntent().getStringExtra("playerId");
        Uri url = Uri.parse(getIntent().getStringExtra("url"));
        android = (AndroidOptions) getIntent().getSerializableExtra("android");
        extra = (ExtraOptions) getIntent().getSerializableExtra("extra");

        mediaPlayerState = MediaPlayerStateProvider.getState(playerId);

        addListeners();
    }

    @OptIn(markerClass = UnstableApi.class)
    private void addListeners() {
        mediaPlayerState.fullscreenState.observe(state -> {
            /*if (state == UI_STATE.WILL_EXIT) {
                finish();
            }*/
        });
        mediaPlayerState.pipState.observe(state -> {
            if (state == UI_STATE.WILL_ENTER) {
                PictureInPictureParams.Builder pictureInPictureParams = new PictureInPictureParams.Builder().setSourceRectHint(mediaPlayerState.sourceRectHint.get()).setAspectRatio(new Rational(android.width, android.height));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    pictureInPictureParams.setAutoEnterEnabled(android.automaticallyEnterPiP);
                    pictureInPictureParams.setSeamlessResizeEnabled(true);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pictureInPictureParams.setTitle(extra.title);
                    pictureInPictureParams.setSubtitle(extra.subtitle);
                    pictureInPictureParams.setExpandedAspectRatio(new Rational(android.width, android.height));
                }
                enterPictureInPictureMode(pictureInPictureParams.build());
                mediaPlayerState.pipState.set(UI_STATE.ACTIVE);
            }
        });
        mediaPlayerState.landscapeState.observe(state -> {
            if (android.fullscreenOnLandscape) {
                if (state == UI_STATE.INACTIVE) {
                    if (mediaPlayerState.fullscreenState.get() == UI_STATE.ACTIVE) {
                        mediaPlayerState.fullscreenState.set(UI_STATE.WILL_EXIT);
                    }
                }
            }
        });

        this.getLifecycle().addObserver((LifecycleEventObserver) (lifecycleOwner, event) -> {
            switch (event) {
                case ON_PAUSE:
                case ON_STOP:
                    mediaPlayerState.backgroundState.set(UI_STATE.ACTIVE);
                    break;
                case ON_START:
                case ON_RESUME:
                    mediaPlayerState.backgroundState.set(UI_STATE.INACTIVE);
                    break;
            }
        });
        addOnPictureInPictureModeChangedListener(info -> {
            mediaPlayerState.pipState.set(info.isInPictureInPictureMode() ? UI_STATE.ACTIVE : UI_STATE.INACTIVE);
            /*HashMap<String, Object> infoPip = new HashMap<String, Object>();
            infoPip.put("playerId", playerId);
            infoPip.put("isInPictureInPicture", isInPipMode);*/
            //NotificationHelpers.defaultCenter().postNotification("MediaPlayer:PictureInPicture", infoPip);
        });
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // TODO: check if it's playing too
                // mediaPlayerHostPlayer.player.isPlaying()
                if (android.automaticallyEnterPiP) {
                    mediaPlayerState.pipState.set(UI_STATE.WILL_ENTER);
                } else {
                    mediaPlayerState.fullscreenState.set(UI_STATE.WILL_EXIT);
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        MediaPlayerStateProvider.clearState(playerId);
        super.onDestroy();
    }

}