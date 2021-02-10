package com.sm.music.SQL;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

public class PersonSQL extends SQLiteOpenHelper {
    public PersonSQL(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public PersonSQL(Context applicationContext) {
        super(applicationContext,"d_music_b",null,1);
    }

    //创建数据库时执行
    @Override
    public void onCreate(SQLiteDatabase db) {
        //建表
        db.execSQL("create table favMus(music_id varchar(100) PRIMARY KEY," +
                "date timestamp not null default (datetime('now','localtime'))," +
                "music text(100000))");
        db.execSQL("create table locMus(_id integer primary key autoincrement," +
                "musname varchar(30)," +
                "musarti varchar(30),location varchar(100))");
        db.execSQL("create table hisplay(_id integer primary key autoincrement," +
                "musname varchar(50)," +
                "musarti varchar(30),mussize int(100))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}