package com.caorenhao.wbcrawler.common;

import org.apache.commons.logging.Log;

import com.caorenhao.util.JedisWrapper;
import com.caorenhao.util.LoggerConfig;

/**
 * TODO Put here a description of what this class does.
 *
 * @author renhao.cao.
 *         Created 2015-1-21.
 */
public class WBTaskMgr {
	
	private Log LOGGER = LoggerConfig.getLog(getClass());
	
	private JedisWrapper jedis;
	
	private String[] taskListKeys;
	
	/**
	 * TODO Put here a description of what this constructor does.
	 *
	 * @param jedis
	 */
	public WBTaskMgr(JedisWrapper jedis) {
		this.jedis = jedis;
		taskListKeys = new String[]{
				WBConst.WEIBO_TASK_GETUSERINFO,
				WBConst.WEIBO_TASK};
	}
	
	/**
	 * TODO Put here a description of what this method does.
	 *
	 * @return WBTask
	 */
	public WBTask getTask() {
		String job = null;
		try {
			for(String key : taskListKeys) {
				String task = jedis.lpop(key);
				if(task != null) {
					job = task;
					break;
				}
			}
		} catch (Exception exception) {
			LOGGER.warn(exception.getMessage());
		}
		
		if(job != null)
			LOGGER.info("获取任务:" + job);
		
		return job == null ? null : new WBTask(job);
	}
}
