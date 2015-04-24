package com.caorenhao.util;

import java.io.File;
import java.lang.management.ManagementFactory;

public final class PIDUtil {

	private static String pid;
	
	/**
	 * Get Process ID
	 * @return
	 */
	public static String getPid() {
		if(pid == null) {
			pid = getPidImpl();
		}
		return pid;
	}
	
	/**
	 * Get Process ID Implementation
	 * @return
	 */
	private static String getPidImpl() {
		final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
		final int index = jvmName.indexOf('@');

	    if (index < 1) {
	        return null;
	    }

	    try {
	        return Long.toString(Long.parseLong(jvmName.substring(0, index)));
	    } catch (NumberFormatException e) {
	        return null;
	    }
	}
	
	/**
	 * 写PID到指定的PID文件
	 * @param pidFile PID文件
	 * @return true表示写入成功， false表示先有的PID文件内容一样，不需要再次写入
	 * @throws Exception
	 */
	public static boolean writePidFile(final File pidFile) throws Exception {
		String pid = getPid();
		if (pidFile.exists()) {
			String oldPid = StrUtil.nullToEmptyString(IOUtil.readFileAsString(pidFile), false);
			if(oldPid.equals(pid) == false) {
				// 删除原先不一样的PID
				OSUtil.killProcess(oldPid);
				// 暂停五秒钟
				NetUtil.sleep(5000);
			} else {
				return false;
			}
		}
		if(pid == null)
			throw new Exception("Can't Gain Process Pid");
		
		IOUtil.writeToFile(pidFile, pid);
		Runtime.getRuntime().addShutdownHook(new Thread(){
    		public void run() {
    			if(pidFile.exists())
    				pidFile.delete();
    		}
        });
		return true;
	}
	
	/**
	 * 自杀，终结自己的程序
	 */
	public static void suicide() throws Exception {
		String pid = getPid();
		if(pid == null)
			throw new Exception("Can't Gain Process Pid");
		OSUtil.killProcess(pid);
	}
}
