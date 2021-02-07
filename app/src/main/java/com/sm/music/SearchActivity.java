package com.sm.music;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import com.sm.music.Bean.Music;
import com.sm.music.MusicUtils.GetMusic;
import com.sm.music.UIUtils.Util;
import com.sm.music.fragment.indexFragment;

import java.io.IOException;
import java.util.List;

public class SearchActivity extends AppCompatActivity {


    final static private int NETWORK_REFRESH_TAG = 202;

    final static private int NETWORK_ONLOAD_TAG = 201;

    final static private int REQUEST_MUSIC_URL = 203;

    final static private int REQUEST_MUSIC_LIST = 204;

    final static private int SHOW_SEARCH_LOADING = 304;

    final static private int SHOW_SEARCH_MUSIC_LIST = 302;


    //index search music list
    private List<Music> searchList = null;

    private GlobalApplication globalApplication = null;

    private RadioGroup source_group = null;

    private RadioButton kugou = null;
    private RadioButton netease = null;
    private RadioButton tencent = null;

    private ConstraintLayout searchBar = null;
    private ConstraintLayout searchPlayer = null;

    private TextView search_button = null;
    private EditText search = null;

    private int currentType = GetMusic.MUSIC_SOURCE_KUGOU;
    private String search_text = null;

    //more windows view
    private View more_view = null;

    private ConstraintLayout searchLoading = null;

    private ListView indexList_list = null;

    private RefreshLayout indexList_refresh = null;
    //more windows is open
    private Boolean isMoreOpen = false;

    private GetMusic conn = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.setActivityBarAlpha(this, false);
        setContentView(R.layout.activity_search);
        final int statusBarHeight = Util.getStatusBarHeight(this);
        final int NavigationBarHeight = Util.getNavigationBarHeight(SearchActivity.this);
        final Boolean HasNavigationBar = !Util.checkDeviceHasNavigationBar(this);
        globalApplication = (GlobalApplication) getApplication();

        conn = new GetMusic();

        source_group = findViewById(R.id.source_group);
        kugou = findViewById(R.id.kugou);
        netease = findViewById(R.id.netease);
        tencent = findViewById(R.id.tencent);

        indexList_refresh = findViewById(R.id.indexList_refresh);

        indexList_list = findViewById(R.id.indexList_list);

        searchLoading = findViewById(R.id.searchLoading);

        searchBar = findViewById(R.id.searchBar);
        searchPlayer = findViewById(R.id.searchPlayer);
        search_button = findViewById(R.id.search_button);
        search = findViewById(R.id.search);
        search.requestFocus();
        more_view = this.findViewById(R.id.more);

