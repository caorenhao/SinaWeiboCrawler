package com.caorenhao.wbcrawler.common;

import java.util.Random;

/**
 * HttpClient 头文件UserAgent的值.
 *
 * @author renhao.cao.
 *         Created 2015年2月5日.
 */
public enum WBUserAgentEnum {
	// Windows NT 6.2 = Win8
	// Windows NT 6.1 = Win7
	// Windows NT 6.0 = Vista
	
	// Chrome
	/** Chrome on Win7*/
	CHROME_WIN7("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30"),
	
	// Firefox
	/** Firefox on Win7*/
	FIREFOX("Mozilla/5.0 (Windows NT 6.1; rv:5.0) Gecko/20100101 Firefox/5.0"),
	
	// IE
	/** IE8 on Win7*/
	IE8_WIN7("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0)"),
	/** IE8 on Vista*/
	IE8_VISTA("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0)"),
	/** IE8 on Vista 兼容浏览*/
	IE8_VISTA_COMPATIBLE("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; Trident/4.0)"),
	/** 64-bit IE on 64-bit Windows 7*/
	IE8_WIN7_64("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Win64; x64; Trident/4.0)"),
	/** 32-bit IE on 64-bit Windows 7*/
	IE8_WIN7_32("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0)");
	
	private String display;
	
	private WBUserAgentEnum(String display) {
		this.display = display;
	}
	
	/**
	 * 获取Enum的内容.
	 *
	 * @return String
	 */
	public String getDisplay() {
		return display;
	}
	
	/**
	 * 获取一个随机UserAgent.
	 *
	 * @return String
	 */
	public static String getRandomUserAgent() {
		Random rand = new Random();
		WBUserAgentEnum[] agents = WBUserAgentEnum.values();
		int flag = rand.nextInt(agents.length);
		
		return agents[flag].getDisplay();
	}
}
