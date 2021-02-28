package com.sm.music.MusicUtils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.*;
import com.sm.music.Bean.Music;
import com.sm.music.GlobalApplication;
import com.sm.music.Listener.OnMusicChange;
import com.sm.music.Listener.OnRecentPlayDialogCloseListener;
import com.sm.music.Listener.OnRemoveAllRecentMusicListener;
import com.sm.music.R;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecentPlay {

    private static final int RECENT_MUSIC_NUM = 50;

    private OnRemoveAllRecentMusicListener onRemoveAllRecentMusicListener = null;

    private Context context;

    private FrameLayout root;

    private View recent;

    boolean isRecentShow = false;

    private ListView recent_play_list = null;

    GlobalApplication globalApplication = null;

    private OnRecentPlayDialogCloseListener onRecentPlayDialogCloseListener = null;

    public RecentPlay(Context context, GlobalApplication globalApplication, FrameLayout root) {
        this.context = context;
        this.root = root;
        this.recent = View.inflate(context, R.layout.recent_play_layout, null);
        this.globalApplication = globalApplication;
    }

    public boolean isRecentShow(){
        return isRecentShow;
    }

    public void setOnRemoveAllRecentMusicListener(OnRemoveAllRecentMusicListener onRemoveAllRecentMusicListener){
        this.onRemoveAllRecentMusicListener = onRemoveAllRecentMusicListener;
    }

    public void show(){
        LinearLayout recent_play_page = recent.findViewById(R.id.recent_play_page);
        LinearLayout recent_play_container = recent.findViewById(R.id.recent_play_container);
        recent_play_list = recent.findViewById(R.id.recent_play_list);
        TextView remove_all =  recent.findViewById(R.id.remove_all);
        remove_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearRecentPlayMusic(context,globalApplication);
                ((BaseAdapter)recent_play_list.getAdapter()).notifyDataSetChanged();
                globalApplication.resetAllPlayer();
                if (onRemoveAllRecentMusicListener != null){
                    onRemoveAllRecentMusicListener.onRemoveAll();
                }
            }
        });
        globalApplication.setMusicList(getRecentPlayMusic(context));
        recent_play_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecentShow){
                    hide();
                }
            }
        });
        recent_play_list.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                if (globalApplication.getMusicList() != null)
                    return globalApplication.getMusicList().size();
                else
                    return 0;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = View.inflate(context,R.layout.recent_list_ltem_layout, null);
                if (globalApplication.getMusicList() != null){
                    final Music music = globalApplication.getMusicList().get(position);
                    ((TextView) view.findViewById(R.id.index_list_item_music_name)).setText(music.getName());
                    String temp = "";
                    for (int i = 0; i < music.getArtist().length; i++) {
                        if (i == 0) {
                            temp += music.getArtist()[i];
                        } else {
                            temp += "/" + music.getArtist()[i];
                        }
                    }
                    ((TextView) view.findViewById(R.id.index_list_item_music_singer)).setText(temp);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            globalApplication.setCurrentMusic(music);
//                            globalApplication.setMusicList(addRecentPlayMusic(context,music));
                            globalApplication.setOnMusicChange(new OnMusicChange() {
                                @Override
                                public void OnComplete() {
                                    ((BaseAdapter) recent_play_list.getAdapter()).notifyDataSetChanged();
                                }

                                @Override
                                public void OnFail() {
                                    ((BaseAdapter) recent_play_list.getAdapter()).notifyDataSetChanged();
                                }
                            });
                        }
                    });
                    ((ImageView) view.findViewById(R.id.remove_music)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            removeRecentPlayMusic(context, globalApplication, music);
                            ((BaseAdapter)recent_play_list.getAdapter()).notifyDataSetChanged();
                        }
                    });
                    FrameLayout before_name = view.findViewById(R.id.before_name);
                    if (globalApplication.getCurrentMusic().getId().equals(music.getId())){
                        ImageView imageView = new ImageView(context);
                        imageView.setImageResource(R.drawable.ic_playing);
                        before_name.addView(imageView);
                    }

                }
                return view;
            }
        });
        recent_play_container.startAnimation(AnimationUtils.loadAnimation(context, R.anim.transfrom_buttom_in));
        if (!isRecentShow)
            root.addView(recent);
        isRecentShow = true;
    }

    public void setOnRecentPlayDialogCloseListener(OnRecentPlayDialogCloseListener onRecentPlayDialogCloseListener){
        this.onRecentPlayDialogCloseListener = onRecentPlayDialogCloseListener;
    }

    public void hide(){
        if (isRecentShow){
            isRecentShow = false;
            LinearLayout recent_play_container = recent.findViewById(R.id.recent_play_container);
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.transfrom_buttom_out);
            if (onRecentPlayDialogCloseListener != null){
                onRecentPlayDialogCloseListener.OnClose();
            }
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    root.removeView(recent);
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            recent_play_container.startAnimation(animation);
        }

    }

    public static List<Music> clearRecentPlayMusic(Context context, GlobalApplication globalApplication){
        save(context, new JSONArray());
        globalApplication.setMusicList(null);
        return getRecentPlayMusic(context);
    }

    public List<Music> removeRecentPlayMusic(Context context, GlobalApplication application, Music music){
        JSONArray old = load(context);
        if (old == null || old.size() == 0){
            old = new JSONArray();
        }
        int isHas = -1;
        for (int i = 0; i < old.size(); i++){
            if (music.getId().equals(old.getJSONObject(i).getString("id"))){
                isHas = i;
                break;
            }
        }
        if (music.getId().equals(application.getCurrentMusic().getId())){
            if ((old.size() - 1) != 0){
                application.next();
            }else {
                clearRecentPlayMusic(context,globalApplication);
                ((BaseAdapter)recent_play_list.getAdapter()).notifyDataSetChanged();
                globalApplication.resetAllPlayer();
                if (onRemoveAllRecentMusicListener != null){
                    onRemoveAllRecentMusicListener.onRemoveAll();
                }
            }
        }
        JSONArray last = new JSONArray();
        for (int i = 0; i < old.size() && last.size() <= 50; i++){
            if (i == isHas)
                continue;
            last.add(old.getJSONObject(i));
        }
        save(context, last);
        application.setMusicList(last.toJavaList(Music.class));
        return last.toJavaList(Music.class);
    }

    public static List<Music> addRecentPlayMusic(Context context, Music music){
        JSONArray old = load(context);
        if (old == null || old.size() == 0){
            old = new JSONArray();
        }
        int isHas = -1;
        for (int i = 0; i < old.size(); i++){
            if (music.getId().equals(old.getJSONObject(i).getString("id"))){
                isHas = i;
                break;
            }
        }
        if (isHas == 0){
            return old.toJavaList(Music.class);
        }
        JSONArray last = new JSONArray();
        last.add(JSONObject.toJSON(music));
        for (int i = 0; i < old.size() && last.size() <= 50; i++){
            if (i == isHas)
                continue;
            last.add(old.getJSONObject(i));
        }
        save(context, last);
        return last.toJavaList(Music.class);
    }

    public static List<Music> getRecentPlayMusic(Context context){
        JSONArray j = load(context);
        if (j != null)
            return j.toJavaList(Music.class);
        else
            return new ArrayList<>();
    }

    private static JSONArray load(Context context) {
        try {
            FileInputStream inStream = context.openFileInput("recentPlay");
            ByteArrayOutputStream stream=new ByteArrayOutputStream();
            byte[] buffer=new byte[1024];
            int length = -1;
            while((length=inStream.read(buffer))!=-1)   {
                stream.write(buffer,0,length);
            }
            stream.close();
            inStream.close();
            return JSONArray.parseArray(stream.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        catch (IOException e){
            return null;
        }

    }

    private static boolean save(Context context, JSONArray json) {
        try {
            FileOutputStream outStream=context.openFileOutput("recentPlay",Context.MODE_PRIVATE);
            outStream.write(json.toString().getBytes());
            outStream.close();
            return true;
        } catch (FileNotFoundException e) {
            return false;
        }
        catch (IOException e){
            return false;
        }
    }

    public static int isPlayedRecently(Context context, String id){
        JSONArray list = load(context);
        if (list == null || list.size() == 0){
            return -1;
        }
        for (int i = 0; i < list.size(); i++){
            if (id.equals(list.getJSONObject(i).getString("id"))){
                return i;
            }
        }
        return -1;
    }
}
