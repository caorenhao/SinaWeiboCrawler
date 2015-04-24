package com.caorenhao.wbcrawler.client;

import org.apache.commons.logging.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caorenhao.util.JedisWrapper;
import com.caorenhao.util.LoggerConfig;
import com.caorenhao.util.NetUtil;
import com.caorenhao.util.Pair;
import com.caorenhao.wbcrawler.WBCookieMgr;
import com.caorenhao.wbcrawler.WBCrawlHelper;
import com.caorenhao.wbcrawler.WBGetUserInfo;
import com.caorenhao.wbcrawler.WBHttpClientGetPage;
import com.caorenhao.wbcrawler.WBHttpClientProxy;
import com.caorenhao.wbcrawler.WBHttpClientSearch;
import com.caorenhao.wbcrawler.WBSearchAnalysis;
import com.caorenhao.wbcrawler.common.WBAlgo;
import com.caorenhao.wbcrawler.common.WBConst;
import com.caorenhao.wbcrawler.common.WBTask;
import com.caorenhao.wbcrawler.common.WBVar;
import com.caorenhao.wbcrawler.models.SpecialWeibo;
import com.caorenhao.wbcrawler.models.WBCookieModel;
import com.caorenhao.wbcrawler.models.WBUserModel;
import com.caorenhao.workingthread.MultiThreadModel;
import com.caorenhao.workingthread.WorkingThread;
import com.google.code.morphia.Datastore;

/**
 * 具体处理线程.
 *
 * @author renhao.cao.
 *         Created 2015-1-21.
 */
public class WBCrawlThread extends WorkingThread<WBTask> {
	
	private JedisWrapper jedis;
	
	private WBCookieMgr wbCookieMgr;
	
	private WBSearchAnalysis wbSearchAnalysis;
	
	private WBGetUserInfo wbGetUserInfo;
	
	private WBHttpClientGetPage wbGetPage;
	
	private WBHttpClientSearch wbSearch;
	
	private WBHttpClientProxy wbProxy;
	
	private Datastore ds;
	
	private Log LOGGER = LoggerConfig.getLog(getClass());
	
	/**
	 * 获取线程id, redis等.
	 * 
	 * @param model
	 * @param id
	 */
	public WBCrawlThread(MultiThreadModel<WBTask> model, int id) {
		super(model, id);
		WBCrawlMultiThreadMain wbMgr = (WBCrawlMultiThreadMain)model;
		jedis = wbMgr.getJedisWrapper();
		wbCookieMgr = wbMgr.getWBCookieMgr();
		wbSearchAnalysis = wbMgr.getWBSearchAnalysis();
		wbGetUserInfo = wbMgr.getWBGetUserInfo();
		wbGetPage = wbMgr.getWBHttpClientGetPage();
		wbSearch = wbMgr.getSearch();
		wbProxy = wbMgr.getProxy();
	}
	
	@Override
	public void performTask(WBTask task) {
		// 获取任务参数
		JSONObject obj = JSON.parseObject(task.getJobs());
		String queryType = obj.getString("type");
		String queryUserInfo = obj.getString("userInfo");
		String queryPeople = obj.getString("people");
		String queryBlog = obj.getString("blog");
		int total = obj.getIntValue("total");
		
		// 获取代理和cookie
		String cookie = getProxyAndCookie();
		
		try {
			if(queryType.equals("userInfo")) {
				searchUserInfo(queryUserInfo, cookie);
			} else if(queryType.equals("people")) {
				searchPeople(queryPeople, cookie, total);
			} else if(queryType.equals("blog")){
				searchBlog(queryBlog, cookie, total);
			}
			
			WBAlgo.sleep();
		} catch(Exception e) {
			LOGGER.warn(e.getMessage());
		}
	}
	
	/**
	 * 检查内存中的cookie是否存在, 若不存在则使用一个新的账号获取cookie.
	 * 
	 * @return String
	 */
	private String getProxyAndCookie() {
		// 获取代理
		wbProxy.getProxy();
		
		// 获取cookie
		if(!WBVar.cookieMaps.containsKey(id)) {
			LOGGER.info("获取新的cookie");
			WBCookieModel cookieModel = null;
			while(cookieModel == null) {
				NetUtil.sleep(WBConst.TIMEWAIT_RELINK);
				wbProxy.getProxy();
				cookieModel = wbCookieMgr.getCookie(jedis);
			}
			
			WBVar.cookieMaps.put(id, cookieModel);
			String log = "当前账号情况:可用队列-" + jedis.llen(
					WBConst.ACCOUNT_AVAILABLE_LIST)
					+ ",待用队列-" + jedis.llen(WBConst.ACCOUNT_UNAVAILABLE_LIST);
			LOGGER.info(log);
			LOGGER.info("切换为账号 " + cookieModel.getUsername() + " 的cookie, " 
					+ cookieModel.getCookie());
		}
		
		String cookie = WBVar.cookieMaps.get(id).getCookie();
		LOGGER.info("获取线程" + id + "的cookie:" + cookie + ",线程池共有cookie" + 
				WBVar.cookieMaps.size() + "个");
		
		return cookie;
	}
	
