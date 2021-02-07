package com.sm.music;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import java.io.IOException;

public class MusicPlayer extends Service {

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


        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel("1","1", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(getApplicationContext(),"1").build();
            startForeground(1, notification);
        }
        player = new MediaPlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.release();
    }

    public class musicBinder extends Binder {

        public boolean isPlaying(){
            return player.isPlaying();
        }

        public void start() {
            player.start();
        }

        public void pause(){
            player.pause();
        }

        public int getDuration(){
            return player.getDuration();
        }

        public int getCurrenPostion(){
            return player.getCurrentPosition();
        }

        public void seekTo(int mesc){
            player.seekTo(mesc);
        }

        public void setMusicUrl(String url){
            if (player != null){
                try {
                    player.reset();
                    player.setDataSource(url);
                    player.prepareAsync();
                    player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            mediaPlayer.start();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
