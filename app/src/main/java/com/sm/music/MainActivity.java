package com.sm.music;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //main page
    private LinearLayout mainPage = null;
    //main top or search bar
    private ConstraintLayout mainTop = null;
    //bottom nav
    private RadioGroup navBar = null;
    //music player min above nav
    private ImageView min_music_control = null;
    //This is a tag which music play or stop
    public Boolean isPlaying = false;
    //This is current music id
    public String currentMusicId = "0";

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.setActivityBarAlpha(this, false);
        setContentView(R.layout.activity_main);
        final int statusBarHeight = Util.getStatusBarHeight(MainActivity.this);
        final int NavigationBarHeight = Util.getNavigationBarHeight(MainActivity.this);
        final Boolean HasNavigationBar = !Util.checkDeviceHasNavigationBar(this);
        mainPage = findViewById(R.id.mainPage);
        mainTop = findViewById(R.id.mainTop);
        navBar = findViewById(R.id.navBar);
        min_music_control = findViewById(R.id.min_music_control);
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
        findViewById(R.id.mainPlayer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!currentMusicId.equals("0")){
                    startActivity(new Intent(MainActivity.this, Player.class));
                }else {
                    //No music to play
                    Toast.makeText(MainActivity.this, R.string.no_music_to_play, Toast.LENGTH_SHORT).show();
                }
            }
        });
        min_music_control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying){
                    min_music_control.setImageResource(R.drawable.ic_play);
                    //TODO: Music to stop

                    isPlaying = false;
                }else {
                    if (!currentMusicId.equals("0")){
                        min_music_control.setImageResource(R.drawable.ic_stop);
                        //TODO: Music to play

                        isPlaying = true;
                    }else {
                        //No music to play
                        Toast.makeText(MainActivity.this, R.string.no_music_to_play, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        //TODO: Other code
    }
}
