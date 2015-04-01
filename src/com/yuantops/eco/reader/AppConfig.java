package com.yuantops.eco.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import android.content.Context;
import android.os.Environment;

/** 
 * Keeping constant variables and setting parameters
 * 配置信息： 保存常量以及设置参数
 * 
 * Author:     yuan(yuan.tops@gmail.com)
 * Created on: Mar 29, 2015 
 */
public class AppConfig {
			
	public final static String DEFAULT_CACHE_PATH = Environment.getExternalStorageDirectory() + File.separator + "TopsEcoReader" + File.separator;
	
	public final static String CACHE_PATH_KEY = "cache_path";	
	public final static String APP_CONFIG = "config";
	
	private static int ISSUE_CACHE_COUNT = 5;//Default 
	
	private Context mContext;
	private static AppConfig instance;
	
	private AppConfig() {}
	
	/**
	 * 单例模式。Singleton design pattern。
	 * @param context
	 * @return
	 */
	public static AppConfig getInstance(Context context) {
		if (instance == null) {
			instance = new AppConfig();
			instance.mContext = context;
		}
		return instance;
	}
	
	/**
	 * 获取配置参数。Get setting parameter.
	 * @param key
	 * @return
	 */
	public String get(String key) {
		Properties  props = get();
		return (props != null) ? props.getProperty(key) : null;
	}
	
	/**
	 * 取出App的配置文件
	 * Retrieve setting all of the parameters 
	 * @return
	 */
	public Properties get() {
		FileInputStream ifs = null;
		Properties props = new Properties();
		try {
			//Retrieve/create (if not exists) setting file in app's internal storage
			//从APP的内部存储目录获取/新建（如果不存在的话）配置文件
			File confDir = mContext.getDir(APP_CONFIG, Context.MODE_PRIVATE);
			File confFile = new File(confDir.getPath() + File.separator + APP_CONFIG);
			if (!confFile.exists()) confFile.createNewFile(); 
			ifs = new FileInputStream(confFile);
			props.load(ifs);
		} catch (Exception e){			
		} finally {
			if (ifs != null) {
				try {
					ifs.close();
				} catch (Exception e) {					
				}
			}
		}
		return props;
	}
	
	/**
	 * 将App配置写入文件
	 * Store App setting parameters in config file
	 * @param p
	 */
	private void setProps(Properties p) {
		FileOutputStream fos = null;
		try {
			//Retrieve/create (if not exists) setting file in app's internal storage
			//从APP的内部存储目录获取/新建（如果不存在的话）配置文件
			File confDir = mContext.getDir(APP_CONFIG, Context.MODE_PRIVATE);
			File confFile = new File(confDir.getPath() + File.separator + APP_CONFIG);
			if (!confFile.exists()) confFile.createNewFile(); 
			fos = new FileOutputStream(confFile);
			
			p.store(fos, null);
			fos.flush();
		} catch (Exception e) {			
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (Exception e) {					
				}
			}
		}
	}
	
	public void set(Properties ps) {
		Properties props = get();
		props.putAll(ps);
		setProps(props);
	}
	
	public void set(String key, String value) {
		Properties props = get();
		props.setProperty(key, value);
		setProps(props);
	} 
	
	public void remove(String... keys) {
		Properties props = get();
		for (String key : keys) {
			props.remove(key);
		}
		setProps(props);
	}
}
