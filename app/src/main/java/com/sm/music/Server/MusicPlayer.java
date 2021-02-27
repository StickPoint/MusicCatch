package com.sm.music.Server;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import java.util.ArrayList;
import java.util.List;

public class MusicPlayer extends MediaBrowserServiceCompat {

    private static final String MEDIA_ROOT_ID = "StickPointMediaID";
    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";
    private static final String LOG_TAG = "MusicPlayerService:";

    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat playerState;

    private MediaPlayer player = null;

    @Override
    public void onCreate() {
        super.onCreate();
        playerState = new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PAUSED,0,1.0f).build();
        mediaSession = new MediaSessionCompat(getApplicationContext(), LOG_TAG);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setPlaybackState(playerState);
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                if(playerState.getState() == PlaybackStateCompat.STATE_PAUSED){
                    player.start();
                    playerState = new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PLAYING,0,1.0f).build();
                    mediaSession.setPlaybackState(playerState);
                }
            }

            @Override
            public void onPause() {
                super.onPause();
                if(playerState.getState() == PlaybackStateCompat.STATE_PLAYING){
                    player.pause();
                    playerState = new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PAUSED,0,1.0f).build();
                    mediaSession.setPlaybackState(playerState);
                }
            }
        });
        setSessionToken(mediaSession.getSessionToken());

    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot(MEDIA_ROOT_ID, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        if (TextUtils.equals(MY_EMPTY_MEDIA_ROOT_ID, parentId)) {
            result.sendResult(null);
            return;
        }
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        if (MEDIA_ROOT_ID.equals(parentId)) {
            // Build the MediaItem objects for the top level,
            // and put them in the mediaItems list...
        } else {
            // Examine the passed parentMediaId to see which submenu we're at,
            // and put the children of that menu in the mediaItems list...
        }
        result.sendResult(mediaItems);
    }
}
