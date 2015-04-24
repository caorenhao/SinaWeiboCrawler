package com.caorenhao.wbcrawler;

import java.util.List;

import com.caorenhao.wbcrawler.models.WBUserModel;

/**
 * 获取用户详细信息类.
 *
 * @author renhao.cao.
 *         Created 2015-1-12.
 */
public class WBGetUserInfo {
	
	/**
	 * TODO Put here a description of what this method does.
	 * 
	 * @param getPage
	 * @param authorUrl
	 * @param cookie
	 * @return WBUserModel
	 * @throws Exception
	 */
	public WBUserModel getUserInfo_(WBHttpClientGetPage getPage, String authorUrl, 
			String cookie) throws Exception {
		WBUserModel userModel = new WBUserModel();
		String userPage = getPage.getPage(authorUrl, cookie);
		userModel = getUserInfo(getPage, authorUrl, userPage, cookie);
		
		return userModel;
	}
	
	/**
	 * 从用户首页中获取用户的所有信息.
	 * 
	 * @param wbGetPage 获取页面类
	 * @param url 用户首页链接
	 * @param content 用户首页内容
	 * @param cookie
	 * @return WBUserModel 用户详细信息模型
	 * @throws Exception 
	 */
	public WBUserModel getUserInfo(WBHttpClientGetPage wbGetPage, String url, 
			String content, String cookie) throws Exception {
		// 1.获取用户的uid和用户详情页的url
		List<String> list = WBSearchAnalysis.getUidAndInfo(url, content);
		WBUserModel userModel = new WBUserModel();
		userModel.setUid(list.get(0));
		
		// 2.获取用户粉丝数
		String fans = WBCrawlHelper.extract(content, ">粉丝\\[", "\\]");
		userModel.setFans(Integer.parseInt(fans));
		
		// 2.获取用户详情页的详细资料
		userModel = getUserDetail(wbGetPage, list.get(1), cookie, 
				userModel);
		
		return userModel;
	}
	
	/**
	 * 获取用户的详细信息.
	 * 
	 * @param wbSearchAnalysis 
	 * @param url 
	 * @param cookie 
	 * @param userModel 
	 * @return WBUserModel 
	 * @throws Exception 
	 */
	private WBUserModel getUserDetail(WBHttpClientGetPage wbGetPage, 
			String url, String cookie, WBUserModel userModel) throws Exception {
		// 1.获取用户详情页内容
		String content = wbGetPage.getPage(url, cookie);
		
		// 2.由用户详情页抽取用户详细资料
		userModel = WBSearchAnalysis.getUserDeatil(content, userModel);
		
		return userModel;
	}
}
