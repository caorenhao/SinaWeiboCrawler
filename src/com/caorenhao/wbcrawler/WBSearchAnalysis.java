package com.caorenhao.wbcrawler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.caorenhao.util.IOUtil;
import com.caorenhao.util.JedisWrapper;
import com.caorenhao.util.LoggerConfig;
import com.caorenhao.util.Pair;
import com.caorenhao.vcrawler.vextractor.CssSelectorTplFactory;
import com.caorenhao.vcrawler.vextractor.CssTemplateException;
import com.caorenhao.wbcrawler.common.WBAlgo;
import com.caorenhao.wbcrawler.common.WBConst;
import com.caorenhao.wbcrawler.common.WBVar;
import com.caorenhao.wbcrawler.models.WBBlogModel;
import com.caorenhao.wbcrawler.models.WBUserModel;

/**
 * 微博页面的解析类.
 * 
 * @author renhao.cao.
 *         Created 2015-1-12.
 */
public class WBSearchAnalysis {
	
	private static Log LOGGER = LoggerConfig.getLog(WBSearchAnalysis.class);
	
	private static String url_tpl = "http://weibo.cn/caorenhao0213?f=search_0";
	
	/**
	 * 在搜人结果中抽取出所搜用户的链接.
	 * 
	 * @param content
	 * @return Strings null表示代理不可用没有获取到内容
	 * @throws Exception 
	 */
	public String getUserUrl(String content) {
		if(content == null)
			return null;
		
		String url = "";
		HtmlCleaner cleaner = new HtmlCleaner();
		
		try {
			TagNode tagNode = cleaner.clean(content);
			Object[] objs = tagNode.evaluateXPath(
					"/body/table[1]/tbody/tr/td[2]/a[1]");
			for(Object obj : objs) {
				TagNode nodeChild = (TagNode) obj;
		    	url = nodeChild.getAttributeByName("href");
		    	url = WBConst.PREFIX_URL_CN + url;
			}
		} catch (XPatherException exception) {
			LOGGER.warn(exception.toString());
		}
		
		return url;
	}
	
	/**
	 * 在用户信息页面抽取出用户资料.
	 * 
	 * @param content 
	 * @param userModel 
	 * @return WBUserModel
	 */
	public static WBUserModel getUserDeatil(String content, WBUserModel userModel) {
		if(content == null)
			return null;
		
		LOGGER.info(content);
		
		//获取头像图片地址
		String imgUrl = WBCrawlHelper.extract(content, "src=\"", "\" alt=\"头像\"") == 
				null ? "" : WBCrawlHelper.extract(content, "src=\"", "\" alt=\"头像\"");
		userModel.setImgUrl(imgUrl != null ? imgUrl : "");
		
		//获取基本信息
		userModel.setNick(WBCrawlHelper.extract(content, "昵称:", "<br"));
		String gender = WBCrawlHelper.extract(content, "性别:", "<br") == null 
				? "" : WBCrawlHelper.extract(content, "性别:", "<br");
		String location = WBCrawlHelper.extract(content, "地区:", "<br") == null 
				? "" : WBCrawlHelper.extract(content, "地区:", "<br");
		String birthday = WBCrawlHelper.extract(content, "生日:", "<br") == null 
				? "" : WBCrawlHelper.extract(content, "生日:", "<br");
		userModel.setGender(gender);
		userModel.setLocation(location);
		userModel.setBirthday(birthday);
		
		return userModel;
	}
	
	/**
	 * 在用户首页抽取出用户uid和用户详情url.
	 * 
	 * @param url 
	 * @param content
	 * @param cookie
	 * @param userModel
	 * @return WBUserModel
	 * @throws Exception
	 */
	public static List<String> getUidAndInfo(String url, String content) 
			throws Exception {
		List<String> list = new ArrayList<String>();
		
		JSONObject obj = CssSelectorTplFactory.parse(url, content, true, false);
		
		//获取微博作者的uid和个人资料
		String uidJson = obj.getString("uid");
		uidJson = WBAlgo.textFormat(uidJson);
		String uid = uidJson.split("/")[1];
		String infoUrl = WBConst.PREFIX_URL_CN + uidJson;
		list.add(uid);
		list.add(infoUrl);
		
		return list;
	}
	
