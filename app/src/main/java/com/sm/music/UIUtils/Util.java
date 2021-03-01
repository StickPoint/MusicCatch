package com.sm.music.UIUtils;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.core.graphics.ColorUtils;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import com.sm.music.R;

import static android.view.View.*;

public class Util {

    private static final String NAVIGATION= "navigationBarBackground";

    static public void setActivityFullScreen(Activity activity) {
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    static public void setActivityBarAlpha(Activity activity, Boolean isLightColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if (isLightColor) {
                activity.getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | SYSTEM_UI_FLAG_LAYOUT_STABLE);
            } else {
                activity.getWindow().getDecorView().setSystemUiVisibility( SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
            activity.getWindow().setStatusBarColor(activity.getResources().getColor(R.color.transparent));
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }
    static public int getStatusBarHeight(Activity activity) {
        Resources resources = activity.getResources();
        int resourceId = resources.getIdentifier("status_bar_height","dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }
    static public int getNavigationBarHeight(Activity activity) {
        Resources resources = activity.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height","dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    static public int getWindowsHeight(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
    }
    static public int getWindowsWeight(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    static public int DipToPx(Activity activity, float dpValue) {
        float scale = activity.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    static public int PxToDip(Activity activity, float pxValue) {
        float scale = activity.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

//    static public boolean checkDeviceHasNavigationBar(Activity activity) {
//        WindowManager windowManager = activity.getWindowManager();
//        Display d = windowManager.getDefaultDisplay();
//
//        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            d.getRealMetrics(realDisplayMetrics);
//        }
//
//        int realHeight = realDisplayMetrics.heightPixels;
//        int realWidth = realDisplayMetrics.widthPixels;
//
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        d.getMetrics(displayMetrics);
//
//        int displayHeight = displayMetrics.heightPixels;
//        int displayWidth = displayMetrics.widthPixels;
//
//        return (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
//    }


//    public static  boolean checkDeviceHasNavigationBar(Activity activity){
//        boolean menu = ViewConfiguration.get(activity).hasPermanentMenuKey();
//        boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
//        if (menu || back) {
//            ViewGroup vp = (ViewGroup) activity.getWindow().getDecorView();
//            if (vp != null) {
//                for (int i = 0; i < vp.getChildCount(); i++) {
//                    vp.getChildAt(i).getContext().getPackageName();
//                    if (vp.getChildAt(i).getId()!= NO_ID && NAVIGATION.equals(activity.getResources().getResourceEntryName(vp.getChildAt(i).getId()))) {
//                        return true;
//                    }
//                }
//            }
//            return true;
//        } else {
//            ViewGroup vp = (ViewGroup) activity.getWindow().getDecorView();
//            if (vp != null) {
//                for (int i = 0; i < vp.getChildCount(); i++) {
//                    vp.getChildAt(i).getContext().getPackageName();
//                    if (vp.getChildAt(i).getId()!= NO_ID && NAVIGATION.equals(activity.getResources().getResourceEntryName(vp.getChildAt(i).getId()))) {
//                        return true;
//                    }
//                }
//            }
//            return false;
//        }
//    }

    public static  boolean checkDeviceHasNavigationBar(Activity activity){
        ViewGroup vp = (ViewGroup) activity.getWindow().getDecorView();
        if (vp != null) {
            for (int i = 0; i < vp.getChildCount(); i++) {
                vp.getChildAt(i).getContext().getPackageName();
                if (vp.getChildAt(i).getId()!= NO_ID && NAVIGATION.equals(activity.getResources().getResourceEntryName(vp.getChildAt(i).getId()))) {
                    return true;
                }
            }
        }
        return false;
    }


    static public boolean isLightColor(@ColorInt int color) {
        return ColorUtils.calculateLuminance(color) >= 0.5;
    }
}
