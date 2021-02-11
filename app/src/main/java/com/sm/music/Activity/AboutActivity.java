package com.sm.music.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.sm.music.R;
import com.sm.music.UIUtils.Util;

public class AboutActivity extends AppCompatActivity {

    private WebView aboutUs_Web = null;

    private TextView about_cancel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.setActivityBarAlpha(this, true);
        setContentView(R.layout.activity_about);
        aboutUs_Web = findViewById(R.id.aboutUs_Web);
        aboutUs_Web.loadUrl("file:///android_asset/web/index.html");
        about_cancel = findViewById(R.id.about_cancel);
        about_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AboutActivity.this.finish();
                overridePendingTransition(0,R.anim.transfrom_buttom_out);
            }
        });
    }
}
