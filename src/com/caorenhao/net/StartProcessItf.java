package com.caorenhao.net;

/**
 * 启动进程的额外接口
 * @author Vernkin
 *
 */
public interface StartProcessItf {

	String getRoot(NodeConf conf) throws Exception;
	
	boolean isDevelopMode();
}
