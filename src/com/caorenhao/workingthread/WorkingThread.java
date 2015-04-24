package com.caorenhao.workingthread;

import com.caorenhao.util.NetUtil;
import com.caorenhao.util.WorkingState;

/**
 * TODO Put here a description of what this class does.
 *
 * @author renhao.cao.
 *         Created 2015年4月15日.
 * @param <T>
 */
public abstract class WorkingThread<T> extends Thread {
	/** id从0开始*/
	protected int id;
	
	protected MultiThreadModel<T> model;
		
	protected WorkingState state = WorkingState.INIT;
	
	public WorkingThread(MultiThreadModel<T> model, int id) {
		this.model = model;
		this.id = id;
		super.setDaemon(true);
		super.setName("WorkingThread-" + id);
	}
	
	public void start() {
		updateState(WorkingState.AVAILABLE);
		super.start();
	}
	
	public void run() {
		while(true) {
			if(model.isTerminated()) {
				updateState(WorkingState.TERMINATED);
				return;
			}
			
			T curTask = null;
			try {
				curTask = model.nextTask(this.id);
			} catch (Throwable e) {
				model.logThreadOtherException(id, e);
				curTask = null;
			}
			if(curTask == null) {
				updateState(WorkingState.AVAILABLE);
				NetUtil.sleep(model.getThreadFreeInterval());
				continue;
			}
			
			updateState(WorkingState.BUSY);
			try {
				performTask(curTask);
			} catch(Throwable t) {
				model.logThreadException(id, curTask, null, t);
			} finally {
				// 忙碌时等待时间
				if(model.getThreadBusyInterval() > 0) {
					NetUtil.sleep(model.getThreadBusyInterval());
				}
			}
		}
	}
	
	/**
	 * 线程结束时候释放资源，若有需要
	 */
	public void close() {
		
	}
	
	/**
	 * 当前线程是否空闲
	 * @return
	 */
	public boolean isAvailable() {
		return state.isAvailable();
	}
	
	public void updateState(WorkingState newState) {
		if(state == newState)
			return;
		state = newState;
		model.onThreadStateChanged(id, state);
	}
	
	public WorkingState getWorkingState() {
		return state;
	}
	
	/**
	 * 工作线程执行具体的操作
	 * @param task
	 * @throws Exception
	 */
	public abstract void performTask(T task) throws Exception;
}
