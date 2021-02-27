package com.sm.music.Fragment;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.scwang.smart.refresh.header.MaterialHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.sm.music.Activity.MainActivity;
import com.sm.music.Bean.Music;
import com.sm.music.GlobalApplication;
import com.sm.music.Listener.OnChangeFavStatusListener;
import com.sm.music.Listener.OnMusicChange;
import com.sm.music.MusicUtils.MoreWindowDialog;
import com.sm.music.R;
import com.sm.music.SQL.SQLUtils;
import com.sm.music.UIUtils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class likeFragment extends Fragment {


    private static final int NO_FAV_MUSIC = 533;

    private static final int SHOW_MUSIC_LIST = 874;

    private GlobalApplication globalApplication = null;

    private ConstraintLayout top = null;
    private FrameLayout likePage = null;
    private ListView likeList_list = null;
    private RefreshLayout likeList_container = null;
    private LinearLayout like = null;
    private SQLUtils sqlUtils = null;
    private ConstraintLayout no_fav = null;

    private List<Music> like_list = null;

    MoreWindowDialog moreWindowDialog;

    public likeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        moreWindowDialog = new MoreWindowDialog();

        View view = inflater.inflate(R.layout.fragment_like, container, false);
        final int statusBarHeight = Util.getStatusBarHeight(getActivity());
        globalApplication = (GlobalApplication) getActivity().getApplication();
        top = view.findViewById(R.id.likeBar);
        likePage = view.findViewById(R.id.likePage);
        likeList_list = view.findViewById(R.id.likeList_list);
        likeList_container = view.findViewById(R.id.likeList_container);
        like = view.findViewById(R.id.like);
        no_fav = view.findViewById(R.id.no_fav);

        sqlUtils = new SQLUtils();

        top.post(new Runnable() {
            public void run() {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) top.getLayoutParams();
                layoutParams.height = statusBarHeight + top.getHeight();
                top.setLayoutParams(layoutParams);
                top.setPadding(0,statusBarHeight,0,0);
            }
        });

        likeList_container.setRefreshHeader(new MaterialHeader(getActivity()));
        likeList_container.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                //TODO: Search page refresh to do
                refreshlayout.finishRefresh(updataList());
            }
        });
        moreWindowDialog.setOnChangeFavStatusListener(new OnChangeFavStatusListener() {
            @Override
            public void OnChange() {
                updataList();
            }
        });



        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updataList();
    }

    private void showContainer(int tag){
        if (tag == NO_FAV_MUSIC){
            likeList_list.setVisibility(View.INVISIBLE);
            no_fav.setVisibility(View.VISIBLE);
        }else if (tag == SHOW_MUSIC_LIST){
            likeList_list.setVisibility(View.VISIBLE);
            no_fav.setVisibility(View.INVISIBLE);
        }else {
            likeList_list.setVisibility(View.INVISIBLE);
            no_fav.setVisibility(View.VISIBLE);
        }
    }

    private Boolean updataList(){
        like_list = sqlUtils.getFavMus(getActivity().getApplicationContext());
        if (like_list != null && like_list.size() != 0){
            showContainer(SHOW_MUSIC_LIST);
            if (likeList_list.getAdapter() == null){
                likeList_list.setAdapter(new listPageAdapter());
            }else {
                ((listPageAdapter) likeList_list.getAdapter()).notifyDataSetChanged();
//                likeList_list.setAdapter(new listPageAdapter());
            }
            return true;
        }else {
            showContainer(NO_FAV_MUSIC);
            return true;
        }
    }

    class listPageAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return like_list.size();
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
            View view = View.inflate(getContext(), R.layout.list_ltem_layout, null);
            final Music music = like_list.get(position);
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
                    moreWindowDialog.show(getActivity().getSupportFragmentManager(), music.getId(), music);
                }
            });
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((TextView) v.findViewById(R.id.index_list_item_music_name)).setTextColor(getActivity().getResources().getColor(R.color.textHint));
                    globalApplication.setOnMusicChange(new OnMusicChange() {
                        @Override
                        public void OnComplete() {
                            ((MainActivity) getActivity()).setMainPlayerVisiable(MainActivity.SHOW_MIN_PLAY_FLAG);
                            ((BaseAdapter) likeList_list.getAdapter()).notifyDataSetChanged();
                        }

                        @Override
                        public void OnFail() {
                            ((BaseAdapter) likeList_list.getAdapter()).notifyDataSetChanged();
                        }
                    });
                    globalApplication.setCurrentMusic(music);
                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    moreWindowDialog.show(getActivity().getSupportFragmentManager(), music.getId(), music);
                    return true;
                }
            });
            FrameLayout before_name = view.findViewById(R.id.before_name);
            if (globalApplication.getCurrentMusic() != null && globalApplication.getCurrentMusic().getId().equals(music.getId())){
                ImageView imageView = new ImageView(getContext());
                imageView.setImageResource(R.drawable.ic_playing);
                before_name.addView(imageView);
            }
            return view;
        }
    }
}
