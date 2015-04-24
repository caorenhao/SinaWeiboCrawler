package com.caorenhao.workingthread;

import com.caorenhao.util.BooleanLock;
import com.caorenhao.util.NetUtil;
import com.caorenhao.util.WorkingState;
import com.caorenhao.util.task.PeriodTask;

/**
 * 多工作线程模型, TaskTracker 和 ProcessorUnit 共享使用.
 *
 * @author renhao.cao.
 *         Created 2015年4月15日.
 * @param <T>
 */
public abstract class MultiThreadModel<T> {

	/** 空闲等待一秒 */
	public static final long THREAD_FREE_INTERVAL = 1 * 1000;
	
	/** true 表示taskTracker 正在正常关闭中 */
	protected boolean terminated = false;
	
	/** 空闲时候不同任务等待的时间(毫秒) */
	protected long threadAvailableInterval = THREAD_FREE_INTERVAL;
	
	/** 忙碌时候不同任务等待的时间(毫秒) */
	protected long threadBusyInterval = THREAD_FREE_INTERVAL;
	
	protected int threadNum;
	
	protected WorkingThread<T>[] workers;
	
	private WorkingState state = WorkingState.INIT;
	
	private BooleanLock onStateChangedLock = new BooleanLock(true);
	
	/** 最近一次开始AVAILABLE的时间 */
	private long lastAvailableTime = 0;
	
	public MultiThreadModel() {
		// 如果是空闲状态，每30秒显式触发一次空闲状态的状态改变
		TaskRunnerMgr.getInstance().commonRunner.addTask(new PeriodTask(30 * 1000){
			@Override
			public void run() throws Exception {
				super.run();
				if(state != WorkingState.AVAILABLE)
					return;
				synchronized(onStateChangedLock) {
					onStateChangedEntry(state, state);
				}
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public void initWorkingThreads(int threadNum) throws Exception {
		this.threadNum = threadNum;
		workers = new WorkingThread[threadNum];
		for(int i = 0; i < threadNum; ++i) {
			workers[i] = createWorkingThread(i);
			workers[i].start();
		}
		updateState(WorkingState.AVAILABLE);
	}
	
	public boolean isTerminated() {
		return terminated;
	}
	
	public long getThreadFreeInterval() {
		return threadAvailableInterval;
	}
	
	public void setThreadFreeInterval(long ms) {
		threadAvailableInterval = ms;
	}
	
	public void setThreadBusyInterval(long threadBusyInterval) {
		this.threadBusyInterval = threadBusyInterval;
	}

	public long getThreadBusyInterval() {
		return threadBusyInterval;
	}

	public void onThreadStateChanged(int threadId, WorkingState threadState) {
		if(threadState == WorkingState.BUSY) {
			if(state == WorkingState.AVAILABLE) {
				updateState(WorkingState.BUSY);
			}
			return;
		}
		if(threadState == WorkingState.AVAILABLE) {
			if(isAllWorkersWithState(WorkingState.AVAILABLE)) {
				updateState(WorkingState.AVAILABLE);
			}
			return;
		}
		if(threadState == WorkingState.TERMINATED) {
			if(isAllWorkersWithState(WorkingState.TERMINATED)) {
				updateState(WorkingState.TERMINATED);
			}
		}
	}
	
	private boolean isAllWorkersWithState(WorkingState state) {
		for(WorkingThread<T> worker : workers) {
			if(worker == null || worker.getWorkingState() != state)
				return false;
		}
		return true;
	}
	
	private void updateState(WorkingState newState) {
		if(state == newState)
			return;
		WorkingState oldState = state;
		state = newState;
		synchronized(onStateChangedLock) {
			onStateChangedEntry(oldState, newState);
		}
		
		if(newState == WorkingState.TERMINATED) {
			onAllThreadTerminated();
		}
	}
	
	/**
	 * 停止所有的工作线程, 停止后不可恢复
	 */
	public void stopAllWorkingThreads() {
		if(workers == null || workers.length == 0) {
			return;
		}
		
		// 不显示关闭线程
		/*
		// 不处理任何消息
		UncaughtExceptionHandler emptyHandler = new UncaughtExceptionHandler(){
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				// do nothing
			}
		};
		
		for(WorkingThread<T> thread : workers) {
			if(thread == null)
				return;
			// 已经关闭的线程不再捕捉移除
			thread.setUncaughtExceptionHandler(emptyHandler);
			thread.stop();
		}
		*/
	}
	
	private void onStateChangedEntry(WorkingState oldState, WorkingState newState) {
		long now = NetUtil.getTimeInMillis();
		long availableTimeMs = 0;
		if(newState == WorkingState.AVAILABLE) {
			if(oldState != WorkingState.AVAILABLE) {
				lastAvailableTime = now;
			} else {
				availableTimeMs = now - lastAvailableTime;
			}
		}
		onStateChanged(oldState, newState, availableTimeMs);
	}
	
	/**
	 * 当前模型的状态改变了 
	 * @param oldState 老的状态
	 * @param newState 新的状态
	 * @param availableTimeMs available 已经持续的时间，单位毫秒
	 */
	public abstract void onStateChanged(WorkingState oldState, 
			WorkingState newState, long availableTimeMs);
	
	/** 当所有的工作线程结束后调用  */
	public abstract void onAllThreadTerminated();
	
	/**
	 * 记录WorkerThread执行任务失败的异常
	 * @param threadId 线程id
	 * @param task 当前执行的任务
	 * @param ctx TODO
	 * @param t 异常
	 */
	public abstract void logThreadException(int threadId, 
			T task, MultiThreadContext ctx, Throwable t);
	
	/**
	 * 记录WorkerThread除了执行任务以外的异常。默认在终端抵用
	 * @param threadId 线程id
	 * @param t 异常
	 */
	public void logThreadOtherException(int threadId, Throwable t) {
		Exception e = new Exception("WorkerThread-" + threadId + 
				" has other exception: " + t.getMessage(), t);
		e.printStackTrace();
	}
	
	/**
	 * 得到下一个任务，没有任务返回null
	 * @param threadId 线程id
	 * @return
	 */
	public abstract T nextTask(int threadId);
	
	public abstract WorkingThread<T> createWorkingThread(int id) throws Exception;
	
	/**
	 * 提交最后的任务
	 * @param ctx TODO
	 * @param task 正在执行的任务
	 * @param threadId 线程ID
	 */
	public abstract void sumbitTask(MultiThreadContext ctx, T task, int threadId);
}
