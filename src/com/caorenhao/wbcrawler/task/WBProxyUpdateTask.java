package com.caorenhao.wbcrawler.task;

import org.apache.commons.logging.Log;

import com.caorenhao.util.JedisWrapper;
import com.caorenhao.util.LoggerConfig;
import com.caorenhao.util.task.PeriodTask;
import com.caorenhao.wbcrawler.WBHttpClientProxy;
import com.caorenhao.wbcrawler.WBHttpClientVerify;

/**
 * 定时检查Cookie的可用性.
 *
 * @author renhao.cao.
 *         Created 2015-1-8.
 */
public class WBProxyUpdateTask extends PeriodTask {
	
	private Log LOGGER = LoggerConfig.getLog(getClass());
	
	private WBProxyUpdateMgr proxyMgr;
	
	private WBHttpClientVerify verify;
	
	private WBHttpClientProxy proxy;
	
	private JedisWrapper jedis;
	
	/**
	 * TODO 监控器初始化.
	 * 
	 * @param proxyMgr 
	 * @param wbVerify 
	 * @param pairProxy 
	 * @param proxy 
	 * @param jedis 
	 * @throws Exception 
	 */
	public WBProxyUpdateTask(WBProxyUpdateMgr proxyMgr, WBHttpClientVerify wbVerify, 
			WBHttpClientProxy proxy, JedisWrapper jedis) 
					throws Exception {
		super(30 * 60 * 1000);
		this.proxyMgr = proxyMgr;
		this.verify = wbVerify;
		this.jedis = jedis;
		this.proxy = proxy;
		LOGGER.info("Create the task of update proxy , the frequency is 30 min");
	}
	
	@Override
	public void onException(Throwable t) {
		LOGGER.warn("ExceptionInRun", t);
	}
	
	@Override
	public void run() throws Exception {
		super.run();
		runOnce();
	}
	
	private void runOnce() throws Exception {
		proxyMgr.proxyMgr(verify, proxy, jedis);
	}
}
