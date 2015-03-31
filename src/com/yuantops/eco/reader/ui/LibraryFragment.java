package com.yuantops.eco.reader.ui;

import com.yuantops.eco.reader.R;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

@SuppressLint("NewApi")
public class LibraryFragment extends Fragment {
	private static final String TAG = LibraryFragment.class.getSimpleName();
	
	private String[] listExample = {"dfadfd", "fda", "fdae"};
	private ListView listView;
	
	public LibraryFragment() {}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	 @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
		 Log.v(TAG, "onCreatedView");
		 View rootView = inflater.inflate(R.layout.fragment_library, container, false);
		 listView = (ListView) rootView.findViewById(R.id.library_list);
		 listView.setAdapter(new ArrayAdapter<String>(getActivity(),
	                R.layout.drawer_list_item, listExample));
		 return rootView;
	 }
}
