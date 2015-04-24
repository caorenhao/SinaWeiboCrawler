package com.caorenhao.wbcrawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.logging.Log;

import com.caorenhao.util.LoggerConfig;
import com.caorenhao.util.Pair;
import com.caorenhao.wbcrawler.common.WBConst;
import com.caorenhao.wbcrawler.common.WBUserAgentEnum;
import com.caorenhao.wbcrawler.common.WBVar;

/**
 * HttpClient连接基类.
 *
 * @author renhao.cao.
 *         Created 2015-1-19.
 */
public abstract class WBHttpClientCommon_ {
	
	private Log LOGGER = LoggerConfig.getLog(getClass());
	
	/** httpclient连接类*/
	protected HttpClient httpclient = null;
	
	/** 初始化请求信息*/
	public WBHttpClientCommon_() {
		initHttpClient();
	}
	
	/** 初始化httpclient*/
	protected void initHttpClient() {
		MultiThreadedHttpConnectionManager connectionManager = 
				new MultiThreadedHttpConnectionManager();
		httpclient = new HttpClient(connectionManager);
		httpclient.getHttpConnectionManager().getParams()
			.setConnectionTimeout(WBConst.TIMEOUT_CONN);
		httpclient.getHttpConnectionManager().getParams()
			.setSoTimeout(WBConst.TIMEOUT_SO);
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
		StringBuffer sb = new StringBuffer();
		Pair<String, Integer> proxy = WBVar.proxy;
		int num = 0;// 重试次数
		
		try {
			if(proxy == null)
				httpclient.executeMethod(request);
			else {
				// 如果代理可用则使用代理提交
				WBHttpClientVerify verify = new WBHttpClientVerify();
				boolean flag = verify.verifyProxy(proxy);
				if(flag) {
					HostConfiguration hconf = new HostConfiguration();
					hconf.setProxy(proxy.first, proxy.second);
					httpclient.executeMethod(hconf, request);
				} else {
					LOGGER.info("代理验证失败, 删除无法使用的代理");
					WBVar.proxy = null;
					
					// 代理验证失败, 不使用代理
					httpclient.executeMethod(request);
				}
			}
			
			InputStream in = request.getResponseBodyAsStream();
	    	reader = new BufferedReader(new InputStreamReader(in, WBConst.CHARSET));
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (Exception e) {
			LOGGER.info(e.toString());
			/*
			if(num < WBConst.NUM_RETRY) {
				LOGGER.warn("the request is failure, wait for " 
						+ (WBConst.TIMEWAIT_RELINK/1000)*2 + "s then retry");
				NetUtil.sleep(WBConst.TIMEWAIT_RELINK * 2);
				executeMethod(httpclient, request);
			}
			num++;
			*/
		} finally {
			try {
				if(reader != null) {
					reader.close();
				}
				request.releaseConnection();
			} catch (IOException exception) {
				LOGGER.info(exception.toString());
			}
		}
		
		return sb.toString();
	}
}