package dev.eduardoroth.mediaplayer;

import static android.app.Notification.BADGE_ICON_LARGE;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.cast.CastPlayer;
import androidx.media3.cast.SessionAvailabilityListener;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;
import androidx.media3.session.MediaSession;
import androidx.media3.ui.PlayerNotificationManager;
import androidx.mediarouter.media.MediaControlIntent;
import androidx.mediarouter.media.MediaRouteSelector;
import androidx.mediarouter.media.MediaRouter;

import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import dev.eduardoroth.mediaplayer.models.AndroidOptions;
import dev.eduardoroth.mediaplayer.models.ExtraOptions;
import dev.eduardoroth.mediaplayer.state.MediaPlayerState;
import dev.eduardoroth.mediaplayer.state.MediaPlayerState.UI_STATE;
import dev.eduardoroth.mediaplayer.state.MediaPlayerStateProvider;
import dev.eduardoroth.mediaplayer.utilities.NotificationHelpers;

@UnstableApi
public class MediaPlayerController {
    private final int _layoutId;
    private final String _url;
    private final String _playerId;
    private final AndroidOptions _android;
    private final ExtraOptions _extra;
    private final Handler _handler = new Handler();
    private final Map<String, MediaPlayerMediaItem> _mediaItems = new HashMap<>();

    private final AppCompatActivity _activity;
    private final Context _context;
    private final ExoPlayer _exoPlayer;
    private final CastPlayer _castPlayer;
    private final MediaPlayerState _mediaPlayerState;
    private MediaSession _exoPlayerMediaSession;
    private MediaSession _castPlayerMediaSession;
    private PlayerNotificationManager _playerNotificationManager;

    private final MediaPlayerControllerView _playerView;
    private Player _activePlayer;

    public MediaPlayerController(AppCompatActivity activity, String url, String playerId, AndroidOptions android, ExtraOptions extra) {
        _layoutId = playerId.chars().reduce(0, Integer::sum);
        _activity = activity;
        _url = url;
        _playerId = playerId;
        _android = android;
        _extra = extra;
        _context = activity.getBaseContext();

        _mediaPlayerState = MediaPlayerStateProvider.getState(_playerId, activity);

        _playerView = new MediaPlayerControllerView(_playerId, Uri.parse(_url), android, extra);

        createPlayerNotificationManager();

        _exoPlayer = createExoPlayer();
        _castPlayer = createCastPlayer();

        setActivePlayer();

        _mediaPlayerState.castingState.observe(state -> {
            if (state == UI_STATE.WILL_ENTER) {
                set_activePlayer(true);
            }
            if (state == UI_STATE.WILL_EXIT) {
                set_activePlayer(false);
            }
        });
        _mediaPlayerState.willBeDestroyed.observe(willBeDestroyed -> {
            if (willBeDestroyed) {
                _exoPlayer.release();
                _exoPlayerMediaSession.release();
                _exoPlayerMediaSession = null;

                if (_mediaPlayerState.canCast.get()) {
                    assert _castPlayer != null;
                    _castPlayer.release();
                    _castPlayerMediaSession.release();
                    _castPlayerMediaSession = null;
                }

                _playerNotificationManager.setPlayer(null);
                _playerNotificationManager.invalidate();
            }
        });

    }

    public Player getActivePlayer() {
        return _activePlayer;
    }

    public MediaPlayerControllerView getPlayerView() {
        return _playerView;
    }

    public void addMediaItem(MediaPlayerMediaItem item) {
        _mediaItems.put(item.getMediaItem().mediaId, item);
        _exoPlayer.addMediaItem(item.getMediaItem());
        if (_castPlayer != null) {
            _castPlayer.addMediaItem(item.getMediaItem());
        }
    }

    public void addMediaItems(ArrayList<MediaPlayerMediaItem> items) {
        items.forEach(this::addMediaItem);
    }

    public boolean shouldShowSubtitles() {
        MediaItem current = _exoPlayer.getCurrentMediaItem();
        if (current != null) {
            MediaPlayerMediaItem mediaItem = _mediaItems.get(current.mediaId);
            if (mediaItem != null) {
                return mediaItem.hasSubtitles();
            }
        }
        return false;
    }

