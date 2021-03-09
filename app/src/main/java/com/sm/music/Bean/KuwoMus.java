package com.sm.music.Bean;

import java.io.Serializable;

public class KuwoMus implements Serializable {

    private String people;

    private String music;

    private String url;

    private String rid;

    private String song_time;

    private String album;

    public String getPeople() {
        return people;
    }

    public void setPeople(String people) {
        this.people = people;
    }

    public String getMusic() {
        return music;
    }

    public void setMusic(String music) {
        this.music = music;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public String getSong_time() {
        return song_time;
    }

    public void setSong_time(String song_time) {
        this.song_time = song_time;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    @Override
    public String toString() {
        return "KuwoMus{" +
                "people='" + people + '\'' +
                ", music='" + music + '\'' +
                ", url='" + url + '\'' +
                ", rid='" + rid + '\'' +
                ", song_time='" + song_time + '\'' +
                ", album='" + album + '\'' +
                '}';
    }
}
