package com.caorenhao.wbcrawler.server;

import org.apache.commons.logging.Log;

import com.caorenhao.util.JedisWrapper;
import com.caorenhao.util.LoggerConfig;
import com.caorenhao.util.WorkingState;
import com.caorenhao.util.task.TaskRunner;
import com.caorenhao.wbcrawler.WBCookieMgr;
import com.caorenhao.wbcrawler.WBGetUserInfo;
import com.caorenhao.wbcrawler.WBHttpClientGetPage;
import com.caorenhao.wbcrawler.WBHttpClientProxy;
import com.caorenhao.wbcrawler.WBHttpClientSearch;
import com.caorenhao.wbcrawler.WBHttpClientVerify;
import com.caorenhao.wbcrawler.WBSearchAnalysis;
import com.caorenhao.wbcrawler.common.WBAlgo;
import com.caorenhao.wbcrawler.common.WBConst;
import com.caorenhao.wbcrawler.common.WBTask;
import com.caorenhao.wbcrawler.common.WBTaskMgr;
import com.caorenhao.wbcrawler.common.WBVar;
import com.caorenhao.wbcrawler.conf.ConfigSingleton;
import com.caorenhao.wbcrawler.conf.WBCrawlerConfig;
import com.caorenhao.wbcrawler.task.WBCookieUpdateMgr;
import com.caorenhao.wbcrawler.task.WBCookieUpdateTask;
import com.caorenhao.wbcrawler.task.WBProxyUpdateMgr;
import com.caorenhao.wbcrawler.task.WBProxyUpdateTask;
import com.caorenhao.wbcrawler.task.WBUnavailableListCheckMgr;
import com.caorenhao.wbcrawler.task.WBUnavailableListCheckTask;
import com.caorenhao.workingthread.MultiThreadContext;
import com.caorenhao.workingthread.MultiThreadModel;
import com.caorenhao.workingthread.WorkingThread;

/**
 * 多线程管理类.
 *
 * @author renhao.cao.
 *         Created 2015年4月13日.
 */
public class WBCrawlServerMultiThreadMain extends MultiThreadModel<WBTask> {
	
	private Log LOGGER = LoggerConfig.getLog(getClass());
	
	private JedisWrapper jedis;
	
	private WBCookieMgr cookieMgr;
	
	private WBSearchAnalysis searchAnalysis;
	
	private WBGetUserInfo wbGetUserInfo;
	
	private WBHttpClientGetPage wbGetPage;
	
	private WBHttpClientSearch search;
	
	private WBTaskMgr taskMgr;
	
	private WBCookieUpdateMgr updateMgr;
	
	private WBUnavailableListCheckMgr checkMgr;
	
	private WBProxyUpdateMgr updateProxyMgr;
	
	private WBHttpClientVerify verify;
	
	private WBHttpClientProxy wbProxy;
	
	private TaskRunner updateProxyTaskRn = new TaskRunner("updateProxyTaskRn");
	
	private TaskRunner updateCookieTaskRn = new TaskRunner("updateCookieTaskRn");
	
	private TaskRunner checkTaskRn = new TaskRunner("checkTaskRn");
	
	private WorkingState state = WorkingState.AVAILABLE;
	
	private static int maxSize = 50;
	
	/**
	 * 初始化.
	 * 
	 * @param palmAndDeptList 
	 * @throws Exception
	 */
	public WBCrawlServerMultiThreadMain() 
			throws Exception {
		LOGGER.info("Create WBCrawl");
		WBCrawlerConfig conf = ConfigSingleton.getWBCrawlerConfig();
		String redisHost = conf.redisConf.host;
		int port = conf.redisConf.port;
		jedis = JedisWrapper.getInstance(redisHost, port, maxSize);
		
		WBVar.IP = WBAlgo.getLocalIP();
		LOGGER.info("The local host is " + WBVar.IP);
		LOGGER.info("Set the WorkingStatus as RUNNING");
		LOGGER.info("Use redis : " + jedis.getAddress());
		
		taskMgr = new WBTaskMgr(jedis);
		cookieMgr = new WBCookieMgr();
		searchAnalysis = new WBSearchAnalysis();
		wbGetUserInfo = new WBGetUserInfo();
		wbGetPage = new WBHttpClientGetPage();
		search = new WBHttpClientSearch();
		
		wbProxy = new WBHttpClientProxy();
		updateMgr = new WBCookieUpdateMgr();
		updateProxyMgr = new WBProxyUpdateMgr();
		verify = new WBHttpClientVerify();
		checkMgr = new WBUnavailableListCheckMgr();
		
		updateProxyTaskRn.addTask(new WBProxyUpdateTask(updateProxyMgr, verify, 
				wbProxy, jedis));
		updateCookieTaskRn.addTask(new WBCookieUpdateTask(updateMgr, verify, jedis));
		checkTaskRn.addTask(new WBUnavailableListCheckTask(checkMgr, jedis));
	}
	
