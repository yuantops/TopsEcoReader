package com.yuantops.eco.reader.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/** 
 * 日期工具包
 * Package for dates
 * 
 * Author:     yuan(yuan.tops@gmail.com)
 * Created on: Apr 6, 2015 
 */
public class DateUtils {
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	/**
	 * Get current available issue's publish date
	 * @return string like "20150404"
	 */
	public static String currentPubdate() {
		Calendar cal = Calendar.getInstance();
		
		if (cal.getTime().getDay() != Calendar.SATURDAY) {
			cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
			cal.add(Calendar.DATE, -7);
		}		
		String pubdate = sdf.format(cal.getTime());
		return pubdate;
	}
	
	/**
	 * Get "count" past issues' pubdates 
	 * @param count: number of issues
	 * @return
	 */
	public static List<String> pastIssuePubdates(int count) {
		List<String> dates = new ArrayList<String>();
		Calendar cal = Calendar.getInstance();
		if (cal.getTime().getDay() != Calendar.SATURDAY) {
			cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
			cal.add(Calendar.DATE, -7);
		}
		
		for (int i = count; i > 0; i--) {
			dates.add(sdf.format(cal.getTime()));
			cal.add(Calendar.DATE, -7);
		}
		return dates;
	}
	
	/**
	 * Change date format from "20150405" to "2015-04-05"
	 * @param origin date format like "20140405"
	 * @return
	 */
	public static String formatPubdate(String origin) {
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		Date date = null;
		try {
			date = df.parse(origin);
		} catch (ParseException e) {
		}
		return sdf.format(date);
	} 
	
	public static void main(String[] args) {
		//DateUtils dateU = new DateUtils();
		System.out.println(DateUtils.currentPubdate());
		System.out.printf("passed %d issues: %n", 15);
		System.out.println(DateUtils.pastIssuePubdates(15).toString());
		System.out.println("test format pubdate");
		System.out.println(DateUtils.formatPubdate("20140404"));
	}
}

