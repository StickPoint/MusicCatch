package com.sm.music.Server;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.DecoratedMediaCustomViewStyle;

import com.sm.music.Activity.PlayerActivity;
import com.sm.music.GlobalApplication;
import com.sm.music.R;

import java.io.IOException;

public class MusicPlayer extends Service {

    private static final String CHANNEL_NAME = "Music Notification Player";

    private static final String CHANNEL_ID = "com.sm.music.Player";

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

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(channel);
        }
        Notification builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
//                .setCustomContentView(new RemoteViews(getPackageName(), R.layout.notification_layout))
                .setWhen(SystemClock.currentThreadTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setPriority(Notification.VISIBILITY_PUBLIC)
//                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, PlayerActivity.class), PendingIntent.FLAG_NO_CREATE))
                .setOngoing(true)
                .build();
        startForeground(1, builder);
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
    }

}
