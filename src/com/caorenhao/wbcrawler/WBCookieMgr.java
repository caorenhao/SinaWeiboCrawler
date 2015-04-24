package com.caorenhao.wbcrawler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caorenhao.util.JedisWrapper;
import com.caorenhao.util.LoggerConfig;
import com.caorenhao.util.Pair;
import com.caorenhao.wbcrawler.common.WBConst;
import com.caorenhao.wbcrawler.conf.ConfigSingleton;
import com.caorenhao.wbcrawler.conf.WBCrawlerConfig;
import com.caorenhao.wbcrawler.models.WBCookieModel;

/**
 * cookie池的管理、验证.
 *
 * @author renhao.cao.
 *         Created 2014-12-2.
 */
public class WBCookieMgr {
	
	private Log LOGGER = LoggerConfig.getLog(getClass());
	
	/**
	 * 将配置文件中的所有账号初始化到可用队列中.
	 * 
	 * @param jedis
	 */
	public void init(JedisWrapper jedis) {
		try {
			// 清空redis中的账号队列
			WBAccountMgr.delAllAvailable(jedis);
			WBAccountMgr.delAllUnavailable(jedis);
			
			// 获取初始账号列表
			WBCrawlerConfig config = ConfigSingleton.getWBCrawlerConfig();
			List<Pair<String, String>> accounts = new ArrayList<Pair<String, String>>();
			accounts = config.accountConf.accounts;
			
			// 将账号列表写入redis队列中
			for(Pair<String, String> account : accounts) {
				JSONObject rootJson = new JSONObject();
				rootJson.put("username", account.first);
				rootJson.put("password", account.second);
				jedis.lpush(WBConst.ACCOUNT_AVAILABLE_LIST, rootJson.toJSONString());
			}
			
			LOGGER.info("Account queue initialization has successed. "
					+ "The total is " + accounts.size());
		} catch (Exception exception) {
			LOGGER.info(exception.getMessage());
		}
	}
	
	/**
	 * 从可用队列中取出一个账号, 登陆并获取Cookie等信息.
	 * 
	 * @param jedis 
	 * @return WBCookieModel
	 */
	public WBCookieModel getCookie(JedisWrapper jedis) {
		String username = null;
		String password = null;
		try {
			if(jedis.llen(WBConst.ACCOUNT_AVAILABLE_LIST) != 0) {
				String available = jedis.lpop(WBConst.ACCOUNT_AVAILABLE_LIST);
				JSONObject obj = JSON.parseObject(available);
				username = obj.getString("username");
				password = obj.getString("password");
				WBHttpClientLogin login = new WBHttpClientLogin();
				
				//获取微博登录参数
				String[] loginParameters = login.getLoginParameters();
				
				if(loginParameters[0] == null) {
					//没有获取到登录参数, 将账号重新放入可用队列中
					LOGGER.info("账号：" + username + " 没有获取到登录参数");
					repush(username, password, jedis);
					return null;
				} else {
					WBCookieModel cookieModel = new WBCookieModel();
					cookieModel = login.doLogin(username, password, loginParameters);
					if(cookieModel == null) {
						repush(username, password, jedis);
						return null;
					}
					
					String cookie = cookieModel.getCookie();
					WBHttpClientVerify verify = new WBHttpClientVerify();
					boolean flag = verify.verify(cookie);
					if(flag) {
						return cookieModel;
					} else {
						repush(username, password, jedis);
					}
				}
			} else {
				LOGGER.info("可用队列中没有可用账号");
			}
		} catch (Exception exception) {
			repush(username, password, jedis);
			LOGGER.info(exception.getMessage());
		}
		
		return null;
	}
	
	/**
	 * 将无法登陆的账号重新放入可用账号队列中.
	 *
	 * @param username
	 * @param password
	 * @param jedis
	 */
	private void repush(String username, String password, JedisWrapper jedis) {
		LOGGER.info("账号：" + username + "不可用, 换新的账号");
		JSONObject rootJson = new JSONObject();
		rootJson.put("username", username);
		rootJson.put("password", password);
		jedis.rpush(WBConst.ACCOUNT_AVAILABLE_LIST, rootJson.toJSONString());
	}
}
