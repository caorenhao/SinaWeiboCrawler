package com.caorenhao.wbcrawler;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.caorenhao.util.JedisWrapper;
import com.caorenhao.wbcrawler.common.WBConst;
import com.caorenhao.wbcrawler.conf.ConfigSingleton;
import com.caorenhao.wbcrawler.conf.WBCrawlerConfig;

/**
 * 账号管理.
 *
 * @author renhao.cao.
 *         Created 2015-1-14.
 */
public class WBAccountMgr {
	
	/**
	 * 增加可用账号.
	 * 
	 * @param jedis
	 * @param username
	 * @param password
	 */
	public void addAvailable(JedisWrapper jedis, String username, String password) {
		JSONObject rootJson = new JSONObject();
		rootJson.put("username", username);
		rootJson.put("password", password);
		jedis.lpush(WBConst.ACCOUNT_AVAILABLE_LIST, rootJson.toJSONString());
		System.out.println("添加成功");
	}
	
	/**
	 * 清空可用账号队列.
	 *
	 * @param jedis
	 */
	public static void delAllAvailable(JedisWrapper jedis) {
		jedis.del(WBConst.ACCOUNT_AVAILABLE_LIST);
	}
	
	/**
	 * 清空待用账号队列.
	 *
	 * @param jedis
	 */
	public static void delAllUnavailable(JedisWrapper jedis) {
		jedis.del(WBConst.ACCOUNT_UNAVAILABLE_LIST);
	}

	/**
	 * 显示可用账号队列中的所有账号.
	 *
	 * @param jedis
	 */
	public void showAllAvailable(JedisWrapper jedis) {
		List<String> list = jedis.lrange(WBConst.ACCOUNT_AVAILABLE_LIST, 0, -1);
		System.out.println("可用账号队列:");
		for(String str : list) {
			System.out.println(str);
		}
	}
	
	/**
	 * 显示待用账号队列中的所有账号.
	 *
	 * @param jedis
	 */
	public void showAllUnavailable(JedisWrapper jedis) {
		List<String> list = jedis.lrange(WBConst.ACCOUNT_UNAVAILABLE_LIST, 0, -1);
		System.out.println("待用账号队列:");
		for(String str : list) {
			System.out.println(str);
		}
	}
	
	/**
	 * TODO Put here a description of what this method does.
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		WBCrawlerConfig config = ConfigSingleton.getWBCrawlerConfig();
		String jedisHost = config.redisConf.host;
		int jedisPort = config.redisConf.port;
		jedisHost = "172.16.0.138";
		JedisWrapper jedis = JedisWrapper.getInstance(jedisHost, jedisPort, 10);
		WBAccountMgr accountMgr = new WBAccountMgr();
		
		//删除可用账号队列和待用账号队列
		//delAllAvailable(jedis);
		//delAllUnavailable(jedis);
		
		//String password = "sinaTest";
		
		//添加可用账号
		//accountMgr.addAvailable(jedis, username20, password);
		
		//显示可用账号队列和待用账号队列的所有内容
		accountMgr.showAllAvailable(jedis);
		accountMgr.showAllUnavailable(jedis);
	}
}
