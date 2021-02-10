package com.sm.music.MusicUtils;
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
public class HttpConn {
    private String uri;
    private String accept;
    private InputStream input;
    private BufferedReader reader = null;
    private HttpURLConnection con = null;
    private String referer = null;
    public String getReferer() {
        return referer;
    }
    public HttpConn setReferer(String referer) {
        this.referer = referer;
        return this;
    }
    public HttpConn setUri(String uri) {
        this.uri = uri;
        return this;
    }
    public HttpConn setAccept(String accept) {
        this.accept = accept;
        return this;
    }
    private void send(String method){
        try {
            URL url = new URL(this.uri);
            this.con = (HttpURLConnection) url.openConnection();
            this.con.setRequestMethod(method);
            this.con.setRequestProperty("Accept",this.accept);
            this.con.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.61 Safari/537.36");
            if (this.referer != null)
                this.con.setRequestProperty("referer",this.referer);
            this.con.setConnectTimeout(5000);
            InputStream input = con.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len;
            byte[] buffer = new byte[1024];
            while ((len = input.read(buffer)) > -1 ) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            this.input = new ByteArrayInputStream(baos.toByteArray());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (this.con != null) {
                this.con.disconnect();
            }
        }
    }
    public HttpConn get(){
        this.send("GET");
        return this;
    }
    public HttpConn post(){
        this.send("POST");
        return this;
    }
    public InputStream getInputStream(){
        if (this.con != null) {
            this.con.disconnect();
        }
        return this.input;
    }
    @Override
    public String toString(){
        String result = "";
        try {
            this.reader = new BufferedReader(new InputStreamReader(this.input));
            String line;
            while ((line = this.reader.readLine()) != null) {
                result += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (reader != null) {
                try {
                    this.reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return result;
        }
    }
}
