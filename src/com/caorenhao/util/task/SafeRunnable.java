package com.caorenhao.util.task;

public interface SafeRunnable extends Runnable {

	/**
	 * 任务被终止时调用，不能抛出任何异常
	 */
	void onTerminated();
}
