package com.sm.music;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.sm.music.Activity.PlayerActivity;
import com.sm.music.Bean.Music;
import com.sm.music.MusicUtils.GetMusic;
import com.sm.music.Server.MusicPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalApplication extends Application {



    final static private int REQUEST_MUSIC_URL = 203;
    //updata frequency ms
    final static private int UPDATA_FREQUENCY = 500;

    private Context context = null;
    //This is current music id
    private Music currentMusic = null;
    //This is current music duration
    private String currentMusicDuration = "";
    //index search music list
    private List<Music> musicList = new ArrayList<Music>();
    //min music player list
    private static Map<Integer,View> minMusicPlayerList = new HashMap<>();
    //MediaPlayer control
    private static MediaPlayer player = null;
    //updata thread
    private static Thread updataThread = null;
    //updata thread handler
    private Handler updata_thread_handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            updataPlayer();
        }
    };


    private GetMusic conn = null;

    @Override
    public void onCreate() {
        super.onCreate();
        conn = new GetMusic();
        Intent musicIntent = new Intent(getApplicationContext(), MusicPlayer.class);
        GlobalApplication.MusicPlayerConnection musicPlayerConnection = new GlobalApplication.MusicPlayerConnection();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(musicIntent);
        } else {
            startService(musicIntent);
        }
        bindService(musicIntent, musicPlayerConnection, BIND_AUTO_CREATE);
    }


    public Music getCurrentMusic() {
        return currentMusic;
    }

    public void addMusicList(Music musicList) {
        this.musicList.add(musicList);
    }

    public void addMusicList(List<Music> musicList) {
        this.musicList.addAll(musicList);
    }

    public List<Music> getMusicList() {
        return musicList;
    }

    public Boolean isMusicListEmpty(){
        return musicList.isEmpty();
    }

    //music control

    private Boolean isPlaying(){
        return player.isPlaying();
    }

    private void musicPlay(){
        player.start();
        updataPlayer();
    }

    private void musicPause(){
        player.pause();
        updataPlayer();
    }

    private void updataPlayer(){
        updataMinMusicPlayer();
        updataMusicPlayer();
    }

    private void updataMinMusicPlayer(){
        int currentPosition = player.getCurrentPosition() / 1000;
        String currentMusicPosition = ((currentPosition / 60) >= 10 ? String.valueOf(currentPosition / 60) : ("0" + (currentPosition / 60))) +
                ":" + ((currentPosition % 60)  >= 10 ? String.valueOf(currentPosition % 60) : ("0" + (currentPosition % 60)));
        for (Map.Entry<Integer,View> i : minMusicPlayerList.entrySet()) {
            View view = i.getValue();
            ImageView min_music_control = view.findViewById(R.id.min_music_control);
            if (isPlaying()){
                min_music_control.setImageResource(R.drawable.ic_stop);
            }else {
                min_music_control.setImageResource(R.drawable.ic_play);
            }
            ImageView musicPic = view.findViewById(R.id.musicPic);
            TextView current_music_name = view.findViewById(R.id.current_music_name);
            current_music_name.setText(currentMusic.getName());
            TextView current_music_time = view.findViewById(R.id.current_music_time);
            current_music_time.setText( currentMusicPosition + "/" + currentMusicDuration);
        }
    }

    private void updataMusicPlayer(){
    }

    //user

    public void setCurrentMusic(final Music music){
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.arg1 == GetMusic.RESPOND_SUCCESS ){

                    if (msg.what == REQUEST_MUSIC_URL){
                        try {
                            player.reset();
                            player.setDataSource((String) msg.obj);
                            player.prepareAsync();
                            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mediaPlayer) {
                                    if (updataThread != null){
                                        updataThread.interrupt();
                                    }
                                    mediaPlayer.start();
                                    int currentDuration = mediaPlayer.getDuration() / 1000;
                                    currentMusicDuration = ((currentDuration / 60) >= 10 ? String.valueOf(currentDuration / 60) : ("0" + (currentDuration / 60))) +
                                            ":" + ((currentDuration % 60)  >= 10 ? String.valueOf(currentDuration % 60) : ("0" + (currentDuration % 60)));
                                    updataThread = new UpdataThread();
                                    updataThread.start();
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }else{
                    Toast.makeText(getApplicationContext(),R.string.network_wrong,Toast.LENGTH_SHORT).show();
                }
            }
        };
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

                        currentMusic = music;
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

    public View createMinMusicPlayer(final Context context, int tag){
        View view = View.inflate(context, R.layout.music_player,null);
        final ImageView min_music_control = view.findViewById(R.id.min_music_control);
        ImageView musicPic = view.findViewById(R.id.musicPic);
        TextView current_music_name = view.findViewById(R.id.current_music_name);
        TextView current_music_time = view.findViewById(R.id.current_music_time);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getCurrentMusic() != null){
                    context.startActivity(new Intent(context, PlayerActivity.class));
                    //TODO: To shart music player and post some arguments

                }else {
                    Toast.makeText(context, R.string.no_music_to_play, Toast.LENGTH_SHORT).show();
                }
            }
        });
        min_music_control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getCurrentMusic() != null){
                    if (isPlaying()){
                        min_music_control.setImageResource(R.drawable.ic_stop);
                        musicPause();
                    }else {
                        min_music_control.setImageResource(R.drawable.ic_play);
                        musicPlay();
                    }
                }else {
                    Toast.makeText(context, R.string.no_music_to_play, Toast.LENGTH_SHORT).show();
                }
            }
        });
        minMusicPlayerList.put(Integer.valueOf(tag), view);
        return view;
    }

    public void destroyMinMusicPlayer(int tag){
        minMusicPlayerList.remove(Integer.valueOf(tag));
    }

    private static class MusicPlayerConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            player = ((MusicPlayer.musicBinder) service).getPlayer();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }

    private class UpdataThread extends Thread{
        @Override
        public void run() {
            while (!isInterrupted()){
                updata_thread_handler.sendMessage(Message.obtain());
                try {
                    sleep(UPDATA_FREQUENCY);
                } catch (InterruptedException e) {
                    break;
                }

            }
        }
    }
}
