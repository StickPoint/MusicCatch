package com.sm.music.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.MaterialHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.sm.music.Bean.Music;
import com.sm.music.Bean.RecMusic;
import com.sm.music.Fragment.search_pager;
import com.sm.music.GlobalApplication;
import com.sm.music.Listener.OnMusicChange;
import com.sm.music.MusicUtils.ConvertBean;
import com.sm.music.MusicUtils.GetMusic;
import com.sm.music.MusicUtils.MoreWindowDialog;
import com.sm.music.MusicUtils.RecentPlay;
import com.sm.music.R;
import com.sm.music.UIUtils.Util;

import java.util.List;

public class ListActivity extends AppCompatActivity {

    private static final int NETWORK_SEARCH_TAG = 448;

    private static final int NETWORK_REFRESH_TAG = 202;

    private static final int NETWORK_ONLOAD_TAG = 201;

    private static final int NOT_HAVE_MORE = 502;

    private static final int REQUEST_MUSIC_LIST = 204;

    private static final int SHOW_MUSIC_LIST = 592;

    private static final int SHOW_LOADING = 5;

    private static final int ACTIVITY_TAG = 857;

    private static final int ON_PAGE_LOAD_NUM = 20;

    private GlobalApplication globalApplication = null;

    private long id = 0;

    private List<Music> music_list = null;

    private GetMusic conn = null;

    private int currentPage = 0;

    private FrameLayout list_page = null;
    private LinearLayout list = null;
    private ConstraintLayout list_top = null;
    private ImageView list_cancel = null;
    private TextView list_title = null;
    private RefreshLayout list_refresh = null;
    private ListView list_container = null;
    private FrameLayout list_buttom = null;
    private ConstraintLayout listLoading = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.setActivityBarAlpha(this, false);
        setContentView(R.layout.activity_list);
        final int statusBarHeight = Util.getStatusBarHeight(this);
        final int NavigationBarHeight = Util.getNavigationBarHeight(ListActivity.this);
        final Boolean HasNavigationBar = !Util.checkDeviceHasNavigationBar(this);
        globalApplication = (GlobalApplication) getApplication();
        list_page = findViewById(R.id.list_page);
        list = findViewById(R.id.list);
        list_top = findViewById(R.id.list_top);
        list_cancel = findViewById(R.id.list_cancel);
        list_title = findViewById(R.id.list_title);
        list_refresh = findViewById(R.id.list_refresh);
        list_container = findViewById(R.id.list_container);
        list_buttom = findViewById(R.id.list_buttom);
        listLoading = findViewById(R.id.listLoading);



        list_buttom.addView(globalApplication.createMinMusicPlayer(ListActivity.this, ACTIVITY_TAG));

