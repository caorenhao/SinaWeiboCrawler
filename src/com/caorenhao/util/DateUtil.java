package com.caorenhao.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间辅助类.
 *
 * @author renhao.cao.
 *         Created 2015-1-21.
 */
public class DateUtil {
	
	/**
	 * 获取今天的0点时间，如果获取失败返回0
	 * 
	 * @param nowDate Date类型时间
	 * @return long
	 */
	public static long getToDayTime(Date nowDate) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = dateFormat.format(nowDate);
		long dayTime = 0;
		try {
			Date dayDate = dateFormat.parse(dateStr);
			dayTime = dayDate.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dayTime;		
	}
	
	/**
	 * 将long型时间转为 yyyy-MM-dd 的String类型时间.
	 *
	 * @param time
	 * @return String
	 */
	public static String getDayTimeString(long time) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = dateFormat.format(new Date(time));
		return dateStr;
	}
	
	/**
	 * 获取当前时间long型
	 * 
	 * @return long
	 */
	public static long getCurTime() {
		return new Date().getTime();
	}
}
