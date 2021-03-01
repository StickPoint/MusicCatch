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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.sm.music.Activity.IntentActivity;
import com.sm.music.Activity.PlayerActivity;
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

    private static NotificationCompat.Builder playerNotificationBuilder = null;

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

        NotificationPlayerBroadcastReceiver notificationPlayerBroadcastReceiver = new NotificationPlayerBroadcastReceiver();

        IntentFilter notificationPlayerBroadcastFilter = new IntentFilter();
        notificationPlayerBroadcastFilter.addAction(NotificationPlayerBroadcastReceiver.ACTION_PLAY_AND_STOP);
        notificationPlayerBroadcastFilter.addAction(NotificationPlayerBroadcastReceiver.ACTION_NEXT);
        notificationPlayerBroadcastFilter.addAction(NotificationPlayerBroadcastReceiver.ACTION_PREV);

        registerReceiver(notificationPlayerBroadcastReceiver, notificationPlayerBroadcastFilter);


        playerNotificationManager = getSystemService(NotificationManager.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Music play control");
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            playerNotificationManager.createNotificationChannel(channel);
        }


        playerNotificationBuilder = getPlayerNotificationBuilder(getApplicationContext(),
                BitmapFactory.decodeResource(getResources(), R.mipmap.default_music_pic),
                getResources().getString(R.string.no_music_to_play),
                getResources().getString(R.string.singer), false);

        startForeground(NOTICFY_ID, playerNotificationBuilder.build());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.release();
    }

    public static NotificationCompat.Builder getPlayerNotificationBuilder(Context context, Bitmap pic, String title, String text, Boolean isPlaying){
//        Intent resultIntent = new Intent(context, IntentActivity.class);
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//        stackBuilder.addNextIntentWithParentStack(resultIntent);
//        PendingIntent contentIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//        Intent in = new Intent(context, context.getClass());
//        in.setAction(Intent.ACTION_MAIN);
//        in.addCategory(Intent.CATEGORY_LAUNCHER);
//        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, in,PendingIntent.FLAG_UPDATE_CURRENT);

//        Intent resultIntent = new Intent(context, PlayerActivity.class);
//        PendingIntent contentIntent = PendingIntent.getActivity(context,0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent contentIntent = PendingIntent.getActivity(context,0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action action_prev = new NotificationCompat.Action(R.drawable.ic_min_prev, "next",
                PendingIntent.getBroadcast(context, 0, new Intent(NotificationPlayerBroadcastReceiver.ACTION_PREV), PendingIntent.FLAG_UPDATE_CURRENT));
        NotificationCompat.Action action_play_and_stop = new NotificationCompat.Action(isPlaying ? R.drawable.ic_min_stop : R.drawable.ic_min_play, "PlayAndStop",
                PendingIntent.getBroadcast(context, 0, new Intent(MusicPlayer.NotificationPlayerBroadcastReceiver.ACTION_PLAY_AND_STOP), PendingIntent.FLAG_UPDATE_CURRENT));
        NotificationCompat.Action action_next = new NotificationCompat.Action(R.drawable.ic_min_next, "next",
                PendingIntent.getBroadcast(context, 0, new Intent(NotificationPlayerBroadcastReceiver.ACTION_NEXT), PendingIntent.FLAG_UPDATE_CURRENT));

        androidx.media.app.NotificationCompat.MediaStyle mediaStyle = new androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(1);

        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setStyle(mediaStyle)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(title)
                .setContentText(text)
                .setLargeIcon(pic)

                .addAction(action_prev)
                .addAction(action_play_and_stop)
                .addAction(action_next)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.FLAG_FOREGROUND_SERVICE)
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), PendingIntent.FLAG_NO_CREATE))
                .setOngoing(true)
                .setShowWhen(false)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setChannelId(CHANNEL_ID)
                .setContentIntent(contentIntent);
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
        public NotificationCompat.Builder getPlayerNotification(){
            return playerNotificationBuilder;
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
