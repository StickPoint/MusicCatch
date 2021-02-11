package com.sm.music.SQL;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sm.music.Bean.FavMus;
import com.sm.music.Bean.LocMus;
import com.sm.music.Bean.Music;
import com.sm.music.Bean.RecMusic;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lengend YC & fntp
 */
public class SQLUtils {
    /**
     * sql需要的数据data
     */
    private SQLiteDatabase database;
    private Cursor cursor;
    private MusSQL musSQL;
    private FavMus favMus;
    private LocMus locMus;
    private List<FavMus> favMusList;
    private List<LocMus> locMusList;

    /**
     * @ 添加我的喜欢的音乐
     */
    public Boolean setFavMus(Context applicationContext, Music music) {
        //这个参数请使用getApplicationContext()方法获得当前调用方法的类的context
        boolean flag = false;
        try {
            musSQL = new MusSQL(applicationContext);
            database = musSQL.getWritableDatabase();
            String json = JSONArray.toJSONString(music);
            database.execSQL("insert into favmus(music_id,music)values('" + music.getId() + music.getSource() + "','"
                    + json + "');");
            database.close();
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;
        }
        return flag;
    }

    /**
     * @param applicationContext
     * @return
     */
    public List<FavMus> getFavMus(Context applicationContext) {
        try {
            musSQL = new MusSQL(applicationContext);
            database = musSQL.getReadableDatabase();
            cursor = database.rawQuery("select * from favmus", new String[]{});
            favMus = new FavMus();
            while (cursor.moveToNext()) {
                favMus.setMusicid(cursor.getString(1));
                favMus.setDate(cursor.getString(2));
                favMus.setMusic(JSONObject.parseObject(cursor.getString(3), Music.class));
                favMusList.add(favMus);
            }
            database.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return favMusList;
    }

    /**
     * 判断我喜欢的音乐是否存在
     * @param applicationContext
     * @return
     */
    public boolean getFavMus(Context applicationContext,String musicId) {
        boolean flag = false;
        try {
            musSQL = new MusSQL(applicationContext);
            database = musSQL.getReadableDatabase();
            cursor = database.rawQuery("select * from favmus where musicid = ?", new String[]{musicId});
            if (cursor.moveToNext()){
                flag = true;
            }
            database.close();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * 删除我喜欢
     *
     * @param applicationContext
     * @param musicid
     * @return
     */
    public boolean delFavMus(Context applicationContext, String musicid) {
        boolean flag = false;
        try {
            musSQL = new MusSQL(applicationContext);
            database = musSQL.getWritableDatabase();
            database.execSQL("delete from favmus where musicid = ?", new Object[]{musicid});
            flag = true;
            database.close();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * 本地音乐播放列表添加（初始化）
     *
     * @param applicationContext
     * @param music
     * @return
     */
    public Boolean setLocMus(Context applicationContext, LocMus music) {
        //这个参数请使用getApplicationContext()方法获得当前调用方法的类的context
        boolean flag = false;
        try {
            musSQL = new MusSQL(applicationContext);
            database = musSQL.getWritableDatabase();
            database.execSQL("insert into favmus(musicid,musicname,location)values('" +
                    music.getMusicid() + "','" + music.getMusicname() + "','"
                    + music.getLocation() + "');");
            database.close();
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;
        }
        return flag;
    }

    public boolean delLocMus(Context applicationContext) {
        boolean flag = false;
        try {
            musSQL = new MusSQL(applicationContext);
            database = musSQL.getWritableDatabase();
            database.execSQL("delete from locmus", new Object[]{});
            flag = true;
            database.close();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    public List<LocMus> getLocMus(Context applicationContext) {
        try {
            musSQL = new MusSQL(applicationContext);
            database = musSQL.getReadableDatabase();
            cursor = database.rawQuery("select * from locmus", new String[]{});
            locMus = new LocMus();
            while (cursor.moveToNext()) {
                locMus.setMusicid(cursor.getInt(1));
                locMus.setMusicname(cursor.getString(2));
                locMus.setLocation(cursor.getString(3));
                locMusList.add(locMus);
            }
            database.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return locMusList;
    }
}
