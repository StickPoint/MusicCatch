package com.sm.music.MusicUtils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.*;
import com.sm.music.Bean.Music;
import com.sm.music.GlobalApplication;
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


    private Context context;

    private FrameLayout root;

    private View recent;

    private List<Music> music_list;

    boolean isRecentShow = false;

    GlobalApplication globalApplication = null;

    public RecentPlay(Context context, GlobalApplication globalApplication, FrameLayout root) {
        this.context = context;
        this.root = root;
        this.recent = View.inflate(context, R.layout.recent_play_layout, null);
        this.globalApplication = globalApplication;
    }

    public boolean isRecentShow(){
        return isRecentShow;
    }

    public void show(){
        isRecentShow = true;
        LinearLayout recent_play_page = recent.findViewById(R.id.recent_play_page);
        LinearLayout recent_play_container = recent.findViewById(R.id.recent_play_container);
        final ListView recent_play_list = recent.findViewById(R.id.recent_play_list);
        music_list = getRecentPlayMusic(context);
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
                return music_list.size();
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
                final Music music = music_list.get(position);
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
                        music_list = addRecentPlayMusic(context,music);
                        ((BaseAdapter) recent_play_list.getAdapter()).notifyDataSetChanged();
                    }
                });
                return view;
            }
        });
        recent_play_container.startAnimation(AnimationUtils.loadAnimation(context, R.anim.transfrom_buttom_in));
        root.addView(recent);
    }

    public void hide(){
        root.removeView(recent);

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
