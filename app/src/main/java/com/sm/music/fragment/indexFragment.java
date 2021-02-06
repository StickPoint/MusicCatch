package com.sm.music.fragment;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.MaterialHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.sm.music.GetMusic;
import com.sm.music.Music;
import com.sm.music.R;
import com.sm.music.Util;

import java.util.List;

public class indexFragment extends Fragment {

    private ConstraintLayout top = null;

    private RadioGroup source_group = null;

    private RadioButton kugou = null;
    private RadioButton netease = null;
    private RadioButton tencent = null;

    private TextView search_button = null;
    private EditText search = null;

    private int currentType = GetMusic.MUSIC_SOURCE_KUGOU;

    private List<Music> musicList = null;

    private ListView indexList_list = null;

    private GetMusic conn = null;



    public Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == GetMusic.RESPOND_SUCCESS){
                musicList = (List<Music>) msg.obj;
                indexList_list.setAdapter(new indexListAdapter());
            }else{
                ((indexListAdapter) indexList_list.getAdapter()).notifyDataSetChanged();
            }
        }
    };


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
        RefreshLayout indexList_container = (RefreshLayout)view.findViewById(R.id.indexList_container);
        indexList_container.setRefreshHeader(new MaterialHeader(getActivity()));
        indexList_container.setRefreshFooter(new ClassicsFooter(getActivity()));
        indexList_container.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                //TODO: Index page refresh to do

                refreshlayout.finishRefresh(2000/*,false*/);//传入false表示刷新失败
            }
        });
        indexList_container.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                //TODO: Index page load more to do

                refreshlayout.finishLoadMore(2000/*,false*/);//传入false表示加载失败
            }
        });
        //TODO: index
        source_group = view.findViewById(R.id.source_group);
        kugou = view.findViewById(R.id.kugou);
        netease = view.findViewById(R.id.netease);
        tencent = view.findViewById(R.id.tencent);
        indexList_list = view.findViewById(R.id.indexList_list);

        search_button = view.findViewById(R.id.search_button);
        search = view.findViewById(R.id.search);

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String url = conn.getSearchRequsetURL(String.valueOf(search.getText()), currentType);
                        String json_string = null;
                        try {
                            json_string = conn.getJSON(url);
                            List temp = conn.getMusicList(json_string);
                            if (temp != null){
                                Message msg = Message.obtain();
                                msg.what = GetMusic.RESPOND_SUCCESS;
                                msg.obj = temp;
                                handler.sendMessage(msg);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        });

        currentType = getCurrentType();
        source_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                currentType = getCurrentType();
            }
        });


        return view;
    }

    private int getCurrentType(){
        int currentId = source_group.getCheckedRadioButtonId();
        if (currentId == kugou.getId()){
            return GetMusic.MUSIC_SOURCE_KUGOU;
        }else if (currentId == netease.getId()){
            return GetMusic.MUSIC_SOURCE_NETEASE;
        }else if (currentId == tencent.getId()){
            return GetMusic.MUSIC_SOURCE_TENCENT;
        }else {
            return GetMusic.MUSIC_SOURCE_KUGOU;
        }
    }

    class indexListAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return musicList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = View.inflate(getActivity(), R.layout.index_list_ltem_layout, null);
            ((TextView) view.findViewById(R.id.index_list_item_music_name)).setText(musicList.get(position).getName());
            ((TextView) view.findViewById(R.id.index_list_item_music_singer)).setText(musicList.get(position).getArtist()[0]);
            ((TextView) view.findViewById(R.id.index_list_item_music_adlbm)).setText(musicList.get(position).getAlbum());

            return view;
        }
    }

}
