package com.yuantops.eco.reader.ui;

import java.util.ArrayList;
import java.util.List;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mustafaferhan.debuglog.DebugLog;
import com.yuantops.eco.reader.AppContext;
import com.yuantops.eco.reader.AppException;
import com.yuantops.eco.reader.R;
import com.yuantops.eco.reader.adapters.LibraryAdapter;
import com.yuantops.eco.reader.bean.Issue;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LibraryFragment_ptr extends Fragment {
	private static final int LOAD_FINISHED = 1;
	
	private List<Issue>           mLibraryIssues = new ArrayList<Issue> ();
	private PullToRefreshListView mPTRListView;
	private LibraryAdapter        mLibraryAdapter;
	private AppContext            mAppContext;
	private View                  mRootView;
	
	private Handler mLibHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case LOAD_FINISHED:
				DebugLog.v("Msg received");
				mLibraryAdapter.notifyDataSetChanged();
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
		
		new Thread() {
			@Override
			public void run() {
				if (mAppContext.cachedIssueNumber() > 0) {
					DebugLog.v("Loading cached issues...");					
					for (Issue issue : mAppContext.loadCachedIssues()) {
						mLibraryIssues.add(issue);
					}	
				} else {
					DebugLog.v("Downloading from website...");
					try {
						for (Issue issue : mAppContext.loadOnlineIssues()) {
							mLibraryIssues.add(issue);
						}
					} catch (AppException e) {
						e.printStackTrace();
					}
				}
				Message onlineLdFinish = new Message();
				onlineLdFinish.what = LOAD_FINISHED;
				mLibHandler.sendMessage(onlineLdFinish);
			}
		}.start();		
		
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		DebugLog.v("onCreateView()...");
		if (mRootView == null) {
			DebugLog.v("RootView is null");
			mRootView = inflater.inflate(R.layout.fragment_library_ptr, container, false);
			mPTRListView = (PullToRefreshListView) mRootView.findViewById(R.id.lib_pull_refresh_list);
			mPTRListView.setAdapter(mLibraryAdapter);
		}
		return mRootView;
	}
}
