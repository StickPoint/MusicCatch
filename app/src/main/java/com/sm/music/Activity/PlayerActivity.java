package com.sm.music.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sm.music.R;
import com.sm.music.UIUtils.Util;

public class PlayerActivity extends AppCompatActivity {

    private ConstraintLayout playerTop = null;
    private ConstraintLayout player_control = null;
    private ImageView playerTop_close = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.setActivityBarAlpha(this, true);
        setContentView(R.layout.activity_player);
        final int statusBarHeight = Util.getStatusBarHeight(PlayerActivity.this);
        final int NavigationBarHeight = Util.getNavigationBarHeight(PlayerActivity.this);
        final Boolean HasNavigationBar = !Util.checkDeviceHasNavigationBar(this);

        playerTop = findViewById(R.id.playerTop);
        player_control = findViewById(R.id.player_control);

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
            }
        });

    }
}
