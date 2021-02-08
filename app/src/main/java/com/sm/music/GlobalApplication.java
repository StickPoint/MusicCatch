package com.sm.music;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.sm.music.Bean.Music;
import com.sm.music.Fragment.search_pager;
import com.sm.music.MusicUtils.GetMusic;

import java.util.List;

public class GlobalApplication extends Application {

    final static private int REQUEST_MUSIC_URL = 203;

    private Context context = null;
    //This is current music id
    private String currentMusicId = null;
    //index search music list
    private List<Music> musicList = null;
    //music control
    static MusicPlayer.musicBinder binder = null;

    private GetMusic conn = null;

    @Override
    public void onCreate() {
        super.onCreate();
        conn = new GetMusic();
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

    //music control

    public Boolean isPlaying(){
        return binder.isPlaying();
    }

    public void musicPlay(){
        binder.start();
    }

    public void musicPause(){
        binder.pause();
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.arg1 == GetMusic.RESPOND_SUCCESS ){

                if (msg.what == REQUEST_MUSIC_URL){
                    binder.setMusicUrl((String) msg.obj);
                }

            }else{
//                Toast.makeText(getActivity(),R.string.network_wrong,Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void setPlayMusic(final Music music){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = conn.getMusicPlayURL(music.getId(), music.getSource());
                    Message msg = Message.obtain();
                    if (url != null){
                        msg.what = REQUEST_MUSIC_URL;
                        msg.arg1 = GetMusic.RESPOND_SUCCESS;
                        msg.obj = url;

                        setCurrentMusicId(music.getId());

                    }else {
                        msg.what = GetMusic.RESPOND_TIMEOUT;
                    }
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                };
            }
        }).start();
    }

    public View getMinMusicPlayer(final Context context){
        View view = View.inflate(context, R.layout.music_player,null);
        final ImageView min_music_control = view.findViewById(R.id.min_music_control);
        ImageView musicPic = view.findViewById(R.id.musicPic);
        TextView current_music_name = view.findViewById(R.id.current_music_name);
        TextView current_music_time = view.findViewById(R.id.current_music_time);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getCurrentMusicId() != null){
                    context.startActivity(new Intent(context, Player.class));
                    //TODO: To shart music player and post some arguments

                }else {
                    Toast.makeText(context, R.string.no_music_to_play, Toast.LENGTH_SHORT).show();
                }
            }
        });
        min_music_control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getCurrentMusicId() != null){
                    if (isPlaying()){
                        min_music_control.setImageResource(R.drawable.ic_play);
                        musicPause();
                    }else {
                        min_music_control.setImageResource(R.drawable.ic_stop);
                        musicPlay();
                    }
                }else {
                    Toast.makeText(context, R.string.no_music_to_play, Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    public static class MusicPlayerConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (MusicPlayer.musicBinder) service;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }
}
