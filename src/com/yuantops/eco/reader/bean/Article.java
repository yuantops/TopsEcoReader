package com.yuantops.eco.reader.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** 
 * Class for a single article.
 * 
 * Author:     yuan(yuan.tops@gmail.com)
 * Created on: Mar 21, 2015 
 */
public class Article implements Serializable {
	//private int    issueNo;
	private int    articleNo;	
	private String section;
	private String flyTitle;
	private String headline;
	private String uri;
	
	private String rubric;
	private String dateCreated;
	private List<String> imgUrls = null;
	private String content;	
	
	public Article(int no, String section, String flyTitle, String headline, String webUrl) {
		this.articleNo = no;
		this.section   = section;
		this.flyTitle  = flyTitle;
		this.headline  = headline;
		this.uri       = webUrl;
	}
	
	@Override
	public String toString() {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(String.format("No:       %d%n", articleNo));
		strBuf.append(String.format("section:  %s%n", section));
		strBuf.append(String.format("flyTitle: %s%n", flyTitle));
		strBuf.append(String.format("headline: %s%n", headline));
		strBuf.append(String.format("rubric:   %s%n", rubric));
		strBuf.append(String.format("date:     %s%n", dateCreated));
		strBuf.append(String.format("uri:      %s%n", uri));
		strBuf.append(String.format("content:  %s%n", content));
		if (imgUrls != null) {
			for (String url : imgUrls) {
				strBuf.append(String.format("imgUrl:   %s%n", url));
			}
		}
		strBuf.append("\n");
		return strBuf.toString();
	}
	
	public void addImgUrl(String url) {
		if (this.imgUrls == null) this.imgUrls = new ArrayList<String> ();
		imgUrls.add(url);
	}
	public List<String> getImgUrls() {
		return this.imgUrls;
	}
	
	public String getUri() {
		return this.uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public int getArticleNo() {
		return articleNo;
	}
	public void setArticleNo(int articleNo) {
		this.articleNo = articleNo;
	}
	public String getSection() {
		return section;
	}
	public void setSection(String section) {
		this.section = section;
	}
	public String getHeadline() {
		return (headline == null) ? "" : headline;
	}
	public void setHeadline(String headline) {
		this.headline = headline;
	}
	public String getFlyTitle() {
		return (flyTitle == null) ? "" : flyTitle;
	}
	public void setFlyTitle(String flyTitle) {
		this.flyTitle = flyTitle;
	}
	public String getRubric() {
		return (rubric == null) ? "" : rubric;
	}
	public void setRubric(String rubric) {
		this.rubric = rubric;
	}
	public String getDateCreated() {
		return (dateCreated == null) ? "" : dateCreated;
	}
	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}
	public String getContent() {
		return (content == null) ? "" : content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
