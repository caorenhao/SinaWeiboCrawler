package com.caorenhao.util.task;

import com.caorenhao.util.BooleanLock;

public class SafeThread extends Thread {

	private SafeThreadExecutor executor;
	
	private volatile boolean running = true;
	
	private BooleanLock taskLock = new BooleanLock();
	
	private SafeRunnable curTask;

    public void terminate() {
    	//System.out.println("terminate: " + this.getName());
    	running = false;
    	// 主动通知要终止的任务
    	synchronized(taskLock) {
    		if(curTask != null) {
    			try {
					curTask.onTerminated();
				} catch (Exception e) {}
    		}
    	}
    	
    	this.interrupt();
    }
	
	
	public SafeThread(SafeThreadExecutor executor, String name) {
		this.executor = executor;
		super.setName(name);
	}
	
	public void run() {
		while(running) {
			try {
				synchronized(taskLock) {
					if(!running) {
						break;
					}
					curTask = executor.nextTask(this);
				}
				
				if(curTask != null) {
					// 存在任务的时候进行执行
					try {
						curTask.run();
						executor.onFinished(this, curTask);
					} catch(Throwable t) {
						executor.onException(this, curTask, t);
					} finally {
						synchronized(taskLock) {
							curTask = null;
						}
					}
					
					// 判断是否忙碌等待
					if(executor.getFreeSleepInterval() > 0) {
						Thread.sleep(executor.getFreeSleepInterval());
					}
				} else {
					// 没有任务执行的时候判断是否需要退出
					if(executor.shouldThreadDestroyed(this)) {
						break;
					}
					
					// 不需要退出的情况下执行空闲区间的等待
					Thread.sleep(executor.getFreeSleepInterval());
				}
			} catch (Throwable e) {
				// 每次新欢捕捉非预知的异常, 结束程序。遇到时退出线程
				running = false;
			} // end while(running)
		}
		executor.removeTerminatedThread(this);
	}
}
