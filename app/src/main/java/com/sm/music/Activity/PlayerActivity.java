package com.sm.music.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sm.music.Bean.Music;
import com.sm.music.GlobalApplication;
import com.sm.music.Listener.OnMusicChange;
import com.sm.music.Listener.OnRecentPlayDialogCloseListener;
import com.sm.music.Listener.OnRemoveAllRecentMusicListener;
import com.sm.music.MusicUtils.MusicDownloadDialog;
import com.sm.music.MusicUtils.RecentPlay;
import com.sm.music.R;
import com.sm.music.SQL.SQLUtils;
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

    private SQLUtils sqlUtils = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.setActivityBarAlpha(this, true);
        setContentView(R.layout.activity_player);
        globalApplication = (GlobalApplication)getApplication();
        sqlUtils = new SQLUtils();
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
        CheckBox player_like = findViewById(R.id.player_like);

        Music c = globalApplication.getCurrentMusic();
        if (sqlUtils.getFavMus(getApplicationContext(), c.getId() + c.getSource())){
            player_like.setChecked(true);
        }else {
            player_like.setChecked(false);
        }

        player_like.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Music currentMusic = globalApplication.getCurrentMusic();
                if (isChecked){
                    if (sqlUtils.setFavMus(getApplicationContext(), currentMusic))
                        Toast.makeText(PlayerActivity.this, R.string.fav_failed, Toast.LENGTH_LONG);
                }else {
                    if (!sqlUtils.delFavMus(PlayerActivity.this, currentMusic.getId() + currentMusic.getSource()))
                        Toast.makeText(PlayerActivity.this,R.string.fav_del_failed, Toast.LENGTH_LONG);
                }
            }
        });

        globalApplication.setOnMusicChange(new OnMusicChange() {
            @Override
            public void OnComplete() {
                Music currentMusic = globalApplication.getCurrentMusic();
                if (sqlUtils.getFavMus(getApplicationContext(), currentMusic.getId() + currentMusic.getSource())){
                    player_like.setChecked(true);
                }else {
                    player_like.setChecked(false);
                }
            }

            @Override
            public void OnFail() {

            }
        });

        recentPlay.setOnRecentPlayDialogCloseListener(new OnRecentPlayDialogCloseListener() {
            @Override
            public void OnClose() {
                Music c = globalApplication.getCurrentMusic();
                if (sqlUtils.getFavMus(getApplicationContext(), c.getId() + c.getSource())){
                    player_like.setChecked(true);
                }else {
                    player_like.setChecked(false);
                }

                globalApplication.setOnMusicChange(new OnMusicChange() {
                    @Override
                    public void OnComplete() {
                        Music currentMusic = globalApplication.getCurrentMusic();
                        if (sqlUtils.getFavMus(getApplicationContext(), currentMusic.getId() + currentMusic.getSource())){
                            player_like.setChecked(true);
                        }else {
                            player_like.setChecked(false);
                        }
                    }

                    @Override
                    public void OnFail() {

                    }
                });
            }
        });
        ImageView player_down = findViewById(R.id.player_down);
        player_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Music currentMusic = globalApplication.getCurrentMusic();
                new MusicDownloadDialog().show(getSupportFragmentManager(), currentMusic.getId() + "download", currentMusic);
            }
        });

        ImageView player_share = findViewById(R.id.player_share);
        player_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: to share music

            }
        });
        ImageView player_copy = findViewById(R.id.player_copy);
        player_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Music currentMusic = globalApplication.getCurrentMusic();
                String singer = "";
                for (int i = 0; i < currentMusic.getArtist().length; i++){
                    if (i == 0){
                        singer += currentMusic.getArtist()[i];
                    }else {
                        singer += "/" + currentMusic.getArtist()[i];
                    }
                }
                String temp = currentMusic.getName() + "    " +
                        singer.toString() + "    " +
                        currentMusic.getAlbum() + "    " +
                        PlayerActivity.this.getResources().getString(R.string.introduce);
                ((ClipboardManager) PlayerActivity.this.getSystemService(CLIPBOARD_SERVICE))
                        .setPrimaryClip(ClipData.newPlainText("StickPoint Music Info", temp));
                Toast.makeText(PlayerActivity.this, R.string.copy_introduce, Toast.LENGTH_LONG).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        globalApplication.destoryMusicPlayerPageView();
    }
}
