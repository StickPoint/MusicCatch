package com.sm.music.Share.Tencent.QQ;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import com.sm.music.Bean.Music;
import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
/**
 * @author fntp  LengendYC StickPointMusic
 * 顶点音乐 v1.5.6 版本 音乐分享__qq实现分享应用，分享音乐，在线播放，文字信息等等。
 */
public class SharedByQQ implements Connect{

    private Tencent tencent;

    private Context context;

    private String logoIconUrl;

    public SharedByQQ(Context context) {
        this.context = context;
        if (tencent == null){
            //初始化赋值
            tencent = Tencent.createInstance(String.valueOf(QQ_App_Id), context);
        }
        logoIconUrl="你把顶点音乐的logo的地址放到这里";
    }

    /**
     * 调用qq分享图文消息
     */
    public void SharedByQQ_PicMess() {
        final Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        //分享的标题
        params.putString(QQShare.SHARE_TO_QQ_TITLE, "顶点音乐·让音乐更懂你");
        //分享的内容
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, "全网VIP音乐免费听！让音乐回归本质！");
        //分享的连接点击时候跳转的网络地址，默认为软件官网
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, "https://stickpoint.github.io/ddmsuic.github.io/");
        //软件的分享时候的logo
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, "你把软件的LOGO放到服务器里面去，或者放在软件资源列表里面，然后分享的时候调用这个图片的地址");
        //软件的标题
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, "顶点音乐");
        //额外的功能（我这里开启了，分享时自动打开分享到QZone的对话框。）
        params.putInt(QQShare.SHARE_TO_QQ_EXT_INT,QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE);
        /**
         *  TODO 传入三个参数
         * （1）调用者所在Activity
         * （2）KV参数对，具体取值见下表
         * （3）回调监听类（qq专用的监听类）
         */
        tencent.shareToQQ((Activity) context, params,qqShareListener);
    }

    /**
     * 分享纯图片
     * @param
     */
    public void sharedByQQ_OnlyPic(){
        Bundle params = new Bundle();
        //TODO 以下的文字请根据文字描述做相应的操作
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL,"本地图片路径，就是生成的图片海报的地址");
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, "手Q客户端顶部，替换“返回”按钮文字，如果为空，用返回代替。");
        //纯图片分享接口API类型
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare. SHARE_TO_QQ_TYPE_IMAGE);
        //分享时自动打开分享到QZone的对话框。
        params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare. SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
        tencent.shareToQQ((Activity) context, params, qqShareListener);
    }

    /**
     * 分享音乐
     * @param
     */
    public void sharedByQQ_Music(Music current_music){
        final Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_AUDIO);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, "顶点音乐·聆听你我");
        //你把当前播放的音乐的信息传进来
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY,  "我正在听："+current_music.getAlbum()+"--"+current_music.getArtist()[0]+"，一起听歌吧~");
        //点击打开后是软件的首页Github的首页
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL,  "https://stickpoint.github.io/ddmsuic.github.io/");
        //你把歌曲的专辑封面放进来，没有就拿那个顶点音乐的红色logo充当，把地址放进去
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, "把专辑封面地址放在这里");
        //你可以吧音乐的播放地址放进来，这个是必填参数
        params.putString(QQShare.SHARE_TO_QQ_AUDIO_URL, "当前音乐的播放地址");
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME,  "顶点音乐");
        params.putInt(QQShare.SHARE_TO_QQ_EXT_INT,QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
        tencent.shareToQQ((Activity) context, params, qqShareListener);
    }

    /**
     * 分享到qq空间
     */
    public void shareToQZone() {
        final Bundle params = new Bundle();
        params.putString(QQShare.SHARE_TO_QQ_TITLE, "顶点音乐·传递快乐");
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, "顶点音乐，让音乐回归本质，让音乐传递更多快乐~");
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL,"你把Logo的URL放到这里（直链）");
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL,"https://stickpoint.github.io/ddmsuic.github.io/");
        params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
        tencent.shareToQQ((Activity) context, params, qqShareListener);
    }

    /**
     * 分享结果回调事件
     */
    IUiListener qqShareListener = new IUiListener() {

        /**
         * 下面的注释代码 弹窗事件 你只需要更换 统一资源定位符号即可 R.String.XXX
         *  这里的XXX你自己创建取消的弹窗icon图标，或者用我给你准备好的
         */

        //取消事件
        @Override
        public void onCancel() {
        //弹窗显示  取消 -- 这里的id你自己创建取消的弹窗icon图标，或者用我给你准备好的

//            Toast.makeText(context, context.getString(R.string.share_cancel),Toast.LENGTH_LONG);

        }

        @Override
        public void onWarning(int i) { }

        //完成事件
        @Override
        public void onComplete(Object response) {
            // 弹窗显示  完成 -- 这里的id你自己创建取消的弹窗icon图标，或者用我给你准备好的

//            Toast.makeText(context, context.getString(R.string.share_success),Toast.LENGTH_LONG);

        }

        //错误事件
        @Override
        public void onError(UiError e) {
            //弹窗显示  错误 -- 这里的id你自己创建取消的弹窗icon图标，或者用我给你准备好的

//            Toast.makeText(context, context.getString(R.string.share_failure),Toast.LENGTH_LONG);

        }
    };

}
