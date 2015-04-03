package com.yuantops.eco.reader.ui;

import java.util.Iterator;
import java.util.List;

import com.mustafaferhan.debuglog.DebugLog;
import com.yuantops.eco.reader.AppContext;
import com.yuantops.eco.reader.AppException;
import com.yuantops.eco.reader.R;
import com.yuantops.eco.reader.bean.Issue;
import com.yuantops.eco.reader.loader.HttpLoader;
import com.yuantops.eco.reader.loader.LocalLoader;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

@SuppressLint("NewApi")
public class LibraryFragment extends Fragment {
	private static final String TAG = LibraryFragment.class.getSimpleName();
	
	private AppContext mAppContext = null;
	
	private String[] listExample = {"dfadfd", "fda", "fdae"};
	private View     rootView;
	private ListView listView;
	private TextView testView;
	
	private LocalLoader lcLoader;
	
	private Handler mLibHandler = new Handler() {
		@Override  
        public void handleMessage(Message msg) {  
            super.handleMessage(msg);  
            if(msg.what == 1)  {
            	testView.setText(msg.obj.toString());
            }
       }
	};
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAppContext = (AppContext) getActivity().getApplicationContext();
		lcLoader    = new LocalLoader(getActivity());		
		if (lcLoader.cacheSize() == 0) {
			new Thread() {				
				@Override
				public void run() {
					try {
						HttpLoader hpLoader = new HttpLoader(getActivity());
						Issue currentIssue = hpLoader.FetchIssueManifest();
						Message dlFinished = new Message();
						dlFinished.what = 1;
						dlFinished.obj  = currentIssue;
						mLibHandler.sendMessage(dlFinished);
					} catch (AppException e) {						
					}
				}
				
			}.start();
		} else {
			List<Issue> issueList = lcLoader.loadCachedIssues();
			Log.v(TAG + " >retrived issueList size", issueList.size() + "");
			Iterator<Issue> issueIte = issueList.iterator();
			while (issueIte.hasNext()) {
				Issue issue = issueIte.next();
				Message msg = new Message();
				msg.what = 1;
				msg.obj  = issue;
				mLibHandler.sendMessage(msg);
			}
		}
		setHasOptionsMenu(true);
	}

	 @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
		 Log.v(TAG, "onCreatedView");
		 if (rootView == null) {
			 rootView = inflater.inflate(R.layout.fragment_library, container, false);
			 listView = (ListView) rootView.findViewById(R.id.library_list);
			 testView = (TextView) rootView.findViewById(R.id.libary_test);
			 listView.setAdapter(new ArrayAdapter<String>(getActivity(),
		                R.layout.drawer_list_item, listExample));
		 }
		 return rootView;
	 }
}
