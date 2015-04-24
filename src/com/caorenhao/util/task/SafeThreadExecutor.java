package com.caorenhao.util.task;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class SafeThreadExecutor {	
	
	private long freeSleepInterval = 1000;
	
	private long busySleepInterval = 200;
	
	/** 最大线程数量， 非正数表示不限制 */
	private int maxThreadNum = -1;

	/** 最大缓存的线程数量，非正数表示没有缓存  */
	private int cachedThreadNum = 3;
	
	/** 当前线程的id，每次都进行自增 */
	private int threadId = 0;
	
	/** 管理活跃线程的数量 和 SafeThread操作锁 */
	private Map<String, SafeThread> threadMap = new HashMap<String, SafeThread>();
	
	/** 任务列表 */
	private LinkedList<SafeRunnable> tasks = new LinkedList<SafeRunnable>();
	
	/** 当前的任务 和  所有任务操作的锁 */
	private Set<SafeRunnable> currentTasks = new HashSet<SafeRunnable>();
	
	/** 活跃中的任务 */
	private Map<SafeRunnable, SafeThread> activeTasks = new HashMap<SafeRunnable, SafeThread>();
	
	public long getFreeSleepInterval() {
		return freeSleepInterval;
	}

	public void setFreeSleepInterval(long freeSleepInterval) {
		if(freeSleepInterval < 50) {
			throw new IllegalArgumentException("freeSleepInterval at least 50 ms");
		}
		this.freeSleepInterval = freeSleepInterval;
	}

	public long getBusySleepInterval() {
		return busySleepInterval;
	}

	public void setBusySleepInterval(long busySleepInterval) {
		
		this.busySleepInterval = busySleepInterval;
	}
	
	public int getMaxThreadNum() {
		return maxThreadNum;
	}

	/**
	 * 设置最大线程数量，非正数表示不限制
	 * @param maxThreadNum
	 */
	public void setMaxThreadNum(int maxThreadNum) {
		this.maxThreadNum = maxThreadNum;
	}

	public int getCachedThreadNum() {
		return cachedThreadNum;
	}

	/**
	 * 设置最大缓存线程的数量，非正数表示没有任何缓存
	 * @param cachedThreadNum
	 */
	public void setCachedThreadNum(int cachedThreadNum) {
		this.cachedThreadNum = cachedThreadNum;
	}

	/**
	 * 添加新的任务，未完成的情况下，重复的任务会被忽略
	 * @param task
	 * @return false表示任务已经存在，添加失败
	 */
	public boolean addTask(SafeRunnable task) {
		synchronized(currentTasks) {
			if(currentTasks.contains(task)) {
				return false;
			}
			
			// 添加到任务队列中
			tasks.addLast(task);
			currentTasks.add(task);
		}
		
		// 不内嵌调用synchronized
		createSafeThread();
		return true;
	}
	
	/**
	 * 删除任务，如果任务在运行中也进行暂停
	 * @param task
	 * @return true表示删除任务成功，false表示任务不存在
	 */
	public boolean removeTask(SafeRunnable task) {
		SafeThread toStopThread = null;
		synchronized(currentTasks) {
			// 任务不存在
			if(!currentTasks.contains(task)) {
				return false;
			}
			
			// 获取正在执行的线程，如果不存在，忽略
			toStopThread = activeTasks.remove(task);
			if(toStopThread == null) {
				// 如果不存在与活跃的任务中，直接删除任务队列
				tasks.remove(task);
			}
			
			// 删除对应的任务映射
			currentTasks.remove(task);
			
		}
		
		// 如果存在需要终止的线程
		if(toStopThread != null) {
			removeTerminatedThread(toStopThread);
			try {
				toStopThread.terminate();
			} catch (Exception e) {
				// 不处理任务异常，存在死锁的风险
			}
		}
		
		return true;
	}
	
	/**
	 * 在线程映射中删除 需要停止的线程
	 * @param thread
	 */
	protected void removeTerminatedThread(SafeThread thread) {
		synchronized(threadMap) {
			threadMap.remove(thread.getName());		
		}
	}
	
	/**
	 * 判断当前没有任务的线程是否需要退出
	 * @param thread
	 * @return
	 */
	protected boolean shouldThreadDestroyed(SafeThread thread) {
		synchronized(threadMap) {
			// 没有设置缓存或者 当前线程大于缓存数量 时候销毁空闲线程
			if(getCachedThreadNum() <= 0 || threadMap.size() > getCachedThreadNum()) {
				threadMap.remove(thread.getName());
				return true;
			}
			return false;
		}
		
	}
	
	private void createSafeThread() {
		synchronized(threadMap) {
			// 没有设置最大线程数量 或者 当前线程没有到达最大线程数量时 创建新线程
			if(getMaxThreadNum() <= 0 || threadMap.size() < getMaxThreadNum()) {
				++threadId;
				String name = "SafeThread-" + threadId;
				SafeThread newThread = new SafeThread(this, name);
				threadMap.put(newThread.getName(), newThread);
				newThread.start();
			}
		}
	}
	
	/**
	 * 返回下一个执行的任务
	 * @param thread 当前线程
	 * @return
	 */
	protected final SafeRunnable nextTask(SafeThread thread) {
		SafeRunnable task = null;
		synchronized(currentTasks) {
			task = tasks.pollFirst();
			// 记录活跃的任务状态
			if(task != null) {
				activeTasks.put(task, thread);
			}
		}
		return task;
	}
	
	/**
	 * 任务成功执行的处理方法
	 * @param thread
	 * @param task
	 */
	protected final void onFinished(SafeThread thread, SafeRunnable task) {
		onReturnActiveTask(task);
		onTaskFinished(task);
	}
	
	/**
	 * 处理执行任务出现的异常
	 */
	protected final void onException(SafeThread thread, SafeRunnable task, Throwable t) {
		onReturnActiveTask(task);
		onTaskExeception(task, t);
	}
	
	/**
	 * 任务执行后调用，不管正常还是异常
	 * @param task
	 */
	private final void onReturnActiveTask(SafeRunnable task) {
		// 从活跃任务 和 任务映射中删除任务
		synchronized(currentTasks) {
			currentTasks.remove(task);
			activeTasks.remove(task);
		}
	}
	
	/**
	 * 获取线程的数量
	 * @return
	 */
	public int getThreadNum() {
		synchronized(threadMap) {
			return threadMap.size();
		}
	}
	
	/**
	 * 获取当前任务的数量，包括活跃的任务
	 * @return
	 */
	public int getTaskNum() {
		synchronized(currentTasks) {
			return currentTasks.size();
		}
	}
	
	/**
	 * 获取当前活跃任务的数量
	 * @return
	 */
	public int getActiveTaskNum() {
		synchronized(currentTasks) {
			return activeTasks.size();
		}
	}
	
	/**
	 * 任务执行异常的处理方法，可以重载
	 * @param task
	 * @param t
	 */
	public void onTaskExeception(SafeRunnable task, Throwable t) {
		
	}
	
	/**
	 * 任务成功执行的处理方法，可以重载
	 * @param task
	 * @param t
	 */
	public void onTaskFinished(SafeRunnable task) {
		
	}
}
