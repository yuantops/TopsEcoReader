package com.yuantops.eco.reader.loader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.util.Log;

import com.mustafaferhan.debuglog.DebugLog;
import com.yuantops.eco.reader.AppContext;
import com.yuantops.eco.reader.AppException;
import com.yuantops.eco.reader.bean.Article;
import com.yuantops.eco.reader.bean.Issue;
import com.yuantops.eco.reader.utils.DateUtils;
import com.yuantops.eco.reader.utils.StringUtils;

/** 
 * Load/download data from the Internet
 * 从网上加载/下载数据
 * 
 * Author:     yuan(yuan.tops@gmail.com)
 * Created on: Apr 1, 2015 
 */
public class HttpLoader {
	private static final String TAG = "HttpLoader";	

	private final static String ECONOMIST_SITE_ROOT_URL     = "http://www.economist.com/";
	private final static String ECONOMIST_CURRENT_ISSUE_URL = "http://www.economist.com/printedition/";
	
	private Context mContext;
	private String  mCacheRootPath;
	private String  mIndexPath;
		
	public HttpLoader(Context context) {
		this.mContext = context;
		this.mCacheRootPath = ((AppContext) mContext.getApplicationContext()).getCacheDirRoot();
		this.mIndexPath     = ((AppContext) mContext.getApplicationContext()).getIndexDir();
	}	

	/**
	 * 下载当前Issue
	 * Download Current issue
	 */
	public Issue FetchIssueManifest() throws AppException {
		return FetchIssueManifest("");
	}
	
	/**
	 * 根据pubdate下载Issue
	 * @param pubdate
	 * @return
	 * @throws AppException
	 */
	public Issue FetchIssueManifest(String date) throws AppException {		
		if (!((AppContext) mContext.getApplicationContext()).isNetworkConnected()) {
			Log.d(TAG, "Fetch issue failure due to no network connection");
			return null;
		}
		
		String url = null;
		if (StringUtils.isEmpty(date)) {
			url = ECONOMIST_CURRENT_ISSUE_URL;
		} else {
			url = ECONOMIST_CURRENT_ISSUE_URL + date;
		}
		
		DebugLog.v("Fetching from url: " + url);
		
		HttpClient client = new DefaultHttpClient();
		CookieStore cookieStore = new BasicCookieStore();
		// Create local HTTP context
		HttpContext localContext = new BasicHttpContext();
		cookieStore.clear();
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
	
		HttpGet httpGet = new HttpGet(url);
		HttpResponse response1 = null;
		String htmlRsp = null;
		try {
			DebugLog.v("Begin executing get...");
			response1 = client.execute(httpGet, localContext);
			HttpEntity entity = response1.getEntity();
			htmlRsp = EntityUtils.toString(entity, "UTF-8");
			//DebugLog.v("http Response \n" + htmlRsp);
			entity.consumeContent();
		} catch (IOException e) {
			e.printStackTrace();
			DebugLog.d("Exception Http");
			throw AppException.io(e);
		}
		
		Element rawDoc = Jsoup.parse(htmlRsp);		
		String pubdate   = DateUtils.formatPubdate(rawDoc.select("meta[name=pubdate]").first().attr("content"));
		Element cover    = rawDoc.getElementsByClass("issue-image").first().select("img").first();
		String title     = cover.attr("title");
		String thumbnail = cover.attr("src");
		String fullCover = thumbnail.replace("thumbnail", "full");			
				
		Issue issue = new Issue(pubdate, title, thumbnail, fullCover);	
		
		DebugLog.d("raw issue content:\n" + issue.toString());
				
		Elements sections = rawDoc.select("div[class^=section]");
		int articleNo = 1;
		for (Element section:sections) {			
			String sectionName = section.select("h4").first().text();			
			Elements articles  = section.select("div.article");
			Elements flytitles = section.select("h5");
			Iterator<Element> itFly = flytitles.iterator();
			String headline, flytitle, webUrl;
			for (Element article:articles) {
				DebugLog.v("Processing article " + articleNo);
				headline = article.select("a").first().text();
				webUrl   = ECONOMIST_SITE_ROOT_URL + article.select("a").first().attr("href");
				if (itFly.hasNext()) {
					flytitle = itFly.next().text();
				} else {
					flytitle = "";
				}
				Article arti = new Article(articleNo++, sectionName, flytitle, headline, webUrl);
				issue.addArticle(arti);
			}
		}		
		
		//Download and save issue cover;		
		File issueDataDir = new File(mCacheRootPath, issue.getPubDate());
		try {
			if (!issueDataDir.exists() || !issueDataDir.isDirectory()) {
				issueDataDir.mkdirs();
			}
		} catch (Exception e) {
			throw AppException.io(e);
		}		
		
		//Replace cover URL (http://) with local URI (file://)
		File coverThumbDest = new File(issueDataDir, "cover_thumbnail");
		File coverFullDest = new File(issueDataDir, "cover_full");
		saveImageFromUrl(issue.getCoverThumbUrl(), coverThumbDest);
		issue.setCoverThumbUrl("file://" + coverThumbDest.getAbsolutePath());
		saveImageFromUrl(issue.getCoverFullUrl(), coverFullDest);
		issue.setCoverFullUrl("file://" + coverFullDest.getAbsolutePath());	
				
		//Serialize issue object and save on disk
		File issueFile = new File(mIndexPath, issue.getPubDate() + ".issue");
		Issue.serialize(issue, issueFile);
		return issue;
	}
	
