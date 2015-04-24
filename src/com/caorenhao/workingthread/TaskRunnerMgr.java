package com.caorenhao.workingthread;

import com.caorenhao.util.BooleanLock;
import com.caorenhao.util.task.TaskRunner;

/**
 * TODO Put here a description of what this class does.
 *
 * @author renhao.cao.
 *         Created 2015年4月15日.
 */
public class TaskRunnerMgr {

	private static TaskRunnerMgr mgr = new TaskRunnerMgr();
	
	public static TaskRunnerMgr getInstance() {
		return mgr;
	}
	
	private BooleanLock isInited = new BooleanLock(false);
	
	public TaskRunner commonRunner;
	
	public TaskRunnerMgr() {
		initialize();
	}
	
	public void initialize() {
		synchronized(isInited) {
			if(isInited.booleanValue())
				return;
			isInited.revertValue();
		}
		commonRunner = new TaskRunner("commonRunner");
		commonRunner.start();
	}
}
