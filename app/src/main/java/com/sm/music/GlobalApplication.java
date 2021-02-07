package com.sm.music;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.sm.music.Bean.Music;

import java.util.List;

public class GlobalApplication extends Application {


    //This is current music id
    private String currentMusicId = null;
    //index search music list
    private List<Music> musicList = null;
    //music control
    MusicPlayer.musicBinder binder = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Intent musicIntent = new Intent(this, MusicPlayer.class);
        MusicPlayerConnection musicPlayerConnection = new MusicPlayerConnection();
        bindService(musicIntent, musicPlayerConnection, BIND_AUTO_CREATE);
        startService(musicIntent);
    }


    public String getCurrentMusicId() {
        return currentMusicId;
    }

    public void setCurrentMusicId(String currentMusicId) {
        this.currentMusicId = currentMusicId;
    }

    public List<Music> getMusicList() {
        return musicList;
    }

    public void setMusicList(List<Music> musicList) {
        this.musicList = musicList;
    }

    public void addMusicList(List<Music> musicList) {
        this.musicList.addAll(musicList);
    }

    public Boolean isMusicListEmpty(){
        return musicList == null;
    }

    public Boolean isPlaying(){
        return binder.isPlaying();
    }

    public void setMusicUrl(String url){
        binder.setMusicUrl(url);
    }



    private class MusicPlayerConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (MusicPlayer.musicBinder) service;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }
}