	/**
	 * 下载Issue里的所有文章到本地
	 * @param issue
	 */
	public void DownloadIssueArticles(Issue issue) throws AppException {
		if (!((AppContext) mContext.getApplicationContext()).isNetworkConnected()) {
			Log.d(TAG, "Download article failure due to no network connection");
			return;
		}
				
		File issueDataDir = new File(mCacheRootPath, issue.getPubDate());
		File issueImgDir  = new File(issueDataDir, "images");
		try {
			if (!issueImgDir.exists() || !issueImgDir.isDirectory()) {
				issueImgDir.mkdirs();
			}
		} catch (Exception e) {
			throw AppException.io(e);
		}
		
		// Crawl and process each article
		Iterator ite = issue.iterator();
		while (ite.hasNext()) {
			Article article = (Article) ite.next();
			HttpLoader.downloadAndSave(article, issueDataDir);
		}		
	}
	
	/**
	 * Download article html file, download all images in the file, replace
	 * image link in html file with local relative link, process html file and
	 * save it to disk.
	 * 
	 * @param arti
	 * @throws AppException 
	 */
	private static void downloadAndSave(Article arti, File destiFolder) throws AppException {
		File articleFolder = destiFolder;
		File imageFolder   = new File(destiFolder, "images");
		
		System.out.printf("Fetching article %d...%n", arti.getArticleNo());
		String artiUrl = arti.getUri();
		String htmlRsp = null;
		
		HttpClient client = new DefaultHttpClient();
		CookieStore cookieStore = new BasicCookieStore();
		// Create local HTTP context
		HttpContext localContext = new BasicHttpContext();
		cookieStore.clear();
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
		HttpGet httpGet = new HttpGet(artiUrl);
		HttpResponse response1 = null;
		try {
			response1 = client.execute(httpGet, localContext);
			HttpEntity entity = response1.getEntity();
			htmlRsp = EntityUtils.toString(entity, "UTF-8");
			entity.consumeContent();
		} catch (IOException e) {
			throw AppException.io(e);
		} 
		
		/*RequestConfig noCookieConfig = RequestConfig.custom()
				.setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
		CloseableHttpClient client = HttpClients.custom()
				.setDefaultRequestConfig(noCookieConfig).build();
		HttpGet httpGet = new HttpGet(artiUrl);
		CloseableHttpResponse response1 = null;
		try {
			response1 = client.execute(httpGet);
			HttpEntity entity = response1.getEntity();
			htmlRsp = EntityUtils.toString(entity, "UTF-8");
			entity.consumeContent();
		} catch (IOException e) {
			System.out.printf("%s***Crawl html error...***%n", TAG);
		} finally {
			try {
				response1.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/

		// Create Doc from String
		Element rawDoc = Jsoup.parse(htmlRsp).select("article").first();
		if (rawDoc == null) {
			System.out.println("***No Content in article...**");
			return;
		}
		
		String rubric = "";
		if (!rawDoc.select("[class=rubric]").isEmpty()) {
			rubric = rawDoc.select("[class=rubric]").first().text();
		}
		String dateCreated = "";
		if (!rawDoc.select("time").isEmpty()) {
			dateCreated = rawDoc.select("time").first().text();
		}

		Element body = rawDoc.select("div.main-content").first();
		if (!body.select("aside.main-content-container").isEmpty()) {
			body.select("aside.main-content-container").remove();
		}
		if (!body.select("a.ec-active-image").isEmpty()) {
			body.select("a.ec-active-image").parents().first().remove();
		}
		if (!body.select("p.ec-article-info").isEmpty()) {
			body.select("p.ec-article-info").remove();
		}
		if (!body.select("div:not([class^=content-image])").isEmpty()) {
			body.select("div:not([class^=content-image])").remove();
		}

		// Save Image in article to disk
		Elements images = body.select("img");
		Iterator imgIte = images.iterator();
		int imgRank = 1;
		while (imgIte.hasNext()) {
			Element imgEle = (Element) imgIte.next();
			String imgUrl = imgEle.attr("src");
			String newUri = arti.getArticleNo() + "." + imgRank++;
			try {
				URL url = new URL(imgUrl);
				InputStream imgIs = new BufferedInputStream(url.openStream());
				HttpLoader.saveFile(imgIs, new File(imageFolder, newUri));
			} catch (Exception e) {
			}
			body.select("[src=" + imgUrl + "]").first()
					.attr("src", "images/" + newUri);
			//add imgUrl in article object
			arti.addImgUrl(newUri);
		}

		//Trim and decorate html content
		Elements eles = body.select("p, img[src]");
		for (Element img : eles.select("img[src]")) {
			img.removeAttr("itemprop").removeAttr("alt").removeAttr("width")
					.removeAttr("height");
			img.addClass("article-image");
		}
		body = Jsoup.parse(eles.toString());
		body.select("body").first().prependElement("p").attr("class", "date")
				.text(dateCreated);
		body.select("body").first().prependElement("p").attr("class", "rubric")
				.text(rubric);
		body.select("body").first().prependElement("p")
				.attr("class", "headline").text(arti.getHeadline());
		if (arti.getFlyTitle() != "") {
			body.select("body").first().prependElement("p")
					.attr("class", "fly-title").text(arti.getFlyTitle());
		}
		body.select("body").attr("class", "main-body");
		body.select("head").first().prependElement("meta")
				.attr("http-equiv", "Content-Type")
				.attr("content", "text/html; charset=utf-8");
		body.select("html").first()
				.attr("xmlns", "http://www.w3.org/1999/xhtml");

		// Save processed html file
		InputStream fileIs = null;
		try {
			fileIs = new ByteArrayInputStream(body.toString().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
		}
		File newFile = new File(articleFolder, "Article_" + arti.getArticleNo()
				+ ".html");
		HttpLoader.saveFile(fileIs, newFile);

		arti.setDateCreated(dateCreated);
		arti.setRubric(rubric);
	}

	public static void saveImageFromUrl(String u, File destination) {
		InputStream in = null;
		try {
			URL url = new URL(u);
			in = new BufferedInputStream(url.openStream());
			HttpLoader.saveFile(in, destination);
		} catch (IOException e) {
		}
	}
	
	public static void saveFile(InputStream is, File destination) {
		OutputStream out = null;

		try {
			if (!destination.exists())
				destination.createNewFile();
			out = new BufferedOutputStream(new FileOutputStream(destination));

			int n = 0;
			byte[] buf = new byte[1024];
			while ((n = is.read(buf)) != -1) {
				out.write(buf, 0, n);
			}
		} catch (IOException e) {
			System.out.printf("Error when saving to %s%n",
					destination.getAbsolutePath());
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					System.out.println("Cannot close inputstream");
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					System.out.println("Cannot close outputstream");
					e.printStackTrace();
				}
			}
		}
	}
	
}
