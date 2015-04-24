package com.caorenhao.util;

/**
 * 节点或者线程的工作状态
 * @author vernkin
 *
 */
public enum WorkingState {
	/** 初始化 */ INIT, 
	/** 可用 */ AVAILABLE,
	/** 已完成基本工作，等待后期处理 */ AVAILABLE_APPENDING,
	/** 忙碌 */ BUSY,
	/** 已经结束 */ TERMINATED,
	/** 需要等待下属工作线程结束 */ TERMINATE_PENDING,
	/** 强制关闭 */ FORCE_CLOSE,
	/** 子结点请求重启 */REQ_RESTART;
	
	public boolean isAvailable() {
		return this == AVAILABLE;
	}
	
	/**
	 * 是否处于关机流程中
	 * @return
	 */
	public boolean isTerminateAppening() {
		return this == TERMINATE_PENDING;
	}
	
	/**
	 * 是否已经关机或者处于关机流程中
	 * @return
	 */
	public boolean isTerminatedOrIng() {
		return this == TERMINATE_PENDING || this == TERMINATED;
	}
}
