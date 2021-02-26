package com.sm.music.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sm.music.GlobalApplication;
import com.sm.music.Listener.OnRemoveAllRecentMusicListener;
import com.sm.music.MusicUtils.RecentPlay;
import com.sm.music.R;
import com.sm.music.UIUtils.Util;

import java.util.ArrayList;
import java.util.List;

public class PlayerActivity extends AppCompatActivity {

    private GlobalApplication globalApplication = null;

    private FrameLayout playerPage = null;
    private ConstraintLayout playerTop = null;
    private ConstraintLayout player_control = null;
    private ImageView playerTop_close = null;
    private ImageView recent_button = null;

    private ViewPager player_content = null;

    private RecentPlay recentPlay = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.setActivityBarAlpha(this, true);
        setContentView(R.layout.activity_player);
        globalApplication = (GlobalApplication)getApplication();
        final int statusBarHeight = Util.getStatusBarHeight(PlayerActivity.this);
        final int NavigationBarHeight = Util.getNavigationBarHeight(PlayerActivity.this);
        final Boolean HasNavigationBar = !Util.checkDeviceHasNavigationBar(this);

        playerTop = findViewById(R.id.playerTop);
        player_control = findViewById(R.id.player_control);
        playerPage = findViewById(R.id.playerPage);
        recent_button = findViewById(R.id.recent_button);
        recentPlay = new RecentPlay(PlayerActivity.this, globalApplication, playerPage);

        playerTop.post(new Runnable() {
            public void run() {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) playerTop.getLayoutParams();
                layoutParams.height = statusBarHeight + playerTop.getHeight();
                playerTop.setLayoutParams(layoutParams);
                playerTop.setPadding(0,statusBarHeight,0,0);
            }
        });
        player_control.post(new Runnable() {
            @Override
            public void run() {
                if (HasNavigationBar){
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) player_control.getLayoutParams();
                    layoutParams.height = NavigationBarHeight + player_control.getHeight();
                    player_control.setLayoutParams(layoutParams);
                    player_control.setPadding(0,0,0,NavigationBarHeight);
                }
            }
        });

        playerTop_close = findViewById(R.id.playerTop_close);

        playerTop_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayerActivity.this.finish();
                overridePendingTransition(0,R.anim.transfrom_buttom_out);
            }
        });
        recent_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recentPlay.show();
            }
        });
        recentPlay.setOnRemoveAllRecentMusicListener(new OnRemoveAllRecentMusicListener() {
            @Override
            public void onRemoveAll() {
                PlayerActivity.this.finish();
                overridePendingTransition(0,R.anim.transfrom_buttom_out);
            }
        });
        globalApplication.setMusicPlayerPageView(findViewById(R.id.playerPage));

    }


    public void onBackPressed() {
        if (recentPlay.isRecentShow()){
            recentPlay.hide();
        }else {
            super.onBackPressed();
        }

    }
}
