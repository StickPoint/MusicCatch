package com.sm.music.MusicUtils;

import com.sm.music.Bean.Music;
import com.sm.music.Bean.RecMusic;

public class ConvertBean {
    public static Music RecMusicToMusic(RecMusic music){
        Music m = new Music();
        m.setName(music.getName());
        m.setId(music.getId());
        m.setAlbum(music.getAl().toString());
        m.setArtist(music.getAr());
        m.setPic_id(null);
        m.setLyric_id(null);
        return m;
    }
}
