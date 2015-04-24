package com.caorenhao.util.task;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.caorenhao.util.BooleanLock;
import com.caorenhao.util.DoubleList;
import com.caorenhao.util.NetUtil;
import com.caorenhao.util.StrUtil;

/**
 * 负责按照计划时间运行下属的Task
 * @author vernkin
 *
 */
public class TaskRunner extends Thread {
	
	/** 检查周期为一秒 */
	private long period = 1 * 1000;
	
	/** 下一个周期要添加的任务 */
	private DoubleList<Task> nextToAddTasks = new DoubleList<Task>(DoubleList.ListType.ArrayList);
	/** 下一个周期要删除的任务 */
	private DoubleList<Task> nextToDeleteTasks = new DoubleList<Task>(DoubleList.ListType.ArrayList);
	
	private BooleanLock startLock = new BooleanLock(false);
	
	/** 任务列表 */
	private List<Task> tasks = new LinkedList<Task>();
	
	public TaskRunner() {
		this(null);
	}
	
	/**
	 * 构造函数, 默认守护进程进行
	 * @param name 线程名字，为空或者为null忽略
	 */
	public TaskRunner(String name) {
		this(name, true);
	}
	
	/**
	 * 构造函数
	 * @param name 线程名字，为空或者为null忽略
	 * @param daemon true 表示守护进程
	 */
	public TaskRunner(String name, boolean daemon) {
		super.setDaemon(daemon);
		name = StrUtil.emptyStringToNull(name, true);
		if(name != null) {
			super.setName(name);
		}
	}
	
	/**
	 * 处理一次
	 */
	private void processOnce() {
		// 轮换一个Batch模式
		nextToDeleteTasks.swap();
		nextToAddTasks.swap();
		
		
		// 处理等待要删除的任务
		List<Task> nextToAddBatchList = nextToAddTasks.getBatchList();
		List<Task> nextToDeleteBatchList = nextToDeleteTasks.getBatchList();
		Set<Task> nextToDeleteBatchSet = new HashSet<Task>();
		for(Task t : nextToDeleteBatchList) {
			tasks.remove(t);
			nextToDeleteBatchSet.add(t);
		}
		
		// 处理等待要添加的任务
		
		for(Task t : nextToAddBatchList) {
			// 位于刚刚删除的队列，忽略
			if(nextToDeleteBatchSet.contains(t))
				continue;
			if(t.isPeriod() == false) {
				// 非周期性任务执行成功后添加
				try {
					t.run();
					// 执行成功后添加
					tasks.add(t);
				} catch(Throwable e) {
					t.onException(e);
				}
			} else {
				// 周期性任务直接添加
				tasks.add(t);
			}
		}
		
		// 清空 刚刚处理的数据
		nextToAddBatchList.clear();
		nextToDeleteBatchList.clear();
		
		//System.out.println("###############processOnce() " + tasks.size());		
		Date curTime = NetUtil.getCalendar().getTime();
		Iterator<Task> itr = tasks.iterator();
		while(itr.hasNext()) {
			Task curTask = itr.next();
			if(curTask == null) {
				itr.remove();
				continue;
			}
				
			try {
				if(curTask.isFinished()) {
					// 处理已经结束的任务
					Task furtherTask = curTask.furtherTask();
					if(furtherTask != null) {
						addTask(furtherTask);
					}
					itr.remove();
				} else if(curTask.isExpired(curTime)){
					// 周期性任务再次执行
					if(curTask.isPeriod()) {
						try {
							curTask.run();
						} catch(Throwable e) {
							// 周期性任务不自动移除
							curTask.onException(e);
						}
					}
					// 非周期性任务报错和移除 
					else {
						try {
							curTask.onExpired(curTime);
						} catch (Throwable e) {
							curTask.onException(new Exception("Exception in task.onExpired", e));
						} finally {
							itr.remove();
						}
					}
				}
			} catch(Throwable t) {
				curTask.onException(new Exception("Exception in process task", t));
			}
		} // end while
	}
	
	/**
	 * 不间歇运行
	 */
	public void run() {
		while(true) {
			try {
				processOnce();
			} catch (Exception e) {
				e.printStackTrace();
				//LOGGER.warn("Exception in processOnce: " + e.getMessage(), e);
			}
			NetUtil.sleep(period);		
		}
	}
	
	/**
	 * 添加任务
	 * @param task
	 */
	public void addTask(Task task) {
		if(task == null)
			return;
		nextToAddTasks.addToBufferList(task);
	}
	
	/**
	 * 移除任务
	 * @param task
	 */
	public void removeTask(Task task) {
		if(task == null)
			return;
		nextToDeleteTasks.addToBufferList(task);
	}
	
	public void start() {
		synchronized(startLock) {
			// 已经启动，忽略
			if(startLock.booleanValue())
				return;
			// 启动后设置标志位
			super.start();
			startLock.setValue(true);
		}
	}
}
