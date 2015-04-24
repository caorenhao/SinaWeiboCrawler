package com.caorenhao.wbcrawler.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caorenhao.util.JedisWrapper;
import com.caorenhao.util.LoggerConfig;
import com.caorenhao.wbcrawler.common.WBConst;

/**
 * TODO Put here a description of what this class does.
 *
 * @author renhao.cao.
 *         Created 2015-1-8.
 */
public class WBUnavailableListCheckMgr {

	private Log LOGGER = LoggerConfig.getLog(getClass());
	
	/**
	 * 检查待用队列, 将符合条件的账号加入可用队列.
	 * 
	 * @param jedis
	 */
	public void unavailableListCheck(JedisWrapper jedis) {
		//取出待用队列中的所有值
		List<String> unavailables = new ArrayList<String>();
		long length = jedis.llen(WBConst.ACCOUNT_UNAVAILABLE_LIST);
		if(length == 0)
			LOGGER.info("待用队列中没有账号");
		
		for(int i = 0; i < length; i++) {
			unavailables.add(jedis.lpop(WBConst.ACCOUNT_UNAVAILABLE_LIST));
		}
		
		long nowTimeL = System.currentTimeMillis();
		for(String unavailable : unavailables) {
			JSONObject obj = JSON.parseObject(unavailable);
			// 待用队列账号的等待时间超过阈值, 则进入可用队列的队尾
			if(nowTimeL - obj.getLongValue("waitBeginTimeL") > WBConst.TIMEWAIT_UNAVAILABLE) {
				JSONObject rootJson = new JSONObject();
				rootJson.put("username", obj.getString("username"));
				rootJson.put("password", obj.getString("password"));
				jedis.rpush(WBConst.ACCOUNT_AVAILABLE_LIST, rootJson.toJSONString());
				
				this.LOGGER.info("将待用队列中可用的账号放入可用队列中\n" + rootJson.toJSONString() + "\n");
			} else {
				JSONObject rootJson = new JSONObject();
				rootJson.put("username", obj.getString("username"));
				rootJson.put("password", obj.getString("password"));
				rootJson.put("waitBeginTimeL", obj.getLongValue("waitBeginTimeL"));
				jedis.lpush(WBConst.ACCOUNT_UNAVAILABLE_LIST, rootJson.toJSONString());
				
				this.LOGGER.info("将不可用账号重新放入待用队列中\n" + rootJson.toJSONString() + "\n");
			}
		}
	}
	
}