    private void setActivePlayer() {
        set_activePlayer(false);
    }

    private void set_activePlayer(boolean isCasting) {
        setActivePlayer(isCasting ? _castPlayer : _exoPlayer, isCasting);
    }

    private void setActivePlayer(Player playerToChange, boolean isCasting) {
        if (_activePlayer == playerToChange) {
            return;
        }
        long currentTime = _mediaPlayerState.getCurrentTime.get();
        if (_activePlayer != null) {
            _activePlayer.stop();
        }
        _activePlayer = playerToChange;
        _activePlayer.seekTo(currentTime);
        _playerNotificationManager.setPlayer(_activePlayer);
        _playerNotificationManager.setMediaSessionToken(isCasting ? _castPlayerMediaSession.getPlatformToken() : _exoPlayerMediaSession.getPlatformToken());
        _activePlayer.prepare();
        _mediaPlayerState.castingState.set(isCasting ? UI_STATE.ACTIVE : UI_STATE.INACTIVE);
    }

    private void createPlayerNotificationManager() {
        _playerNotificationManager = new PlayerNotificationManager.Builder(_context, _layoutId, _context.getString(R.string.channel_id)).setChannelNameResourceId(R.string.channel_name).setChannelDescriptionResourceId(R.string.channel_description).setChannelImportance(NotificationManager.IMPORTANCE_DEFAULT).setNotificationListener(new PlayerNotificationManager.NotificationListener() {
            @Override
            public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                PlayerNotificationManager.NotificationListener.super.onNotificationCancelled(notificationId, dismissedByUser);
            }

            @Override
            public void onNotificationPosted(int notificationId, @NonNull Notification notification, boolean ongoing) {
                PlayerNotificationManager.NotificationListener.super.onNotificationPosted(notificationId, notification, ongoing);
            }
        }).build();
        _playerNotificationManager.setBadgeIconType(BADGE_ICON_LARGE);
        _playerNotificationManager.setShowPlayButtonIfPlaybackIsSuppressed(true);
        _playerNotificationManager.setUseChronometer(true);
        _playerNotificationManager.setUseFastForwardAction(true);
        _playerNotificationManager.setUseFastForwardActionInCompactView(true);
        _playerNotificationManager.setUseNextAction(false);
        _playerNotificationManager.setUseNextActionInCompactView(false);
        _playerNotificationManager.setUsePlayPauseActions(true);
        _playerNotificationManager.setUsePreviousAction(false);
        _playerNotificationManager.setUsePreviousActionInCompactView(false);
        _playerNotificationManager.setUseRewindAction(true);
        _playerNotificationManager.setUseRewindActionInCompactView(true);
        _playerNotificationManager.setUseStopAction(true);
    }

    @OptIn(markerClass = UnstableApi.class)
    private ExoPlayer createExoPlayer() {
        ExoPlayer exoPlayer = new ExoPlayer.Builder(_context).setName(_playerId).setTrackSelector(new DefaultTrackSelector(_context, new AdaptiveTrackSelection.Factory())).setLoadControl(new DefaultLoadControl()).setBandwidthMeter(new DefaultBandwidthMeter.Builder(_context).build()).setDeviceVolumeControlEnabled(true).setSeekBackIncrementMs(MediaPlayer.VIDEO_STEP).setSeekForwardIncrementMs(MediaPlayer.VIDEO_STEP).setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT).build();

        exoPlayer.setRepeatMode(_extra.loopOnEnd ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF);
        exoPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).setAllowedCapturePolicy(C.ALLOW_CAPTURE_BY_SYSTEM).setUsage(C.USAGE_MEDIA).build(), true);

        /// Listeners
        Player.Listener playerListener = new Player.Listener() {
            @Override
            public void onPositionDiscontinuity(@NonNull Player.PositionInfo oldPosition, @NonNull Player.PositionInfo newPosition, int reason) {
                Player.Listener.super.onPositionDiscontinuity(oldPosition, newPosition, reason);
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    HashMap<String, Object> info = new HashMap<>();
                    info.put("playerId", _playerId);
                    info.put("previousTime", oldPosition.positionMs / 1000);
                    info.put("newTime", newPosition.positionMs / 1000);
                    NotificationHelpers.defaultCenter().postNotification("MediaPlayer:Seeked", info);
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                Player.Listener.super.onIsPlayingChanged(isPlaying);
                HashMap<String, Object> info = new HashMap<>();
                info.put("playerId", _playerId);
                if (isPlaying) {
                    _handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            HashMap<String, Object> timeUpdate = new HashMap<>();
                            timeUpdate.put("playerId", _playerId);
                            timeUpdate.put("currentTime", exoPlayer.getCurrentPosition() / 1000);
                            NotificationHelpers.defaultCenter().postNotification("MediaPlayer:TimeUpdate", timeUpdate);
                            _handler.postDelayed(this, 100);
                        }
                    }, 100);
                    NotificationHelpers.defaultCenter().postNotification("MediaPlayer:Play", info);
                } else {
                    _handler.removeCallbacksAndMessages(null);
                    NotificationHelpers.defaultCenter().postNotification("MediaPlayer:Pause", info);
                }
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                HashMap<String, Object> info = new HashMap<>();
                info.put("playerId", _playerId);
                switch (playbackState) {
                    case Player.STATE_BUFFERING:
                    case Player.STATE_IDLE:
                        break;
                    case Player.STATE_ENDED:
                        NotificationHelpers.defaultCenter().postNotification("MediaPlayer:Ended", info);
                        break;
                    case Player.STATE_READY:
                        NotificationHelpers.defaultCenter().postNotification("MediaPlayer:Ready", info);
                        if (_extra.autoPlayWhenReady) {
                            exoPlayer.play();
                        }
                        break;
                }
            }
        };
        exoPlayer.addListener(playerListener);

        _exoPlayerMediaSession = new MediaSession.Builder(_context, exoPlayer).setPeriodicPositionUpdateEnabled(true).build();

        _exoPlayerMediaSession.setPlayer(exoPlayer);
        exoPlayer.prepare();

        return exoPlayer;
    }

    @OptIn(markerClass = UnstableApi.class)
    private CastPlayer createCastPlayer() {
        CastContext castContext = null;
        try {
            castContext = CastContext.getSharedInstance(_context, MoreExecutors.directExecutor()).getResult();
        } catch (RuntimeException ignored) {
        }
        if (castContext == null) {
            return null;
        }

        CastPlayer castPlayer = new CastPlayer(castContext);

        MediaRouter mRouter = MediaRouter.getInstance(_context);
        MediaRouteSelector mSelector = new MediaRouteSelector.Builder().addControlCategories(Arrays.asList(MediaControlIntent.CATEGORY_LIVE_AUDIO, MediaControlIntent.CATEGORY_LIVE_VIDEO)).build();

        mRouter.addCallback(mSelector, new MediaRouter.Callback() {
        }, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);

        castContext.addCastStateListener(state -> _mediaPlayerState.canCast.set(state != CastState.NO_DEVICES_AVAILABLE));

        castPlayer.addListener(new CastPlayer.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                CastPlayer.Listener.super.onPlaybackStateChanged(playbackState);
                HashMap<String, Object> info = new HashMap<>();
                info.put("playerId", _playerId);
                if (playbackState == CastPlayer.STATE_READY) {
                    if (castPlayer.isPlaying()) {
                        NotificationHelpers.defaultCenter().postNotification("MediaPlayer:Play", info);
                    } else {
                        NotificationHelpers.defaultCenter().postNotification("MediaPlayer:Pause", info);
                    }
                }
            }
        });
        castPlayer.setSessionAvailabilityListener(new SessionAvailabilityListener() {
            @Override
            public void onCastSessionAvailable() {
                _mediaPlayerState.castingState.set(UI_STATE.WILL_ENTER);
            }

            @Override
            public void onCastSessionUnavailable() {
                _mediaPlayerState.castingState.set(UI_STATE.WILL_EXIT);
            }
        });

        _castPlayerMediaSession = new MediaSession.Builder(_context, castPlayer).setPeriodicPositionUpdateEnabled(true).build();

        _castPlayerMediaSession.setPlayer(castPlayer);

        castPlayer.prepare();

        return castPlayer;
    }

}
