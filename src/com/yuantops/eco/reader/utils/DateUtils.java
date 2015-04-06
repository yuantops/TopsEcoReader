package com.yuantops.eco.reader.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/** 
 * 日期工具包
 * Package for dates
 * 
 * Author:     yuan(yuan.tops@gmail.com)
 * Created on: Apr 6, 2015 
 */
public class DateUtils {
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	
	public static String currentPubdate() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		String pubdate = sdf.format(cal);
		System.out.println("Current publish date: " + pubdate);
		return pubdate;
	}
}
