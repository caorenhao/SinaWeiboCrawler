package com.caorenhao.wbcrawler.common;

import java.util.HashMap;
import java.util.Map;

import com.caorenhao.util.Pair;
import com.caorenhao.wbcrawler.models.WBCookieModel;

/**
 * TODO Put here a description of what this class does.
 *
 * @author renhao.cao.
 *         Created 2015-1-8.
 */
public class WBVar {
	
	/** 多线程所用的cookie池<线程号, cookieModel>*/
	public static Map<Integer, WBCookieModel> cookieMaps = new HashMap<Integer, WBCookieModel>();
	
	/** 多线程所用的代理*/
	public static Pair<String, Integer> proxy = null;
	
	/** 抓取日志保存路径*/
	public static String savePath;
	
	/** 本机的IP地址*/
	public static String IP;
	
}
