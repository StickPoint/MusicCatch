package com.sm.music.fragment;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.sm.music.GetMusic;
import com.sm.music.GlobalApplication;
import com.sm.music.MainActivity;
import com.sm.music.Music;
import com.sm.music.R;
import com.sm.music.Util;

import java.util.List;

public class indexFragment extends Fragment {

    final static private int NETWORK_REFRESH_TAG = 202;

    final static private int NETWORK_ONLOAD_TAG = 201;

    private GlobalApplication globalApplication = null;

    private ConstraintLayout top = null;

    private RadioGroup source_group = null;

    private RadioButton kugou = null;
    private RadioButton netease = null;
    private RadioButton tencent = null;

    private TextView search_button = null;
    private EditText search = null;

    private int currentType = GetMusic.MUSIC_SOURCE_KUGOU;
    private String search_text = null;


    private RefreshLayout indexList_container = null;
    private ListView indexList_list = null;
    private ConstraintLayout loading = null;

    private GetMusic conn = null;


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

        source_group = view.findViewById(R.id.source_group);
        kugou = view.findViewById(R.id.kugou);
        netease = view.findViewById(R.id.netease);
        tencent = view.findViewById(R.id.tencent);
        indexList_container = view.findViewById(R.id.indexList_container);
        indexList_list = view.findViewById(R.id.indexList_list);
        loading = view.findViewById(R.id.loading);

        search_button = view.findViewById(R.id.search_button);
        search = view.findViewById(R.id.search);

        indexList_container.setRefreshHeader(new MaterialHeader(getActivity()));
        indexList_container.setRefreshFooter(new ClassicsFooter(getActivity()));
        indexList_container.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                //TODO: Index page refresh to do
                refresh_data(search_text, currentType,getActivity());
                refreshlayout.finishRefresh(2000);
            }
        });
        indexList_container.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                //TODO: Index page load more to do

                refreshlayout.finishLoadMore(2000);
            }
        });

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((View) indexList_container).setVisibility(View.INVISIBLE);
                loading.setVisibility(View.VISIBLE);
                search_text = String.valueOf(search.getText());
                refresh_data(search_text, currentType,getActivity());
            }
        });

        currentType = getCurrentType();
        source_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                currentType = getCurrentType();
                if (search_text != null){
                    ((View) indexList_container).setVisibility(View.INVISIBLE);
                    loading.setVisibility(View.VISIBLE);
                    refresh_data(search_text, currentType,getActivity());
                }
            }
        });

        if (!globalApplication.isMusicListEmpty()){
            indexList_list.setAdapter(new indexListAdapter());
        }


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


    public Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            ((View) indexList_container).setVisibility(View.VISIBLE);
            loading.setVisibility(View.INVISIBLE);
            if (msg.what == GetMusic.RESPOND_SUCCESS){
                if (msg.arg1 == NETWORK_REFRESH_TAG){
                    globalApplication.setMusicList((List<Music>) msg.obj);
                    indexList_list.setAdapter(new indexListAdapter());
                }else if (msg.arg1 == NETWORK_ONLOAD_TAG){
                    globalApplication.addMusicList((List<Music>) msg.obj);
                    ((indexListAdapter) indexList_list.getAdapter()).notifyDataSetChanged();
                }
            }else{
                Toast.makeText(getActivity(),R.string.network_wrong,Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void refresh_data(final String text, final int type, final Context context){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = conn.getSearchRequsetURL(text, type);
                String json_string = null;
                try {
                    json_string = conn.getJSON(url);
                    List temp = conn.getMusicList(json_string);
                    if (temp != null){
                        Message msg = Message.obtain();
                        msg.what = GetMusic.RESPOND_SUCCESS;
                        msg.arg1 = NETWORK_REFRESH_TAG;
                        msg.obj = temp;
                        handler.sendMessage(msg);
                    }else {
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, R.string.network_wrong, Toast.LENGTH_SHORT).show();
                }

            }
        }).start();
    }

    class indexListAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return globalApplication.getMusicList().size();
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = View.inflate(getActivity(), R.layout.index_list_ltem_layout, null);
            final Music music = globalApplication.getMusicList().get(position);
            ((TextView) view.findViewById(R.id.index_list_item_music_name)).setText(music.getName());
            ((TextView) view.findViewById(R.id.index_list_item_music_singer)).setText(music.getArtist()[0]);
            ((TextView) view.findViewById(R.id.index_list_item_music_adlbm)).setText(music.getAlbum());
            view.findViewById(R.id.index_list_item_music_more).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) getActivity()).showMore(music);
                }
            });
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((TextView) v.findViewById(R.id.index_list_item_music_name)).setTextColor(getActivity().getResources().getColor(R.color.textHint));
                    globalApplication.setMusicUrl(music.getUrl_id());
                }
            });
            return view;
        }
    }

}
