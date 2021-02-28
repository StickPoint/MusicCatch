package com.sm.music.Server;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.sm.music.GlobalApplication;
import com.sm.music.R;

public class MusicPlayer extends Service {

    private static final String CHANNEL_NAME = "Music Notification Player";

    public static final String CHANNEL_ID = "com.sm.music.Player";

    public static final int NOTICFY_ID = 639;

    private GlobalApplication globalApplication = null;

    private MediaPlayer player = null;

    private static RemoteViews notificationPlayerView = null;

    private static NotificationManager playerNotificationManager = null;

    private static Notification playerNotification = null;

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



        notificationPlayerView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification_layout);
        notificationPlayerView.setImageViewBitmap(R.id.notice_pic, BitmapFactory.decodeResource(getResources(), R.mipmap.default_music_pic));
        playerNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            playerNotificationManager.createNotificationChannel(channel);
        }
        playerNotification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
//                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setWhen(0)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.FLAG_FOREGROUND_SERVICE)
                .setContent(notificationPlayerView)
                .setCustomContentView(notificationPlayerView)
                .setCustomBigContentView(notificationPlayerView)
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), PendingIntent.FLAG_NO_CREATE))
                .setOngoing(true)
                .build();
        playerNotification.contentView = notificationPlayerView;
        IntentFilter notificationPlayerBroadcastFilter = new IntentFilter();
        notificationPlayerBroadcastFilter.addAction(NotificationPlayerBroadcastReceiver.ACTION_PLAY_AND_STOP);
        notificationPlayerBroadcastFilter.addAction(NotificationPlayerBroadcastReceiver.ACTION_NEXT);
        notificationPlayerBroadcastFilter.addAction(NotificationPlayerBroadcastReceiver.ACTION_PREV);
        NotificationPlayerBroadcastReceiver notificationPlayerBroadcastReceiver = new NotificationPlayerBroadcastReceiver();
        registerReceiver(notificationPlayerBroadcastReceiver, notificationPlayerBroadcastFilter);

        Intent intentPlayAndStop = new Intent(NotificationPlayerBroadcastReceiver.ACTION_PLAY_AND_STOP);

        Intent intentPrev = new Intent(NotificationPlayerBroadcastReceiver.ACTION_NEXT);

        Intent intentNext = new Intent(NotificationPlayerBroadcastReceiver.ACTION_PREV);

        notificationPlayerView.setOnClickPendingIntent(R.id.notice_music_start_and_stop,
                PendingIntent.getBroadcast(getApplicationContext(), 0, intentPlayAndStop, PendingIntent.FLAG_UPDATE_CURRENT));

        notificationPlayerView.setOnClickPendingIntent(R.id.notice_music_prev,
                PendingIntent.getBroadcast(getApplicationContext(), 0, intentPrev, PendingIntent.FLAG_UPDATE_CURRENT));

        notificationPlayerView.setOnClickPendingIntent(R.id.notice_music_next,
                PendingIntent.getBroadcast(getApplicationContext(), 0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT));

        startForeground(NOTICFY_ID, playerNotification);

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

        public RemoteViews getNotificationPlayerView(){
            return notificationPlayerView;
        }
        public NotificationManager getPlayerNotificationManager(){
            return playerNotificationManager;
        }
        public Notification getPlayerNotification(){
            return playerNotification;
        }
    }


    public class NotificationPlayerBroadcastReceiver extends BroadcastReceiver {

        public static final String ACTION_PLAY_AND_STOP = "pp";
        public static final String ACTION_NEXT = "n";
        public static final String ACTION_PREV = "p";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (globalApplication.getCurrentMusic() != null){
                String act = intent.getAction();
                switch (act){
                    case ACTION_PLAY_AND_STOP:
                        if (globalApplication.isPlaying()){
                            globalApplication.musicPause();
                        }else {
                            globalApplication.musicPlay();
                        }
                        break;
                    case ACTION_NEXT:
                        globalApplication.next();
                        break;
                    case ACTION_PREV:
                        globalApplication.prev();
                        break;
                }
            }else {
                Toast.makeText(context, R.string.no_music_to_play, Toast.LENGTH_SHORT);
            }
        }
    }
}
