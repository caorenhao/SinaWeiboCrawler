package com.caorenhao.wbcrawler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;

import com.caorenhao.util.LoggerConfig;
import com.caorenhao.wbcrawler.common.WBConst;
import com.caorenhao.wbcrawler.common.WBSeniorSearchWeiboValue;
import com.caorenhao.wbcrawler.common.WBVar;

/**
 * 微博搜索接口,包括普通搜索和高级搜索.
 *
 * @author renhao.cao.
 *         Created 2014-12-19.
 */
public class WBHttpClientSearch extends WBHttpClientCommon {
	
	private Log LOGGER = LoggerConfig.getLog(getClass());
	
	/**
	 * 微博搜索.
	 *
	 * @param query
	 * @param cookie
	 * @return String 返回微博搜索结果首页的内容
	 * @throws Exception 
	 */
	public String searchBlog(String query, String cookie) throws Exception {
		HttpMethod postMethod = new PostMethod(WBConst.URL_SEARCH);
		setHeader(postMethod);
		setCookie(postMethod, cookie);
		addHeader(postMethod, "Referer", WBConst.URL_SEARCH_REFERER);
		
		//设置传递的参数
		NameValuePair[] valuePairs = new NameValuePair[2];
		query= java.net.URLEncoder.encode(query, WBConst.CHARSET);
		valuePairs[0] = new NameValuePair("keyword", query);
		valuePairs[1] = new NameValuePair("smblog", "搜微博");
		postMethod.setQueryString(valuePairs);
		
		HttpClient httpclient = getHttpClient();
		String content = executeMethod(httpclient, postMethod);
		/*
		int num = 0;
		while(content == null || content.isEmpty()) {
			num++;
			if(num < WBConst.NUM_RETRY) {
				LOGGER.warn("获取页面内容为空, 等待" + (WBConst.TIMEWAIT_RELINK/1000) 
						+ "s重新请求");
				NetUtil.sleep(WBConst.TIMEWAIT_RELINK);
				content = executeMethod(httpclient, postMethod);
			} else {
				return null;
			}
		}
		*/
		return content;
	}
	
	/**
	 * 微博高级搜索.
	 *
	 * @param query
	 * @param cookie
	 * @param startTime 开始时间
	 * @param endTime 结束时间
	 * @param sortType 排序方式
	 * @param weiboTypesAndUserTypes 至少需要有一个微博类型和一个用户类型
	 * @throws Exception 
	 */
	public void searchSeniorBlog(String query, String cookie, String startTime, 
			String endTime, String sortType, WBSeniorSearchWeiboValue... weiboTypesAndUserTypes) 
					throws Exception {
		HttpMethod postMethod = new PostMethod(WBConst.URL_SENIOR_SEARCH);
		setHeader(postMethod);
		setCookie(postMethod, cookie);
		addHeader(postMethod, "Referer", WBConst.URL_SENIOR_SEARCH_BLOG_REFERER);
		
		//设置传递的参数
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		query= java.net.URLEncoder.encode(query, WBConst.CHARSET);
		nvps.add(new NameValuePair("keyword", query));
		nvps.add(new NameValuePair("advancedfilter", "1"));
		for(WBSeniorSearchWeiboValue weiboType : weiboTypesAndUserTypes) {
			nvps.add(new NameValuePair(weiboType.name(), "1"));
		}
		nvps.add(new NameValuePair("nick", ""));
		nvps.add(new NameValuePair("starttime", startTime));
		nvps.add(new NameValuePair("endtime", endTime));
		nvps.add(new NameValuePair("sort", sortType));
		nvps.add(new NameValuePair("smblog", "搜索"));
		NameValuePair[] valuePairs = new NameValuePair[nvps.size()];
		int i = 0;
		for(NameValuePair nvp : nvps) {
			valuePairs[i] = nvp;
			i++;
		}
		postMethod.setQueryString(valuePairs);
		
		HttpClient httpclient = getHttpClient();
		String content = executeMethod(httpclient, postMethod);
	}
	
	/**
	 * 搜人.
	 * 
	 * @param searchAnalysis 页面解析方法类
	 * @param query 用户昵称
	 * @param cookie 
	 * @return String 所搜用户的首页链接
	 * @throws Exception 
	 */
	public String searchUser(WBSearchAnalysis searchAnalysis, String query, 
			String cookie) throws Exception {
		HttpMethod postMethod = new PostMethod(WBConst.URL_SEARCH);
		setHeader(postMethod);
		setCookie(postMethod, cookie);
		addHeader(postMethod, "Referer", WBConst.URL_SEARCH_REFERER);
		
		//设置传递的参数
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		query= java.net.URLEncoder.encode(query, WBConst.CHARSET);
		nvps.add(new NameValuePair("keyword", query));
		nvps.add(new NameValuePair("suser", "找人"));
		NameValuePair[] valuePairs = new NameValuePair[nvps.size()];
		int i = 0;
		for(NameValuePair nvp : nvps) {
			valuePairs[i] = nvp;
			i++;
		}
		postMethod.setQueryString(valuePairs);
		
		HttpClient httpclient = getHttpClient();
		String content = executeMethod(httpclient, postMethod);
		/*
		int num = 0;
		while(content == null || content.isEmpty()) {
			num++;
			if(num < WBConst.NUM_RETRY) {
				LOGGER.warn("获取页面内容为空, 等待" + (WBConst.TIMEWAIT_RELINK/1000) 
						+ "s重新请求");
				NetUtil.sleep(WBConst.TIMEWAIT_RELINK);
				content = executeMethod(httpclient, postMethod);
			} else {
				return null;
			}
		}
		*/
		String url = searchAnalysis.getUserUrl(content);
		
		return url;
	}
	
