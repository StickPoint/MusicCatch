package com.sm.music;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class MainActivity extends AppCompatActivity {

    private LinearLayout mainPage = null;
    private ConstraintLayout mainTop = null;
    private RadioGroup navBar = null;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.setActivityBarAlpha(this);
        setContentView(R.layout.activity_main);
        final int statusBarHeight = Util.getStatusBarHeight(MainActivity.this);
        final int NavigationBarHeight = Util.getNavigationBarHeight(MainActivity.this);
        final Boolean HasNavigationBar = !Util.checkDeviceHasNavigationBar(this);
        mainPage = findViewById(R.id.mainPage);
        mainTop = findViewById(R.id.mainTop);
        navBar = findViewById(R.id.navBar);
        navBar.post(new Runnable() {
            @Override
            public void run() {
                if (HasNavigationBar){
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) navBar.getLayoutParams();
                    layoutParams.height = NavigationBarHeight + navBar.getHeight();
                    navBar.setLayoutParams(layoutParams);
                    navBar.setPadding(0,0,0,NavigationBarHeight);
                }
            }
        });
        mainTop.post(new Runnable() {
            public void run() {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mainTop.getLayoutParams();
                layoutParams.height = statusBarHeight + mainTop.getHeight();
                mainTop.setLayoutParams(layoutParams);
                mainTop.setPadding(0,statusBarHeight,0,0);
            }
        });
    }
}
