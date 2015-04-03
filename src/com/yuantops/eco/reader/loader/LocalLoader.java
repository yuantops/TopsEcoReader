package com.yuantops.eco.reader.loader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.yuantops.eco.reader.AppContext;
import com.yuantops.eco.reader.bean.Issue;

import android.content.Context;
import android.util.Log;

/** 
 * Load data from local cache
 * 从本地缓存中加载数据
 * 
 * Author:     yuan(yuan.tops@gmail.com)
 * Created on: Apr 1, 2015 
 */
public class LocalLoader {
	private static final String TAG = "LocalLoader";

	private Context mContext;
	private String  mCacheRootPath;
	private String  mIndexPath;
	private String  mStarsPath;
	
	public LocalLoader(Context context) {
		this.mContext = context;
		this.mCacheRootPath = ((AppContext) mContext.getApplicationContext()).getCacheDirRoot();
		this.mIndexPath     = ((AppContext) mContext.getApplicationContext()).getIndexDir();
		this.mStarsPath     = ((AppContext) mContext.getApplicationContext()).getStarsDir();
	}
	
	/**
	 * Get number of cached issue objects
	 * 得到本地缓存的issue对象个数
	 * @return 
	 */
	public int CachedSize() {
		File indexDir = new File(mIndexPath);
		Log.v(TAG + " > inspecting dir:" , indexDir.getAbsolutePath());

		File[] files = null;
		if (indexDir.exists()) {
			Log.v(TAG, indexDir.getAbsolutePath() + " exists");
		} else {
			Log.v(TAG, indexDir.getAbsolutePath() + " does not exist");
		}
		
		if (indexDir.isDirectory()){
			files = indexDir.listFiles();
			Log.v(TAG, indexDir.getAbsolutePath() + " is directory");
		} else {
			Log.v(TAG, indexDir.getAbsolutePath() + " not directory");
		}
		
		if (indexDir.isFile()) {
			Log.v(TAG, indexDir.getAbsolutePath() + " is file");
		} else {
			Log.v(TAG, indexDir.getAbsolutePath() + " not  file");
		}
		
		int count = 0;
		if (files == null || files.length == 0) {
			Log.v(TAG + " >cached size", "0");
			return count;
		}
		for (File f : files) {
			if (f.isFile() && f.getName().toLowerCase().endsWith("issue")) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Load and return cached issue objects
	 * 加载并返回本地缓存的issue对象
	 * @return Issue List Issue对象列表
	 */
	public List<Issue> LoadIssueCache() {
		File indexDir = new File(mIndexPath);
		File[] files = indexDir.listFiles();
		List<Issue> issueList = new ArrayList<Issue> (); 
		for (File f : files) {
			if (f.isFile() && f.getName().toLowerCase().endsWith("issue")) {
				issueList.add(Issue.deserialize(f));
			}
		}
		return issueList;
	}
	
	/**
	 * Load and return an issue object published on a given date 
	 * 加载并返回某个日期对应的Issue对象
	 * @param date 
	 * @return 如果存在，返回Issue; 不存在，返回null
	 */
	public Issue LoadIssueCache(String date) {
		File file = new File(mIndexPath, date + ".issue");
		if (file.exists()) {
			return Issue.deserialize(file);
		} else {
			Log.d(TAG, file.getAbsolutePath() + " deserialization failure");
		}
		return null;
	}
	
}