	/**
	 * 抓取用户的所有微博.
	 * 
	 * @param url 
	 * @param searchAnalysis 
	 * @param content 用户首页内容
	 * @param cookie 
	 * @param userModel 用户信息模型
	 * @param start 抓取的开始数目
	 * @param end 抓取的结束数目
	 * @return Pair<下页链接, 当前抓取的条数>
	 * @throws Exception 
	 */
	public static Pair<String, Integer> getUserAllBlog(String url, String content, 
			String cookie, WBUserModel userModel, int start, int end) throws Exception {
		JSONObject obj = null;
		try {
			obj = CssSelectorTplFactory.parse(url, content, true, false);
		} catch(CssTemplateException ex) {
			return new Pair<String, Integer>("", -1);
		}
		
		LOGGER.info(obj.toJSONString());
		
		//获取微博作者
		String authorJson = obj.getString("author");
		String author = null;
		if(authorJson!=null && !authorJson.isEmpty()) {
			author = WBAlgo.textFormat(authorJson).split("  ")[0];
		}
		
		//保存微博
		JSONArray urlArray = obj.getJSONArray("urls");
		JSONObject urlsObj = urlArray.getJSONObject(0);
		JSONArray urlsArray = urlsObj.getJSONArray("url");
		for (int i = 0; i < urlsArray.size() && start < end; i++) {
			start++;
			
			JSONObject blog = urlsArray.getJSONObject(i);
			WBBlogModel blogModel = new WBBlogModel();
			//保存微博作者昵称
			blogModel.setAuthor(author);
			
			//保存微博url
			String id = blog.getString("id");
			id = WBAlgo.textFormat(id);
			String blogId = id.replaceAll("M_", "");
			blogModel.setUrl(WBConst.PREFIX_URL_COM + "/" + userModel.getUid() + "/" + blogId);
			
			// 获取微博博文及转发原文
			String title1 = blog.getString("title") == null ? "" : blog.getString("title");
			String title2 = blog.getString("title2") == null ? "" : blog.getString("title2");
			String title3 = blog.getString("title3") == null ? "" : blog.getString("title3");
			if(title2.contains("转发理由:") && title2.contains("  赞["))
				title2 = WBCrawlHelper.extract(title2, "转发理由:", "  赞\\[");
			if(title3.contains("转发理由:") && title3.contains("  赞["))
				title3 = WBCrawlHelper.extract(title3, "转发理由:", "  赞\\[");
			title1 = WBAlgo.jsonFormat(title1).trim();
			if(title1.startsWith(":"))
				title1 = title1.substring(1);
			title2 = WBAlgo.jsonFormat(title2).trim();
			title3 = WBAlgo.jsonFormat(title3).trim();
			// 获取微博是否存在转发原文作者
			String repostAuthor = blog.getString("repostAuthor");
			if(repostAuthor != null) {
				blogModel.setRepostAuthor(WBAlgo.jsonFormat(repostAuthor));
			}
			// 获取若存在两行内容时该条微博是否是转发微博
			String isRepost = blog.getString("isRepost");
			// 保存微博内容与转发内容
			if(title3 == null || title3.isEmpty()) {
				// 此处有两种情况:作者转发了一条没有图片的微博;作者自己发了一条包含图片的微博
				if(title2 == null || title2.isEmpty() || 
						(!title2.isEmpty() && isRepost==null)) {
					blogModel.setContent(title1);
					blogModel.setRepostContent(null);
				} else {
					blogModel.setContent(title2);
					blogModel.setRepostContent(title1);
				}
			} else {
				blogModel.setContent(title3);
				blogModel.setRepostContent(title1);
			}
			
			// 保存微博时间
			String time = blog.getString("time");
			time = WBAlgo.textFormat(time);
			if(time != null)
				time = time.split("来")[0];
			time = WBAlgo.timeFormat(time);
			blogModel.setTime(time);
			
			// 保存赞、评论、转发的数量
			String statistics1 = blog.getString("statistics1");
			String statistics2 = blog.getString("statistics2");
			String statistics3 = blog.getString("statistics3");
			String statistics = null;
			if(statistics3 != null)
				statistics = statistics3;
			else {
				if(statistics2 != null)
					statistics = statistics2;
				else
					statistics = statistics1;
			}
			blogModel = WBAlgo.getStatisticsResult(statistics, blogModel, ",");
			
			LOGGER.info(WBAlgo.toXlsLine(userModel.getNick(), blogModel.getTime(), 
					String.valueOf(blogModel.getAttitudeNum()), 
					String.valueOf(blogModel.getCommentNum()), 
					String.valueOf(blogModel.getRepostNum()), blogModel.getUrl(), 
					blogModel.getContent(), blogModel.getRepostAuthor(), 
					blogModel.getRepostContent()));
		}
		
		if(start < end) {
			//获取下页链接
			String nextPageText = obj.getString("nextPageText");
			nextPageText = WBAlgo.textFormat(nextPageText);
			if("下页".equals(nextPageText)) {
				String nextPage = obj.getString("nextpage");
				nextPage = WBAlgo.textFormat(nextPage);
				String nextpageUrl = WBConst.PREFIX_URL_CN + nextPage;
				
				return new Pair<String, Integer>(nextpageUrl, start);
			}
		}
		
		LOGGER.info("抓取结束");
		return null;
	}
	
