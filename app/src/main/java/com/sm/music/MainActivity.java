package com.sm.music;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.PeriodicSync;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //main page
    private LinearLayout mainPage = null;
    //main top or search bar
    private ConstraintLayout mainTop = null;
    //bottom nav
    private ViewPager nav = null;
    //music player min above nav
    private ImageView min_music_control = null;
    //This is a tag which music play or stop
    public Boolean isPlaying = false;
    //This is current music id
    public String currentMusicId = "0";
    //nav Array List
    private ArrayList<View> nav_list = new ArrayList();
    //nav bar layout view
    private View navBar_layout = null;
    //music player bar layout view
    private View musicPlayer_layout = null;

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
        nav = findViewById(R.id.nav);
        navBar_layout = View.inflate(this, R.layout.nav_bar,null);
        musicPlayer_layout = View.inflate(this, R.layout.music_player,null);
        min_music_control = musicPlayer_layout.findViewById(R.id.min_music_control);
        nav.post(new Runnable() {
            @Override
            public void run() {
                if (HasNavigationBar){
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) nav.getLayoutParams();
                    layoutParams.height = NavigationBarHeight + nav.getHeight();
                    nav.setLayoutParams(layoutParams);
                    nav.setPadding(0,0,0,NavigationBarHeight);
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
        musicPlayer_layout.findViewById(R.id.mainPlayer).setOnClickListener(new View.OnClickListener() {
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
        nav_list.add(0,navBar_layout);
        nav_list.add(1, musicPlayer_layout);
        nav.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView(nav_list.get(position));
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                container.addView(nav_list.get(position));
                return nav_list.get(position);
            }
        });
        //TODO: Other code
    }
}
