package com.sm.music.MusicUtils;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sm.music.Bean.Music;
import com.sm.music.Listener.OnChangeFavStatusListener;
import com.sm.music.R;
import com.sm.music.SQL.SQLUtils;

import static android.content.Context.CLIPBOARD_SERVICE;

public class MoreWindowDialog extends DialogFragment {

    private MusicDownloadDialog musicDownloadDialog;

    private View view;

    private Music music;

    private SQLUtils sqlUtils = null;

    private Dialog dialog = null;

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
        musicDownloadDialog = new MusicDownloadDialog();
        view = requireActivity().getLayoutInflater().inflate(R.layout.fragment_windows_more_dialog, null);

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
        }


        indexMore_name.setText(music.getName());
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
                }
                if (onChangeFavStatusListener != null){
                    onChangeFavStatusListener.OnChange();
                }
                dismiss();
            }
        });
        ToDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: to download music
                dismiss();
                musicDownloadDialog.show(getActivity().getSupportFragmentManager(), music.getId() + "download", music);
            }
        });
        ToShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: to share music

            }
        });
        view.startAnimation(AnimationUtils.loadAnimation(requireActivity(), R.anim.show_more));

        this.dialog = builder.setView(view).create();
        this.dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        return this.dialog;
    }

    public void show(@NonNull FragmentManager manager, @Nullable String tag, Music music) {
        this.music = music;
        super.show(manager, tag);
    }

    public void setOnChangeFavStatusListener(OnChangeFavStatusListener onChangeFavStatusListener){
        this.onChangeFavStatusListener = onChangeFavStatusListener;
    }

}