        searchBar.post(new Runnable() {
            public void run() {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) searchBar.getLayoutParams();
                layoutParams.height = statusBarHeight + searchBar.getHeight();
                searchBar.setLayoutParams(layoutParams);
                searchBar.setPadding(0,statusBarHeight,0,0);
            }
        });

        searchPlayer.post(new Runnable() {
            @Override
            public void run() {
                if (HasNavigationBar){
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) searchPlayer.getLayoutParams();
                    layoutParams.height = NavigationBarHeight + searchPlayer.getHeight();
                    searchPlayer.setLayoutParams(layoutParams);
                    searchPlayer.setPadding(0,0,0,NavigationBarHeight);
                }
            }
        });

        indexList_refresh.setRefreshHeader(new MaterialHeader(this));
        indexList_refresh.setRefreshFooter(new ClassicsFooter(this));
        indexList_refresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                //TODO: Index page refresh to do
                refresh_music_list_data(search_text, currentType, SearchActivity.this);
                refreshlayout.finishRefresh(2000);
            }
        });
        indexList_refresh.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                //TODO: Index page load more to do

                refreshlayout.finishLoadMore(2000);
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")){
                    search_button.setText(R.string.cancel);
                    search_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SearchActivity.this.finish();
                        }
                    });
                }else {
                    search_button.setText(R.string.search);
                    search_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showContainer(SHOW_SEARCH_LOADING);
                            search_text = String.valueOf(search.getText());
                            refresh_music_list_data(search_text, currentType, SearchActivity.this);
                        }
                    });
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchActivity.this.finish();
            }
        });
        currentType = getCurrentType();
        source_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                currentType = getCurrentType();
                if (search_text != null){
                    showContainer(SHOW_SEARCH_LOADING);
                    refresh_music_list_data(search_text, currentType,SearchActivity.this);
                }
            }
        });


    }

    public void onBackPressed() {
        if (isMoreOpen){
            more_view.setVisibility(View.INVISIBLE);
            isMoreOpen = false;
        }else {
            super.onBackPressed();
        }

    }

    public Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            showContainer(SHOW_SEARCH_MUSIC_LIST);
            if (msg.what == GetMusic.RESPOND_SUCCESS){
                if (msg.arg2 == REQUEST_MUSIC_URL){
                    globalApplication.setMusicUrl((String) msg.obj);
                }else if (msg.arg2 == REQUEST_MUSIC_LIST){
                    if (msg.arg1 == NETWORK_REFRESH_TAG){
                        searchList = (List<Music>) msg.obj;
                        indexList_list.setAdapter(new indexListAdapter());
                    }else if (msg.arg1 == NETWORK_ONLOAD_TAG){
                        searchList.addAll((List<Music>) msg.obj);
                        ((indexListAdapter) indexList_list.getAdapter()).notifyDataSetChanged();
                    }
                }
            }else{
                Toast.makeText(SearchActivity.this,R.string.network_wrong,Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void refresh_music_list_data(final String text, final int type, final Context context){
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
                        msg.arg2 = REQUEST_MUSIC_LIST;
                        msg.obj = temp;
                        handler.sendMessage(msg);
                    }else {
                        Toast.makeText(context, R.string.network_wrong, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private void setPlayerUrl(final String musicID, final String musicSource, final Context context){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = conn.getMusicPlayURL(musicID, musicSource);
                    if (url != null){
                        Message msg = Message.obtain();
                        msg.what = GetMusic.RESPOND_SUCCESS;
                        msg.arg2 = REQUEST_MUSIC_URL;
                        msg.obj = url;
                        handler.sendMessage(msg);
                    }else {
                        Toast.makeText(context, R.string.network_wrong, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                };

            }
        }).start();

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


    private void showContainer(int container){
        switch (container){
            case SHOW_SEARCH_MUSIC_LIST:
                ((View) indexList_list).setVisibility(View.VISIBLE);
                searchLoading.setVisibility(View.INVISIBLE);
                break;
            case SHOW_SEARCH_LOADING:
                ((View) indexList_list).setVisibility(View.INVISIBLE);
                searchLoading.setVisibility(View.VISIBLE);
                break;
            default:
                ((View) indexList_list).setVisibility(View.VISIBLE);
                searchLoading.setVisibility(View.INVISIBLE);
                break;
        }
    }


    class indexListAdapter extends BaseAdapter {

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
            View view = View.inflate(SearchActivity.this, R.layout.index_list_ltem_layout, null);
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
                    SearchActivity.this.showMore(music);
                }
            });
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((TextView) v.findViewById(R.id.index_list_item_music_name)).setTextColor(SearchActivity.this.getResources().getColor(R.color.textHint));
                    setPlayerUrl(music.getId(),music.getSource(), SearchActivity.this);
                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    SearchActivity.this.showMore(music);
                    return true;
                }
            });
            return view;
        }
    }


    public void showMore(Music music){
        LinearLayout more_container =  more_view.findViewById(R.id.more_container);
        LinearLayout more_top = more_view.findViewById(R.id.more_top);
        TextView indexMore_name = more_view.findViewById(R.id.indexMore_name);
        TextView indexMore_singer = more_view.findViewById(R.id.indexMore_singer);
        TextView indexMore_album = more_view.findViewById(R.id.indexMore_album);
        CheckBox forLike = more_view.findViewById(R.id.forLike);
        TextView ToDown = more_view.findViewById(R.id.ToDown);
        TextView ToShare = more_view.findViewById(R.id.ToShare);

        //TODO: to init like button

        more_view.setVisibility(View.VISIBLE);
        isMoreOpen = true;

        indexMore_name.setText(music.getName());
        more_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                more_view.setVisibility(View.INVISIBLE);
                isMoreOpen = false;
            }
        });
        more_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        String temp = "";
        for (int i = 0; i < music.getArtist().length; i++){
            if (i == 0){
                temp += music.getArtist()[i];
            }else {
                temp += "/" + music.getArtist()[i];
            }
        }
        indexMore_singer.setText(temp);
        indexMore_album.setText(music.getAlbum());

        more_top.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                TextView indexMore_name = v.findViewById(R.id.indexMore_name);
                TextView indexMore_singer = v.findViewById(R.id.indexMore_singer);
                TextView indexMore_album = v.findViewById(R.id.indexMore_album);
                String temp = indexMore_name.getText().toString() + "    " +
                        indexMore_singer.getText().toString() + "    " +
                        indexMore_album.getText().toString() + "    " +
                        getResources().getString(R.string.introduce);
                ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE))
                        .setPrimaryClip(ClipData.newPlainText("StickPoint Music Info", temp));
                Toast.makeText(SearchActivity.this, R.string.copy_introduce, Toast.LENGTH_LONG).show();
                return true;
            }
        });


        forLike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //TODO: to add & remove favoriate music
            }
        });
        ToDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: to download music

            }
        });
        ToShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: to share music
            }
        });
    }

}
