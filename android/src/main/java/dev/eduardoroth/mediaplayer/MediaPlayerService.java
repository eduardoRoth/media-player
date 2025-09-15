package dev.eduardoroth.mediaplayer;

import static android.media.MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT;
import static androidx.media3.common.C.ALLOW_CAPTURE_BY_SYSTEM;
import static androidx.media3.common.C.AUDIO_CONTENT_TYPE_MOVIE;
import static androidx.media3.common.C.USAGE_MEDIA;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ServiceLifecycleDispatcher;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSession.ControllerInfo;
import androidx.media3.session.MediaSessionService;
import dev.eduardoroth.mediaplayer.models.AndroidOptions;
import dev.eduardoroth.mediaplayer.models.ExtraOptions;
import dev.eduardoroth.mediaplayer.models.MediaItem;
import dev.eduardoroth.mediaplayer.models.MediaPlayerNotification;
import dev.eduardoroth.mediaplayer.models.PlacementOptions;
import dev.eduardoroth.mediaplayer.state.MediaPlayerState;
import dev.eduardoroth.mediaplayer.state.MediaPlayerStateProvider;

public class MediaPlayerService extends MediaSessionService implements LifecycleOwner {

    public static long VIDEO_STEP = 10000;

    private final ServiceLifecycleDispatcher mDispatcher = new ServiceLifecycleDispatcher(this);

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public MediaSession onGetSession(@NonNull ControllerInfo controllerInfo) {
        String playerId = controllerInfo.getConnectionHints().getString("playerId", "no-player-id");

        MediaSession doesSessionExists = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            doesSessionExists = this.getSessions().stream().filter(s -> s.getId().equals(playerId)).findFirst().orElse(null);
        } else {
            for (int i = 0; i < getSessions().toArray().length; i++) {
                MediaSession s = (MediaSession) getSessions().toArray()[i];
                if (s.getId().equals(playerId)) {
                    doesSessionExists = s;
                    break;
                }
            }
        }

        String videoUrl = controllerInfo.getConnectionHints().getString("videoUrl");
        PlacementOptions placement = (PlacementOptions) controllerInfo.getConnectionHints().getSerializable("placement");
        AndroidOptions android = (AndroidOptions) controllerInfo.getConnectionHints().getSerializable("android");
        ExtraOptions extra = (ExtraOptions) controllerInfo.getConnectionHints().getSerializable("extra");

        // Validate and provide defaults if options are missing
        if (placement == null) {
            android.util.Log.w("MediaPlayerService", "PlacementOptions is null for player " + playerId + ", using defaults");
            placement = new PlacementOptions();
        }
        if (android == null) {
            android.util.Log.w("MediaPlayerService", "AndroidOptions is null for player " + playerId + ", using defaults");
            android = new AndroidOptions();
        }
        if (extra == null) {
            android.util.Log.w("MediaPlayerService", "ExtraOptions is null for player " + playerId + ", using defaults");
            extra = new ExtraOptions();
        }

        if (doesSessionExists != null) {
            String currentPlayerId = doesSessionExists.getSessionExtras().getString("playerId");
            if (currentPlayerId != null && currentPlayerId.equalsIgnoreCase(playerId)) {
                MediaPlayerNotificationCenter.post(
                    MediaPlayerNotification.create(playerId, MediaPlayerNotificationCenter.NOTIFICATION_TYPE.MEDIA_PLAYER_READY).build()
                );
            } else {
                MediaPlayerNotificationCenter.post(
                    MediaPlayerNotification.create(
                        currentPlayerId,
                        MediaPlayerNotificationCenter.NOTIFICATION_TYPE.MEDIA_PLAYER_ENDED
                    ).build()
                );
                doesSessionExists.getPlayer().stop();
                doesSessionExists.getPlayer().release();
                doesSessionExists.setPlayer(createPlayer(playerId, videoUrl, android, extra));
            }
            return doesSessionExists;
        }

        mDispatcher.onServicePreSuperOnDestroy();
        mDispatcher.onServicePreSuperOnStart();

        MediaPlayerState mediaPlayerState = MediaPlayerStateProvider.createState(playerId, this);
        mediaPlayerState.placementOptions.set(placement);
        mediaPlayerState.androidOptions.set(android);
        mediaPlayerState.extraOptions.set(extra);

        Bundle sessionExtras = new Bundle();
        sessionExtras.putString("playerId", playerId);
        sessionExtras.putString("videoUrl", videoUrl);
        sessionExtras.putBoolean("stopOnTaskRemoved", android.stopOnTaskRemoved);

        MediaSession playerSession = new MediaSession.Builder(this, createPlayer(playerId, videoUrl, android, extra))
            .setId(playerId)
            .setPeriodicPositionUpdateEnabled(true)
            .setSessionExtras(sessionExtras)
            .build();

