package com.caorenhao.wbcrawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.logging.Log;

import com.caorenhao.util.LoggerConfig;
import com.caorenhao.util.Pair;
import com.caorenhao.wbcrawler.common.WBConst;
import com.caorenhao.wbcrawler.common.WBUserAgentEnum;

/**
 * HttpClient连接基类.
 *
 * @author renhao.cao.
 *         Created 2015-1-19.
 */
public abstract class WBHttpClientCommon {
	
	private Log LOGGER = LoggerConfig.getLog(getClass());
	
	/** httpclient连接类*/
	protected HttpClient httpclient = null;
	
	/** 初始化请求信息*/
	public WBHttpClientCommon() {
		initHttpClient();
	}
	
	/** 初始化httpclient*/
	protected void initHttpClient() {
		MultiThreadedHttpConnectionManager connectionManager = 
				new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams httpConnectionManagerParams = new HttpConnectionManagerParams();
		httpConnectionManagerParams.setMaxTotalConnections(10);
		httpConnectionManagerParams.setDefaultMaxConnectionsPerHost(10);
		httpConnectionManagerParams.setSoTimeout(WBConst.TIMEOUT_SO);
		httpConnectionManagerParams.setConnectionTimeout(WBConst.TIMEOUT_CONN);
		connectionManager.setParams(httpConnectionManagerParams);
		
		httpclient = new HttpClient(connectionManager);
		httpclient.getParams().setConnectionManagerTimeout(5 * 1000);
	}
	
	/**
	 * 获取httpclient.
	 *
	 * @return HttpClient
	 */
	protected HttpClient getHttpClient() {
		return httpclient;
	}
	
	/**
	 * 向http请求增加请求头.
	 *
	 * @param request http请求
	 * @param key http请求头的名称
	 * @param value http请求头的值
	 */
	protected void addHeader(HttpMethod request, String key, String value) {
		request.addRequestHeader(key, value);
	}
	
	/**
	 * 向http请求头增加cookie.
	 * 
	 * @param request
	 * @param cookie
	 */
	protected void addCookie(HttpMethod request, String cookie) {
		addHeader(request, "Cookie", cookie);
	}
	
	/**
	 * 设置http请求头的Cookie值.
	 * 
	 * @param request
	 * @param cookie
	 */
	protected void setCookie(HttpMethod request, String cookie) {
		request.setRequestHeader("Cookie", cookie);
	}
	
	/**
	 * 设置请求的header值
	 * 
	 * @param request http的get或者post请求
	 */
	protected void setHeader(HttpMethod request) {
		request.addRequestHeader("Accept", 
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		request.addRequestHeader("Accept-Language", "en-us,en;q=0.5");
		request.addRequestHeader("Connection", "keep-alive");
		request.addRequestHeader("User-Agent", WBUserAgentEnum.getRandomUserAgent());
	}
	
	/**
	 * 提交请求, 获取内容
	 * 
	 * @param request http的get或者post请求
	 * 返回为null时表示代理无法使用
	 */
	protected String executeMethod(HttpClient httpclient, HttpMethod request) {
		BufferedReader reader = null;
		InputStream in = null;
		StringBuffer sb = new StringBuffer();
		
		try {
			// 获取一个已验证的代理, 并用此代理发送请求
			WBHttpClientProxy wbProxy = new WBHttpClientProxy();
			Pair<String, Integer> proxy = wbProxy.getProxy();
			HostConfiguration hconf = new HostConfiguration();
			hconf.setProxy(proxy.first, proxy.second);
			httpclient.executeMethod(hconf, request);
			
			in = request.getResponseBodyAsStream();
	    	reader = new BufferedReader(new InputStreamReader(in, WBConst.CHARSET));
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (Exception e) {
			LOGGER.info(e.toString());
		} finally {
			try {
				if(reader != null)
					reader.close();
				if(in != null)
					in.close();
				request.releaseConnection();
			} catch (IOException exception) {
				LOGGER.info(exception.toString());
			}
		}
		
		return sb.toString();
	}
}