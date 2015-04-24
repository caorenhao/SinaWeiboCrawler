package com.caorenhao.net;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import com.caorenhao.util.IOUtil;
import com.caorenhao.util.task.PeriodTask;

/**
 * 日志清理任务.
 *
 * @author renhao.cao.
 *         Created 2015年4月15日.
 */
public class LogCleanTask extends PeriodTask {
	
	public static final long DEL_PERIOD = 24 * 60 * 60 * 1000;
	
	public static final long maxSize = 500 * 1024 * 1024;
	
	public static final int maxFileNum = 7;
	
	private File logDir;
	
	public LogCleanTask(File logDir) {
		super(DEL_PERIOD);
		this.logDir = logDir;
	}
	
	/**
	 * 用于删除日志文件，保存至少maxFileNum个，
	 * 如果文件总大小小于maxSize则多保留maxSize大小的文件
	 * @param maxSize 最大的容量
	 * @param maxFileNum 最多的文件数量
	 */
	public static void deleteLogFile(File logDir, long maxSize, int maxFileNum) {
		File [] files = logDir.listFiles();
		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				if (!o2.getName().contains(".")) {
					return 1;
				}
				if (!o1.getName().contains(".")) {
					return -1;
				}
				return o2.getName().compareTo(o1.getName());
			}
		});	
		int fileNum = 0;
		long fileSize = 0;
		while((fileNum < maxFileNum || fileSize < maxSize ) && fileNum < files.length) {
			fileSize += files[fileNum].length();
			fileNum++;
		}
		for(int i = fileNum; i < files.length; i++) {
			//System.out.println("del:" + i);
			IOUtil.deleteFile(files[i]);
			
		}
	}
	
	@Override
	public void run() throws Exception {
		deleteLogFile(logDir, maxSize, maxFileNum);
		super.run();
	}

	public static void main(String [] args) {
		deleteLogFile(new File("D:/temp/logs/_modulename_"), 16 * 1024 * 1024, 7);
	}
}
