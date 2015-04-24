package com.caorenhao.wbcrawler.conf;

import java.io.File;

import com.caorenhao.util.OSUtil;

/**
 * TODO Put here a description of what this class does.
 *
 * @author renhao.cao.
 *         Created 2015-1-8.
 */
public final class ProcessCtr {
	
	/**
	 * 是否开发模式
	 * 通过检查项目根目录下是否包含bin目录
	 * 
	 * @return boolean
	 */
	public static boolean isDevelopMode() {
		return (new File("bin/com/dbapp")).exists();
	}
	
	private static File rootDir;
	
	/**
	 * 获取程序的目录.
	 *
	 * @return File
	 * @throws Exception
	 */
	public static File getLocalSIndexRoot() throws Exception {
		if(OSUtil.getOSType().isWindows()) {
			rootDir = new File(".");		
		} else {
			rootDir = new File("/home/dicl/iyq/iyuqing");
		}
		return rootDir;
	}
}
