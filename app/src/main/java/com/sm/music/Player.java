package com.sm.music;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class Player extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.setActivityBarAlpha(this, true);
        setContentView(R.layout.activity_player);
    }
}