	/**
	 * 获取搜索微博结果页面的所有结果.
	 * 
	 * @param getUserInfo 
	 * @param getPage 
	 * @param queryWord 搜索词
	 * @param content 搜索微博的页面内容
	 * @param cookie cookie
	 * @param start 抓取的开始数目
	 * @param end 抓取的结束数目
	 * @param jedis 
	 * @return Pair<下页链接, 当前抓取的条数> 抓取条数为-1:无法抽取当前页面的信息
	 * @throws Exception 
	 */
	public static Pair<String, Integer> getSearchResultAllBlog(WBGetUserInfo getUserInfo, 
			WBHttpClientGetPage getPage, String queryWord, String content, 
			String cookie, int start, int end, JedisWrapper jedis) throws Exception {
		LOGGER.info(content);
		
		JSONObject obj = null;
		try {
			obj = CssSelectorTplFactory.parse(url_tpl, content, true, false);
		} catch(CssTemplateException ex) {
			return new Pair<String, Integer>("", -1);
		}
		
		if(obj == null) {
			LOGGER.info(content);
		} else {
			LOGGER.info(obj.toJSONString());
			
			//保存微博
			JSONArray urlArray = obj.getJSONArray("urls");
			JSONObject urlsObj = urlArray.getJSONObject(0);
			JSONArray urlsArray = urlsObj.getJSONArray("url");
			for (int i = 0; i < urlsArray.size() && start < end; i++) {
				start++;
				
				JSONObject blog = urlsArray.getJSONObject(i);
				WBBlogModel blogModel = new WBBlogModel();
				// 保存微博作者昵称
				String author = blog.getString("au");
				author = WBAlgo.jsonFormat(author);
				blogModel.setAuthor(author);
				
				// 保存微博作者主页链接
				String authorUrl = blog.getString("auUrl");
				authorUrl = WBAlgo.jsonFormat(authorUrl);
				blogModel.setAuthorUrl(authorUrl);
				//String userPage = getPage.getPage(authorUrl, cookie);
				WBUserModel userModel = new WBUserModel();
				//userModel = getUserInfo.getUserInfo(getPage, url_tpl, 
				//		userPage, cookie);
				
				// 获取uid
				String uid = blog.getString("uid3");
				if(uid == null) {
					uid = blog.getString("uid2");
					if(uid == null)
						uid = blog.getString("uid1");
				}
				uid = WBAlgo.textFormat(uid);
				uid = WBCrawlHelper.extract(uid, "uid=", "&rl");
				blogModel.setUid(Long.parseLong(uid));
				
				//保存微博url
				String id = blog.getString("id");
				id = WBAlgo.textFormat(id);
				String blogId = id.replaceAll("M_", "");
				blogModel.setUrl(WBConst.PREFIX_URL_COM + "/" + uid + "/" + blogId);
				
				// 获取微博内容
				String title1 = blog.getString("title") == null ? "" : blog.getString("title");
				String title2 = blog.getString("title2") == null ? "" : blog.getString("title2");
				String title3 = blog.getString("title3") == null ? "" : blog.getString("title3");
				if(title2.contains("转发理由:") && title2.contains("  赞["))
					title2 = WBCrawlHelper.extract(title2, "转发理由:", "  赞\\[");
				if(title3.contains("转发理由:") && title3.contains("  赞["))
					title3 = WBCrawlHelper.extract(title3, "转发理由:", "  赞\\[");
				title1 = WBAlgo.jsonFormat(title1).trim();
				if(title1.startsWith(":"))
					title1 = title1.substring(1);
				title2 = WBAlgo.jsonFormat(title2).trim();
				title3 = WBAlgo.jsonFormat(title3).trim();
				// 获取微博是否存在转发原文作者及原文链接
				String repostAuthor = blog.getString("repostAuthor");
				String repostUrl = blog.getString("repostUrl");
				//System.out.println(blogModel.getContent());
				if(repostAuthor != null) {
					blogModel.setRepostAuthor(WBAlgo.jsonFormat(repostAuthor));
					blogModel.setRepostUrl(repostUrl);
				}
				// 获取若存在两行内容时该条微博是否是转发微博
				String isRepost = blog.getString("isRepost");
				// 保存微博内容与转发内容
				if(title3 == null || title3.isEmpty()) {
					// 此处有两种情况:作者转发了一条没有图片的微博;作者自己发了一条包含图片的微博
					if(title2 == null || title2.isEmpty() || 
							(!title2.isEmpty() && isRepost==null)) {
						blogModel.setContent(title1);
						blogModel.setRepostContent(null);
					} else {
						blogModel.setContent(title2);
						blogModel.setRepostContent(title1);
					}
				} else {
					blogModel.setContent(title3);
					blogModel.setRepostContent(title1);
				}
				
				//保存微博时间
				String time = blog.getString("time");
				time = WBAlgo.textFormat(time);
				if(time != null)
					time = time.split("来")[0];
				time = WBAlgo.timeFormat(time);
				blogModel.setTime(time);
				
				// 保存赞、评论、转发的数量
				String statistics1 = blog.getString("statistics1");
				String statistics2 = blog.getString("statistics2");
				String statistics3 = blog.getString("statistics3");
				String statistics = null;
				if(statistics3 != null)
					statistics = statistics3;
				else {
					if(statistics2 != null)
						statistics = statistics2;
					else
						statistics = statistics1;
				}
				blogModel = WBAlgo.getStatisticsResult(statistics, blogModel, ",");
				
				String proxy = WBVar.proxy == null ? "" : WBVar.proxy.toString();
				
				LOGGER.info(WBAlgo.toXlsLine(queryWord, proxy, blogModel.getAuthor(),
						userModel.getLocation(), String.valueOf(userModel.getFans()), 
						uid, blogModel.getTime(), String.valueOf(blogModel.getAttitudeNum()), 
						String.valueOf(blogModel.getCommentNum()), 
						String.valueOf(blogModel.getRepostNum()), 
						blogModel.getUrl(), blogModel.getContent(), 
						blogModel.getRepostAuthor(), 
						blogModel.getRepostContent()));
				
				// 添加进主流程,并保存抓取数据
				WBAlgo.Statistics(jedis);
			}
			
			if(start < end) {
				//获取下页链接
				String nextPageText = obj.getString("nextPageText");
				nextPageText = WBAlgo.textFormat(nextPageText);
				if("下页".equals(nextPageText)) {
					String nextPage = obj.getString("nextpage");
					nextPage = WBAlgo.textFormat(nextPage);
					String nextpageUrl = WBConst.PREFIX_URL_CN + nextPage;
					
					return new Pair<String, Integer>(nextpageUrl, start);
				}
			}
		}
		
		LOGGER.info("抓取结束");
		return null;
	}
	
