package com.caorenhao.net;

import java.io.File;
import java.io.IOException;

import com.caorenhao.util.BooleanLock;
import com.caorenhao.util.task.TaskRunner;

/**
 * 一个节点的工作目录, 只用来管理目录结构，本身不存储目录
 * @author vernkin
 *
 */
public class WorkingDir {
	
	/**
	 * 获取其它节点的路径 (File 属性无效)
	 * @param file
	 * @return
	 */
	public static String getRemoteFilePath(File file) {
		String path = file.getPath();
		return path.replaceAll("\\\\", "/");
	}
	
	protected File rootDir;
	/** 是否属于本节点 */
	protected boolean isLocal;
	
	/** 数据目录 */
	private File dataDir;
	
	/** 配置目录 */
	private File confDir;
	
	/** 日志目录 */
	private File logDir;
	
	/** 临时文件目录 */
	private File tmpDir;
	
	/** 备份目录，用以关机时保存本地数据 和 下次启动恢复数据 */
	private File backupDir;
	
	/** 启动参数文件 */
	private File runParamsFile;
	
	/** 进程ID文件 */
	private File pidFile;
	
	/**
	 * isLocal为true的构造函数
	 * @param rootDirPath
	 * @throws Exception
	 */
	public WorkingDir(String rootDirPath) throws Exception {
		this(rootDirPath, true);
	}
	
	/**
	 * 创造工作目录。isLocal 为true是本节点工作目录，会自动创建需要的目录。
	 * 本函数自动创造pid方法
	 * @param rootDirPath 根目录路径
	 * @param isLocal 是否属于本节点的工作目录
	 * @throws IOException
	 */
	public WorkingDir(String rootDirPath, boolean isLocal) throws Exception {
		this.isLocal = isLocal;
		rootDir = new File(rootDirPath);
		if(isLocal) {
			if(rootDir.exists() == false)
				rootDir.mkdirs();
			
			if(rootDir.exists() == false)
				throw new Exception("Can't mkdir WorkingDir rootDir: " + rootDir);
			if(rootDir.isDirectory() == false) {
				throw new Exception("WorkingDir rootDir isn't a directory: " + 
						rootDir);
			}
		}
		
		dataDir = getAbsDir("data");
		confDir = getAbsDir("conf");
		logDir = getAbsDir("log");
		tmpDir = getAbsDir("tmp");
		backupDir = getAbsDir("backup");
		
		pidFile = new File(rootDir, "run.pid");
		runParamsFile = new File(rootDir, "run_params.properties");
	}
	
	/**
	 * isLocal为true时候，获取rootDir下相对目录，目录不存在时候自动创建。
	 * 为false不创建
	 * @param absName 相对路径
	 * @return
	 */
	protected File getAbsDir(String absName) {
		File ret = new File(rootDir, absName);
		if(isLocal && ret.exists() == false)
			ret.mkdirs();
		return ret;
	}
	
	public File getDataDir() {
		return dataDir;
	}
	
	public File getConfDir() {
		return confDir;
	}
	
	public File getLogDir() {
		return logDir;
	}
	
	public File getTmpDir() {
		return tmpDir;
	}
	
	/**
	 * 备份目录，用以关机时保存本地数据 和 下次启动恢复数据
	 * @return
	 */
	public File getBackupDir() {
		return backupDir;
	}
	
	/**
	 * 启动参数文件
	 * @return
	 */
	public File getRunParamsFile() {
		return runParamsFile;
	}
	
	public File getPidFile() {
		return pidFile;
	}

	//删除日志线程
	public static TaskRunner delLogRunner = new TaskRunner("del log");
	public static LogCleanTask logCleanTask;
	private static BooleanLock logCleanTaskLock = new BooleanLock();
	/**
	 * 开启日志清除，由手工开启
	 */
	public void startLogClean() {
		synchronized(logCleanTaskLock) {
			if (logCleanTask != null) {
				return ;
			}
			logCleanTask = new LogCleanTask(getLogDir());
			delLogRunner.addTask(logCleanTask);
			delLogRunner.start();
		}
	}
}
