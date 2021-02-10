package com.sm.music.Bean;

public class LocMus {

    private int musicid;

    private String musicname;

    private String location;

    public int getMusicid() {
        return musicid;
    }

    public void setMusicid(int musicid) {
        this.musicid = musicid;
    }

    public String getMusicname() {
        return musicname;
    }

    public void setMusicname(String musicname) {
        this.musicname = musicname;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "LocMus{" +
                "musicid=" + musicid +
                ", musicname='" + musicname + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}