	/**
	 * 获取搜索微博结果页面的所有结果.
	 * 
	 * @param queryWord 搜索词
	 * @param content 搜索微博的页面内容
	 * @param cookie cookie
	 * @param start 抓取的开始数目
	 * @param end 抓取的结束数目
	 * @return Pair<下页链接, 当前抓取的条数> 抓取条数为-1:无法抽取当前页面的信息
	 * @throws Exception 
	 */
	public static Pair<String, Integer> getSingleBlog(String queryWord, 
			String content, String cookie) throws Exception {
		LOGGER.info(content);
		
		JSONObject obj = null;
		try {
			obj = CssSelectorTplFactory.parse(url_tpl, content, false, true);
		} catch(CssTemplateException ex) {
			return new Pair<String, Integer>("", -1);
		}
		
		if(obj == null) {
			LOGGER.info(content);
		} else {
			LOGGER.info(obj.toJSONString());
			
			//保存微博
			JSONObject blog = obj;
			WBBlogModel blogModel = new WBBlogModel();
			//保存微博作者昵称
			String author = blog.getString("au");
			author = WBAlgo.jsonFormat(author);
			blogModel.setAuthor(author);
			
			//获取uid
			String uid = blog.getString("uid3");
			if(uid == null) {
				uid = blog.getString("uid2");
				if(uid == null)
					uid = blog.getString("uid1");
			}
			uid = WBAlgo.textFormat(uid);
			uid = uid.substring(3);
			
			//保存微博url
			String id = blog.getString("blogId");
			String blogId = WBCrawlHelper.extract(id, "\\/comment\\/", "\\?");
			blogModel.setUrl(WBConst.PREFIX_URL_COM + "/" + uid + "/" + blogId);
			
			// 获取微博内容
			String title1 = blog.getString("title") == null ? "" : blog.getString("title");
			String title2 = blog.getString("title2") == null ? "" : blog.getString("title2");
			String title3 = blog.getString("title3") == null ? "" : blog.getString("title3");
			if(title2.contains("转发理由:") && title2.contains("  赞["))
				title2 = WBCrawlHelper.extract(title2, "转发理由:", "  赞\\[");
			if(title3.contains("转发理由:") && title3.contains("  赞["))
				title3 = WBCrawlHelper.extract(title3, "转发理由:", "  赞\\[");
			title1 = WBAlgo.jsonFormat(title1).trim();
			if(title1.startsWith(":"))
				title1 = title1.substring(1);
			title2 = WBAlgo.jsonFormat(title2).trim();
			title3 = WBAlgo.jsonFormat(title3).trim();
			// 获取微博是否存在转发原文作者及原文链接
			String repostAuthor = blog.getString("repostAuthor");
			String repostUrl = blog.getString("repostUrl");
			//System.out.println(blogModel.getContent());
			if(repostAuthor != null) {
				blogModel.setRepostAuthor(WBAlgo.jsonFormat(repostAuthor));
				blogModel.setRepostUrl(repostUrl);
			}
			// 获取若存在两行内容时该条微博是否是转发微博
			String isRepost = blog.getString("isRepost");
			// 保存微博内容与转发内容
			if(title3 == null || title3.isEmpty()) {
				// 此处有两种情况:作者转发了一条没有图片的微博;作者自己发了一条包含图片的微博
				if(title2 == null || title2.isEmpty() || 
						(!title2.isEmpty() && isRepost==null)) {
					blogModel.setContent(title1);
					blogModel.setRepostContent(null);
				} else {
					blogModel.setContent(title2);
					blogModel.setRepostContent(title1);
				}
			} else {
				blogModel.setContent(title3);
				blogModel.setRepostContent(title1);
			}
			
			//保存微博时间
			String time = blog.getString("time");
			time = WBAlgo.textFormat(time);
			if(time != null)
				time = time.split("来")[0];
			time = WBAlgo.timeFormat(time);
			blogModel.setTime(time);
			
			// 保存赞、评论、转发的数量
			String statistics1 = blog.getString("statistics1");
			String statistics2 = blog.getString("statistics2");
			String statistics3 = blog.getString("statistics3");
			String statistics = null;
			if(statistics3 != null)
				statistics = statistics3;
			else {
				if(statistics2 != null)
					statistics = statistics2;
				else
					statistics = statistics1;
			}
			blogModel = WBAlgo.getStatisticsResult(statistics, blogModel, " ");
			
			String proxy = WBVar.proxy == null ? "" : WBVar.proxy.toString();
			LOGGER.info(WBAlgo.toXlsLine(queryWord, proxy, blogModel.getAuthor(),
					blogModel.getTime(), String.valueOf(blogModel.getAttitudeNum()), 
					String.valueOf(blogModel.getCommentNum()), 
					String.valueOf(blogModel.getRepostNum()), 
					blogModel.getUrl(), blogModel.getContent(), 
					blogModel.getRepostAuthor(), 
					blogModel.getRepostContent()));
		}
		
		LOGGER.info("抓取结束");
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		WBSearchAnalysis analysis = new WBSearchAnalysis();
		String content = IOUtil.readFileAsString(new File("E:/temp.txt"));
		WBUserModel userModel = new WBUserModel();
		WBGetUserInfo wbGetUserInfo = new WBGetUserInfo();
		WBHttpClientGetPage wbGetPage = new WBHttpClientGetPage();
		JedisWrapper jedis = JedisWrapper.getInstance("192.168.32.210", 6879, 50);
		String cookie = "gsid_CTandWM=4uamea4e1WsVF1UNnrZqnnoksf8;SUB=_2A254GIf6DeTxGeNL7FcR9ibFyDSIHXVb4imyrDV6PUJbrdAKLUn9kW0YtZ9i6ndg47-XPHs6DX0AtJudjQ..;_T_WM=47ad4130cab405aaae2b1641580bfd60";
		//Pair<String, Integer> pair = analysis.getSingleBlog("草根", content,"");
		Pair<String, Integer> pair = analysis.getSearchResultAllBlog(wbGetUserInfo, 
				wbGetPage, "草根", content, cookie, 0, 10, jedis);
		//System.out.println(pair.toString());
	}
}
