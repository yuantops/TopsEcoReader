package com.yuantops.eco.reader.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class for an Issue.
 * 
 * Author: yuan(yuan.tops@gmail.com) Created on: Mar 21, 2015
 */
public class Issue implements Serializable, Iterable<Article> {

	private String pubdate; // print date for this issue
	private String title;
	private String cover_thumb;
	private String cover_full;
	private List<Article> articleList;

	public Issue(String date, String title, String thumb, String full) {
		this.pubdate = date;
		this.title = title;
		this.cover_full = full;
		this.cover_thumb = thumb;
		this.articleList = new ArrayList<Article>();
	}

	public Issue() {
	}

	/**
	 * Serialize Issue object
	 * 
	 * @param issue
	 * @param file
	 */
	public static void serialize(Issue issue, File file) {

		ObjectOutputStream ofs = null;
		try {
			if (!file.exists())
				file.createNewFile();
			ofs = new ObjectOutputStream(new FileOutputStream(file));
			ofs.writeObject(issue);
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			try {
				if (ofs != null) {
					ofs.close();
				}
			} catch (IOException e) {
			}
		}
	}

	public static Issue deserialize(File file) {
		Issue issue = null;

		FileInputStream ifs    = null;
		ObjectInputStream obis = null;
		try {
			ifs   = new FileInputStream(file);
			obis  = new ObjectInputStream(ifs);
			issue = (Issue) obis.readObject();
		} catch (ClassNotFoundException e) {
			System.out.println("Class not found");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IOException");
		} finally {
			if (ifs != null) {
				try {
					ifs.close();
				} catch (IOException e) {
				}
			}
			if (obis != null) {
				try {
					obis.close();
				} catch (IOException e) {
				}
			}
		}
		return issue;
	}

	public void addArticle(Article art) {
		articleList.add(art);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPubDate() {
		return pubdate;
	}

	public int getArticleCount() {
		return articleList.size();
	}

	public String getCoverThumbUrl() {
		return this.cover_thumb;
	}

	public void setCoverThumbUrl(String url) {
		this.cover_thumb = url;
	}

	public void setCoverFullUrl(String url) {
		this.cover_full = url;
	}

	public String getCoverFullUrl() {
		return this.cover_full;
	}

	@Override
	public Iterator<Article> iterator() {
		return articleList.iterator();
	}

	@Override
	public String toString() {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(String.format(
				"pubdate: %s%ntitle:   %s%nthumb:   %s%nfull:    %s%n",
				this.pubdate, this.title, this.cover_thumb, this.cover_full));
		strBuf.append("\n");
		Iterator ite = this.iterator();
		while (ite.hasNext()) {
			Article art = (Article) ite.next();
			strBuf.append(art.toString());
		}
		return strBuf.toString();
	}

}
