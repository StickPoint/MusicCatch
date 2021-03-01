package com.sm.music.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.sm.music.GlobalApplication;
import com.sm.music.R;
import com.sm.music.UIUtils.Util;

public class WelcomeActivity extends AppCompatActivity {

    private static final String LOG_TAG = "WelcomeActivityLog";

    private GlobalApplication globalApplication = null;

    private static final int WELCOME_ACTIVITY_PERMISSION_REQUEST_CODE = 888;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.setActivityBarAlpha(this, false);
        setContentView(R.layout.activity_welcome);

        globalApplication = (GlobalApplication) getApplication();
        globalApplication.init();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i(LOG_TAG, "start check permissions");
                askPermissions();
            }
        },500);

    }


    public void askPermissions() {
        boolean isAllGranted = checkPermissionAllGranted(new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
//                Manifest.permission.READ_PHONE_STATE
        });
        if (!isAllGranted) {
            Log.i(LOG_TAG, "start ask denied permissions");
            requestPermissions(new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.FOREGROUND_SERVICE

            }, WELCOME_ACTIVITY_PERMISSION_REQUEST_CODE);
        }else {
            Log.i(LOG_TAG, "all permissions granted");
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            Log.i(LOG_TAG, "start MainActivity");
            startActivity(intent);
            overridePendingTransition(R.anim.welcome_alpha_in,R.anim.no_transfrom);
            WelcomeActivity.this.finish();
        }
    }

    private boolean checkPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.i(LOG_TAG, "permission " + permission + " denied");
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WELCOME_ACTIVITY_PERMISSION_REQUEST_CODE) {
            boolean isAllGranted = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(LOG_TAG, permissions[i] + " permission are still denied");
                    isAllGranted = false;
                    break;
                }
            }
            if (isAllGranted) {
                Log.i(LOG_TAG, "all permissions granted");
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                Log.i(LOG_TAG, "start MainActivity");
                startActivity(intent);
                overridePendingTransition(R.anim.welcome_alpha_in,R.anim.no_transfrom);
                WelcomeActivity.this.finish();
            } else {
                Toast.makeText(WelcomeActivity.this, R.string.permissions_hint, Toast.LENGTH_LONG).show();
                this.finish();
            }
        }
    }


}
