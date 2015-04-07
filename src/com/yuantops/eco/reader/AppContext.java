package com.yuantops.eco.reader;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.mustafaferhan.debuglog.DebugLog;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.yuantops.eco.reader.bean.Issue;
import com.yuantops.eco.reader.loader.HttpLoader;
import com.yuantops.eco.reader.loader.LocalLoader;
import com.yuantops.eco.reader.utils.DateUtils;
import com.yuantops.eco.reader.utils.StringUtils;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

/** 
 * App Context: for 1) saving and getting app-wide variables,
 * 2) providing apis checking network status and 3) handling 
 * local cached data
 * 全局上下文： 保存、获取全局变量; 检查网络状态; 处理缓存数据
 * 
 * @Author   yuan(yuan.tops@gmail.com), based on liux's (http://my.oschina.net/liux) work
 * @Created  Mar 29, 2015 
 */
public class AppContext extends Application{
	private static final String TAG = "AppContext";
	public static final int NETTYPE_WIFI  = 0x01;
	public static final int NETTYPE_CMWAP = 0x02;
	public static final int NETTYPE_CMNET = 0x03;
	
	public static final int PAGE_SIZE = 10;//默认分页大小
	
	private String dataRootPath;//存放App缓存的根目录
	private int    cacheSize;   //缓存的issue数量
	
	@Override
	public void onCreate() {
		Log.v(TAG, "onCreate....");
		super.onCreate();
		Thread.setDefaultUncaughtExceptionHandler(AppException.getAppExceptionHandler());
		
		init();
	}
	
	/**
	 * Initialization: retrieve cache folder root path (If not exists then set it); guarantee cache folders exists
	 * 初始化：从APP设置文件中取出缓存根目录的路径(如果不存在则写入); 确保缓存目录存在
	 */
	private void init() {
		//Retrieve/set cache path
		dataRootPath = getProperty(AppConfig.CACHE_PATH);
		if (StringUtils.isEmpty(dataRootPath)) {
			setProperty(AppConfig.CACHE_PATH, AppConfig.DEFAULT_CACHE_PATH);
			dataRootPath = AppConfig.DEFAULT_CACHE_PATH;
		}
		File indexDir = new File(getIndexDir());
		if (! indexDir.exists()) {
			indexDir.mkdirs();
		}		
		File starsDir = new File(getStarsDir());
		if (!starsDir.exists()) {
			starsDir.mkdirs();
		}	
		
		//Retrieve/set cache size
		String cacheSizeStr = getProperty(AppConfig.CACHE_SIZE);
		if (StringUtils.isEmpty(cacheSizeStr)) {
			setProperty(AppConfig.CACHE_SIZE, String.valueOf(AppConfig.DEFAULT_CACHE_SIZE));
			cacheSize = AppConfig.DEFAULT_CACHE_SIZE;
		} else {
			cacheSize = Integer.parseInt(cacheSizeStr);
		}
				
		//Create global configuration and Initialize UniversalImageLoader with
		//this configuration
		ImageLoaderConfiguration UILconfig = new ImageLoaderConfiguration.Builder(this)
			.build();
		ImageLoader.getInstance().init(UILconfig);
	}
	
	/**
	 * 检测网络是否可用
	 * @return
	 */
	public boolean isNetworkConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}

	/**
	 * 获取当前网络类型
	 * @return 0：没有网络   1：WIFI网络   2：WAP网络    3：NET网络
	 */
	public int getNetworkType() {
		int netType = 0;
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo == null) {
			return netType;
		}		
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE) {
			String extraInfo = networkInfo.getExtraInfo();
			if(!StringUtils.isEmpty(extraInfo)){
				if (extraInfo.toLowerCase().equals("cmnet")) {
					netType = NETTYPE_CMNET;
				} else {
					netType = NETTYPE_CMWAP;
				}
			}
		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			netType = NETTYPE_WIFI;
		}
		return netType;
	}
		
	/**
	 * 得到App缓存的根目录
	 * @return Cache root path for storing data
	 */
	public String getCacheDirRoot() {
		return dataRootPath;
	}
	
	/**
	 * 得到App缓存issue对象的目录
	 * @return Directory storing issue index objects
	 */
	public String getIndexDir() {
		return dataRootPath + "index" + File.separator;
	}
	
	/**
	 * 得到App缓存加星文章的目录
	 * @return Directory storing starred article objects
	 */
	public String getStarsDir() {
		return dataRootPath + "stars" + File.separator;
	}
	
	/**
	 * 返回缓存的Issue对象的列表
	 * @return List of serialized issue objects stored locally 
	 */
	public List<Issue> loadCachedIssues() {
		LocalLoader lcLoader = new LocalLoader(this);
		return lcLoader.loadCachedIssues();
	}
	
	/**
	 * 检查出版日期确定的Issue对象是否缓存在本地
	 * Check if an issue object published on a certain date has already been cached.
	 * @param pubdate 形如20150321的出版日期
	 * @return
	 */
	public boolean isIssueCached(String pubdate) {
		LocalLoader lcLoader = new LocalLoader(this);
		Issue issue = lcLoader.loadCachedIssue(pubdate);
		return (issue == null) ? false : true;
	}
	
	/**
	 * 得到缓存在本地的Issue对象的个数
	 * Get number of cached issue objects
	 * @return issue Numbers
	 */
	public int cachedIssueNumber() {
		LocalLoader lcLoader = new LocalLoader(this);
		return lcLoader.cacheSize();
	}
	
	/**
	 * 根据出版日期加载Issue对象: 如果有缓存，则逆序列化并返回;否则从网上下载，保存
	 * Load Issue object according to published date
	 * @param pubdate 形如20150321的字符串
	 * @return
	 */
	public Issue loadIssue(String pubdate) throws AppException {
		LocalLoader lcLoader = new LocalLoader(this);
		Issue issue = lcLoader.loadCachedIssue(pubdate);
		if (issue == null) {
			if (!isNetworkConnected()) {
				DebugLog.d("Network not connected");
				return null;
			}
			HttpLoader hpLoader = new HttpLoader(this);
			issue = hpLoader.FetchIssueManifest(pubdate);
		}
		return issue;
	}
	
	/**
	 * 从网上下载并保存默认数量的Issue对象
	 * Download & save default number of issue objects
	 * @return Issue list
	 * @throws AppException
	 */
	public List<Issue> loadOnlineIssues() throws AppException {
		List<String> dates = DateUtils.pastIssuePubdates(cacheSize);
		return loadOnlineIssues(dates);
	}
	
	public List<Issue> loadOnlineIssues(List<String> pubdates) throws AppException {		
		List<Issue> issues  = new ArrayList<Issue> ();
		HttpLoader hpLoader = new HttpLoader(this);
		for (String date : pubdates) {
			DebugLog.v("Fetching " + date);
			issues.add(hpLoader.FetchIssueManifest(date));
		}
		return issues;
	}
	
	public boolean containsProperty(String key) {
		Properties props = getProperties();
		return props.containsKey(key);
	}	
	public void setProperties(Properties ps) {
		AppConfig.getInstance(this).set(ps);
	}
	public void setProperty(String key, String value) {
		AppConfig.getInstance(this).set(key, value);
	}
	public Properties getProperties() {
		return AppConfig.getInstance(this).get();
	}
	public String getProperty(String key) {
		return AppConfig.getInstance(this).get(key);
	}
	public void removeProperties(String... keys) {
		AppConfig.getInstance(this).remove(keys);
	}
}
