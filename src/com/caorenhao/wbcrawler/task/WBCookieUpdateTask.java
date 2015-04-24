package com.caorenhao.wbcrawler.task;

import org.apache.commons.logging.Log;

import com.caorenhao.util.JedisWrapper;
import com.caorenhao.util.LoggerConfig;
import com.caorenhao.util.task.PeriodTask;
import com.caorenhao.wbcrawler.WBHttpClientVerify;

/**
 * 定时检查Cookie的可用性.
 *
 * @author renhao.cao.
 *         Created 2015-1-8.
 */
public class WBCookieUpdateTask extends PeriodTask {
	
	private Log LOGGER = LoggerConfig.getLog(getClass());
	
	private WBCookieUpdateMgr verifyMgr;
	
	private WBHttpClientVerify verify;
	
	private JedisWrapper jedis;
	
	/**
	 * TODO 监控器初始化.
	 * 
	 * @param verifyMgr 
	 * @param wbLogin 
	 * @param wbVerify 
	 * @param jedis 
	 * @throws Exception 
	 */
	public WBCookieUpdateTask(WBCookieUpdateMgr verifyMgr, WBHttpClientVerify wbVerify, 
			JedisWrapper jedis) throws Exception {
		super(30 * 60 * 1000);
		this.verifyMgr = verifyMgr;
		this.verify = wbVerify;
		this.jedis = jedis;
		LOGGER.info("Create the task of update cookie, the frequency is 30 min");
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
		this.verifyMgr.verifyMgr(this.verify, this.jedis);
	}
}
