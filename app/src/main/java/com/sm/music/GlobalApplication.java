package com.sm.music;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.ColorFilter;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.core.app.NotificationCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestOptions;
import com.sm.music.Activity.PlayerActivity;
import com.sm.music.Bean.Music;
import com.sm.music.Listener.OnMusicChange;
import com.sm.music.MusicUtils.GetMusic;
import com.sm.music.MusicUtils.RecentPlay;
import com.sm.music.Override.UnclickableHorizontalScrollView;
import com.sm.music.SQL.SQLUtils;
import com.sm.music.Server.MusicPlayer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import jp.wasabeef.glide.transformations.BlurTransformation;
import me.wcy.lrcview.LrcView;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;


public class GlobalApplication extends Application {

    private static final String LOG_TAG = "GlobalApplication";

    private static final String UPDATE_INFO_URL = "https://download.micronnetwork.com/ddmusic/ddmusicUpdata.json";

    private static final int REQUEST_MUSIC_PIC = 159;
    private static final int REQUEST_MUSIC_PIC_DEFAULT = 160;

    final static private int REQUEST_MUSIC_URL = 203;

    private static final int REQUEST_MUSIC_LYRIC = 927;

    //updata frequency ms
    final static private int UPDATA_FREQUENCY = 1000;
    //smooth frequency ms
    final static private int SMOOTH_FREQUENCY = 70;

    private static final int MUSIC_LOOP_CONTROL_REANDOM = 902;
    private static final int MUSIC_LOOP_CONTROL_LOOP = 139;
    private static final int MUSIC_LOOP_CONTROL_SINGLE = 129;


    private static final String CHANNEL_NAME = "Music Notification Player";

    public static final String CHANNEL_ID = "com.sm.music.Player";

    public static final int NOTICFY_ID = 639;


    private static String getFileLineInfo(){
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[2];

        return " (" + e.getFileName() + " at line " + e.getLineNumber() + ")";
    }

    private Bitmap current_music_pic = null;
    private String current_music_lrc = null;


    private Context context = null;

    private String currentMusicDuration;

    //This is current music id
    private int last_can_play_music_postion = 0;
    private Music last_can_play_music = null;
    private Music currentMusic = null;

    private int music_loop_method = MUSIC_LOOP_CONTROL_LOOP;

