package com.caorenhao.wbcrawler;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;

import com.caorenhao.util.LoggerConfig;

/**
 * 获取页面内容.
 *
 * @author renhao.cao.
 *         Created 2015年3月4日.
 */
public class WBHttpClientGetPage extends WBHttpClientCommon {
	
	private Log LOGGER = LoggerConfig.getLog(getClass());
	
	/**
	 * 获取页面内容.
	 * 
	 * @param url 
	 * @param cookie 
	 * @return String 
	 * @throws Exception 
	 */
	public String getPage(String url, String cookie) {
		if(url == null || url.isEmpty())
			return null;
		
		HttpClient httpclient = getHttpClient();
		HttpMethod getMethod = new GetMethod(url);
		setHeader(getMethod);
		setCookie(getMethod, cookie);
		
		//LOGGER.info(url);
		String page = null;
		try {
			page = executeMethod(httpclient, getMethod);
		} catch(Exception e) {
			LOGGER.warn(e.toString());
		}
		
		return page;
	}
	
}
