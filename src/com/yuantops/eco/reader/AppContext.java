package com.yuantops.eco.reader;

import java.util.Properties;

import com.yuantops.eco.reader.utils.StringUtils;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/** 
 * App Context: for 1) saving and getting app-wide variables and 
 * 2) providing apis checking network status
 * 全局上下文： 保存、获取全局变量; 检查网络状态
 * 
 * @Author   yuan(yuan.tops@gmail.com), based on liux's (http://my.oschina.net/liux) work
 * @Created  Mar 29, 2015 
 */
public class AppContext extends Application{
	public static final int NETTYPE_WIFI  = 0x01;
	public static final int NETTYPE_CMWAP = 0x02;
	public static final int NETTYPE_CMNET = 0x03;
	
	public static final int PAGE_SIZE = 10;//默认分页大小
	
	private String dataRootPath;//存放App缓存的根目录
	
	@Override
	public void onCreate() {
		super.onCreate();
		Thread.setDefaultUncaughtExceptionHandler(AppException.getAppExceptionHandler());
		
		init();
	}
	
	/**
	 * Initialization: retrieve cache folder root path (If not exists then set it)
	 * 初始化：从APP设置文件中取出缓存根目录的路径(如果不存在则写入)
	 */
	private void init() {
		dataRootPath = getProperty(AppConfig.CACHE_PATH_KEY);
		if (StringUtils.isEmpty(dataRootPath)) {
			setProperty(AppConfig.CACHE_PATH_KEY, AppConfig.DEFAULT_CACHE_PATH);
			dataRootPath = AppConfig.DEFAULT_CACHE_PATH;
		}
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
