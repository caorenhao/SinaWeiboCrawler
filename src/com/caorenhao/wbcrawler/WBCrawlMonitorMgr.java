package com.caorenhao.wbcrawler;

import java.util.Map;

import com.caorenhao.util.JedisWrapper;
import com.caorenhao.util.NetUtil;
import com.caorenhao.wbcrawler.common.WBConst;

/**
 * 微博爬虫系统的状态监控器.
 *
 * @author renhao.cao.
 *         Created 2015年4月13日.
 */
public class WBCrawlMonitorMgr {

	/**
	 * 展示历史数据.
	 *
	 * @param jedis
	 */
	public void show(JedisWrapper jedis) {
		Map<String, String> map = jedis.hgetAll(WBConst.WEIBO_CRAWLER_HISTORY);
		String str = map.get("2015-04-15");
		System.out.println(str);
		/*
		for(Map.Entry<String, String> entry : map.entrySet()) {
			System.out.println(entry.getKey() + "\t" + entry.getValue());	
		}
		*/
	}
	
	/**
	 * TODO Put here a description of what this method does.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		WBCrawlMonitorMgr monitorMgr = new WBCrawlMonitorMgr();
		String server = "172.16.0.138";
		int port = 6379;
		JedisWrapper jedis = JedisWrapper.getInstance(server, port, 50);
		while (true) {
			monitorMgr.show(jedis);
			NetUtil.sleep(30 * 1000);
		}
	}
	
}
