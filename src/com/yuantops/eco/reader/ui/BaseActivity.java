package com.yuantops.eco.reader.ui;

import com.yuantops.eco.reader.AppManager;

import android.app.Activity;
import android.os.Bundle;

/**
 * Activity的基类，将Activity用AppManager管理
 * BaseActivity，manages activities with AppManager
 * @author yuan
 *
 */
public class BaseActivity extends Activity {
	
	@Override 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppManager.getInstance().addActivity(this);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		AppManager.getInstance().finishActivity(this);
	}
        
}
