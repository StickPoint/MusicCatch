package com.sm.music;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.PeriodicSync;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.sm.music.fragment.downloadFragment;
import com.sm.music.fragment.indexFragment;
import com.sm.music.fragment.likeFragment;
import com.sm.music.fragment.moreFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //main page
    private LinearLayout mainPage = null;
    //bottom nav
    private ViewPager nav = null;
    //music player min at nav
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
    //main content
    private ViewPager mainWrapper = null;
    //child page view
    private static ArrayList<Fragment> FragmentList = new ArrayList<Fragment>();

    //add child page view to FragmentList
    static {
        FragmentList.add(new indexFragment());
        FragmentList.add(new likeFragment());
        FragmentList.add(new downloadFragment());
        FragmentList.add(new moreFragment());
    }

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.setActivityBarAlpha(this, false);
        setContentView(R.layout.activity_main);
        final int NavigationBarHeight = Util.getNavigationBarHeight(MainActivity.this);
        final Boolean HasNavigationBar = !Util.checkDeviceHasNavigationBar(this);
        mainPage = findViewById(R.id.mainPage);
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
        musicPlayer_layout.findViewById(R.id.mainPlayer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!currentMusicId.equals("0")){
                    startActivity(new Intent(MainActivity.this, Player.class));
                    //TODO: To shart music player and post some arguments

                }else {
                    //No music to play
                    Toast.makeText(MainActivity.this, R.string.no_music_to_play, Toast.LENGTH_SHORT).show();
                }
            }
        });
        min_music_control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!currentMusicId.equals("0")){
                    if (isPlaying){
                        min_music_control.setImageResource(R.drawable.ic_play);
                        //TODO: Music to stop

                        isPlaying = false;
                    }else {
                        min_music_control.setImageResource(R.drawable.ic_stop);
                        //TODO: Music to play

                        isPlaying = true;
                    }
                }else {
                    //No music to play
                    Toast.makeText(MainActivity.this, R.string.no_music_to_play, Toast.LENGTH_SHORT).show();
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
        mainWrapper = findViewById(R.id.mainWrapper);
        mainWrapper.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return FragmentList.size();
            }

            @Override
            public Fragment getItem(int position) {
                return FragmentList.get(position);
            }
        });
        mainWrapper.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        Util.setActivityBarAlpha(MainActivity.this, false);
                        ((RadioButton) navBar_layout.findViewById(R.id.nav_index) ).setChecked(true);
                        break;
                    case 1:
                        Util.setActivityBarAlpha(MainActivity.this, false);
                        ((RadioButton) navBar_layout.findViewById(R.id.nav_like) ).setChecked(true);
                        break;
                    case 2:
                        Util.setActivityBarAlpha(MainActivity.this, false);
                        ((RadioButton) navBar_layout.findViewById(R.id.nav_download) ).setChecked(true);
                        break;
                    case 3:
                        Util.setActivityBarAlpha(MainActivity.this, true);
                        ((RadioButton) navBar_layout.findViewById(R.id.nav_more) ).setChecked(true);
                        break;
                    default:
                        Util.setActivityBarAlpha(MainActivity.this, false);
                        ((RadioButton) navBar_layout.findViewById(R.id.nav_index) ).setChecked(true);
                        break;
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        navBar_layout.findViewById(R.id.nav_index).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainWrapper.setCurrentItem(0, true);
            }
        });
        navBar_layout.findViewById(R.id.nav_like).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainWrapper.setCurrentItem(1, true);
            }
        });
        navBar_layout.findViewById(R.id.nav_download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainWrapper.setCurrentItem(2, true);
            }
        });
        navBar_layout.findViewById(R.id.nav_more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainWrapper.setCurrentItem(3, true);
            }
        });
        //TODO: Other code
    }
}
