package com.sm.music.MusicUtils;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

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

public class MusicDownload {

    private static final int UPDATA_PROGRESS = 202;

    private static final int DOWNLOAD_SUCCESS = 233;

    private static final int DOWNLOAD_FAILED = 212;

    private static final int TOTAL_LENGTH = 686;

    private static final int ALREADY_DOWNLOAD_LENGTH = 697;
    
    private FrameLayout root;

    private View downloadPage;

    private Context context;

    private GetMusic conn;

    public MusicDownload(Context context, FrameLayout view){
        this.root = view;
        this.context = context;
        conn = new GetMusic();
        downloadPage = View.inflate(context, R.layout.windows_downlaod, null);
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

    private void show(Music music){
        ((TextView) downloadPage.findViewById(R.id.download_musicName)).setText(music.getName());
        setProgressBar(0);
        downloadPage.findViewById(R.id.download_container).startAnimation(AnimationUtils.loadAnimation(context, R.anim.show_more));
        root.addView(downloadPage);
    }

    private void hide(){
        root.removeView(downloadPage);
    }

    public void downloadFile(final Music music){
        final int[] total_size = {0};
//        final String savePath = Environment.getExternalStorageDirectory().getPath() + "/Download/";
        final String savePath = "/sdcard/Download/";
        String artist = "";
        for (int i = 0; i < music.getArtist().length; i++) {
            if (i == 0) {
                artist += music.getArtist()[i];
            } else {
                artist += "/" + music.getArtist()[i];
            }
        }
        final String finalArtist = artist;
        final String fileName = music.getName() + "-" + finalArtist + ".mp3";
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.what == DOWNLOAD_SUCCESS){
                    Toast.makeText(context, context.getResources().getString(R.string.download_success) + savePath + fileName, Toast.LENGTH_LONG).show();
                    hide();
                }else if (msg.what == TOTAL_LENGTH){
                    setDownloadSize(0, msg.arg1);
                    total_size[0] = msg.arg1;
                } else if (msg.what == UPDATA_PROGRESS){
                    setProgressBar(msg.arg1);
                    setDownloadSize(msg.arg2, total_size[0]);
                }else {
                    Toast.makeText(context, R.string.download_failed, Toast.LENGTH_LONG).show();
                    hide();
                }
                
            }
        };
        final String[] url = {null};
        show(music);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
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
                    okHttpClient.newCall(request).enqueue(new Callback() {
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
                                File file = new File(savePath, fileName);
                                Log.i("MusicDownload:filename:", fileName);
                                file.createNewFile();
                                Log.i("MusicDownload:createf:", savePath + fileName);
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
                    Log.e("MusicDownload:url:", "null or empty");
                    Message msg = Message.obtain();
                    msg.what = DOWNLOAD_FAILED;
                    handler.sendMessage(msg);
                }

            }
        }).start();
    }

}
