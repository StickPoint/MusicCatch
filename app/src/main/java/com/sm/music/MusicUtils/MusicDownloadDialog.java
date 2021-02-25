package com.sm.music.MusicUtils;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sm.music.Bean.Music;
import com.sm.music.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class MusicDownloadDialog extends DialogFragment {


    private static final int UPDATA_PROGRESS = 202;

    private static final int DOWNLOAD_SUCCESS = 233;

    private static final int DOWNLOAD_FAILED = 212;

    private static final int TOTAL_LENGTH = 686;

    private static final int ALREADY_DOWNLOAD_LENGTH = 697;

    private final static String SAVE_PATH = "/sdcard/StickPointDownload/";

    private String fileName = "";

    private Music music = null;

    private View downloadPage;

    private GetMusic conn;

    private Dialog dialog = null;

    private Thread downloadThread = null;

    private Call call = null;

    private Boolean canDownload = false;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        conn = new GetMusic();
        downloadPage = View.inflate(getContext(), R.layout.fragment_music_download_dialog, null);
        ((TextView) downloadPage.findViewById(R.id.download_musicName)).setText(this.music.getName());
        setProgressBar(0);
        downloadPage.findViewById(R.id.download_container).startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.show_more));

        downloadPage.startAnimation(AnimationUtils.loadAnimation(requireActivity(), R.anim.show_more));
        this.dialog = new AlertDialog.Builder(getActivity()).setView(downloadPage).create();
        this.dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        downloadFile(music);
        return this.dialog;
    }

    private void setProgressBar(int progress){
        ProgressBar download_progress = downloadPage.findViewById(R.id.download_progress);
        download_progress.setMax(100);
        download_progress.setProgress(progress);
    }

    private void setDownloadSize(int progress, int total){
        TextView download_size = downloadPage.findViewById(R.id.download_size);
        download_size.setText(String.valueOf(progress / 10.0) + "M/" + String.valueOf(total / 10.0) + "M");
    }

    public void show(@NonNull FragmentManager manager, @Nullable String tag, Music music){
        this.music = music;
        super.show(manager, tag);
    }

    public void downloadFile(final Music music){
        final int[] total_size = {0};
//        final String SAVE_PATH = Environment.getExternalStorageDirectory().getPath() + "/Download/";
        final String SAVE_PATH = "/sdcard/StickPointDownload/";
        String artist = "";
        for (int i = 0; i < music.getArtist().length; i++) {
            if (i == 0) {
                artist += music.getArtist()[i];
            } else {
                artist += "/" + music.getArtist()[i];
            }
        }
        final String finalArtist = artist;
        fileName = music.getName() + "-" + finalArtist + ".mp3";
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (call != null && !call.isCanceled()){
                    if (msg.what == DOWNLOAD_SUCCESS){
                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.download_success) + SAVE_PATH + fileName, Toast.LENGTH_LONG).show();
                        dismiss();
                    }else if (msg.what == TOTAL_LENGTH){
                        setDownloadSize(0, msg.arg1);
                        total_size[0] = msg.arg1;
                    } else if (msg.what == UPDATA_PROGRESS){
                        setProgressBar(msg.arg1);
                        setDownloadSize(msg.arg2, total_size[0]);
                    }else {
                        Toast.makeText(getContext(), R.string.download_failed, Toast.LENGTH_LONG).show();
                        dismiss();
                    }
                }
                if (msg.what == DOWNLOAD_FAILED){
                    Toast.makeText(getContext(), R.string.download_failed, Toast.LENGTH_LONG).show();
                    dismiss();
                }

            }
        };
        final String[] url = {null};
//        show(music);
        downloadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i("MusicDownload:request", music.getId() + music.getSource());
                    url[0] = conn.getMusicPlayURL(music.getId(), music.getSource());
                    Log.i("MusicDownload:", url[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = Message.obtain();
                    msg.what = DOWNLOAD_FAILED;
                    handler.sendMessage(msg);
                }
                if (url[0] != null && !url[0].equals("")){
                    Log.i("MusicDownload:url:", "not empty");
                    OkHttpClient okHttpClient = new OkHttpClient();
                    Request request = new Request.Builder().url(url[0]).build();
                    call = okHttpClient.newCall(request);
                    canDownload = true;
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Message msg = Message.obtain();
                            msg.what = DOWNLOAD_FAILED;
                            handler.sendMessage(msg);
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            InputStream httpRead = null;
                            FileOutputStream musicFileInput = null;
                            try {
                                Log.i("MusicDownloadstartRead:", "response.body().byteStream()");
                                httpRead = response.body().byteStream();
                                Log.i("MusicDownload:canRead:", "true");
                                long total = response.body().contentLength();

                                Message msg_total = Message.obtain();
                                msg_total.what = TOTAL_LENGTH;
                                msg_total.arg1 = (int) (total / 1024.0f / 1024.0f * 10.0f);
                                handler.sendMessage(msg_total);

                                Log.i("MusicDownload:length:", String.valueOf(total));
                                File file = new File(SAVE_PATH, fileName);
                                Log.i("MusicDownload:filename:", fileName);

                                if (!file.getParentFile().exists()){
                                    file.getParentFile().mkdirs();
                                    Log.i("MusicDownload:created:", file.getParentFile().toString());
                                }
                                if (!file.exists()){
                                    file.createNewFile();
                                    Log.i("MusicDownload:createf:", SAVE_PATH + fileName);
                                }
                                musicFileInput = new FileOutputStream(file,false);
                                long alreadyDownload = 0;
                                byte[] buf = new byte[2048];
                                int len = 0;
                                Log.i("MusicDownload:", "start write");
                                while ((len = httpRead.read(buf)) != -1) {
                                    musicFileInput.write(buf, 0, len);
                                    alreadyDownload += len;
                                    int progress = (int) (alreadyDownload * 1.0f / total * 100);
                                    Message msg = Message.obtain();
                                    msg.what = UPDATA_PROGRESS;
                                    msg.arg1 = progress;
                                    msg.arg2 = (int) (alreadyDownload / 1024.0f / 1024.0f * 10.0f);
                                    handler.sendMessage(msg);
                                }
                                musicFileInput.flush();
                                Log.i("MusicDownload:", "write finish" +
                                        "");
                                Message msg = Message.obtain();
                                msg.what = DOWNLOAD_SUCCESS;
                                handler.sendMessage(msg);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e("MusicDownloadReadError:", e.getMessage());
                                Message msg = Message.obtain();
                                msg.what = DOWNLOAD_FAILED;
                                handler.sendMessage(msg);
                            } finally {
                                try {
                                    if (httpRead != null)
                                        httpRead.close();
                                } catch (IOException e) {e.printStackTrace();}
                                try {
                                    if (musicFileInput != null)
                                        musicFileInput.close();
                                } catch (IOException e) {e.printStackTrace();}
                            }
                        }
                    });
                }else {
                    canDownload = false;
                    Log.e("MusicDownload:url:", "null or empty");
                    Message msg = Message.obtain();
                    msg.what = DOWNLOAD_FAILED;
                    handler.sendMessage(msg);
                }

            }
        });
        downloadThread.start();

    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        if (canDownload && !call.isCanceled()){
            call.cancel();
        }
        Toast.makeText(getActivity(), R.string.download_cancel, Toast.LENGTH_SHORT).show();
        File file = new File(SAVE_PATH, fileName);
        if (file.exists()){
            file.delete();
            Log.i("MusicDownload:delf:", SAVE_PATH + fileName);
        }
        super.onCancel(dialog);
    }

}