        list_top.post(new Runnable() {
            public void run() {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) list_top.getLayoutParams();
                layoutParams.height = statusBarHeight + list_top.getHeight();
                list_top.setLayoutParams(layoutParams);
                list_top.setPadding(0,statusBarHeight,0,0);
            }
        });

        list_buttom.post(new Runnable() {
            @Override
            public void run() {
                if (HasNavigationBar){
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) list_buttom.getLayoutParams();
                    layoutParams.height = NavigationBarHeight + list_buttom.getHeight();
                    list_buttom.setLayoutParams(layoutParams);
                    list_buttom.setPadding(0,0,0,NavigationBarHeight);
                }
            }
        });

        list_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListActivity.this.finish();
                overridePendingTransition(0,R.anim.transfrom_buttom_out);
            }
        });

        Intent intent = getIntent();

        list_title.setText(intent.getStringExtra("name"));
        id = intent.getLongExtra("id", 0);

        conn = new GetMusic();
        init_music_list_data();

        list_refresh.setRefreshHeader(new MaterialHeader(ListActivity.this));
        list_refresh.setRefreshFooter(new ClassicsFooter(ListActivity.this));
        list_refresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                //TODO: Search page refresh to do
                if (id != 0){
                    refresh_music_list_data();
                }else {
                    refreshlayout.finishRefresh(false);
                }
            }
        });
        list_refresh.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                //TODO: Search page load more to do
                if (id != 0){
                    onload_music_list_data();
                }else {
                    refreshlayout.finishLoadMore(false);
                }
            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        globalApplication.destroyMinMusicPlayer(ACTIVITY_TAG);
    }




    public void init_music_list_data(){
        showContainer(SHOW_LOADING);
        get_List(id,NETWORK_SEARCH_TAG);
    }

    public void refresh_music_list_data(){
        get_List(id, NETWORK_REFRESH_TAG);
    }


    public void onload_music_list_data(){
        get_List(id, NETWORK_ONLOAD_TAG);
    }

    private void get_List(final long id, final int arg2){
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.arg1 == GetMusic.RESPOND_SUCCESS ){
                    showContainer(SHOW_MUSIC_LIST);
                    if (msg.what == REQUEST_MUSIC_LIST){
                        if (msg.arg2 == NETWORK_REFRESH_TAG || msg.arg2 == NETWORK_SEARCH_TAG){
                            music_list = (List<Music>) msg.obj;
                            if (msg.arg2 == NETWORK_REFRESH_TAG)
                                list_refresh.finishRefresh(true);
                            list_container.setAdapter(new ListActivity.listPageAdapter());
                        }else if (msg.arg2 == NETWORK_ONLOAD_TAG){
                            list_refresh.finishLoadMore(true);
                            if (music_list != null){
                                music_list.addAll((List<Music>) msg.obj);
                                ((ListActivity.listPageAdapter) list_container.getAdapter()).notifyDataSetChanged();
                            }else {
                                init_music_list_data();
                            }
                        }else if (msg.arg2 == NOT_HAVE_MORE){
                            list_refresh.finishLoadMore(true);
                        }
                    }
                }else{
                    if (msg.arg2 == NETWORK_REFRESH_TAG)
                        list_refresh.finishRefresh(false);
                    if (msg.arg2 == NETWORK_ONLOAD_TAG)
                        list_refresh.finishLoadMore(false);
                    Toast.makeText(ListActivity.this,R.string.network_wrong,Toast.LENGTH_SHORT).show();
                }
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (currentPage < 10){
                    try {
                        List temp = null;
                        if (arg2 == NETWORK_REFRESH_TAG || arg2 == NETWORK_SEARCH_TAG){
                            currentPage = 0;
                            temp = conn.getRecMusListByPages(String.valueOf(id), currentPage);
                        }else if (arg2 == NETWORK_ONLOAD_TAG){
                            temp = conn.getRecMusListByPages(String.valueOf(id), currentPage);
                        }
                        Message msg = Message.obtain();
                        if (temp != null){
                            currentPage ++;
                            msg.what = REQUEST_MUSIC_LIST;
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
                }else {
                    Message msg = Message.obtain();
                    msg.what = REQUEST_MUSIC_LIST;
                    msg.arg1 = GetMusic.RESPOND_SUCCESS;
                    msg.arg2 = NOT_HAVE_MORE;
                }

            }
        }).start();
    }


    private void showContainer(int container){
        switch (container){
            case SHOW_MUSIC_LIST:
                ((View) list_refresh).setVisibility(View.VISIBLE);
                listLoading.setVisibility(View.INVISIBLE);
                break;
            case SHOW_LOADING:
                ((View) list_refresh).setVisibility(View.INVISIBLE);
                listLoading.setVisibility(View.VISIBLE);
                break;
            default:
                ((View) list_refresh).setVisibility(View.VISIBLE);
                listLoading.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (list_container != null && list_container.getAdapter() != null)
            ((BaseAdapter) list_container.getAdapter()).notifyDataSetChanged();
    }

    class listPageAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return music_list.size();
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
            View view = View.inflate(ListActivity.this, R.layout.ranking_list_item_layout, null);
            final Music music = music_list.get(position);
            if (RecentPlay.isPlayedRecently(ListActivity.this,music.getId()) != -1){
                ((TextView) view.findViewById(R.id.index_list_item_music_name)).setTextColor(getResources().getColor(R.color.textHint));
            }

            ((TextView) view.findViewById(R.id.index_list_rank)).setText(String.valueOf(position + 1));
            if (position < 3){
                ((TextView) view.findViewById(R.id.index_list_rank)).setTextColor(getResources().getColor(R.color.colorPrimary));
            }else {
                ((TextView) view.findViewById(R.id.index_list_rank)).setTextColor(getResources().getColor(R.color.textHint));
            }
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
                    MoreWindowDialog moreWindowDialog = new MoreWindowDialog();
                    moreWindowDialog.show(getSupportFragmentManager(), music.getId(), music);
                }
            });
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((TextView) v.findViewById(R.id.index_list_item_music_name)).setTextColor(getResources().getColor(R.color.textHint));
                    globalApplication.setOnMusicChange(new OnMusicChange() {
                        @Override
                        public void OnComplete() {
                            ((BaseAdapter) list_container.getAdapter()).notifyDataSetChanged();
                        }

                        @Override
                        public void OnFail() {
                            ((BaseAdapter) list_container.getAdapter()).notifyDataSetChanged();
                        }
                    });
                    globalApplication.setCurrentMusic(music);
                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
//                    showMore(music);
                    MoreWindowDialog moreWindowDialog = new MoreWindowDialog();
                    moreWindowDialog.show(getSupportFragmentManager(), music.getId(), music);
                    return true;
                }
            });
            FrameLayout before_name = view.findViewById(R.id.before_name);
            if (globalApplication.getCurrentMusic() != null && globalApplication.getCurrentMusic().getId().equals(music.getId())){
                ImageView imageView = new ImageView(ListActivity.this);
                imageView.setImageResource(R.drawable.ic_playing);
                before_name.removeView(view.findViewById(R.id.index_list_rank));
                before_name.addView(imageView);
            }
            return view;
        }
    }
}
