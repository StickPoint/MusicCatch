package com.sm.music;

import com.alibaba.fastjson.JSONObject;
import com.sm.music.Music;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author fntp
 * 
 */
public class GetMusic {

	public static final int RESPOND_SUCCESS = 200;

	public static final int RESPOND_TIMEOUT = 500;

	public static final int RESPOND_EMPTY = 404;

	public static final int REQUEST_TYPE_URL = 1;

	public static final int REQUEST_TYPE_PIC = 2;

	public static final int REQUEST_TYPE_LYRIC = 3;

	public static final int MUSIC_SOURCE_NETEASE = 0;

	public static final int MUSIC_SOURCE_TENCENT = 1;

	public static final int MUSIC_SOURCE_KUGOU = 2;

	public static final String REQUEST_URL_URL = "https://api.zhuolin.wang/api.php?types=url";

	public static final String REQUEST_URL_SEARCH = "https://api.zhuolin.wang/api.php?types=search";

	public static final String REQUEST_URL_PIC = "https://api.zhuolin.wang/api.php?types=pic";

	public static final String REQUEST_URL_LYRIC = "https://api.zhuolin.wang/api.php?types=lyric";

	private Connection connection;

	private Map<String,String> headerMap;

	private List<Music> musicList ;
	
	private List<String> requestMusicID;
	
	private List<String> requestMusicSource;
	
	private List<String> requestMusicURL_URL;

	private List<String> requestMusicURL_Search;

	private List<String> requestMusicURL_Pic;

	private List<String> requestMusicURL_Lyric;

	public GetMusic(){}

	/**
	 *
	 * @param host
	 * @return
	 */
	public Map<String,String> SetheaderMap(String host) {
		headerMap = new HashMap<String,String>();
		headerMap.put("Host",host);
        return headerMap;
	}

	/**
	 *
	 * @param Url
	 * @param host
	 * @return
	 */
	public Connection GetConnection(String Url,String host) {
		return connection = Jsoup.connect(Url).data(SetheaderMap(host));
	}

	/**
	 *
	 * @param Url
	 * @return
	 */
	public Connection GetConnection(String Url) {
		return connection = Jsoup.connect(Url);
	}

	/**
	 *
	 * @param url
	 * @return
	 */
	public static String getHost(String url) {
		String cache = "";
		if(url.contains("www.")) {
			String regex1 = "www.(.*?).com";
			String host = "";
			Pattern pattern1 = Pattern.compile(regex1);
			Matcher m = pattern1.matcher(url);
			while (m.find()) {  
	            int i = 1;  
	            host+=m.group(i);
	            i++;  
	        } 
			cache= host+".com";
		}else if(url.contains("https://")){
			String regex1 = "https://(.*?).com";
			String host = "";
			Pattern pattern1 = Pattern.compile(regex1);
			Matcher m = pattern1.matcher(url);
			while (m.find()) {  
	            int i = 1;  
	            host+=m.group(i);
	            i++;  
	        } 
			cache = host+".com";
		}else if(url.contains("http://")) {
			String regex1 = "http://(.*?).com";
			String host = "";
			Pattern pattern1 = Pattern.compile(regex1);
			Matcher m = pattern1.matcher(url);
			while (m.find()) {  
	            int i = 1;  
	            host+=m.group(i);
	            i++;  
	        } 
			cache= host+".com";
		}
	return cache;
	}

	/**
	 *
	 * @param url
	 * @param host
	 * @throws Exception
	 */
	public GetMusic(String url, String host) throws Exception{
		 Document document = GetConnection(url,host).get();
		 System.out.println(document.html());
	}

	/**
	 * （1）首先你得根据方法获得返回请求的URL
	 * （2）使用下面的方法进行get请求，拿到最后服务器返回的json字符串
	 * @param url
	 * @throws Exception
	 */
	public String  getJSON(String url) throws Exception{
		Document document = GetConnection(url).ignoreContentType(true).get();
		return document.body().html();
	}

	/**
	 * 返回搜索歌曲的请求连接，
	 * 只需要传入两个参数
	 * @param musicName
	 * @param musicSource
	 * 	 * 根据index来筛选music的来源
	 * 	 *  0 netease
	 * 	 *  1 tencent
	 * 	 *  2 kugou
	 * 	 *  default kugou
	 * @return
	 */
	public String getSearchRequsetURL(String musicName, int musicSource){
		requestMusicURL_Search = new ArrayList<>();
		if(musicName!=null){
			requestMusicURL_Search.add(REQUEST_URL_SEARCH+"&name="+musicName+"&source="+chooseMusicSource(musicSource));
		}
		return requestMusicURL_Search.get(0);
	}

