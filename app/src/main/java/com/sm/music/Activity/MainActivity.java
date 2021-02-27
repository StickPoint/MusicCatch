package com.sm.music.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.sm.music.Bean.Music;
import com.sm.music.GlobalApplication;
import com.sm.music.MusicUtils.SendIMEI;
import com.sm.music.R;
import com.sm.music.UIUtils.Util;
import com.sm.music.Fragment.downloadFragment;
import com.sm.music.Fragment.indexFragment;
import com.sm.music.Fragment.likeFragment;
import com.sm.music.Fragment.moreFragment;
import com.xuexiang.xupdate.XUpdate;
import com.xuexiang.xupdate.entity.UpdateError;
import com.xuexiang.xupdate.listener.OnUpdateFailureListener;
import com.xuexiang.xupdate.service.OnFileDownloadListener;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import static com.xuexiang.xupdate.entity.UpdateError.ERROR.CHECK_NO_NEW_VERSION;

public class MainActivity extends AppCompatActivity {

    public static final String UPDATE_INFO_URL = "https://download.micronnetwork.com/ddmusic/ddmusicUpdata.json";

    public static final int SHOW_MIN_PLAY_FLAG = 255;

    public static final int SHOW_NAV_FLAG = 207;

    //globe class
    GlobalApplication globalApplication = null;
    //main page
    private FrameLayout mainPage = null;
    //bottom nav
    private ViewPager nav = null;
    //music player min at nav
    private ImageView min_music_control = null;
    //nav Array List
    private ArrayList<View> nav_list = new ArrayList();
    //nav bar layout view
    private View navBar_layout = null;
    //main content
    private ViewPager mainWrapper = null;
    //child page view
    private static ArrayList<Fragment> FragmentList = new ArrayList<Fragment>();
    //tag
    private static final int ACTICITY_TAG = 410;

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

        globalApplication = (GlobalApplication) getApplication();


        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        Boolean isFirstLanuch = pref.getBoolean("isFirstLanuch", true);

        String imei = null;
        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(TELEPHONY_SERVICE);
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            imei = UUID.randomUUID().toString();
        }else {
            imei = UUID.randomUUID().toString();
//            imei = telephonyManager.getDeviceId();
        }

        if (isFirstLanuch && imei != null){
            Log.i("IMEI:is:", imei);
//            SendIMEI.send(imei);
//            SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
//            editor.putBoolean("isFirstLanuch",false);
//            editor.commit();
        }

        XUpdate.newBuild(MainActivity.this)
                .updateUrl(UPDATE_INFO_URL)
                .update();

        mainPage = findViewById(R.id.mainPage);
        nav = findViewById(R.id.nav);

        navBar_layout = View.inflate(this, R.layout.nav_bar,null);


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
        nav_list.add(0,navBar_layout);
        nav_list.add(1, globalApplication.createMinMusicPlayer(MainActivity.this, ACTICITY_TAG));
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
                        Util.setActivityBarAlpha(MainActivity.this, false);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        globalApplication.destroyMinMusicPlayer(ACTICITY_TAG);
    }

    public void setMainPlayerVisiable(int tag){
        if (nav != null){
            if (tag == SHOW_MIN_PLAY_FLAG)
                nav.setCurrentItem(1, true);
            else if (tag == SHOW_NAV_FLAG)
                nav.setCurrentItem(0, true);
        }
    }
}
