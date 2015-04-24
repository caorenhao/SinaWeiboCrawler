package com.caorenhao.net;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.caorenhao.util.LoggerConfig;
import com.caorenhao.util.NetUtil;
import com.caorenhao.util.NullDev;
import com.caorenhao.util.OSUtil;

public class ProcessUtil {

	private static Map<String, Process> procMap = new HashMap<String, Process>();
	
	public static void startNode(Class<?> mainClass, NodeConf node, 
			Map<String, String> params, StartProcessItf startItf) 
			throws Exception {
		startNode(mainClass, node, params, 
				startItf.getRoot(node), startItf.isDevelopMode());
	}
	
	/**
	 * 启动一个节点
	 * @param mainClass main函数入口
	 * @param node 节点配置程序
	 * @param params 启动的参数
	 * @throws Exception
	 */
	public static void startNode(Class<?> mainClass, NodeConf node, 
			Map<String, String> params, String rootPath, 
			boolean isDevelopMode) throws Exception {
		// 所有的参数
		Map<String, String> allParams = new HashMap<String, String>();
		allParams.put("host", node.host);
		allParams.put("user", node.user);
		allParams.put("workDir", node.workDir);
		allParams.put("id", String.valueOf(node.id));
		if(params != null && !params.isEmpty()) {
			allParams.putAll(params);
		}
		
		StringBuilder sb = new StringBuilder(512);
		boolean isLocalAddr = NetUtil.isLocalAddress(node.host);
		if(isLocalAddr == false)
			sb.append(node.getSshCmd()).append(' ');

//		final String javahome = getJavaHome(node);
		boolean isLocalWindows = isLocalAddr && OSUtil.getOSType().isWindows(); 
		String curPath = ".";
		if(isLocalWindows == false) {
			//sb.append("cd ").append(StrUtil.quoteWith(diclRoot)).append(" && ");
			curPath = rootPath;
		}
		sb.append("java -Dfile.encoding=UTF-8 -cp " + curPath + "/libs/*");
		if(isLocalAddr && OSUtil.getOSType().isWindows())
			sb.append(";./*;");
		else
			sb.append(":" + rootPath +"/*:");
			
		if(isDevelopMode)
			sb.append(curPath + "/bin");
		else
			sb.append(curPath + "/bin"); // 两个都包含bin目录
		sb.append(' ').append(mainClass.getCanonicalName());
		for(Map.Entry<String, String> p : allParams.entrySet()) {
			sb.append(" --").append(p.getKey());
			sb.append(" ").append(p.getValue());
		}
		sb.append(" &");
		String cmd = sb.toString();
		LoggerConfig.getLog(ProcessUtil.class).info("Run #" + cmd);
		

		StringTokenizer st = new StringTokenizer(cmd);
		String[] cmdarray = new String[st.countTokens()];
	 	for (int i = 0; st.hasMoreTokens(); i++)
		    cmdarray[i] = st.nextToken();
	 	
		ProcessBuilder builder = new ProcessBuilder(cmdarray);
		if(isLocalWindows)
			builder.directory(new File(rootPath));
		
		File stdOutFile = null; // 标准输出文件
		if(NetGlobalVars.workingDir != null && allParams.get("nodeId") != null) {
			String nodeId = allParams.get("nodeId");
			File logDir = NetGlobalVars.workingDir.getLogDir();
			logDir.mkdirs();
			stdOutFile = new File(logDir, nodeId + ".out");
		}
		
		builder.redirectErrorStream(true);
		Process proc = builder.start();
		
		// 利用cmd做为id
		NullDev.redirectInputStream(cmd, proc.getInputStream(), stdOutFile);
		
		// 关闭原先的 一样命令 可能存在的进程
		synchronized(procMap) {
			Process oldProc = procMap.get(cmd);
			if(oldProc != null) {
				try {
					oldProc.destroy();
				} catch(Exception ex) {
					System.err.println("Fail to destroy process: " + cmd);
					ex.printStackTrace();
				}
			}
			
			procMap.put(cmd, proc);
		}
	}

	/**
	 * 获得节点的全局id
	 * node里面 user/host/workDir 均不能为空
	 * @param node 节点配置
	 * @return 全局id
	 * @throws Exception user/host/workDir 为空抛出异常
	 */
	public static String buildGlobalID(NodeConf node) throws Exception {
		return ProcessUtil.buildGlobalID(node.user, node.host, node.workDir);
	}

	/**
	 * 获得节点的全局id
	 * user/host/workDir 均不能为空
	 * @param user 用户名
	 * @param host 域名
	 * @param workDir 工作目录
	 * @return 全局id
	 * @throws Exception user/host/workDir 有一个为空抛出的异常
	 */
	public static String buildGlobalID(String user, String host, 
			String workDir) throws Exception {
		if(user == null || host == null || workDir == null)
			throw new Exception("All of user/host/workDir can't be null");
		return user + "@" + host + "-#-@$@" + workDir;
	}
	
	/**
	 * 获得节点的全局id
	 * params里面的键  user/host/workDir 均不能为空
	 * @param params 必须包含以下键: user(用户), host(域名), workDir(工作目录) 
	 * @return 全局id
	 * @throws Exception 键  user/host/workDir 为空抛出异常
	 */
	public static String buildGlobalID(Map<String, String> params) throws Exception {
		return buildGlobalID(params.get("user"), params.get("host"), 
				params.get("workDir"));
	}

}
