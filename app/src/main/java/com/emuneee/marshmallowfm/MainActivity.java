package com.emuneee.marshmallowfm;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ServiceConnection {

    private MediaController mMediaController;
    private ImageButton mPlayButton;
    private TextView mTitle;

    private MediaController.Callback mMediaControllerCallback = new MediaController.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackState state) {

            switch (state.getState()) {
                case PlaybackState.STATE_NONE:
                    mPlayButton.setImageResource(R.mipmap.ic_play);
                    break;
                case PlaybackState.STATE_PLAYING:
                    mPlayButton.setImageResource(R.mipmap.ic_pause);
                    break;
                case PlaybackState.STATE_PAUSED:
                    mPlayButton.setImageResource(R.mipmap.ic_play);
                    break;
                case PlaybackState.STATE_FAST_FORWARDING:
                    break;
                case PlaybackState.STATE_REWINDING:
                    break;
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            super.onMetadataChanged(metadata);
            mTitle.setText(metadata.getString(MediaMetadata.METADATA_KEY_TITLE));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPlayButton = (ImageButton) findViewById(R.id.play);
        mTitle = (TextView) findViewById(R.id.title);
        mPlayButton.setOnClickListener(this);
        findViewById(R.id.rewind).setOnClickListener(this);
        findViewById(R.id.forward).setOnClickListener(this);

        Intent intent = new Intent(this, AudioPlayerService.class);
        getApplicationContext().bindService(intent, this, 0);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.play:
                if (mMediaController.getPlaybackState().getState() == PlaybackState.STATE_PLAYING) {
                    mMediaController.getTransportControls().pause();
                } else if(mMediaController.getPlaybackState().getState() == PlaybackState.STATE_PAUSED){
                    mMediaController.getTransportControls().play();
                } else {
                    Uri uri = Uri.parse("https://api.soundcloud.com/tracks/300554964/stream?client_id=3ed8237e8a4bfc63db818a732c95bc38");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Bundle bundle = new Bundle();
                        bundle.putString(MediaMetadata.METADATA_KEY_ARTIST, "Zion.T");
                        bundle.putString(MediaMetadata.METADATA_KEY_TITLE, "Complex");
                        bundle.putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, "https://i1.sndcdn.com/artworks-000200892021-b4eaaw-large.jpg");
                        mMediaController.getTransportControls().playFromUri(uri, bundle);
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putString(MediaMetadata.METADATA_KEY_ARTIST, "Zion.T");
                        bundle.putString(MediaMetadata.METADATA_KEY_TITLE, "Complex");
                        bundle.putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, "https://i1.sndcdn.com/artworks-000200892021-b4eaaw-large.jpg");
                        mMediaController.getTransportControls().playFromUri(uri, bundle);
                    }


                }
                break;
            case R.id.rewind:
                mMediaController.getTransportControls().rewind();
                break;
            case R.id.forward:
                mMediaController.getTransportControls().fastForward();
                break;
        }

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        if (service instanceof AudioPlayerService.ServiceBinder) {
            mMediaController = new MediaController(MainActivity.this,
                    ((AudioPlayerService.ServiceBinder) service).getService().getMediaSessionToken());
            mMediaController.registerCallback(mMediaControllerCallback);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, AudioPlayerService.class);
        startService(intent);
    }
}