	/**
	 * 启动管理程序.
	 *
	 * @throws Exception
	 */
	public void start() throws Exception {
		LOGGER.info("Start WBCrawl");
		
		WBCookieMgr mgr = new WBCookieMgr();
		mgr.init(jedis);
		
		updateCookieTaskRn.start();
		checkTaskRn.start();
		updateProxyTaskRn.start();
		
		super.setThreadBusyInterval(0);
		super.setThreadFreeInterval(1000);
		super.initWorkingThreads(WBConst.NUM_WORKINGTHREADS);
 	}
	
	@Override
	public void onStateChanged(WorkingState oldState, WorkingState newState,
			long availableTimeMs) {
		// 关机流程中如果所有的节点
		if(state == WorkingState.TERMINATE_PENDING) {
			if(newState == WorkingState.AVAILABLE) {
				LOGGER.info("Node in under TERMINATE_PENDING and all "
						+ "threads are available. System.exit(0)");
				System.exit(0);
			}
		}
	}
	
	@Override
	public void onAllThreadTerminated() {
		// Do nothing
	}
	
	@Override
	public void logThreadException(int threadId, WBTask task, MultiThreadContext ctx, 
			Throwable t) {
		LOGGER.warn("WorkerThreadException", t);
	}
	
	@Override
	public WBTask nextTask(int threadId) {
		return taskMgr.getTask();
	}
	
	@Override
	public WorkingThread<WBTask> createWorkingThread(int id)
			throws Exception {
		return new WBCrawlServerThread(this, id);
	}
	
	@Override
	public void sumbitTask(MultiThreadContext ctx, WBTask task, int threadId) {
		
	}
	
	/**
	 * 获取redis连接类.
	 *
	 * @return JedisWrapper
	 */
	public JedisWrapper getJedisWrapper() {
		return jedis;
	}
	
	/**
	 * 获取WBCookieMgr类.
	 *
	 * @return WBCookieMgr
	 */
	public WBCookieMgr getWBCookieMgr() {
		return cookieMgr;
	}
	
	/**
	 * 获取WBSearchAnalysis类.
	 *
	 * @return WBSearchAnalysis
	 */
	public WBSearchAnalysis getWBSearchAnalysis() {
		return searchAnalysis;
	}
	
	/**
	 * 获取WBGetUserInfo类.
	 *
	 * @return WBGetUserInfo
	 */
	public WBGetUserInfo getWBGetUserInfo() {
		return wbGetUserInfo;
	}
	
	/**
	 * 获取WBHttpClientGetPage类.
	 *
	 * @return WBHttpClientGetPage
	 */
	public WBHttpClientGetPage getWBHttpClientGetPage() {
		return wbGetPage;
	}
	
	/**
	 * 获取WBHttpClientSearch类.
	 *
	 * @return WBHttpClientSearch
	 */
	public WBHttpClientSearch getSearch() {
		return search;
	}
	
	/**
	 * 获取WBHttpClientVerify类.
	 *
	 * @return WBHttpClientVerify
	 */
	public WBHttpClientVerify getVerify() {
		return verify;
	}
	
	/**
	 * 获取WBHttpClientProxy.
	 *
	 * @return proxy
	 */
	public WBHttpClientProxy getProxy() {
		return wbProxy;
	}
}