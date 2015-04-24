package com.caorenhao.net;

/**
 * 配置文件选择器.
 *
 * @author renhao.cao.
 *         Created 2015年4月15日.
 */
public class NetGlobalVars {

	public static NodeType nodeType;
	
	public static String nodeId;
	
	/** 选择的配置文件，如果不为空，ConfSeletor不再根据配置文件选择 */
	public static String confSelect;
	
	/** 当前工作目录。设置的时候ProcessCtr启动带nodeId参数的节点在 log
	 * 目录下创建进程的 (nodeId).out和 (nodeI).error文件 */
	public static WorkingDir workingDir;

}
