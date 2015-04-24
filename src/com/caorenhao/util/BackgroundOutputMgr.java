package com.caorenhao.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 后台输出的IO流管理类.
 *
 * @author renhao.cao.
 *         Created 2015-1-21.
 */
public class BackgroundOutputMgr extends Thread {

	private static BackgroundOutputMgr instance = new BackgroundOutputMgr();
	
	/**
	 * 获取后台输出的IO流管理类.
	 *
	 * @return BackgroundOutputMgr
	 */
	public static BackgroundOutputMgr getInstance() {
		return instance;
	}
	
	private List<BackgroundOutput> bos;
	
	private byte[] buf = new byte[512];
	
	private BackgroundOutputMgr() {
		this.bos = new LinkedList<BackgroundOutput>();
		super.setName("BackgroundOutputMgrThread");
		super.setDaemon(true);
		super.start(); 
	}
	
	/**
	 * 定时运行实例.
	 *
	 * @return boolean
	 */
	public synchronized boolean runOnce() {
		if(this.bos.isEmpty()) {
			return false;
		}
		Iterator<BackgroundOutput> boItr = this.bos.iterator();
		while(boItr.hasNext()) {
			BackgroundOutput bo = boItr.next();
			
			try {
				int readLen = bo.read(this.buf);
				if(readLen < 0) {
					boItr.remove();
					bo.setFinish(null);
				}
			} catch (Throwable t) {
				bo.setFinish(t);
			}
		}
		
		return true;
	}
	
	@Override
	public void run() {
		while(true) {
			NetUtil.sleep(100);
			if(runOnce() == false) {
				// 空闲状态额外休息一秒
				NetUtil.sleep(1000);
			}
		}
	}
	
	/**
	 * 增加后台输出流.
	 *
	 * @param bo
	 */
	public synchronized void addBackgroundOutput(BackgroundOutput bo) {
		if(bo != null)
			this.bos.add(bo);
	}
	
	/**
	 * 删除后台输出流.
	 *
	 * @param bo
	 * @return boolean
	 */
	public synchronized boolean removeBackgroundOutput(BackgroundOutput bo) {
		if(bo == null)
			return false;
		return this.bos.remove(bo);
	}
}
