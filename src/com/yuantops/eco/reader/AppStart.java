package com.yuantops.eco.reader;

import com.yuantops.eco.reader.ui.HomeActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

public class AppStart extends Activity {
	private static final String TAG = AppStart.class.getSimpleName();
	private AppContext mContext;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		View view = View.inflate(this, R.layout.start_activity, null);
		setContentView(view);
		
		//渐变展示启动屏
		AlphaAnimation aa = new AlphaAnimation(0.3f,1.0f);
		aa.setDuration(2000);
		view.startAnimation(aa);
		aa.setAnimationListener(new AnimationListener()
		{
			@Override
			public void onAnimationEnd(Animation arg0) {
				redirectTo();
			}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			@Override
			public void onAnimationStart(Animation animation) {}			
		});
	}
	
	private void redirectTo() {
		Intent intent = new Intent(this, HomeActivity.class);
		startActivity(intent);
		this.finish();
	}
}