	/**
	 * 抓取给定微博的作者相关信息,并更新入MongoDB.
	 * 
	 * @param queryUserInfo
	 * @param cookie
	 * @throws Exception 
	 */
	public void searchUserInfo(String queryUserInfo, String cookie) throws Exception {
		JSONObject json = JSONObject.parseObject(queryUserInfo);
		String userId = WBCrawlHelper.extract(json.getString("url"), "weibo.com/", "/");
		String url = "http://weibo.cn/u/" + userId;
		
		// 获取用户首页的内容
		String userPage = wbGetPage.getPage(url, cookie);
		
		if(userPage == null || userPage.isEmpty()) {
			JSONObject rootJson = new JSONObject();
			rootJson.put("type", "userInfo");
			rootJson.put("userInfo", queryUserInfo);
			jedis.lpush(WBConst.WEIBO_TASK_GETUSERINFO, rootJson.toJSONString());
			LOGGER.info("抓取用户信息失败, 重新放入任务队列中");
		} else {
			WBUserModel userModel = new WBUserModel();
			userModel = wbGetUserInfo.getUserInfo(wbGetPage, url, userPage, cookie);
			
			SpecialWeibo weibo = new SpecialWeibo();
			weibo.setMid(json.getString("mid"));
			weibo.setUrl(json.getString("url"));
			weibo.setSourceType(1);
			weibo.setUserId("1-" + userModel.getUid());
			weibo.setUserName(json.getString("userName"));
			weibo.setUserLocation(userModel.getLocation());
			weibo.setUserfollowersCount(userModel.getFans());
			weibo.setTime(json.getLongValue("time"));
			weibo.setText(json.getString("text"));
			weibo.setRepostsCount(json.getIntValue("repostsCount"));
			weibo.setCommentsCount(json.getIntValue("commentsCount"));
			weibo.setNegative(json.getBooleanValue("isNegative"));
			weibo.setOfficalReply(json.getIntValue("officalReply"));
			weibo.setQualitiy(json.getIntValue("qualitiy"));
			weibo.setIsNeedCrawler(json.getIntValue("isNeedCrawler"));
			weibo.setTryCount(json.getIntValue("tryCount"));
			long crawlTime = System.currentTimeMillis();
			weibo.setCrawlTime(crawlTime);
			
			LOGGER.info(WBAlgo.toXlsLine(weibo.getMid(), weibo.getUrl(), 
					String.valueOf(weibo.getSourceType()), weibo.getUserId(), 
					weibo.getUserName(), weibo.getUserLocation(),
					String.valueOf(weibo.getUserfollowersCount()), 
					String.valueOf(weibo.getTime()), weibo.getText(),
					String.valueOf(weibo.getRepostsCount()), 
					String.valueOf(weibo.getCommentsCount()), 
					String.valueOf(weibo.isNegative()),
					String.valueOf(weibo.getOfficalReply()), 
					String.valueOf(weibo.getQualitiy()), 
					String.valueOf(weibo.getIsNeedCrawler()), 
					String.valueOf(weibo.getTryCount()), 
					String.valueOf(weibo.getCrawlTime())));
			
			// 将数据更新进MongoDB
			ds.save(weibo);
			WBAlgo.Statistics(jedis);
		}
		
		// 等待，避免被微博封号
		WBAlgo.sleep();
	}
	
	/**
	 * TODO Put here a description of what this method does.
	 *
	 * @param queryPeople
	 * @param cookie
	 * @param total
	 * @throws Exception
	 */
	private void searchPeople(String queryPeople, String cookie, int total) 
			throws Exception {
		// 处理具体用户微博抓取
		// 1.获取用户首页链接
		String url = wbSearch.searchUser(wbSearchAnalysis, queryPeople, cookie);
		// 2.获取用户详细信息
		WBUserModel userModel = searchPeopleUserModelAchieve(url, cookie);
		if(userModel != null) {
			// 3.抓取用户第一页的微博
			Pair<String, Integer> pair = searchPeopleAchieve(url, cookie, 
					userModel, 0, total);
			
			while(pair != null) {
				// 4.抓取等待,避免被新浪封IP
				WBAlgo.sleep();
				cookie = getProxyAndCookie();
				String nextUrl = pair.first;
				int number = pair.second;
				
				// 5.抓取用户的下一页微博
				pair = searchPeopleAchieve(nextUrl, cookie, userModel, number, 
						total);
			}
		} else {
			// FIXME 此时为代理不可用出的错
			WBVar.proxy = null;
			LOGGER.info("代理不可用, 放弃本次抓取操作");
		}
	}
	
