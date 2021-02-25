package com.sm.music.MusicUtils;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sm.music.Bean.Music;
import com.sm.music.Listener.OnChangeFavStatusListener;
import com.sm.music.R;
import com.sm.music.SQL.SQLUtils;

import static android.content.Context.CLIPBOARD_SERVICE;

public class MoreWindowDialog extends DialogFragment {

    private MusicDownload musicDownlaod;

    private View view;

    private Music music;

    boolean isMoreShow = false;

    private SQLUtils sqlUtils = null;

    OnChangeFavStatusListener onChangeFavStatusListener = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        sqlUtils = new SQLUtils();

        view = requireActivity().getLayoutInflater().inflate(R.layout.windows_more_fragment, null);
//        musicDownlaod = new MusicDownload(context, root);

        LinearLayout more_top = view.findViewById(R.id.more_top);
        TextView indexMore_name = view.findViewById(R.id.indexMore_name);
        TextView indexMore_singer = view.findViewById(R.id.indexMore_singer);
        TextView indexMore_album = view.findViewById(R.id.indexMore_album);
        final CheckBox forLike = view.findViewById(R.id.forLike);
        TextView ToDown = view.findViewById(R.id.ToDown);
        TextView ToShare = view.findViewById(R.id.ToShare);

        //TODO: to init like button
        if (sqlUtils.getFavMus(requireActivity().getApplicationContext(), music.getId() + music.getSource())){
            forLike.setText(R.string.remove_like);
            forLike.setChecked(true);
        }else {
            forLike.setText(R.string.add_like);
            forLike.setChecked(false);
//            if (list != null){
//                ((BaseAdapter) list.getAdapter()).notifyDataSetChanged();
//            }
        }


        indexMore_name.setText(music.getName());
//        more.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                root.removeView(more);
//                isMoreShow = false;
//            }
//        });
//        more_container.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
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
                        requireActivity().getResources().getString(R.string.introduce);
                ((ClipboardManager) requireActivity().getSystemService(CLIPBOARD_SERVICE))
                        .setPrimaryClip(ClipData.newPlainText("StickPoint Music Info", temp));
                Toast.makeText(requireActivity(), R.string.copy_introduce, Toast.LENGTH_LONG).show();
                return true;
            }
        });


        forLike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //TODO: to add & remove favoriate music
                if (isChecked){
                    forLike.setText(R.string.remove_like);
                    if (sqlUtils.setFavMus(requireActivity().getApplicationContext(), music))
                        Toast.makeText(requireActivity(), R.string.fav_failed, Toast.LENGTH_LONG);
                }else {
                    forLike.setText(R.string.add_like);
                    if (!sqlUtils.delFavMus(requireActivity().getApplicationContext(), music.getId() + music.getSource()))
                        Toast.makeText(requireActivity(), R.string.fav_del_failed, Toast.LENGTH_LONG);
                    if (onChangeFavStatusListener != null){
                        onChangeFavStatusListener.OnChange();
                    }
                }
            }
        });
//        ToDown.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //TODO: to download music
//                removeMore();
//                musicDownlaod.downloadFile(music);
//            }
//        });
        ToShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: to share music
            }
        });
        view.startAnimation(AnimationUtils.loadAnimation(requireActivity(), R.anim.show_more));
//        if (!isMoreShow){
//            root.addView(view);
//        }
//        isMoreShow = true;


        builder.setView(view);
        return builder.create();
    }

    public void show(@NonNull FragmentManager manager, @Nullable String tag, Music music) {
        this.music = music;
        super.show(manager, tag);
    }
}
