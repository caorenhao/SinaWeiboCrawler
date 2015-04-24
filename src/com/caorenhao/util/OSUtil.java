package com.caorenhao.util;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * 系统相关的操作类.
 *
 * @author renhao.cao.
 *         Created 2015-1-21.
 */
public final class OSUtil {

	private static OSType osType; 
	
	/** 
	 * 操作系统类型 
	 */
	public static enum OSType {
		/** Windows系统*/
		WINDOWS,
		/** Linux系统*/
		LINUX;
		
		/**
		 * 判断是否为Windows系统.
		 *
		 * @return boolean
		 */
		public boolean isWindows() {
			return this == WINDOWS;
		}
		
		/**
		 * 判断是否为Linux系统.
		 *
		 * @return boolean
		 */
		public boolean isLinux() {
			return this == LINUX;
		}
	}
	
	/**
	 * 获取user.home的路径.
	 *
	 * @return String
	 */
	public static String getUserHome() {
		return System.getProperty("user.home");
	}
	
	/**
	 * 获取user.name的路径.
	 *
	 * @return String
	 */
	public static String getUserName() {
		return System.getProperty("user.name");
	}
	
	/**
	 * 获取本机 .ssh 的所在目录
	 * 
	 * @return File
	 */
	public static File getLocalSSHHomeDir() {
		String sshRoot = StrUtil.emptyStringToNull(System.getenv("SSH_CONF_HOME"), true);
		if(sshRoot != null) {
			return new File(sshRoot);
		}
		return new File(new File(getUserHome()), ".ssh");
	}
	
	/**
	 * 获得当前系统命令行的参数
	 * 
	 * @return String
	 */
	public static String getCmdEncoding() {
		return System.getProperty("sun.jnu.encoding");
	}
	
	/**
	 * 获取操作系统的类型.
	 *
	 * @return OSType
	 */
	public static OSType getOSType() {
		if(osType != null)
			return osType;
		String osName = System.getProperty("os.name").toUpperCase();
		if(osName.contains("WINDOWS")) {
			osType = OSType.WINDOWS;
		} else if (osName.contains("LINUX")) {
			osType = OSType.LINUX;
		} else {
			// default value
			osType = OSType.LINUX;
		}
		
		return osType;
	}
	
	/**
	 * 结束进程
	 * 
	 * @param pid 进程id (String)
	 * @return 0 表示成功结束，其它表示错误码
	 */
	public static int killProcess(String pid) {
		pid = pid.trim();
		String cmd = null;
		switch(getOSType()) {
		case WINDOWS:
			cmd = "taskkill /F /PID " + pid;
			break;
		default:
			cmd = "kill -9 " + pid;
			break;
		}
		
		int exitVal = -1;
		try {
			Process proc = Runtime.getRuntime().exec(cmd);
			exitVal = proc.waitFor();
			if(exitVal == 0)
				return exitVal;
			
			String errorStr = IOUtil.readInputStreamAsLocalString(
					proc.getErrorStream());
			System.err.println("Fail to kill process "+ pid + ": " + errorStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return exitVal;
	}
	
	/**
	 * 运行cmd命令.
	 *
	 * @param cmd
	 * @return String
	 * @throws RemoteCmdException
	 */
	public static String runRemoteProcess(String cmd) throws RemoteCmdException{
		String ret = null;
		
		BackgroundOutput inputBo = null;
		BackgroundOutput errorBo = null;
		BackgroundOutputMgr backMgr = BackgroundOutputMgr.getInstance();
		int exitVal = 0;
		try {
			Process proc = Runtime.getRuntime().exec(cmd);
			inputBo = new BackgroundOutput(proc.getInputStream(), 
					new ByteArrayOutputStream(512));
			backMgr.addBackgroundOutput(inputBo);
			errorBo = new BackgroundOutput(proc.getErrorStream(), 
					new ByteArrayOutputStream(512));
			backMgr.addBackgroundOutput(errorBo);
			exitVal = proc.waitFor();
			if(exitVal == 0) {
				inputBo.tryToFinish();
				if(inputBo.hasException())
					throw inputBo.getException();
				ret = new String(((ByteArrayOutputStream)inputBo.outputStream).toByteArray());
			} else {
				errorBo.tryToFinish();
				if(errorBo.hasException())
					throw errorBo.getException();
				ret = new String(((ByteArrayOutputStream)errorBo.outputStream).toByteArray());
			}
		} catch (Throwable e) {
			throw new RemoteCmdException("Error Execute [" + ret + "]", e);
		} finally {
			backMgr.removeBackgroundOutput(inputBo);
			backMgr.removeBackgroundOutput(errorBo);
		}
		
		if(exitVal == 0)
			return ret;
		
		throw new RemoteCmdException("Error Execute [" + cmd + "]: " + ret);
	}
	
	/**
	 * TODO Put here a description of what this method does.
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		System.out.println(runRemoteProcess(args[0]));
	}
}
