package com.sm.music.Fragment;


import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.sm.music.Activity.ListActivity;
import com.sm.music.MusicUtils.GetMusic;
import com.sm.music.GlobalApplication;
import com.sm.music.R;
import com.sm.music.Activity.SearchActivity;
import com.sm.music.UIUtils.Util;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.view.View.inflate;
import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class indexFragment extends Fragment {

    final static private int NETWORK_REFRESH_TAG = 202;

    final static private int NETWORK_ONLOAD_TAG = 201;

    private static JSONArray musicIdList = null;
    private static ArrayList musicListView = null;

    static {
        musicIdList = new JSONArray();
        try {
            musicIdList.put(0,new JSONObject().put("layout_resource",R.mipmap.rege).put("name","热歌榜").put("id",3778678L));
            musicIdList.put(1,new JSONObject().put("layout_resource",R.mipmap.xinge).put("name","新歌榜").put("id",3779629L));
            musicIdList.put(2,new JSONObject().put("layout_resource",R.mipmap.biaosheng).put("name","飙升榜").put("id",19723756L));
            musicIdList.put(3,new JSONObject().put("layout_resource",R.mipmap.gufeng).put("name","古风榜").put("id",5059642708L));
            musicIdList.put(4,new JSONObject().put("layout_resource",R.mipmap.yaogun).put("name","摇滚榜").put("id",5059633707L));
            musicIdList.put(5,new JSONObject().put("layout_resource",R.mipmap.minyao).put("name","民谣榜").put("id",5059661515L));
            musicIdList.put(6,new JSONObject().put("layout_resource",R.mipmap.shuochang).put("name","说唱榜").put("id",991319590L));
            musicIdList.put(7,new JSONObject().put("layout_resource",R.mipmap.jinqu).put("name","华语金曲榜").put("id",4395559L));
            musicIdList.put(8,new JSONObject().put("layout_resource",R.mipmap.neidi).put("name","TOP（内地榜）").put("id",64016L));
            musicIdList.put(9,new JSONObject().put("layout_resource",R.mipmap.gangtai).put("name","TOP（港台榜）").put("id",112504L));
            musicIdList.put(10,new JSONObject().put("layout_resource",R.mipmap.yuanchuang).put("name","网易原创歌曲榜").put("id",2884035L));
            musicIdList.put(11,new JSONObject().put("layout_resource",R.mipmap.oumeire).put("name","欧美热歌榜").put("id",2809513713L));
            musicIdList.put(12,new JSONObject().put("layout_resource",R.mipmap.oumeixin).put("name","欧美新歌榜").put("id",2809577409L));
            musicIdList.put(13,new JSONObject().put("layout_resource",R.mipmap.riyu).put("name","日语榜").put("id",5059644681L));
            musicIdList.put(14,new JSONObject().put("layout_resource",R.mipmap.hanyu).put("name","韩语榜").put("id",745956260L));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private GlobalApplication globalApplication = null;

    private ConstraintLayout top = null;


    private GetMusic conn = null;

    private TextView searchPageIn = null;

    public indexFragment() {
        conn = new GetMusic();
    }

    private GridView index_list = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        musicList = new GetMusic().getSearchRequsetURL();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_index, container, false);
        final int statusBarHeight = Util.getStatusBarHeight(getActivity());
        top = view.findViewById(R.id.indexBar);
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

        searchPageIn = view.findViewById(R.id.searchPageIn);
        searchPageIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SearchActivity.class));
                getActivity().overridePendingTransition(R.anim.transfrom_buttom_in,R.anim.no_transfrom);
            }
        });

        index_list = view.findViewById(R.id.index_list);
        index_list.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return musicIdList.length();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                try {
                    return musicIdList.getJSONObject(position).getLong("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                    return 0;
                }
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = inflate(getActivity(),R.layout.index_list_item, null);
                ImageView item_pic = (ImageView) view.findViewById(R.id.item_pic);
                TextView item_name = (TextView) view.findViewById(R.id.item_text);
                try {
                    int ImgRes = musicIdList.getJSONObject(position).getInt("layout_resource");
                    Glide.with(view)
                        .load(getResources().getDrawable(ImgRes, null))
                        .apply(bitmapTransform(new RoundedCorners(40)))
                        .into(item_pic);
                    item_name.setText(musicIdList.getJSONObject(position).getString("name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return view;
            }
        });
        index_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ListActivity.class);
                try {
                    intent.putExtra("name", musicIdList.getJSONObject(position).getString("name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                intent.putExtra("id", id);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.transfrom_buttom_in,R.anim.no_transfrom);

            }
        });

        return view;
    }


    class ViewHolder {
        ImageView item_pic;
        TextView item_name;
    }

}
