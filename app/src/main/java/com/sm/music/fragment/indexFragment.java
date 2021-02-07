package com.sm.music.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.MaterialHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.sm.music.MusicUtils.GetMusic;
import com.sm.music.GlobalApplication;
import com.sm.music.MainActivity;
import com.sm.music.Bean.Music;
import com.sm.music.R;
import com.sm.music.SearchActivity;
import com.sm.music.UIUtils.Util;

import java.util.List;

public class indexFragment extends Fragment {

    final static private int NETWORK_REFRESH_TAG = 202;

    final static private int NETWORK_ONLOAD_TAG = 201;

    final static private int SHOW_SONG_LIST = 301;


    final static private int SHOW_LOADING = 303;

    private GlobalApplication globalApplication = null;

    private ConstraintLayout top = null;

    private ListView songList_container = null;

    private RefreshLayout indexSongList_refresh = null;

    private ConstraintLayout loading = null;

    private GetMusic conn = null;

    private TextView searchPageIn = null;

    public indexFragment() {
        conn = new GetMusic();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        musicList = new GetMusic().getSearchRequsetURL();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_index, container, false);
        final int statusBarHeight = Util.getStatusBarHeight(getActivity());
        top = view.findViewById(R.id.searchBar);
        top.post(new Runnable() {
            public void run() {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) top.getLayoutParams();
                layoutParams.height = statusBarHeight + top.getHeight();
                top.setLayoutParams(layoutParams);
                top.setPadding(0,statusBarHeight,0,0);
            }
        });
        //TODO: index

        globalApplication = (GlobalApplication) getActivity().getApplication();


        songList_container = view.findViewById(R.id.songList_container);
        loading = view.findViewById(R.id.loading);
        indexSongList_refresh = view.findViewById(R.id.indexSongList_refresh);

        searchPageIn = view.findViewById(R.id.searchPageIn);
        searchPageIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SearchActivity.class));
            }
        });

        indexSongList_refresh.setRefreshHeader(new MaterialHeader(getActivity()));
        indexSongList_refresh.setRefreshFooter(new ClassicsFooter(getActivity()));
        indexSongList_refresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                //TODO: Index page song list refresh to do
                refreshlayout.finishRefresh(2000);
            }
        });
        indexSongList_refresh.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                //TODO: Index page song list load more to do

                refreshlayout.finishLoadMore(2000);
            }
        });



        return view;
    }


    private void showContainer(int container){
        switch (container){
            case SHOW_SONG_LIST:
                ((View) indexSongList_refresh).setVisibility(View.VISIBLE);
                loading.setVisibility(View.INVISIBLE);
                break;
            case SHOW_LOADING:
                loading.setVisibility(View.VISIBLE);

                ((View) indexSongList_refresh).setVisibility(View.INVISIBLE);
                break;
            default:
                ((View) indexSongList_refresh).setVisibility(View.VISIBLE);
                loading.setVisibility(View.INVISIBLE);
                break;
        }

    }

}
