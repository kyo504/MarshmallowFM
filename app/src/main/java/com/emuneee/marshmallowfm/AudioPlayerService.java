package com.emuneee.marshmallowfm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.emuneee.marshmallowfm.playback.Playback;
import com.emuneee.marshmallowfm.playback.PlaybackManager;
import com.emuneee.marshmallowfm.utils.LogHelper;

import java.io.IOException;
import java.util.List;

public class AudioPlayerService extends MediaBrowserServiceCompat implements PlaybackManager.PlaybackServiceCallback{

    private static final String TAG = LogHelper.makeLogTag(AudioPlayerService.class);
    private static final String ROOT_ID = "_ROOT_";
    public static final String SESSION_TAG = "mmFM";

    public static final String ACTION_PLAY = "play";
    public static final String ACTION_PAUSE = "pause";
    public static final String ACTION_FAST_FORWARD = "fastForward";
    public static final String ACTION_REWIND = "rewind";

    public static final String PARAM_TRACK_URI = "uri";

    private MediaSessionCompat mMediaSession;
    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private PlaybackStateCompat mPlaybackState;
    private PlaybackStateCompat.Builder mStateBuilder;
    private MediaNotificationManager mMediaNotificationManager;

    // Extra on MediaSession that contains the Cast device name currently connected to
    public static final String EXTRA_CONNECTED_CAST = "com.example.android.uamp.CAST_NAME";
    // The action of the incoming Intent indicating that it contains a command
    // to be executed (see {@link #onStartCommand})
    public static final String ACTION_CMD = "com.example.android.uamp.ACTION_CMD";
    // The key in the extras of the incoming Intent indicating the command that
    // should be executed (see {@link #onStartCommand})
    public static final String CMD_NAME = "CMD_NAME";
    // A value of a CMD_NAME key in the extras of the incoming Intent that
    // indicates that the music playback should be paused (see {@link #onStartCommand})
    public static final String CMD_PAUSE = "CMD_PAUSE";
    // A value of a CMD_NAME key that indicates that the music playback should switch
    // to local playback from cast playback.
    public static final String CMD_STOP_CASTING = "CMD_STOP_CASTING";
    // Delay stopSelf by using a handler.
    private static final int STOP_DELAY = 30000;




    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot(ROOT_ID, null);
    }

    private MediaSessionCompat.Callback mMediaSessionCallback = new MediaSessionCompat.Callback() {

        @Override
        public void onPlayFromSearch(String query, Bundle extras) {
            Uri uri = extras.getParcelable(PARAM_TRACK_URI);
            onPlayFromUri(uri, null);
        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {

            try {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(AudioPlayerService.this, uri);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
                // The service needs to continue running even after the bound client (usually a
                // MediaController) disconnects, otherwise the music playback will stop.
                // Calling startService(Intent) will keep the service running until it is explicitly killed.
//                switch (mPlaybackState.getState()) {
//                    case PlaybackStateCompat.STATE_PLAYING:
//                    case PlaybackStateCompat.STATE_PAUSED:
//                        mMediaPlayer.reset();
//                        mMediaPlayer.setDataSource(AudioPlayerService.this, uri);
//                        mMediaPlayer.prepare();
//                        mPlaybackState = new PlaybackStateCompat.Builder()
//                                .setState(PlaybackStateCompat.STATE_CONNECTING, 0, 1.0f)
//                                .build();
//                        mMediaSession.setPlaybackState(mPlaybackState);
//                        mMediaSession.setMetadata(new MediaMetadataCompat.Builder()
//                                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, extras.getString(MediaMetadataCompat.METADATA_KEY_ARTIST))
//                                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, extras.getString(MediaMetadataCompat.METADATA_KEY_TITLE))
//                                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, extras.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI))
//                                .build());
//                        break;
//                    case PlaybackStateCompat.STATE_NONE:
//                        mMediaPlayer.setDataSource(AudioPlayerService.this, uri);
//                        mMediaPlayer.prepare();
//                        mPlaybackState = new PlaybackStateCompat.Builder()
//                                .setState(PlaybackStateCompat.STATE_CONNECTING, 0, 1.0f)
//                                .build();
//                        mMediaSession.setPlaybackState(mPlaybackState);
//                        mMediaSession.setMetadata(new MediaMetadataCompat.Builder()
//                                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, extras.getString(MediaMetadataCompat.METADATA_KEY_ARTIST))
//                                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, extras.getString(MediaMetadataCompat.METADATA_KEY_TITLE))
//                                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, extras.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI))
//                                .build());
//                        break;
//
//                }
            } catch (IOException e) {

            }

        }

        @Override
        public void onPlay() {
            switch (mPlaybackState.getState()) {
                case PlaybackStateCompat.STATE_PAUSED:
                    mMediaPlayer.start();
                    mPlaybackState = new PlaybackStateCompat.Builder()
                            .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
                            .build();
                    mMediaSession.setPlaybackState(mPlaybackState);
//                    updateNotification();
                    break;

            }
        }

        @Override
        public void onPause() {
            switch (mPlaybackState.getState()) {
                case PlaybackStateCompat.STATE_PLAYING:
                    mMediaPlayer.pause();
                    mPlaybackState = new PlaybackStateCompat.Builder()
                            .setState(PlaybackStateCompat.STATE_PAUSED, 0, 1.0f)
                            .build();
                    mMediaSession.setPlaybackState(mPlaybackState);
//                    updateNotification();
                    break;

            }
        }

        @Override
        public void onRewind() {
            switch (mPlaybackState.getState()) {
                case PlaybackStateCompat.STATE_PLAYING:
                    mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition() - 10000);
                    break;

            }
        }

        @Override
        public void onFastForward() {
            switch (mPlaybackState.getState()) {
                case PlaybackStateCompat.STATE_PLAYING:
                    mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition() + 10000);
                    break;

            }
        }

        @Override
        public void onStop() {
            stopForeground(true);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        // Create a MediaSessionComPat
        mMediaSession = new MediaSessionCompat(this, SESSION_TAG);

        // Enable callbacks from MediaButtons and TransportControls
        mMediaSession.setActive(true);
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        mStateBuilder = new PlaybackStateCompat.Builder()
                            .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mMediaSession.setPlaybackState(mStateBuilder.build());

        // Register callbacks to handle callbacks from a media controller
        mMediaSession.setCallback(mMediaSessionCallback);

        // Set the session't token so that client activities communicate with it
        setSessionToken(mMediaSession.getSessionToken());

        // Get an instance to AudioManager
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        try {
            mMediaNotificationManager = new MediaNotificationManager(this);
            mMediaNotificationManager.startNotification();
        } catch (RemoteException e) {
            throw new IllegalStateException("Could not create a MediaNotificationManager", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        mMediaSession.release();
    }

    /**
     * Callback method called from PlaybackManager whenever the music is about to play.
     */
    @Override
    public void onPlaybackStart() {
        mMediaSession.setActive(true);

        // The service needs to continue running even after the bound client (usually a
        // MediaController) disconnects, otherwise the music playback will stop.
        // Calling startService(Intent) will keep the service running until it is explicitly killed.
        startService(new Intent(getApplicationContext(), AudioPlayerService.class));
    }


    /**
     * Callback method called from PlaybackManager whenever the music stops playing.
     */
    @Override
    public void onPlaybackStop() {
        mMediaSession.setActive(false);
        stopForeground(true);
    }

    @Override
    public void onNotificationRequired() {
        mMediaNotificationManager.startNotification();
    }

    @Override
    public void onPlaybackStateUpdated(PlaybackStateCompat newState) {
        mMediaSession.setPlaybackState(newState);
    }


//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//
//        if (intent != null && intent.getAction() != null) {
//
//            switch (intent.getAction()) {
//                case ACTION_PLAY:
//                    mMediaController.getTransportControls().play();
//                    break;
//                case ACTION_FAST_FORWARD:
//                    mMediaController.getTransportControls().fastForward();
//                    break;
//                case ACTION_REWIND:
//                    mMediaController.getTransportControls().rewind();
//                    break;
//                case ACTION_PAUSE:
//                    mMediaController.getTransportControls().pause();
//                    break;
//            }
//        }
//
//        return super.onStartCommand(intent, flags, startId);
//    }


//    private void updateNotification() {
//
//        MediaDescriptionCompat description = mMediaController.getMetadata().getDescription();
//
//        String fetchArtUrl = null;
//        Bitmap art = null;
//        if (description.getIconUri() != null) {
//            // This sample assumes the iconUri will be a valid URL formatted String, but
//            // it can actually be any valid Android Uri formatted String.
//            // async fetch the album art icon
//            String artUrl = description.getIconUri().toString();
//            art = AlbumArtCache.getInstance().getBigImage(artUrl);
//            if (art == null) {
//                fetchArtUrl = artUrl;
//                // use a placeholder art while the remote art is being downloaded
////                art = BitmapFactory.decodeResource(mService.getResources(),
////                        R.drawable.ic_default_art);
//            }
//        }
//
//
//        Notification.Action playPauseAction = mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING ?
//                createAction(R.drawable.ic_action_pause, "Pause", ACTION_PAUSE) :
//                createAction(R.drawable.ic_action_play, "Play", ACTION_PLAY);
//
//        Notification.Builder notificationBuilder = new Notification.Builder(this);
//
//        notificationBuilder.setPriority(Notification.PRIORITY_DEFAULT)
//                .setVisibility(Notification.VISIBILITY_PUBLIC)
//                .setCategory(Notification.CATEGORY_TRANSPORT)
//                .setContentTitle(description.getTitle())
//                .setContentText(description.getSubtitle())
//                .setOngoing(mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING)
//                .setShowWhen(false)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setAutoCancel(false)
//                .addAction(createAction(R.drawable.ic_action_rewind, "Rewind", ACTION_REWIND))
//                .addAction(playPauseAction)
//                .addAction(createAction(R.drawable.ic_action_fast_forward, "Fast Forward", ACTION_FAST_FORWARD))
//                .setStyle(new Notification.MediaStyle()
//                        .setMediaSession(mMediaSession.getSessionToken())
//                        .setShowActionsInCompactView(1, 2))
//                .setLargeIcon(art);
//
//        if (fetchArtUrl != null) {
//            fetchBitmapFromURLAsync(fetchArtUrl, notificationBuilder);
//        }
//
//        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(1, notificationBuilder.build());
//    }

//    private void fetchBitmapFromURLAsync(final String bitmapUrl,
//                                         final Notification.Builder builder) {
//        AlbumArtCache.getInstance().fetch(bitmapUrl, new AlbumArtCache.FetchListener() {
//            @Override
//            public void onFetched(String artUrl, Bitmap bitmap, Bitmap icon) {
//                if (mMediaController.getMetadata() != null && mMediaController.getMetadata().getDescription().getIconUri() != null &&
//                        mMediaController.getMetadata().getDescription().getIconUri().toString().equals(artUrl)) {
//                    // If the media is still the same, update the notification:
//                    LogHelper.d(TAG, "fetchBitmapFromURLAsync: set bitmap to ", artUrl);
//                    builder.setLargeIcon(bitmap);
//                    ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(1, builder.build());
//                }
//            }
//        });
//    }
}
