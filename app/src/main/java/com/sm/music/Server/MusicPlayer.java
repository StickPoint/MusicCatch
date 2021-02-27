package com.sm.music.Server;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.DecoratedMediaCustomViewStyle;

import com.sm.music.Activity.MainActivity;
import com.sm.music.Activity.PlayerActivity;
import com.sm.music.GlobalApplication;
import com.sm.music.R;

import java.io.IOException;

public class MusicPlayer extends Service {

    private static final String CHANNEL_NAME = "Music Notification Player";

    public static final String CHANNEL_ID = "com.sm.music.Player";

    public static final int NOTICFY_ID = 639;

    private GlobalApplication globalApplication = null;

    private MediaPlayer player = null;

    public MusicPlayer() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new musicBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        globalApplication = (GlobalApplication) getApplication();
        player = new MediaPlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.release();
    }

    public class musicBinder extends Binder {
        public MediaPlayer getPlayer(){
            return player;
        }

        public MusicPlayer getServer(){return MusicPlayer.this;}
    }


}
