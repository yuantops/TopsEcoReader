package com.yuantops.eco.reader.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mustafaferhan.debuglog.DebugLog;
import com.yuantops.eco.reader.AppConfig;
import com.yuantops.eco.reader.AppContext;
import com.yuantops.eco.reader.AppException;
import com.yuantops.eco.reader.R;
import com.yuantops.eco.reader.adapters.LibraryAdapter;
import com.yuantops.eco.reader.bean.Issue;

public class LibraryFragment_ptr extends Fragment {
	
	private List<Issue>           mLibraryIssues = new ArrayList<Issue> ();
	private PullToRefreshListView mPTRListView;
	private NumberProgressBar     mProgressBar;
	private LibraryAdapter        mLibraryAdapter;
	private AppContext            mAppContext;
	private View                  mRootView;
	
	private Handler mLibHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case AppContext.LOAD_FINISHED:
				DebugLog.v("Msg received");
				mProgressBar.setVisibility(View.GONE);
				mPTRListView.setVisibility(View.VISIBLE);
				mLibraryAdapter.notifyDataSetChanged();
				break;
			case AppContext.LOAD_IN_PROGRESS:
				int progressInt = (Integer) msg.obj;
				DebugLog.i("progress: " + progressInt);
				mProgressBar.setProgress(progressInt);
				break;
			default:
				break;
			}
		}
	};
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		DebugLog.v("onCreate()...");
		
		mAppContext     = (AppContext) getActivity().getApplicationContext();					
		mLibraryAdapter = new LibraryAdapter(mLibraryIssues, getActivity());				
		
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		DebugLog.v("onCreateView()...");
		if (mRootView == null) {
			DebugLog.v("RootView is null");
			mRootView    = inflater.inflate(R.layout.fragment_library_ptr, container, false);
		}
		mPTRListView = (PullToRefreshListView) mRootView.findViewById(R.id.lib_pull_refresh_list);
		mPTRListView.setAdapter(mLibraryAdapter);
		mPTRListView.setVisibility(View.GONE);
		mProgressBar = (NumberProgressBar) mRootView.findViewById(R.id.lib_progress_bar);

		new Thread() {
			@Override
			public void run() {
				if (mAppContext.cachedIssueNumber() > 0) {
					DebugLog.v("Loading cached issues...");	
					mLibraryIssues.addAll(mAppContext.loadCachedIssues());						
				} else {
					DebugLog.v("Downloading from website...");
					try {
						mLibraryIssues.addAll(mAppContext.loadOnlineIssues(mLibHandler));						
					} catch (AppException e) {
						DebugLog.e("Download Exception");
						e.printStackTrace();
					}
				}
				Message onlineLdFinish = new Message();
				onlineLdFinish.what = AppContext.LOAD_FINISHED;
				mLibHandler.sendMessage(onlineLdFinish);
			}
		}.start();	
		
		return mRootView;
	}
}
