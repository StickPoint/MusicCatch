package com.sm.music.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sm.music.Bean.Music;
import com.sm.music.GlobalApplication;
import com.sm.music.MusicUtils.GetMusic;
import com.sm.music.R;
import com.sm.music.UIUtils.MoreWindows;
import com.sm.music.UIUtils.Util;
import com.sm.music.Fragment.search_pager;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {


    private static final int ACTIVITY_TAG = 547;

    final static private int TYPES[] = {GetMusic.MUSIC_SOURCE_KUGOU, GetMusic.MUSIC_SOURCE_NETEASE, GetMusic.MUSIC_SOURCE_TENCENT};


    private GlobalApplication globalApplication = null;

    private RadioGroup source_group = null;

    private RadioButton kugou = null;
    private RadioButton netease = null;
    private RadioButton tencent = null;

    private ConstraintLayout searchBar = null;
    private FrameLayout searchPlayer = null;

    private ImageView search_cancel = null;

    private TextView search_button = null;
    private EditText search = null;

    private int currentType = GetMusic.MUSIC_SOURCE_KUGOU;
    private String search_text = null;

    private ViewPager search_wapper = null;
    private ArrayList<search_pager> search_pager = new ArrayList<>();
    private FragmentManager search_pager_manager = null;

    MoreWindows moreWindows = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.setActivityBarAlpha(this, false);
        setContentView(R.layout.activity_search);
        final int statusBarHeight = Util.getStatusBarHeight(this);
        final int NavigationBarHeight = Util.getNavigationBarHeight(SearchActivity.this);
        final Boolean HasNavigationBar = !Util.checkDeviceHasNavigationBar(this);
        globalApplication = (GlobalApplication) getApplication();
        source_group = findViewById(R.id.source_group);
        kugou = findViewById(R.id.kugou);
        netease = findViewById(R.id.netease);
        tencent = findViewById(R.id.tencent);
        search_wapper = findViewById(R.id.search_wapper);
        searchBar = findViewById(R.id.searchBar);
        searchPlayer = findViewById(R.id.searchPlayer);
        search_button = findViewById(R.id.search_button);
        search_cancel = findViewById(R.id.search_cancel);
        search = findViewById(R.id.search);
        search.requestFocus();
        moreWindows = new MoreWindows(SearchActivity.this,(FrameLayout)findViewById(R.id.searchPage));
        for (int i : TYPES){
            search_pager.add(new search_pager(i));
        }
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

        search_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchActivity.this.finish();
                overridePendingTransition(0,R.anim.transfrom_buttom_out);
            }
        });

        search_pager_manager = getSupportFragmentManager();

        searchPlayer.addView(globalApplication.createMinMusicPlayer(SearchActivity.this, ACTIVITY_TAG));



        search_wapper.setAdapter(new FragmentPagerAdapter(search_pager_manager) {
            @Override
            public int getCount() {
                return search_pager.size();
            }
            @Override
            public Fragment getItem(int position) {
                return search_pager.get(position);
            }
        });


        search_wapper.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                search_pager.get(search_wapper.getCurrentItem()).search_music_list_data(search_text);
                switch (position){
                    case 0:
                        Util.setActivityBarAlpha(SearchActivity.this, false);
                        kugou.setChecked(true);
                        break;
                    case 1:
                        Util.setActivityBarAlpha(SearchActivity.this, false);
                        netease.setChecked(true);
                        break;
                    case 2:
                        Util.setActivityBarAlpha(SearchActivity.this, false);
                        tencent.setChecked(true);
                        break;
                    default:
                        Util.setActivityBarAlpha(SearchActivity.this, false);
                        kugou.setChecked(true);
                        break;
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchActivity.this.finish();
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
                            String temp = String.valueOf(search.getText());
                            if (temp != null && !temp.replace(" ","").equals("")){
                                search_text = temp;
                                search_wapper.setVisibility(View.VISIBLE);
                                search_pager.get(search_wapper.getCurrentItem()).search_music_list_data(search_text);
                            }else {
                                Toast.makeText(SearchActivity.this, R.string.no_search_input, Toast.LENGTH_SHORT).show();;
                            }
                        }
                    });
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        currentType = getCurrentType(source_group.getCheckedRadioButtonId());
        source_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                currentType = getCurrentType(checkedId);
                if (search_text != null && !search_text.equals("")){
                    switch (currentType){
                        case GetMusic.MUSIC_SOURCE_KUGOU:
                            search_wapper.setCurrentItem(0,true);
                            break;
                        case GetMusic.MUSIC_SOURCE_NETEASE:
                            search_wapper.setCurrentItem(1,true);
                            break;
                        case GetMusic.MUSIC_SOURCE_TENCENT:
                            search_wapper.setCurrentItem(2,true);
                            break;
                        default:
                            search_wapper.setCurrentItem(0,true);
                            break;
                    }
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        globalApplication.destroyMinMusicPlayer(ACTIVITY_TAG);
    }

    public String getSearchText(){
        return search_text;
    }

    public void onBackPressed() {
        if (moreWindows.isMoreShow()){
            moreWindows.removeMore();
        }else {
            super.onBackPressed();
        }

    }

    private int getCurrentType(int currentId){
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

//    private void showContainer(int container){
//        switch (container){
//            case SHOW_SEARCH_MUSIC_LIST:
//                ((View) searchList_list).setVisibility(View.VISIBLE);
//                searchLoading.setVisibility(View.INVISIBLE);
//                break;
//            case SHOW_SEARCH_LOADING:
//                ((View) searchList_list).setVisibility(View.INVISIBLE);
//                searchLoading.setVisibility(View.VISIBLE);
//                break;
//            default:
//                ((View) searchList_list).setVisibility(View.VISIBLE);
//                searchLoading.setVisibility(View.INVISIBLE);
//                break;
//        }
//    }
    public void showMore(Music music){
        moreWindows.show(music);
    }



}
