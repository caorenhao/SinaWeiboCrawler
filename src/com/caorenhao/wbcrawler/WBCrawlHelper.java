package com.caorenhao.wbcrawler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 辅助类.
 *
 * @author renhao.cao.
 *         Created 2015年4月10日.
 */
public class WBCrawlHelper {
	
	/**
	 * 根据微博类型 和 一个字符串 生成 主键.
	 *
	 * @param sourceType
	 * @param name
	 * @return String
	 */
	public static String generateId(int sourceType, String name) {
		return String.valueOf(sourceType) + "-" + name;
	}
	
	/**
	 * 抽取起始字符到结束字符之间的信息(不包含起始、结束字符).
	 *
	 * @param text 待匹配的文本
	 * @param str1 起始字符
	 * @param str2 结束字符
	 * @return 返回抽取信息
	 */
	public static String extract(String text, String str1, String str2) {
		if(text == null)
			return null;
		
	    String regex = str1 + "(.*?)" + str2;
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(text);
		String info = null;
		while (m.find()){
			info = m.group(1);
		}
		
		return info;
	}
}
