package com.sm.music.MusicUtils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sm.music.Activity.SearchActivity;
import com.sm.music.Bean.Music;
import com.sm.music.Bean.RecMusic;
import com.sm.music.MusicUtils.MusicDownload;
import com.sm.music.R;
import com.sm.music.SQL.SQLUtils;

import static android.content.Context.CLIPBOARD_SERVICE;

public class MoreWindows {

    private MusicDownload musicDownlaod;

    private Context context;

    private FrameLayout root;

    private View more;

    private Music music;

    boolean isMoreShow = false;

    private SQLUtils sqlUtils = null;

    public MoreWindows(final Context context, final FrameLayout root) {
        this.context = context;
        this.root = root;
        musicDownlaod = new MusicDownload(context, root);
        sqlUtils = new SQLUtils();
        more = View.inflate(context, R.layout.windows_more, null);
    }

    public void show(final Music music){
        this.music = music;

        LinearLayout more_container =  more.findViewById(R.id.more_container);
        LinearLayout more_top = more.findViewById(R.id.more_top);
        TextView indexMore_name = more.findViewById(R.id.indexMore_name);
        TextView indexMore_singer = more.findViewById(R.id.indexMore_singer);
        TextView indexMore_album = more.findViewById(R.id.indexMore_album);
        CheckBox forLike = more.findViewById(R.id.forLike);
        TextView ToDown = more.findViewById(R.id.ToDown);
        TextView ToShare = more.findViewById(R.id.ToShare);

        //TODO: to init like button

        indexMore_name.setText(music.getName());
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                root.removeView(more);
                isMoreShow = false;
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
                        context.getResources().getString(R.string.introduce);
                ((ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE))
                        .setPrimaryClip(ClipData.newPlainText("StickPoint Music Info", temp));
                Toast.makeText(context, R.string.copy_introduce, Toast.LENGTH_LONG).show();
                return true;
            }
        });


        forLike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //TODO: to add & remove favoriate music
                if (isChecked){
                    if (sqlUtils.setFavMus(context, music))
                        Toast.makeText(context, R.string.fav_failed, Toast.LENGTH_LONG);
                }else {
                    if (!sqlUtils.delFavMus(context, music.getId()))
                        Toast.makeText(context, R.string.fav_del_failed, Toast.LENGTH_LONG);
                }
            }
        });
        ToDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: to download music
                removeMore();
                musicDownlaod.downloadFile(music);
            }
        });
        ToShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: to share music
            }
        });
        more_container.startAnimation(AnimationUtils.loadAnimation(context, R.anim.show_more));
        root.addView(more);
        isMoreShow = true;
    }

    public void removeMore(){

        LinearLayout more_container =  more.findViewById(R.id.more_container);
        Animation a = AnimationUtils.loadAnimation(context, R.anim.hide_more);
//        a.setFillAfter(true);
//        a.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                root.removeView(more);
//                isMoreShow = false;
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
        more_container.startAnimation(a);
        root.removeView(more);
        isMoreShow = false;
    }

    public boolean isMoreShow() {
        return isMoreShow;
    }
}
