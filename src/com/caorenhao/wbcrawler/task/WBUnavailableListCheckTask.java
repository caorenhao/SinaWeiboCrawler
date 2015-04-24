package com.caorenhao.wbcrawler.task;

import org.apache.commons.logging.Log;

import com.caorenhao.util.JedisWrapper;
import com.caorenhao.util.LoggerConfig;
import com.caorenhao.util.task.PeriodTask;

/**
 * 定时检查Cookie的可用性.
 *
 * @author renhao.cao.
 *         Created 2015-1-8.
 */
public class WBUnavailableListCheckTask extends PeriodTask {
	
	private Log LOGGER = LoggerConfig.getLog(getClass());
	
	private WBUnavailableListCheckMgr checkMgr;
	
	private JedisWrapper jedis;
	
	/**
	 * 监控器初始化.
	 * 
	 * @param checkMgr 
	 * @param jedis 
	 * @throws Exception 
	 */
	public WBUnavailableListCheckTask(WBUnavailableListCheckMgr checkMgr, 
			JedisWrapper jedis) throws Exception {
		super(30 * 60 * 1000);
		this.checkMgr = checkMgr;
		this.jedis = jedis;
		LOGGER.info("Create the task of check unavailable account, the frequency is "
				+ "30 min");
	}
	
	@Override
	public void onException(Throwable t) {
		this.LOGGER.warn("ExceptionInRun", t);
	}
	
	@Override
	public void run() throws Exception {
		super.run();
		runOnce();
	}
	
	private void runOnce() throws Exception {
		this.checkMgr.unavailableListCheck(jedis);
	}
}
