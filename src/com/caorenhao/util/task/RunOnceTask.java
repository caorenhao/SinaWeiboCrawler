package com.caorenhao.util.task;

import java.util.Date;

import com.caorenhao.util.NetUtil;

/**
 * 只运行一次的任务
 * @author vernkin
 *
 */
public class RunOnceTask implements Task {

	/** 过期的时间间隔，单位ms */
	protected long expiredPeriod;
	
	/** 过期的具体时间, 在run()以后开始计时 */
	protected long expiredTime = 0;
	
	protected boolean isDone = false;
	
	/**
	 * 创建只运行一次的任务
	 * @param expiredPeriod 过期的时间间隔，单位ms
	 */
	public RunOnceTask(long expiredPeriod) {
		this.expiredPeriod = expiredPeriod;
	}
	
	@Override
	public boolean isExpired(Date curTime) {
		return expiredTime != 0 && expiredTime < curTime.getTime();
	}

	@Override
	public boolean isFinished() {
		return isDone;
	}

	@Override
	public boolean isPeriod() {
		return false;
	}

	@Override
	public void run() throws Exception {
		// 重置过期时间
		resetExpireTime();
	}

	@Override
	public Task furtherTask() {
		return null;
	}

	@Override
	public void onException(Throwable t) {
	}

	@Override
	public void onExpired(Date checkTime) {
	}

	/**
	 * 重置过期时间，按照当前时间加上过期时间
	 */
	public void resetExpireTime() {
		this.expiredTime = NetUtil.getCalendar().getTimeInMillis() + 
			expiredPeriod;
	}
	/**
	 * 更新过期的周期
	 * @param newExpiredPeriod
	 */
	public void updateExpirePeriod(long newExpiredPeriod) {
		if(expiredTime != 0)
			expiredTime = expiredTime - expiredPeriod + newExpiredPeriod;
		expiredPeriod = newExpiredPeriod;
	}
}