	/**
	 * 找人高级搜索.
	 *
	 * @param query 用户昵称
	 * @param cookie
	 * @param type 用户类型
	 * @param isv 是否认证
	 * @param gender 性别
	 * @param age 年龄
	 * @throws Exception 
	 */
	public void searchSeniorUser(String query, String cookie, String type, 
			String isv, String gender, String age) throws Exception {
		HttpMethod postMethod = new PostMethod(WBConst.URL_SENIOR_SEARCH);
		setHeader(postMethod);
		setCookie(postMethod, cookie);
		addHeader(postMethod, "Referer", WBConst.URL_SENIOR_SEARCH_USER_REFERER);
		
		//传递的参数
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		query= java.net.URLEncoder.encode(query, WBConst.CHARSET);
		nvps.add(new NameValuePair("keyword", query));
		nvps.add(new NameValuePair("advancedfilter", "1"));
		nvps.add(new NameValuePair("type", type));
		nvps.add(new NameValuePair("isv", isv));
		nvps.add(new NameValuePair("gender", gender));
		nvps.add(new NameValuePair("age", age));
		nvps.add(new NameValuePair("suser", "搜索"));
		NameValuePair[] valuePairs = new NameValuePair[nvps.size()];
		int i = 0;
		for(NameValuePair nvp : nvps) {
			valuePairs[i] = nvp;
			i++;
		}
		postMethod.setQueryString(valuePairs);
		
		HttpClient httpclient = getHttpClient();
		String content = executeMethod(httpclient, postMethod);
	}
	
	/**
	 * 标签搜索.
	 *
	 * @param query
	 * @param cookie
	 * @throws Exception 
	 */
	public void searchTag(String query, String cookie) throws Exception {
		HttpMethod postMethod = new PostMethod(WBConst.URL_SEARCH);
		setHeader(postMethod);
		setCookie(postMethod, cookie);
		addHeader(postMethod, "Referer", WBConst.URL_SEARCH_REFERER);
		
		//传递的参数
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		query= java.net.URLEncoder.encode(query, WBConst.CHARSET);
		nvps.add(new NameValuePair("keyword", query));
		nvps.add(new NameValuePair("stag", "搜标签"));
		NameValuePair[] valuePairs = new NameValuePair[nvps.size()];
		int i = 0;
		for(NameValuePair nvp : nvps) {
			valuePairs[i] = nvp;
			i++;
		}
		postMethod.setQueryString(valuePairs);
		
		HttpClient httpclient = getHttpClient();
		String content = executeMethod(httpclient, postMethod);
	}
	
	/**
	 * 发布微博.
	 *
	 * @param blog
	 * @param cookie
	 * @return String
	 * @throws Exception
	 */
	public String sendmblog(String blog, String cookie) throws Exception {
		HttpMethod postMethod = new PostMethod("http://weibo.cn/mblog/sendmblog?vt=4&st=402645");
		setHeader(postMethod);
		setCookie(postMethod, cookie);
		addHeader(postMethod, "Content-Type", "application/x-www-form-urlencoded");
		addHeader(postMethod, "Referer", "http://weibo.cn/5377008922/profile");
		
		//传递的参数
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		blog = java.net.URLEncoder.encode(blog, WBConst.CHARSET);
		nvps.add(new NameValuePair("rl", "1"));
		nvps.add(new NameValuePair("content", "2121"));
		NameValuePair[] valuePairs = new NameValuePair[nvps.size()];
		int i = 0;
		for(NameValuePair nvp : nvps) {
			valuePairs[i] = nvp;
			i++;
		}
		postMethod.setQueryString(valuePairs);
		
		HttpClient httpclient = getHttpClient();
		String content = executeMethod(httpclient, postMethod);
		
		return content;
	}
	
	/**
	 * TODO Put here a description of what this method does.
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		WBHttpClientSearch search = new WBHttpClientSearch();
		WBSearchAnalysis searchAnalysis = new WBSearchAnalysis();
		String cookie = "_T_WM=0d66c6246aa043225f57f8172aff5365; SUHB=0M2ERDa_OOACUz; SUB=_2A254AkmZDeTxGeNN7FUR8CbFyT6IHXVbDVfRrDV6PUJbrdANLUfhkW0ta11R2S_ZViN0sQBEGDdiM3DKVw..; gsid_CTandWM=4u2Kcf351N8znVLWhLATJmyNWeS; M_WEIBOCN_PARAMS=from%3Dhome";
		WBVar.savePath = "E:/temp/log.txt";
		
		//发布微博测试
		String content = "又到周一了,新的一周开始了";
		String ret = search.sendmblog(content, cookie);
		System.out.println(ret);
		
		/*
		//搜索用户测试
		String url = search.searchUser(searchAnalysis, "laperlee", cookie);
		WBGetUserInfo searchUser = new WBGetUserInfo();
		WBHttpClientGetPage wbGetPage = new WBHttpClientGetPage();
		String userPage = wbGetPage.getPage(url, cookie);
		WBUserModel userModel = new WBUserModel();
		userModel = searchUser.getUserInfo(wbGetPage, searchAnalysis, userPage, cookie);
		searchAnalysis.getUserAllBlog(url, userPage, cookie, userModel, 0, 5);
		*/
		/*
		String page = search.searchBlog("杭州联合银行", cookie);
		WBSearchAnalysis.getSearchResultAllBlog(page, cookie);
		*/
		//System.out.println(url);
		//search.searchTag("明星", cookie);
		//WBSeniorSearchWeiboValue[] type = {WBSeniorSearchWeiboValue.TYPE_ORIGINAL, WBSeniorSearchWeiboValue.USER_AUTH};
		//search.searchSeniorBlog("劳斯莱斯", cookie, "20141218", "20141219", "hot", type);
		//search.search("劳斯莱斯", cookie);
	}
}
