package com.sm.music.Fragment;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.MaterialHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.sm.music.Bean.Music;
import com.sm.music.GlobalApplication;
import com.sm.music.MusicUtils.GetMusic;
import com.sm.music.R;
import com.sm.music.Activity.SearchActivity;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class search_pager extends Fragment {


    final static private int NETWORK_REFRESH_TAG = 202;

    final static private int NETWORK_ONLOAD_TAG = 201;


    final static private int REQUEST_MUSIC_LIST = 204;

    final static private int SHOW_SEARCH_LOADING = 304;

    final static private int SHOW_SEARCH_MUSIC_LIST = 302;

    private GlobalApplication globalApplication = null;

    //index search music list
    private List<Music> searchList = null;

    private ConstraintLayout searchLoading = null;

    private ListView searchList_list = null;

    private RefreshLayout searchList_refresh = null;

    private View view = null;

    private int source = -1;

    private String search_text = null;

    private GetMusic conn = null;


    public search_pager(int source) {
        this.source = source;
        conn = new GetMusic();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_search_pager, container, false);
        globalApplication = (GlobalApplication) getActivity().getApplication();
        searchList_refresh = view.findViewById(R.id.searchList_refresh);
        searchList_list = view.findViewById(R.id.searchList_list);
        searchLoading = view.findViewById(R.id.searchLoading);
        searchList_refresh.setRefreshHeader(new MaterialHeader(getActivity()));
        searchList_refresh.setRefreshFooter(new ClassicsFooter(getActivity()));
        searchList_refresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                //TODO: Search page refresh to do
                if (search_text != null){
                    refresh_music_list_data(search_text);
                    refreshlayout.finishRefresh(2000);
                }else {
                    refreshlayout.finishRefresh(false);
                }
            }
        });
        searchList_refresh.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                //TODO: Search page load more to do
                if (search_text != null){
                    refresh_music_list_data(search_text);
                    refreshlayout.finishLoadMore(2000);
                }else {
                    refreshlayout.finishLoadMore(false);
                }
            }
        });
        if (searchList != null){
            searchList_list.setAdapter(new searchListAdapter());
        }
        String temp = ((SearchActivity) getActivity()).getSearchText();
        if (temp != null && search_text != null && !search_text.equals(temp)){
            search_text = temp;
            search_music_list_data(search_text);
        }

        return view;
    }
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.arg1 == GetMusic.RESPOND_SUCCESS ){

                if (msg.what == REQUEST_MUSIC_LIST){
                    showContainer(SHOW_SEARCH_MUSIC_LIST);

                    if (msg.arg2 == NETWORK_REFRESH_TAG){
                        searchList = (List<Music>) msg.obj;
                        searchList_list.setAdapter(new searchListAdapter());
                    }else if (msg.arg2 == NETWORK_ONLOAD_TAG){
                        searchList.addAll((List<Music>) msg.obj);
                        ((searchListAdapter) searchList_list.getAdapter()).notifyDataSetChanged();
                    }

                }

            }else{
                Toast.makeText(getActivity(),R.string.network_wrong,Toast.LENGTH_SHORT).show();
            }
        }
    };



    public void search_music_list_data(String text){
        if (search_text != null && search_text.equals(text) && searchList != null){
            return;
        }
        search_text = text;
        showContainer(SHOW_SEARCH_LOADING);
        get_List(text, REQUEST_MUSIC_LIST, NETWORK_REFRESH_TAG);
    }

    public void refresh_music_list_data(String text){
        get_List(text, REQUEST_MUSIC_LIST, NETWORK_REFRESH_TAG);
    }

    private void get_List(final String text, final int what, final int arg2){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = conn.getSearchRequsetURL(text, source);
                String json_string = null;
                try {
                    json_string = conn.getJSON(url);
                    List temp = conn.getMusicList(json_string);
                    Message msg = Message.obtain();
                    if (temp != null){
                        msg.what = what;
                        msg.arg1 = GetMusic.RESPOND_SUCCESS;
                        msg.arg2 = arg2;
                        msg.obj = temp;
                    }else {
                        msg.arg1 = GetMusic.RESPOND_TIMEOUT;
                    }
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private void showContainer(int container){
        switch (container){
            case SHOW_SEARCH_MUSIC_LIST:
                ((View) searchList_list).setVisibility(View.VISIBLE);
                searchLoading.setVisibility(View.INVISIBLE);
                break;
            case SHOW_SEARCH_LOADING:
                ((View) searchList_list).setVisibility(View.INVISIBLE);
                searchLoading.setVisibility(View.VISIBLE);
                break;
            default:
                ((View) searchList_list).setVisibility(View.VISIBLE);
                searchLoading.setVisibility(View.INVISIBLE);
                break;
        }
    }

    class searchListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return searchList.size();
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
            final Music music = searchList.get(position);
            ((TextView) view.findViewById(R.id.index_list_item_music_name)).setText(music.getName());
            String temp = "";
            for (int i = 0; i < music.getArtist().length; i++) {
                if (i == 0) {
                    temp += music.getArtist()[i];
                } else {
                    temp += "/" + music.getArtist()[i];
                }
            }
            ((TextView) view.findViewById(R.id.index_list_item_music_singer)).setText(temp);
            ((TextView) view.findViewById(R.id.index_list_item_music_album)).setText(music.getAlbum());
            view.findViewById(R.id.index_list_item_music_more).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((SearchActivity) getActivity()).showMore(music);
                }
            });
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((TextView) v.findViewById(R.id.index_list_item_music_name)).setTextColor(getActivity().getResources().getColor(R.color.textHint));
                    globalApplication.setCurrentMusic(music);
                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ((SearchActivity) getActivity()).showMore(music);
                    return true;
                }
            });
            return view;
        }
    }

}
