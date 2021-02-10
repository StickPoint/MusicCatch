package com.sm.music.Bean;

import androidx.annotation.Nullable;

import java.util.Arrays;

/**
 * @author fntp
 * 基于网页json数据请求的
 * 格式化封装Bean
 */
public class Music {

    private String id;

    private String name;

    // 返回的歌曲演唱者可能有多个人，最多十个人一起唱
    private String artist[] = new String[10];

    private String album;

    private String pic_id;

    private String url_id;

    private String lyric_id;

    private String source;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getArtist() {
        return artist;
    }

    public void setArtist(String[] artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getPic_id() {
        return pic_id;
    }

    public void setPic_id(String pic_id) {
        this.pic_id = pic_id;
    }

    public String getUrl_id() {
        return url_id;
    }

    public void setUrl_id(String url_id) {
        this.url_id = url_id;
    }

    public String getLyric_id() {
        return lyric_id;
    }

    public void setLyric_id(String lyric_id) {
        this.lyric_id = lyric_id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return this.getId().equals( ((Music) obj).getId() );
    }

    @Override
    public String toString() {
        return "Music{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", artist=" + Arrays.toString(artist) +
                ", album='" + album + '\'' +
                ", pic_id='" + pic_id + '\'' +
                ", url_id='" + url_id + '\'' +
                ", lyric_id='" + lyric_id + '\'' +
                ", source='" + source + '\'' +
                '}';
    }

}
