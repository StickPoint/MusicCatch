package com.sm.music.Bean;

/**
 * @author fntp
 */
public class FavMus {

    private String musicid;

    private String date;

    private Music music;

    public String getMusicid() {
        return musicid;
    }

    public void setMusicid(String musicid) {
        this.musicid = musicid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Music getMusic() {
        return music;
    }

    public void setMusic(Music music) {
        this.music = music;
    }

    @Override
    public String toString() {
        return "FavMus{" +
                "musicid='" + musicid + '\'' +
                ", date='" + date + '\'' +
                ", music=" + music +
                '}';
    }
}
