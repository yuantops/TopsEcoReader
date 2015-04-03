package com.yuantops.eco.reader.ui;

import java.util.List;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.yuantops.eco.reader.adapters.LibraryAdapter;
import com.yuantops.eco.reader.bean.Issue;

import android.app.Fragment;
import android.os.Bundle;

public class LibraryFragment_ptr extends Fragment {
	private List<Issue>           mLibraryIssues;
	private PullToRefreshListView mPTRListView;
	private LibraryAdapter        mLibraryAdapter;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
}
