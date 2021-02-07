package com.sm.music;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import java.io.IOException;

public class MusicPlayer extends Service {

    private MediaPlayer player = null;

    private String url = null;

    public MusicPlayer() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player = new MediaPlayer();
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
