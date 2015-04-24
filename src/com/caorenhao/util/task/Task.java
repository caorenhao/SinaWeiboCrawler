package com.caorenhao.util.task;

import java.util.Date;

public interface Task {

	/**
	 * 任务是否结束
	 * @return true 表示任务结束
	 */
	boolean isFinished();
	
	/**
	 * 在任务结束后调用，表示后续任务。若没有返回null
	 * @return 后续任务
	 */
	Task furtherTask();
	
	/**
	 * 任务是否周期性的。对于周期性任务不能结束
	 * @return true 表示是周期性任务
	 */
	boolean isPeriod();
	
	/**
	 * 是否已经过期。对于非周期性任务，抛出异常；对于周期性任务，调用run()方法
	 * @param curTime 当前时间
	 * @return true 表示任务已经过期
	 */
	boolean isExpired(Date curTime);
	
	/**
	 * 处理过期时间
	 * @param checkTime 检查时间
	 */
	void onExpired(Date checkTime);
	
	/**
	 * 执行任务
	 * @throws Exception
	 */
	void run() throws Exception;
	
	/**
	 * 处理 run() 抛出的异常
	 * 本方法不再向外抛出任何异常
	 */
	void onException(Throwable t);
}
