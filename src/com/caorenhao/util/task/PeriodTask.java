package com.caorenhao.util.task;

import java.util.Date;

import com.caorenhao.util.NetUtil;

/**
 * 周期性任务
 * @author vernkin
 *
 */
public class PeriodTask implements Task {

	/** 过期的时间间隔，单位ms */
	protected long expiredPeriod;
	
	/** 下一次执行的时间 */
	protected long nextRunTime = 0;
	
	/**
	 * 指定循环时间间隔的构造函数
	 * @param expiredPeriod 循环周期，单位为ms
	 */
	public PeriodTask(long expiredPeriod) {
		this.expiredPeriod = expiredPeriod;
	}
	
	@Override
	public boolean isExpired(Date curTime) {
		return nextRunTime <= curTime.getTime();
	}

	@Override
	public boolean isFinished() {
		return false;
	}

	@Override
	public boolean isPeriod() {
		return true;
	}

	@Override
	public void run() throws Exception {
		nextRunTime = NetUtil.getCalendar().getTimeInMillis() + 
				expiredPeriod;
	}

	@Override
	public Task furtherTask() {
		return null;
	}

	@Override
	public void onException(Throwable t) {
	}

	/**
	 * 周期性任务不会调用
	 */
	@Override	
	public void onExpired(Date checkTime) {
		
	}

}