        addSession(playerSession);
        return playerSession;
    }

    @OptIn(markerClass = UnstableApi.class)
    private ExoPlayer createPlayer(String playerId, String videoUrl, AndroidOptions android, ExtraOptions extra) {
        ExoPlayer exoPlayer = new ExoPlayer.Builder(this)
            .setName(playerId)
            .setTrackSelector(new DefaultTrackSelector(this, new AdaptiveTrackSelection.Factory()))
            .setLoadControl(new DefaultLoadControl())
            .setBandwidthMeter(new DefaultBandwidthMeter.Builder(this).build())
            .setDeviceVolumeControlEnabled(true)
            .setSeekBackIncrementMs(VIDEO_STEP)
            .setSeekForwardIncrementMs(VIDEO_STEP)
            .setVideoScalingMode(VIDEO_SCALING_MODE_SCALE_TO_FIT)
            .build();

        exoPlayer.setAudioAttributes(
            new AudioAttributes.Builder()
                .setContentType(AUDIO_CONTENT_TYPE_MOVIE)
                .setAllowedCapturePolicy(ALLOW_CAPTURE_BY_SYSTEM)
                .setUsage(USAGE_MEDIA)
                .build(),
            true
        );

        exoPlayer.setRepeatMode(extra.loopOnEnd ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF);

        exoPlayer.setMediaItem(new MediaItem(Uri.parse(videoUrl), extra).getMediaItem());
        Handler handlerCurrentTime = new Handler();
        exoPlayer.addListener(
            new Player.Listener() {
                @Override
                public void onPositionDiscontinuity(
                    @NonNull Player.PositionInfo oldPosition,
                    @NonNull Player.PositionInfo newPosition,
                    int reason
                ) {
                    Player.Listener.super.onPositionDiscontinuity(oldPosition, newPosition, reason);
                    if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                        MediaPlayerNotificationCenter.post(
                            MediaPlayerNotification.create(playerId, MediaPlayerNotificationCenter.NOTIFICATION_TYPE.MEDIA_PLAYER_SEEK)
                                .addData("previousTime", oldPosition.positionMs / 1000)
                                .addData("newTime", newPosition.positionMs / 1000)
                                .build()
                        );
                    }
                }

                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    if (isPlaying) {
                        MediaPlayerNotificationCenter.post(
                            MediaPlayerNotification.create(
                                playerId,
                                MediaPlayerNotificationCenter.NOTIFICATION_TYPE.MEDIA_PLAYER_PLAY
                            ).build()
                        );
                        handlerCurrentTime.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    MediaPlayerNotificationCenter.post(
                                        MediaPlayerNotification.create(
                                            playerId,
                                            MediaPlayerNotificationCenter.NOTIFICATION_TYPE.MEDIA_PLAYER_TIME_UPDATED
                                        )
                                            .addData("currentTime", exoPlayer.getCurrentPosition() / 1000)
                                            .build()
                                    );
                                    handlerCurrentTime.postDelayed(this, 100);
                                }
                            },
                            100
                        );
                    } else {
                        handlerCurrentTime.removeCallbacksAndMessages(null);
                        MediaPlayerNotificationCenter.post(
                            MediaPlayerNotification.create(
                                playerId,
                                MediaPlayerNotificationCenter.NOTIFICATION_TYPE.MEDIA_PLAYER_PAUSE
                            ).build()
                        );
                    }
                }

                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    Player.Listener.super.onPlaybackStateChanged(playbackState);
                    switch (playbackState) {
                        case Player.STATE_BUFFERING:
                        case Player.STATE_IDLE:
                            break;
                        case Player.STATE_ENDED:
                            MediaPlayerNotificationCenter.post(
                                MediaPlayerNotification.create(
                                    playerId,
                                    MediaPlayerNotificationCenter.NOTIFICATION_TYPE.MEDIA_PLAYER_ENDED
                                ).build()
                            );
                            break;
                        case Player.STATE_READY:
                            MediaPlayerNotificationCenter.post(
                                MediaPlayerNotification.create(
                                    playerId,
                                    MediaPlayerNotificationCenter.NOTIFICATION_TYPE.MEDIA_PLAYER_READY
                                ).build()
                            );
                            if (extra.autoPlayWhenReady) {
                                exoPlayer.play();
                            }
                            break;
                    }
                }
            }
        );

        exoPlayer.prepare();
        return exoPlayer;
    }

    @Override
    public void onCreate() {
        mDispatcher.onServicePreSuperOnCreate();
        super.onCreate();
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onTaskRemoved(@Nullable Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        getSessions()
            .forEach(session -> {
                Player player = session.getPlayer();
                if (!player.getPlayWhenReady() || player.getMediaItemCount() == 0 || player.getPlaybackState() == Player.STATE_ENDED) {
                    stopSelf();
                }
                if (session.getSessionExtras().getBoolean("stopOnTaskRemoved")) {
                    player.pause();
                    stopSelf();
                }
            });
    }

    @Override
    public IBinder onBind(Intent intent) {
        mDispatcher.onServicePreSuperOnBind();
        return super.onBind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mDispatcher.onServicePreSuperOnStart();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mDispatcher.onServicePreSuperOnDestroy();
        super.onDestroy();
        getSessions()
            .forEach(session -> {
                session.getPlayer().release();
                session.release();
                removeSession(session);
            });
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return mDispatcher.getLifecycle();
    }
}
