package com.caorenhao.wbcrawler.server;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;

import org.apache.commons.logging.Log;

import com.caorenhao.net.WorkingDir;
import com.caorenhao.util.LoggerConfig;
import com.caorenhao.util.NetUtil;
import com.caorenhao.util.PIDUtil;
import com.caorenhao.wbcrawler.conf.ConfigSingleton;
import com.caorenhao.wbcrawler.conf.WBCrawlerConfig;

/**
 * 微博抓取管理类.
 * 
 * @author renhao.cao.
 *         Created 2015-4-13.
 */
public class WBCrawlServerMain {
	
	/**
	 * 主节点启动主程序.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		WBCrawlerConfig conf = ConfigSingleton.getWBCrawlerConfig();
		WorkingDir wd = new WorkingDir(conf.workDirConf.workDir);
		PIDUtil.writePidFile(wd.getPidFile());
		wd.startLogClean();
		
		File logFile = new File(wd.getLogDir().getPath() + "/WBServerCrawler");
		LoggerConfig.initLog(logFile.getPath());
		
		final Log LOGGER = LoggerConfig.getLog(WBCrawlServerMain.class);
		LOGGER.info("Process ID[" + PIDUtil.getPid() + "]");
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(){
			
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				LOGGER.error("Catch Thread Exception: " + t, e);
			}
		});
		
		try {
			WBCrawlServerMultiThreadMain multiThreadMain  = 
					new WBCrawlServerMultiThreadMain();
			multiThreadMain.start();
			
			while(true) {
				NetUtil.sleep(600000);
			}
		} catch(Exception ex) {
			LOGGER.error("Error in WBCrawlMain", ex);
		}
	}
}