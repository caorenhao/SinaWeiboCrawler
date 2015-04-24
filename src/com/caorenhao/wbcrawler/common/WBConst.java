package com.caorenhao.wbcrawler.common;

/**
 * 微博抓取常量类.
 *
 * @author renhao.cao.
 *         Created 2014-12-2.
 */
public class WBConst {
	
	/** 新浪微博爬虫 类别配置文件目录名*/
	public static final String CONF_TYPE_WBCRAWLER = "wbcrawler";
	
	/** cookie失效的时间*/
	public static int TIMEOUT_COOKIE = 4 * 60 * 60 * 1000;
	
	/** 待用队列等待时间*/
	public static int TIMEWAIT_UNAVAILABLE = 6 * 60 * 60 * 1000;
	
	/** 访问失败重连等待时间*/
	public static int TIMEWAIT_RELINK = 6 * 1000;
	
	/** HttpClient连接等待超时时间*/
	public static int TIMEOUT_CONN = 45 * 1000;
	
	/** HttpClient读取数据超时时间*/
	public static int TIMEOUT_SO = 45 * 1000;
	
	/** 两次抓取的基础时间间隔*/
	public static int TIME_INTERVAL_BASE = 25 * 1000;
	
	/** 两次抓取的随机时间间隔*/
	public static int TIME_INTERVAL_RANDOM = 10 * 1000;
	
	/** 抓取程序运行线程数*/
	public static int NUM_WORKINGTHREADS = 3;
	
	/** 失败重试的次数*/
	public static int NUM_RETRY = 3;
	
	/** redis中可用账号队列*/
	public static String ACCOUNT_AVAILABLE_LIST = "wbcrawler_available_account";
	
	/** redis中待用账号队列 队列值为: 用户名,密码,进入待用队列的时间(13位long型)*/
	public static String ACCOUNT_UNAVAILABLE_LIST = "wbcrawler_unavailable_account";
	
	/** redis中的任务队列*/
	public static String WEIBO_TASK = "wbcrawler_task";
	
	/** redis中需要获取用户信息的任务队列, 优先级为最高*/
	public static String WEIBO_TASK_GETUSERINFO = "wbcrawler_task_getuserinfo";
	
	/** redis中的抓取统计队列*/
	public static String WEIBO_CRAWLER_HISTORY = "wbcrawler_history";
	
	/** 新浪微博移动端前缀*/
	public static String PREFIX_URL_CN = "http://weibo.cn";
	
	/** 微博链接web端前缀*/
	public static String PREFIX_URL_COM = "http://weibo.com";
	
	/** 页面编码*/
	public static String CHARSET = "utf-8";
	
	/** 字符串间隔*/
	public static String INTERVAL = "\t";
	
	/** 获取登录参数的url地址*/
	public static String URL_PARAMETERS = "http://login.weibo.cn/login/?backURL=http%3A%2F%2Fsina.cn&amp;backTitle=%CA%D6%BB%FA%D0%C2%C0%CB%CD%F8&amp;vt=4&amp;pt=1";
	
	/** 验证cookie是否可用的url*/
	public static String URL_VERIFY = "http://weibo.cn/renminwang";
	
	/** 验证代理是否可用的url*/
	public static String URL_PROXY_VERITY = "http://weibo.cn/renmingwang";
	
	/** 搜索请求url*/
	public static String URL_SEARCH = "http://weibo.cn/search/?pos=search";
	
	/** 高级搜索请求url*/
	public static String URL_SENIOR_SEARCH = "http://weibo.cn/search/";
	
	/** 搜索返回url*/
	public static String URL_SEARCH_REFERER = "http://weibo.cn/search/?tf=5_012";
	
	/** 高级搜索博客返回url*/
	public static String URL_SENIOR_SEARCH_BLOG_REFERER = "http://weibo.cn/search/mblog?advanced=mblog&f=s";
	
	/** 高级搜索用户返回url*/
	public static String URL_SENIOR_SEARCH_USER_REFERER = "http://weibo.cn/search/user/?keyword=&advanced=user&rl=1&f=s";
}
