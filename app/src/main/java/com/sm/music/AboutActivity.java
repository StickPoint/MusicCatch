package com.sm.music;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;

public class AboutActivity extends AppCompatActivity {

    private WebView aboutUs_Web = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        aboutUs_Web = findViewById(R.id.aboutUs_Web);
//        aboutUs_Web.
    }
}
