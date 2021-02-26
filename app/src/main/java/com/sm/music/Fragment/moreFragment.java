package com.sm.music.Fragment;


import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sm.music.Activity.MainActivity;
import com.sm.music.R;
import com.sm.music.UIUtils.Util;
import com.xuexiang.xupdate.XUpdate;

/**
 * A simple {@link Fragment} subclass.
 */
public class moreFragment extends Fragment {

    private ConstraintLayout top = null;

    private TextView check_update = null;

    private WebView aboutUs_Web = null;

    public moreFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false);
        final int statusBarHeight = Util.getStatusBarHeight(getActivity());
        top = view.findViewById(R.id.moreBar);
        top.post(new Runnable() {
            public void run() {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) top.getLayoutParams();
                layoutParams.height = statusBarHeight + top.getHeight();
                top.setLayoutParams(layoutParams);
                top.setPadding(0,statusBarHeight,0,0);
            }
        });

//        TextView about_us_button = view.findViewById(R.id.about_us_button);
//        about_us_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(getActivity(), AboutActivity.class));
//                getActivity().overridePendingTransition(R.anim.transfrom_buttom_in,R.anim.no_transfrom);
//            }
//        });
        check_update = view.findViewById(R.id.check_update);
        check_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XUpdate.newBuild(getActivity())
                        .updateUrl(MainActivity.UPDATE_INFO_URL)
                        .update();
            }
        });

        aboutUs_Web = view.findViewById(R.id.aboutUs_Web);
        aboutUs_Web.loadUrl("file:///android_asset/web/index.html");

        return view;
    }

}
