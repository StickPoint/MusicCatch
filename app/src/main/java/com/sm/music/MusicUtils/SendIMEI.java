package com.sm.music.MusicUtils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class SendIMEI {


    private static final String SEND_URL = "https://micronnetwork.com/ddmusic/CUsers?iemi={{iemi}}";

    public static void send(String IEMI){
        new Thread(() -> {
            HttpURLConnection con = null;
            try {
                URL url = new URL(SEND_URL.replace("{{iemi}}", IEMI));
                Log.e("Sendiemi", SEND_URL.replace("{{iemi}}", IEMI));
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Accept","*/*");
                con.setRequestProperty("User-Agent","ddmusicv1.0.0");
                con.setRequestProperty("referer","ddmusicv1.0.0");
                con.setConnectTimeout(5000);
                InputStream input = con.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int len;
                byte[] buffer = new byte[1024];
                while ((len = input.read(buffer)) > -1 ) {
                    baos.write(buffer, 0, len);
                }
                baos.flush();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (con != null) {
                    con.disconnect();
                }
            }
        }).start();
    }


}
