package com.yuantops.eco.reader.adapters;

import java.util.List;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.yuantops.eco.reader.R;
import com.yuantops.eco.reader.bean.Issue;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/** 
 * Adapter for list of issues
 * Issue列表的适配器
 * 
 * Author:     yuan(yuan.tops@gmail.com)
 * Created on: Apr 3, 2015 
 */
public class LibraryAdapter extends BaseAdapter {
	List<Issue> mIssueList;
	Context     mContext;
	ImageLoader mImgLoader;
		
	public LibraryAdapter(List<Issue> issueList, Context context) {
		this.mContext = context;
		this.mIssueList = issueList;
		this.mImgLoader = ImageLoader.getInstance();
	} 

	@Override
	public int getCount() {
		return mIssueList.size();
	}

	@Override
	public Object getItem(int position) {
		return mIssueList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh = null;
		
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.view_issue_list_item, null);
			
			vh = new ViewHolder((ImageView) convertView.findViewById(R.id.cover), 
					(TextView) convertView.findViewById(R.id.issue_pubdate),
					(TextView) convertView.findViewById(R.id.issue_title),
					(ImageButton) convertView.findViewById(R.id.issue_button));
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		
		Issue iss = mIssueList.get(position);
		if (iss != null) {
			vh.mIssuePubdate.setText(iss.getPubDate());
			vh.mIssueTitle.setText(iss.getTitle());
			mImgLoader.displayImage(iss.getCoverThumbUrl(), vh.mCover);
		}
		return convertView;
	}

	private class ViewHolder {
		ImageView mCover;
		TextView  mIssuePubdate, mIssueTitle;
		ImageButton mIssueButton;
		
		ViewHolder(ImageView cover, TextView issuePubdate, TextView issueTitle, ImageButton imgButton) {
			mCover = cover;
			mIssuePubdate = issuePubdate;
			mIssueTitle   = issueTitle;
			mIssueButton  = imgButton;
		}
	}
}
