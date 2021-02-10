package com.sm.music;

import android.app.Activity;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.sm.music.Activity.PlayerActivity;
import com.sm.music.Bean.Music;
import com.sm.music.MusicUtils.GetMusic;
import com.sm.music.Override.UnclickableHorizontalScrollView;
import com.sm.music.Server.MusicPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalApplication extends Application {



    final static private int REQUEST_MUSIC_URL = 203;
    //updata frequency ms
    final static private int UPDATA_FREQUENCY = 1000;
    //smooth frequency ms
    final static private int SMOOTH_FREQUENCY = 70;

    private Context context = null;
    //This is current music id
    private Music currentMusic = null;
    //This is current music duration
    private String currentMusicDuration = "";
    //index search music list
    private List<Music> musicList = new ArrayList<Music>();
    //min music player list
    private static Map<Integer,View> minMusicPlayerList = new HashMap<>();
    private static View musicPlayerPageView = null;
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
        updataPlayer();
        int currentDuration = player.getDuration() / 1000;
        currentMusicDuration = ((currentDuration / 60) >= 10 ? String.valueOf(currentDuration / 60) : ("0" + (currentDuration / 60))) +
                ":" + ((currentDuration % 60)  >= 10 ? String.valueOf(currentDuration % 60) : ("0" + (currentDuration % 60)));
        player.start();
        updataThread = new UpdataThread();
        updataThread.start();
    }

    private void musicPause(){
        if (updataThread != null){
            updataThread.interrupt();
            updataThread = null;
        }
        if (player.isPlaying())
            player.pause();
        updataPlayer();
    }

    private void initPlayerOnMusic(){
        for (Map.Entry<Integer,View> i : minMusicPlayerList.entrySet()) {
            initMinPlayerOnMusic(i.getValue());
        }
        initPagePlayerOnMusic();
    }

    private void initMinPlayerOnMusic(View view){
        if (currentMusic != null){

            ImageView musicPic = view.findViewById(R.id.musicPic);

            TextView current_music_name = view.findViewById(R.id.current_music_name);
            current_music_name.setText(currentMusic.getName());

            TextView current_music_singer = view.findViewById(R.id.current_music_singer);
            String temp = "";
            for (int j = 0; j < currentMusic.getArtist().length; j++) {
                if (j == 0) {
                    temp += currentMusic.getArtist()[j];
                } else {
                    temp += "/" + currentMusic.getArtist()[j];
                }
            }
            current_music_singer.setText(temp);
        }
        ImageView min_music_control = view.findViewById(R.id.min_music_control);
        ProgressBar minPlayerProgress = view.findViewById(R.id.minPlayerProgress);
        if (player != null){
            if (isPlaying()){
                min_music_control.setImageResource(R.drawable.ic_stop);
            }else {
                min_music_control.setImageResource(R.drawable.ic_play);
            }
        }
    }

    private void initPagePlayerOnMusic(){
        if (musicPlayerPageView != null && currentMusic != null){
            ImageView StartAndStop = musicPlayerPageView.findViewById(R.id.StartAndStop);
            if (isPlaying()){
                StartAndStop.setImageResource(R.drawable.ic_stop);
            }else {
                StartAndStop.setImageResource(R.drawable.ic_play);
            }

            ImageView musicPic = musicPlayerPageView.findViewById(R.id.musicPic);

            TextView player_musicName = musicPlayerPageView.findViewById(R.id.player_musicName);
            player_musicName.setText(currentMusic.getName());

            TextView player_musicInfo_singer = musicPlayerPageView.findViewById(R.id.player_musicInfo_singer);
            String temp = "";
            for (int j = 0; j < currentMusic.getArtist().length; j++) {
                if (j == 0) {
                    temp += currentMusic.getArtist()[j];
                } else {
                    temp += "/" + currentMusic.getArtist()[j];
                }
            }
            player_musicInfo_singer.setText(temp);

            TextView player_musicInfo_album = musicPlayerPageView.findViewById(R.id.player_musicInfo_album);
            player_musicInfo_album.setText(currentMusic.getAlbum());

            TextView duration = musicPlayerPageView.findViewById(R.id.duration);
            int id = player.getDuration() / 1000;
            duration.setText((id / 60 >= 10 ? String.valueOf(id / 60) : "0" + id / 60) + ":" +
                    (id % 60 >= 10 ? String.valueOf(id % 60) : "0" + id % 60));

            SeekBar music_seekBar = musicPlayerPageView.findViewById(R.id.music_seekBar);
            music_seekBar.setMax(100);
            music_seekBar.setProgress(0);
        }
    }

    private void updataPlayer(){
        updataMinMusicPlayer();
        updataMusicPlayer();
    }

    private void updataMinMusicPlayer(){
        for (Map.Entry<Integer,View> i : minMusicPlayerList.entrySet()) {
            View view = i.getValue();

            ImageView min_music_control = view.findViewById(R.id.min_music_control);
            if (isPlaying()){
                min_music_control.setImageResource(R.drawable.ic_stop);
            }else {
                min_music_control.setImageResource(R.drawable.ic_play);
            }

            UnclickableHorizontalScrollView minPlayer_title = view.findViewById(R.id.minPlayer_title);
            int innerWidth = minPlayer_title.findViewById(R.id.minPlayer_title_container).getWidth();
            int scrollViewWidth =  minPlayer_title.getWidth();
            if (scrollViewWidth < innerWidth){
                minPlayer_title.smoothScrollBy(SMOOTH_FREQUENCY,0);
                if ((innerWidth - scrollViewWidth) <= minPlayer_title.getScrollX()){
                    minPlayer_title.fullScroll(View.SCROLLBAR_POSITION_LEFT);
                }
            }

            ProgressBar minPlayerProgress = view.findViewById(R.id.minPlayerProgress);
            if (player.getDuration() != 0){
                minPlayerProgress.setMax(player.getDuration());
                minPlayerProgress.setProgress(player.getCurrentPosition());
            }
        }
    }

    private void updataMusicPlayer(){
        if (musicPlayerPageView != null){
            SeekBar music_seekBar = musicPlayerPageView.findViewById(R.id.music_seekBar);
            if (player.getDuration() != 0){
                music_seekBar.setMax(player.getDuration());
                music_seekBar.setProgress(player.getCurrentPosition());
            }

            ImageView StartAndStop = musicPlayerPageView.findViewById(R.id.StartAndStop);
            if (isPlaying()){
                StartAndStop.setImageResource(R.drawable.ic_stop);
            }else {
                StartAndStop.setImageResource(R.drawable.ic_play);
            }

            TextView postion = musicPlayerPageView.findViewById(R.id.postion);
            int ic = player.getCurrentPosition() / 1000;
            postion.setText((ic / 60 >= 10 ? String.valueOf(ic / 60) : "0" + ic / 60) + ":" +
                    (ic % 60 >= 10 ? String.valueOf(ic % 60) : "0" + ic % 60));

        }
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
                                    initPlayerOnMusic();
                                    musicPlay();
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
        if (currentMusic != null && currentMusic.equals(music)){
            musicPlay();
        }else {
            currentMusic = music;
            musicPause();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg = Message.obtain();
                    try {
                        String id = music.getId();
                        String url = conn.getMusicPlayURL(id, music.getSource());
                        if (id.equals(currentMusic.getId())){
                            if (url != null){
                                msg.what = REQUEST_MUSIC_URL;
                                msg.arg1 = GetMusic.RESPOND_SUCCESS;
                                msg.obj = url;

                            }else {
                                msg.what = GetMusic.RESPOND_TIMEOUT;
                            }
                        }
                    } catch (Exception e) {
                        msg.what = GetMusic.RESPOND_TIMEOUT;
                        e.printStackTrace();
                    };
                    handler.sendMessage(msg);
                }
            }).start();
        }
    }

    public void setMusicPlayerPageView(View view){
        ImageView StartAndStop = view.findViewById(R.id.StartAndStop);
        StartAndStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying()){
                    musicPause();
                }else {
                    musicPlay();
                }
            }
        });

        SeekBar music_seekBar = view.findViewById(R.id.music_seekBar);
        music_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    player.seekTo(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        musicPlayerPageView = view;
        initPagePlayerOnMusic();
    }

    public View createMinMusicPlayer(final Activity activity, int tag){
        View view = View.inflate(activity, R.layout.min_music_player,null);
        final ImageView min_music_control = view.findViewById(R.id.min_music_control);
        ImageView musicPic = view.findViewById(R.id.musicPic);
        TextView current_music_name = view.findViewById(R.id.current_music_name);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getCurrentMusic() != null){
                    activity.startActivity(new Intent(activity, PlayerActivity.class));
                    activity.overridePendingTransition(R.anim.transfrom_buttom_in,R.anim.no_transfrom);
                    //TODO: To shart music player and post some arguments

                }else {
                    Toast.makeText(activity, R.string.no_music_to_play, Toast.LENGTH_SHORT).show();
                }
            }
        });
        min_music_control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getCurrentMusic() != null){
                    if (isPlaying()){
                        musicPause();
                    }else {
                        musicPlay();
                    }
                }else {
                    Toast.makeText(activity, R.string.no_music_to_play, Toast.LENGTH_SHORT).show();
                }
            }
        });
        UnclickableHorizontalScrollView minPlayer_title = view.findViewById(R.id.minPlayer_title);
        initMinPlayerOnMusic(view);
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
