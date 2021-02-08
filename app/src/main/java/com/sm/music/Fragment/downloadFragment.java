package com.sm.music.Fragment;


import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.sm.music.R;
import com.sm.music.UIUtils.Util;

/**
 * A simple {@link Fragment} subclass.
 */
public class downloadFragment extends Fragment {

    private ConstraintLayout top = null;

    public downloadFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_download, container, false);
        final int statusBarHeight = Util.getStatusBarHeight(getActivity());
        top = view.findViewById(R.id.downloadBar);
        top.post(new Runnable() {
            public void run() {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) top.getLayoutParams();
                layoutParams.height = statusBarHeight + top.getHeight();
                top.setLayoutParams(layoutParams);
                top.setPadding(0,statusBarHeight,0,0);
            }
        });
        return view;
    }

}