	/**
	 * TODO Put here a description of what this method does.
	 *
	 * @param queryBlog
	 * @param cookie
	 * @param total
	 * @throws Exception
	 */
	private void searchBlog(String queryBlog, String cookie, int total) 
			throws Exception {
		// 处理微博搜索抓取
		// 1.获取搜索首页内容
		String page = wbSearch.searchBlog(queryBlog, cookie);
		
		if(page != null) {
			// 2.抓取搜索首页微博
			Pair<String, Integer> pair = WBSearchAnalysis.getSearchResultAllBlog(
					wbGetUserInfo, wbGetPage, queryBlog, page, cookie, 0, total, jedis);
			
			while(pair != null) {
				// 3.抓取等待,避免被新浪封IP
				WBAlgo.sleep();
				cookie = getProxyAndCookie();
				String nextUrl = pair.first;
				int number = pair.second;
				
				// 4.抓取下一页的微博
				pair = searchBlogAchieve(queryBlog, nextUrl, cookie, number, total);
				
				if(pair != null) {
					int repostNum = 0;
					while(pair != null && pair.second == -1 && repostNum < 
							WBConst.NUM_RETRY) {
						repostNum++;
						LOGGER.warn("获取搜索微博列表页"+nextUrl+"失败, 等待" 
								+ (WBConst.TIMEWAIT_RELINK/1000) + "s重新请求");
						NetUtil.sleep(WBConst.TIMEWAIT_RELINK);
						pair = searchBlogAchieve(queryBlog, nextUrl, cookie, 
								number, total);
					}
					
					if(repostNum >= WBConst.NUM_RETRY) {
						LOGGER.info("获取搜索微博列表页失败" + WBConst.NUM_RETRY 
								+ "次, 放弃此次任务, 列表页:" + nextUrl);
						pair = null;
					}
				}
			}
		} else {
			// 无法获取到页面信息
			LOGGER.info("无法获取到页面信息, 放弃本次抓取操作");
		}
	}
	
	/**
	 * 抓取用户详细信息的具体实现流程.
	 *
	 * @param url
	 * @param cookie
	 * @param start 
	 * @param total
	 * @return Pair<String, Integer> <下页链接, 当前抓取的数量>, 若为null则表示抓取结束
	 * @throws Exception
	 */
	private WBUserModel searchPeopleUserModelAchieve(String url, String cookie) 
			throws Exception {
		if(url == null)
			return null;
		
		String userPage = wbGetPage.getPage(url, cookie);
		
		WBUserModel userModel = new WBUserModel();
		if(userPage != null) {
			userModel = wbGetUserInfo.getUserInfo(wbGetPage, url, userPage, cookie);
		} else {
			return null;
		}
		
		return userModel;
	}
	
	/**
	 * 抓取用户微博的具体实现流程.
	 * 
	 * @param url 访问的url链接
	 * @param cookie 使用的cookie
	 * @param userModel 用户信息
	 * @param start 当前已抓取数量
	 * @param total 需要抓取的总数
	 * @return Pair<String, Integer> <下页链接, 当前抓取的数量>, 若为null则表示抓取结束
	 * @throws Exception
	 */
	private Pair<String, Integer> searchPeopleAchieve(String url, String cookie, 
			WBUserModel userModel, int start, int total) throws Exception {
		LOGGER.info("抓取\t" + url);
		
		String page = wbGetPage.getPage(url, cookie);
		
		Pair<String, Integer> pair = new Pair<String, Integer>();
		if(page != null) {
			pair = WBSearchAnalysis.getUserAllBlog(url, page, 
					cookie, userModel, start, total);
			
			//如果本页没有微博内容, 且不是最后一页, 则手动抽取下一页的链接
			if(pair != null && pair.second == -1) {
				if(page.contains("下页")) {
					String nextPage = WBCrawlHelper.extract(page, 
							"method=\"post\"><div><a href=\"", "\">下页");
					String nextpageUrl = WBConst.PREFIX_URL_CN + nextPage;
					
					pair = new Pair<String, Integer>(nextpageUrl, start);
				} else {
					// 当前列表页获取失败, 重新获取
					while(pair != null && pair.second == -1) {
						LOGGER.warn("获取用户微博列表页失败, 等待" 
								+ (WBConst.TIMEWAIT_RELINK/1000) + "s重新请求");
						NetUtil.sleep(WBConst.TIMEWAIT_RELINK);
						pair = searchPeopleAchieve(url, cookie, userModel, start, 
								total);
					}
				}
			}
		} else {
			// FIXME 获取不到搜索页面的处理
			return null;
		}
		
		return pair;
	}
	
	/**
	 * 抓取微博搜索的具体实现.
	 * 
	 * @param queryWord 搜索词
	 * @param url 
	 * @param cookie
	 * @param start
	 * @param total
	 * @return Pair<String, Integer>
	 * @throws Exception
	 */
	private Pair<String, Integer> searchBlogAchieve(String queryWord, String url, 
			String cookie, int start, int total) throws Exception {
		String page = wbGetPage.getPage(url, cookie);
		
		Pair<String, Integer> pair = new Pair<String, Integer>();
		if(page != null) {
			pair = WBSearchAnalysis.getSearchResultAllBlog(wbGetUserInfo, wbGetPage, 
					queryWord, page, cookie, start, total, jedis);
		} else {
			// FIXME 获取不到的处理
			return null;
		}
		
		return pair;
	}
	
}