	/**
	 * 返回搜索歌词的请求连接，
	 * 只需要传入两个参数
	 * @param musicIDList
	 * @param musicSource
	 * 	 * 根据index来筛选music的来源
	 * 	 *  0 netease
	 * 	 *  1 tencent
	 * 	 *  2 kugou
	 * 	 *  default kugou
	 * @return
	 */
	public List<String> getLyricRequestURL(List<String> musicIDList,int musicSource){
		requestMusicURL_Lyric = new ArrayList<>();
		if(musicIDList!=null){
			for (String musicID : musicIDList) {
				requestMusicURL_Lyric.add(REQUEST_URL_LYRIC+"&id="+musicID+"&source="+chooseMusicSource(musicSource));
			}
		}
		return requestMusicURL_Lyric;
	}
	/**
	 * 传入index（int）类型的参数，
	 * 返回相应的请求类型  type
	 * @param index
	 * @return
	 */
	public String chooseRequestType(int index){
		switch (index){
			case 0:
				return "search";
			case 1:
				return "url";
			case 2:
				return "pic";
			case 3:
				return "lyric";
			default:
				return "search";
		}
	}
	/**
	 * 根据index来筛选music的来源
	 *  0 netease
	 *  1 tencent
	 *  2 kugou
	 *  default kugou
	 * @param index
	 * @return
	 */
	public String chooseMusicSource(int index){
		switch (index){
			case 0:
				return  "netease";
			case 1:
				return  "tencent";
			case 2:
				return  "kugou";
			default:
				return  "kugou";
		}
	}

	/**
	 * 接受来自服务器的返回的json字符串
	 * 将字符串转为对应的Music对象
	 * @param json
	 * @return
	 */
	public List<Music> getMusicList(String json){
		musicList = new ArrayList<>();
		if(json!=null){
			musicList= JSONObject.parseArray(json,Music.class);
		}else{
			throw new RuntimeException("json is null");
		}
		return  musicList;
	}

	/**
	 * 获得请求播放地址的url时候，必要的参数之一：
	 * 获得播放地址必要有两个参数：
	 * 		第一个参数：音乐ID musicId
	 * 		第二个参数：播放源 source	
	 * 	是一个必要的参数，返回的是一个String类型的List集合	
	 * @param musicList
	 * @return
	 */
	public List<String> requestMusicID(List<Music> musicList){
		requestMusicID = new ArrayList<>();
		if (musicList!=null){
			for (Music music : musicList) {
				requestMusicID.add(music.getId());
			}
		}else{
			throw new RuntimeException("musicList is null");
		}
		return  requestMusicID;
	}

	/**
	 * 传入的参数是一个musiclist 取出请求播放地址的第二个参数：source
	 * 是一个必要的参数，返回的是一个String类型的List集合
	 * @param musicList
	 * @return
	 */
	public List<String> requestMusicSource(List<Music> musicList){
		requestMusicSource = new ArrayList<>();
		if(musicList!=null){
			for (Music music : musicList) {
				requestMusicSource.add(music.getSource());
			}
		}else{
			throw new RuntimeException("musicList is null");
		}
		return  requestMusicSource;
	}
	
	public List<String> requestMusicURL(List<String> requestMusicID,List<String> requestMusicSource){
		requestMusicURL_URL = new ArrayList<>();
		if(requestMusicID.size()!=0){
			for (int index = 0; index < requestMusicID.size(); index++) {
				requestMusicURL_URL.add(requestMusicID.get(index)+"&"+requestMusicSource.get(index));
			}
		}else{
			throw new RuntimeException("requestMusicList is null");
		}
		return requestMusicURL_URL;
	}
	
	public static void main(String[] args) throws Exception {
		GetMusic getMusic =new GetMusic();
		String json = getMusic.getJSON("https://api.zhuolin.wang/api.php?types=search&count=20&source=tencent&pages=1&name=%E6%88%91%E4%B8%8D%E5%AF%B9");
		List<Music> musicList = getMusic.getMusicList(json);
		System.out.println(musicList);
		//获得播放的requestMusicID的List
		List<String> requestMusicIDList = getMusic.requestMusicID(musicList);
		System.out.println(requestMusicIDList);
		//获得播放的requestMusicSource的List
		List<String> requestMusicSourceList = getMusic.requestMusicSource(musicList);
		System.out.println(requestMusicSourceList);
		//获得播放的requestMusicURL
		List<String> requestMusicURLList = getMusic.requestMusicURL(requestMusicIDList, requestMusicSourceList);
		System.out.println(requestMusicURLList);
		System.out.println(getMusic.getSearchRequsetURL("我不对", 0));
	}

}
