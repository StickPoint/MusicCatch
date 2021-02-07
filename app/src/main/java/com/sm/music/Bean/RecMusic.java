package com.sm.music.Bean;

import com.alibaba.fastjson.JSON;

import java.util.Arrays;

public class RecMusic {

    private String name;

    private String id;

    private String[] ar;

    private JSON al;

    private JSON l;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String[] getAr() {
        return ar;
    }

    public void setAr(String[] ar) {
        this.ar = ar;
    }

    public JSON getAl() {
        return al;
    }

    public void setAl(JSON al) {
        this.al = al;
    }

    public JSON getL() {
        return l;
    }

    public void setL(JSON l) {
        this.l = l;
    }

    @Override
    public String toString() {
        return "RecMusic{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", ar=" + Arrays.toString(ar) +
                ", al=" + al +
                ", l=" + l +
                '}';
    }
}