    private int currentMusicIndexInMusicList = 0;
    //index search music list
    private List<Music> musicList = new ArrayList<Music>();
    //min music player list
    private static Map<Integer, View> minMusicPlayerList = new HashMap<>();
    private static View musicPlayerPageView = null;
    //MediaPlayer control
    private static MediaPlayer player = null;
    //updata thread
    private static Thread updataThread = null;
    //updata thread handler
    private Handler updata_thread_handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            updateplayer();
        }
    };

    private GetMusic conn = null;
    private SQLUtils sqlUtils = null;

    private static OnMusicChange onMusicChange = null;

    private static List<View> player_content_view = new ArrayList<>();

    //Notification

    private static RemoteViews notificationPlayerView = null;

    private static NotificationManager playerNotificationManager = null;

    private static NotificationCompat.Builder playerNotificationBuilder = null;

    //Application

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void init(){
        conn = new GetMusic();
//        sqlUtils = new SQLUtils();
        Log.i(LOG_TAG, "get recent music list.");
        musicList = RecentPlay.getRecentPlayMusic(getApplicationContext());
        Intent musicIntent = new Intent(getApplicationContext(), MusicPlayer.class);
        GlobalApplication.MusicPlayerConnection musicPlayerConnection = new GlobalApplication.MusicPlayerConnection();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(musicIntent);
        } else {
            startService(musicIntent);
        }
        bindService(musicIntent, musicPlayerConnection, BIND_AUTO_CREATE);
        Log.i(LOG_TAG, "starting music service ...");
    }

    public void appEnd(){

    }

    //music control

    public Boolean isPlaying(){
        return player.isPlaying();
    }//

    public void musicPlay(){
        int currentDuration = player.getDuration() / 1000;
        currentMusicDuration = ((currentDuration / 60) >= 10 ? String.valueOf(currentDuration / 60) : ("0" + (currentDuration / 60))) +
                ":" + ((currentDuration % 60)  >= 10 ? String.valueOf(currentDuration % 60) : ("0" + (currentDuration % 60)));
        player.start();
        if (updataThread != null) {
            updataThread.interrupt();
            updataThread = null;
        }
        updataThread = new UpdataThread();
        updataThread.start();

        if (musicPlayerPageView != null) {
            ImageView StartAndStop = musicPlayerPageView.findViewById(R.id.StartAndStop);
            StartAndStop.setImageResource(R.drawable.ic_stop_dark);
        }
        if (playerNotificationManager != null) {
            initNotificationPlayerOnMusic();
        }

        for (Map.Entry<Integer,View> view : minMusicPlayerList.entrySet()) {
            ImageView min_music_control = view.getValue().findViewById(R.id.min_music_control);
                min_music_control.setImageResource(R.drawable.ic_stop);
        }
        Log.i(LOG_TAG, "music " + currentMusic.getName() + " start");
    }//

    public void musicPause(){
        if (updataThread != null) {
            updataThread.interrupt();
            updataThread = null;
        }
        if (player.isPlaying())
            player.pause();

        if (musicPlayerPageView != null) {
            ImageView StartAndStop = musicPlayerPageView.findViewById(R.id.StartAndStop);
            StartAndStop.setImageResource(R.drawable.ic_play_dark);
        }
        if (playerNotificationManager != null) {
            initNotificationPlayerOnMusic();
        }
        for (Map.Entry<Integer,View> view : minMusicPlayerList.entrySet()) {
            ImageView min_music_control = view.getValue().findViewById(R.id.min_music_control);
                min_music_control.setImageResource(R.drawable.ic_play);
        }
        Log.i(LOG_TAG, "music " + ( currentMusic == null ? "" : currentMusic.getName()) + " pause");
    }//

    public void next(){
        if (musicList != null){
            int next = (currentMusicIndexInMusicList + 1) % musicList.size();
            switch (music_loop_method){
                case MUSIC_LOOP_CONTROL_REANDOM:
                    Log.i(LOG_TAG, "to set next music of shuffle with click");
                    setCurrentMusic(musicList.get((new Random().nextInt(musicList.size()))),false, true);
                    break;
                case MUSIC_LOOP_CONTROL_LOOP:
                    Log.i(LOG_TAG, "to set next music of repeat all with click");
                    setCurrentMusic(musicList.get(next),false, true);
                    break;
                case MUSIC_LOOP_CONTROL_SINGLE:
                    Log.i(LOG_TAG, "to set next music of repeat single with click");
                    setCurrentMusic(musicList.get(next),false, true);
                    break;
            }
        }
        Log.i(LOG_TAG, "music next");
    }//

    public void prev(){
        if (musicList != null){
            int next = (currentMusicIndexInMusicList + musicList.size() - 1) % musicList.size();
            switch (music_loop_method){
                case MUSIC_LOOP_CONTROL_REANDOM:
                    Log.i(LOG_TAG, "to set prev music of shuffle with click");
                    setCurrentMusic(musicList.get((new Random().nextInt(musicList.size()))),false, true);
                    break;
                case MUSIC_LOOP_CONTROL_LOOP:
                    Log.i(LOG_TAG, "to set prev music of repeat all with click");
                    setCurrentMusic(musicList.get(next),false, true);
                    break;
                case MUSIC_LOOP_CONTROL_SINGLE:
                    Log.i(LOG_TAG, "to set prev music of repeat single with click");
                    setCurrentMusic(musicList.get(next),false, true);
                    break;
            }
        }
        Log.i(LOG_TAG, "music prev");
    }//

    private void setPlayerLoopControl(int tag){
        Log.i(LOG_TAG, "to set music repeat mode");
        music_loop_method = tag;
        switch (tag){
            case MUSIC_LOOP_CONTROL_REANDOM:
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Log.i(LOG_TAG, "to set next music of shuffle with complete playing");
                        if (musicList.size() == 1){
                            mp.seekTo(0);
                            mp.start();
                        }else {
                            setCurrentMusic(musicList.get((new Random().nextInt(musicList.size()))),false, true);
                        }
                    }
                });
                break;
            case MUSIC_LOOP_CONTROL_LOOP:
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Log.i(LOG_TAG, "to set next music of repeat all with complete playing");
                        int next = 0;
                        if (musicList.size() > 1) {
                            next = (currentMusicIndexInMusicList + 1) % musicList.size();
                            setCurrentMusic(musicList.get(next), false, true);
                        }else {
                            mp.seekTo(0);
                            mp.start();
                        }

                    }
                });
                break;
            case MUSIC_LOOP_CONTROL_SINGLE:
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Log.i(LOG_TAG, "to set music of repeat single with complete playing");
                        mp.seekTo(0);
                        mp.start();
                    }
                });
                break;
        }
    }//

    private void initPlayerOnMusic(){
        for (Map.Entry<Integer,View> i : minMusicPlayerList.entrySet()) {
            initMinPlayerOnMusic(i.getValue());
        }
        if (musicPlayerPageView != null){
            initPagePlayerOnMusic();
        }
        if (playerNotificationManager != null){
            initNotificationPlayerOnMusic();
        }
    }

    private void initMinPlayerOnMusic(View view){
        if (currentMusic != null){

            if (current_music_pic != null){
                ImageView musicPic = view.findViewById(R.id.musicPic);
                musicPic.setImageBitmap(current_music_pic);
            }

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
                StartAndStop.setImageResource(R.drawable.ic_stop_dark);
            }else {
                StartAndStop.setImageResource(R.drawable.ic_play_dark);
            }

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

            ImageView music_loop_control = musicPlayerPageView.findViewById(R.id.music_loop_control);
            switch (music_loop_method){
                case MUSIC_LOOP_CONTROL_REANDOM:
                    music_loop_control.setImageResource(R.drawable.ic_random);
                    break;
                case MUSIC_LOOP_CONTROL_LOOP:
                    music_loop_control.setImageResource(R.drawable.ic_loop);
                    break;
                case MUSIC_LOOP_CONTROL_SINGLE:
                    music_loop_control.setImageResource(R.drawable.ic_single);
                    break;
            }

            TextView postion = musicPlayerPageView.findViewById(R.id.postion);
            int ic = player.getCurrentPosition() / 1000;
            postion.setText((ic / 60 >= 10 ? String.valueOf(ic / 60) : "0" + ic / 60) + ":" +
                    (ic % 60 >= 10 ? String.valueOf(ic % 60) : "0" + ic % 60));

            SeekBar music_seekBar = musicPlayerPageView.findViewById(R.id.music_seekBar);
            music_seekBar.setMax(player.getDuration());
            music_seekBar.setProgress(player.getCurrentPosition());

            ImageView pic_bg = musicPlayerPageView.findViewById(R.id.pic_bg);
            ImageView music_pic_view = player_content_view.get(0).findViewById(R.id.music_pic);
            if (current_music_pic != null){
                music_pic_view.setImageBitmap(current_music_pic);
                Glide.with(musicPlayerPageView)
                        .load(current_music_pic)
                        .apply(RequestOptions.bitmapTransform(new BlurTransformation(25,2)))
                        .into(pic_bg);
            }else {
                music_pic_view.setImageResource(R.mipmap.default_music_pic);
                pic_bg.setImageResource(R.mipmap.default_pic_bg);
            }


            LrcView music_lrc_view = player_content_view.get(1).findViewById(R.id.music_lrc);
            if (current_music_lrc !=  null){
                music_lrc_view.loadLrc(current_music_lrc);
            }


        }
    }

    private void initNotificationPlayerOnMusic(){
        Bitmap b = null;
        if (current_music_pic != null){
            b = current_music_pic;
        }else {
            b = BitmapFactory.decodeResource(getResources(), R.mipmap.default_music_pic);
        }
        String singer = "";
        if (currentMusic != null){
            for (int j = 0; j < currentMusic.getArtist().length; j++) {
                if (j == 0) {
                    singer += currentMusic.getArtist()[j];
                } else {
                    singer += "/" + currentMusic.getArtist()[j];
                }
            }
        }else {
            singer = getResources().getString(R.string.singer);
        }
        String name = getResources().getString(R.string.no_music_to_play);
        if (currentMusic != null){
            name = currentMusic.getName();
        }
        playerNotificationManager.notify(NOTICFY_ID,
                MusicPlayer.getPlayerNotificationBuilder(getApplicationContext(),b,name,singer, isPlaying()).build());
    }

    private void updateplayer(){
        for (Map.Entry<Integer,View> i : minMusicPlayerList.entrySet()) {
            updateMinMusicPlayer(i.getValue());
        }
        if (musicPlayerPageView != null) {
            updateMusicPlayer();
        }
        if (notificationPlayerView != null && playerNotificationManager != null && playerNotificationBuilder != null){
            updateNotificationPlayerOnMusic();
        }
    }

    private void updateMinMusicPlayer(View v){
        View view = v;


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

    private void updateMusicPlayer(){
        SeekBar music_seekBar = musicPlayerPageView.findViewById(R.id.music_seekBar);
        music_seekBar.setProgress(player.getCurrentPosition());


        TextView postion = musicPlayerPageView.findViewById(R.id.postion);
        int ic = player.getCurrentPosition() / 1000;
        postion.setText((ic / 60 >= 10 ? String.valueOf(ic / 60) : "0" + ic / 60) + ":" +
                (ic % 60 >= 10 ? String.valueOf(ic % 60) : "0" + ic % 60));

        LrcView music_lrc_view = player_content_view.get(1).findViewById(R.id.music_lrc);
        music_lrc_view.updateTime(player.getCurrentPosition());
    }

    private void updateNotificationPlayerOnMusic(){
//        playerNotificationManager.notify(NOTICFY_ID, playerNotificationBuilder);
    }

    //player util

    private int getMusicSourceInt(String source){
        switch (source){
            case "netease":
                return  0;
            case "tencent":
                return  1;
            case "kugou":
                return  2;
            case "migu":
                return  3;
            default:
                return  0;
        }
    }

    private class MusicPlayerConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(LOG_TAG, "started music service successful");
            player = ((MusicPlayer.musicBinder) service).getPlayer();
            Log.i(LOG_TAG, "make music service notification");
            notificationPlayerView = ((MusicPlayer.musicBinder) service).getNotificationPlayerView();
            playerNotificationManager = ((MusicPlayer.musicBinder) service).getPlayerNotificationManager();
            playerNotificationBuilder = ((MusicPlayer.musicBinder) service).getPlayerNotification();
            Log.i(LOG_TAG, "get music repeat mode");
            SharedPreferences pref = getSharedPreferences("data",MODE_PRIVATE);
            setPlayerLoopControl(pref.getInt("music_loop_method", MUSIC_LOOP_CONTROL_LOOP));
            Log.i(LOG_TAG, "set first music of recent music list");
            if (musicList.size() != 0)
                setCurrentMusic(musicList.get(0), false, false);
            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    return true;
                }
            });
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

    private class MusicPlayerPagerAdapter extends PagerAdapter{

        MusicPlayerPagerAdapter(Context context){
            player_content_view.clear();
            player_content_view.add(View.inflate(context, R.layout.player_pic_content, null));
            player_content_view.add(View.inflate(context, R.layout.player_lrc_content, null));
        }

        @Override
        public int getCount() {
            return player_content_view.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View v = player_content_view.get(position);
            if (position == 0){
                if (current_music_pic != null){
                    ImageView music_pic_view = v.findViewById(R.id.music_pic);
                    music_pic_view.setImageBitmap(current_music_pic);
                }
            }else if (position == 1){
                if (current_music_lrc != null){
                    LrcView music_lrc_view = v.findViewById(R.id.music_lrc);
                    Log.e("AdapterMusicLyric:", current_music_lrc);
                    music_lrc_view.loadLrc(current_music_lrc);
                    music_lrc_view.setDraggable(true, new LrcView.OnPlayClickListener() {
                        @Override
                        public boolean onPlayClick(LrcView view, long time) {
                            player.seekTo((int) time);
                            musicPlay();
//                            updateplayer();
                            return false;
                        }
                    });
                }
            }
            container.addView(v);
            return v;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView(player_content_view.get(position));
        }

    }


    //user

    public Music getCurrentMusic() {
        return currentMusic;
    }

    public void setMusicList(List<Music> musicList) {
        this.musicList = musicList;
    }

    public List<Music> getMusicList() {
        return musicList;
    }

    public Boolean isMusicListEmpty(){
        return musicList.isEmpty();
    }

    public void resetAllPlayer(){
        player.reset();
        if (updataThread != null){
            updataThread.interrupt();
        }
        currentMusic = null;
        last_can_play_music = null;
        current_music_pic = null;
        current_music_lrc = null;
        currentMusicIndexInMusicList = 0;
        musicList = null;
        for (Map.Entry<Integer,View> i : minMusicPlayerList.entrySet()) {
            View view = i.getValue();

            ImageView min_music_control = view.findViewById(R.id.min_music_control);
            min_music_control.setImageResource(R.drawable.ic_play);

            ProgressBar minPlayerProgress = view.findViewById(R.id.minPlayerProgress);
            minPlayerProgress.setMax(100);
            minPlayerProgress.setProgress(0);

            TextView current_music_name = view.findViewById(R.id.current_music_name);
            current_music_name.setText(R.string.no_music_to_play);

            TextView current_music_singer = view.findViewById(R.id.current_music_singer);
            current_music_singer.setText(R.string.singer);

            ImageView musicPic = view.findViewById(R.id.musicPic);
            musicPic.setImageResource(R.mipmap.default_music_pic);

        }
        if (musicPlayerPageView != null){
            ImageView StartAndStop = musicPlayerPageView.findViewById(R.id.StartAndStop);
            StartAndStop.setImageResource(R.drawable.ic_play);


            ImageView music_pic_view = player_content_view.get(0).findViewById(R.id.music_pic);
            if (current_music_pic != null){
                music_pic_view.setImageBitmap(current_music_pic);
            }else {
                music_pic_view.setImageResource(R.mipmap.default_music_pic);
            }

            LrcView music_lrc_view = player_content_view.get(1).findViewById(R.id.music_lrc);
            if (current_music_lrc != null){
                music_lrc_view.loadLrc(current_music_lrc);
            }else {
                music_lrc_view.loadLrc("");
            }

            TextView player_musicName = musicPlayerPageView.findViewById(R.id.player_musicName);
            player_musicName.setText(R.string.no_music_to_play);

            TextView player_musicInfo_singer = musicPlayerPageView.findViewById(R.id.player_musicInfo_singer);
            player_musicInfo_singer.setText(R.string.singer);

            TextView player_musicInfo_album = musicPlayerPageView.findViewById(R.id.player_musicInfo_album);
            player_musicInfo_album.setText(R.string.album);

            TextView duration = musicPlayerPageView.findViewById(R.id.duration);
            duration.setText(R.string.zero_time);
            SeekBar music_seekBar = musicPlayerPageView.findViewById(R.id.music_seekBar);
            music_seekBar.setMax(100);
            music_seekBar.setProgress(0);

            TextView postion = musicPlayerPageView.findViewById(R.id.postion);
            postion.setText(R.string.zero_time);
        }
        if (playerNotificationManager != null){
            Bitmap b = BitmapFactory.decodeResource(getResources(), R.mipmap.default_music_pic);
            String singer = getResources().getString(R.string.singer);
            String name = getResources().getString(R.string.no_music_to_play);
            playerNotificationManager.notify(NOTICFY_ID,
                    MusicPlayer.getPlayerNotificationBuilder(getApplicationContext(),b,name,singer, false).build());
        }
    }

    public void setCurrentMusic(Music music){
        setCurrentMusic(music, true, true);
    }

    public void setCurrentMusic(Music music, Boolean updataRecentList, Boolean autoPlay){
        setCurrentMusic(music, updataRecentList, autoPlay, 0);
    }

    public void setCurrentMusic(final Music music, final Boolean updataRecentList, final Boolean autoPlay, int msec){
        Log.i(LOG_TAG, "set " + music.getName() + " to play");
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.arg1 == GetMusic.RESPOND_SUCCESS ){
                    if (msg.what == REQUEST_MUSIC_URL){
                        currentMusic = music;
                        try {
                            player.reset();
                            player.setDataSource((String) msg.obj);
                            player.prepareAsync();
                            player.setOnPreparedListener(mediaPlayer -> {
                                last_can_play_music = music;
                                getMoreInformationOfMusic(currentMusic);
                                initPlayerOnMusic();
                                if (updataRecentList){
                                    Log.i(LOG_TAG, "prepared successful, add Music Object to recent list : " + music.getName());
                                    musicList = RecentPlay.addRecentPlayMusic(getApplicationContext(),music);
                                }
                                currentMusicIndexInMusicList = RecentPlay.isPlayedRecently(getApplicationContext(), music.getId());
                                if (autoPlay){
                                    Log.i(LOG_TAG, "prepared successful, play immediately : " + music.getName());
                                    musicPlay();
                                    if (msec != 0)
                                        player.seekTo(msec);
                                } else {
                                    Log.i(LOG_TAG, "prepared successful, play later on : " + music.getName());
                                    int currentDuration = player.getDuration() / 1000;
                                    currentMusicDuration = ((currentDuration / 60) >= 10 ? String.valueOf(currentDuration / 60) : ("0" + (currentDuration / 60))) +
                                            ":" + ((currentDuration % 60)  >= 10 ? String.valueOf(currentDuration % 60) : ("0" + (currentDuration % 60)));
                                    updateplayer();
                                }
                                if (onMusicChange != null){
                                    onMusicChange.OnComplete();
                                }
                            });
                            player.setOnErrorListener((mp, what, extra) -> {
                                Log.i(LOG_TAG, "play fail, Consequently set prev music that can enable : " + music.getName());
                                Toast.makeText(getApplicationContext(), R.string.play_fail, Toast.LENGTH_SHORT).show();
                                if (last_can_play_music != null){
                                    setCurrentMusic(last_can_play_music, false, true, last_can_play_music_postion);
                                }
                                if (onMusicChange != null){
                                    onMusicChange.OnFail();
                                }
                                return true;
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
            Log.i(LOG_TAG, "There is no music to play, And select music already play ! : " + music.getName());
            if (autoPlay)
                musicPlay();
        }else {
            musicPause();
            Log.i(LOG_TAG, "storage current music position : " + (currentMusic == null ? "no music" : currentMusic.getName())
                    + ",position : " + player.getCurrentPosition());
            last_can_play_music_postion = player.getCurrentPosition();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg = Message.obtain();
                    try {
                        String id = music.getId();
                        String url = conn.getMusicPlayURL(id, music.getSource());
                        if (currentMusic == null || !id.equals(currentMusic.getId())){
                            if (url != null && !url.equals("")){
                                msg.what = REQUEST_MUSIC_URL;
                                msg.arg1 = GetMusic.RESPOND_SUCCESS;
                                msg.obj = url;
                            }else {
                                Log.w(LOG_TAG, "can't got music url because network and url is null or empty : " + music.getName());
                                msg.what = GetMusic.RESPOND_TIMEOUT;
                            }
                        }
                    } catch (Exception e) {
                        Log.w(LOG_TAG, "request music url wrong : " + music.getName() + ",Because " + e.getMessage());
                        msg.what = GetMusic.RESPOND_TIMEOUT;
                        e.printStackTrace();
                    };
                    handler.sendMessage(msg);
                }
            }).start();
        }
    }

    private void getMoreInformationOfMusic(final Music music){
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.arg1 == GetMusic.RESPOND_SUCCESS ){
                    if (msg.what == REQUEST_MUSIC_PIC){
                        current_music_pic = (Bitmap) msg.obj;
                        initPlayerOnMusic();
                    }else if (msg.what == REQUEST_MUSIC_LYRIC){
                        current_music_lrc = (String) msg.obj;
                        initPlayerOnMusic();
                    }
                }else{
//                    Toast.makeText(getApplicationContext(),R.string.network_wrong,Toast.LENGTH_SHORT).show();
                }
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                try {
                    String id = music.getId();
                    if (id != null){
                        InputStream inputStream = conn.getMusicPlayPicUrl(id, music.getPic_id(), getMusicSourceInt(music.getSource()));
                        if (inputStream != null){
                            msg.what = REQUEST_MUSIC_PIC;
                            msg.arg1 = GetMusic.RESPOND_SUCCESS;
                            msg.obj = BitmapFactory.decodeStream(inputStream);
                        }else {
                            msg.what = REQUEST_MUSIC_PIC;
                            msg.arg1 = GetMusic.RESPOND_SUCCESS;
                            msg.obj = BitmapFactory.decodeResource(getResources(), R.mipmap.default_music_pic);
                        }
                    }else {
                        msg.what = REQUEST_MUSIC_PIC_DEFAULT;
                        msg.arg1 = GetMusic.RESPOND_SUCCESS;
                        msg.obj = BitmapFactory.decodeResource(getResources(), R.mipmap.default_music_pic);
                    }
                } catch (Exception e) {
                    msg.what = GetMusic.RESPOND_TIMEOUT;
                    e.printStackTrace();
                };
                handler.sendMessage(msg);
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                try {
                    String id = music.getId();
                    if (id != null){
                        String lyric = conn.getMusicLyric(id, getMusicSourceInt(music.getSource()));
                        if (lyric != null){
                            msg.what = REQUEST_MUSIC_LYRIC;
                            msg.arg1 = GetMusic.RESPOND_SUCCESS;
                            msg.obj = lyric;
                        }else {
                            msg.what = REQUEST_MUSIC_LYRIC;
                            msg.arg1 = GetMusic.RESPOND_SUCCESS;
                            msg.obj = null;
                        }
                    }else {
                        msg.what = REQUEST_MUSIC_PIC_DEFAULT;
                        msg.arg1 = GetMusic.RESPOND_SUCCESS;
                        msg.obj = BitmapFactory.decodeResource(getResources(), R.mipmap.default_music_pic);
                    }
                } catch (Exception e) {
                    msg.what = GetMusic.RESPOND_TIMEOUT;
                    e.printStackTrace();
                };
                handler.sendMessage(msg);
            }
        }).start();

    }

    public void setOnMusicChange(OnMusicChange onMusicChange){
        this.onMusicChange = onMusicChange;
    };

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
                TextView postion = musicPlayerPageView.findViewById(R.id.postion);
                int ic = seekBar.getProgress() / 1000;
                postion.setText((ic / 60 >= 10 ? String.valueOf(ic / 60) : "0" + ic / 60) + ":" +
                        (ic % 60 >= 10 ? String.valueOf(ic % 60) : "0" + ic % 60));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                    player.seekTo(seekBar.getProgress());
            }
        });
        final ImageView music_loop_control = view.findViewById(R.id.music_loop_control);
        music_loop_control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
                switch (music_loop_method){
                    case MUSIC_LOOP_CONTROL_REANDOM:
                        setPlayerLoopControl(MUSIC_LOOP_CONTROL_SINGLE);
                        music_loop_control.setImageResource(R.drawable.ic_single);
                        editor.putInt("music_loop_method",MUSIC_LOOP_CONTROL_SINGLE);
                        break;
                    case MUSIC_LOOP_CONTROL_LOOP:
                        setPlayerLoopControl(MUSIC_LOOP_CONTROL_REANDOM);
                        music_loop_control.setImageResource(R.drawable.ic_random);
                        editor.putInt("music_loop_method",MUSIC_LOOP_CONTROL_REANDOM);
                        break;
                    case MUSIC_LOOP_CONTROL_SINGLE:
                        setPlayerLoopControl(MUSIC_LOOP_CONTROL_LOOP);
                        music_loop_control.setImageResource(R.drawable.ic_loop);
                        editor.putInt("music_loop_method",MUSIC_LOOP_CONTROL_LOOP);
                        break;
                }
                editor.commit();
            }
        });
        ImageView music_prev = view.findViewById(R.id.music_prev);
        music_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prev();
            }
        });
        ImageView music_next = view.findViewById(R.id.music_next);
        music_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next();
            }
        });

        ViewPager player_content = view.findViewById(R.id.player_content);
        player_content.setAdapter(new MusicPlayerPagerAdapter(getApplicationContext()));

        musicPlayerPageView = view;
        initPagePlayerOnMusic();
    }

    public void destoryMusicPlayerPageView(){
        this.musicPlayerPageView = null;
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
        minMusicPlayerList.put(Integer.valueOf(tag), view);
        initMinPlayerOnMusic(view);
        return view;
    }

    public void destroyMinMusicPlayer(int tag){
        minMusicPlayerList.remove(Integer.valueOf(tag));
    }

}
