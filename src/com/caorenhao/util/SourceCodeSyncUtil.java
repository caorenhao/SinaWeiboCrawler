package com.caorenhao.util;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * 两个源代码之间进行同步
 * @author vernkin
 *
 */
public class SourceCodeSyncUtil {

	/**
	 * 选择同步的文件过滤器
	 * @author vernkin
	 *
	 */
	public static interface SyncFileFilter {
		/**
		 * 是否判断指定的文件
		 * @param src 源文件
		 * @param relativePath 相对路径，文件分隔符统一采用 "/"
		 * @return true 表示处理该文件
		 */
		boolean accept(File src, String relativePath);
	}
	
	public static void sync(File destDir, File srcDir, 
			SyncFileFilter filter) throws Exception {
		String destPath = destDir.getAbsolutePath().replaceAll("\\\\", "/");
		if(destPath.endsWith("/") == false)
			destPath = destPath + "/";
		syncDirImpl(destDir, srcDir, null, destPath, filter);
	}
	
	/**
	 * 同步目录
	 */
	private static void syncDirImpl(File destRoot, File srcRoot, 
			String relativePath, String destRootPath, SyncFileFilter filter) 
			throws Exception {
		File dest = destRoot;
		if( relativePath != null ) {
			dest = new File(destRoot, relativePath);
			if(filter != null && !filter.accept(dest, relativePath))
				return;
		} 
		
		for(File curDest : dest.listFiles()) {
			String curRelativePath = curDest.getAbsolutePath().
					replaceAll("\\\\", "/").substring(destRootPath.length());
			if(filter != null && !filter.accept(curDest, curRelativePath))
				continue;
			if(curDest.isDirectory()) {
				// 继续同步目录
				syncDirImpl(destRoot, srcRoot, curRelativePath, destRootPath, filter);
				continue;
			}
			
			if(curDest.isFile() == false)
				continue;
			
			// 同步文件
			File curSrc = new File(srcRoot, curRelativePath);
			if(curSrc.exists() == false) {
				// 对应的源目录不存在，忽略
				continue;
			}
			
			// 忽略完全一致的内容
			if(isSameContent(curDest, curSrc))
				continue;
			System.out.println("copy " + curRelativePath + "\t" + curSrc + " => " + curDest);
			// 拷贝内容
			byte[] curSrcContent = IOUtil.readFileAsByteArray(curSrc);
			IOUtil.writeToFile(curDest, curSrcContent);
		}
	}
	
	/**
	 * 判断文件(非目录)内容是否一致
	 * @param file1 第一个文件
	 * @param file2 第二个文件
	 * @return true表示文件内容一致
	 */
	public static boolean isSameContent(File file1, File file2) 
			throws Exception {
		if(!file1.isFile() || !file2.isFile()) {
			throw new IllegalArgumentException("file1 and file2 " +
					"should be file, not directory");
		}
		if(file1.length() != file2.length())
			return false;
		return getMD5(file1).equals(getMD5(file2));
	}
	
	/**
	 * 得到一个文件的MD5值,
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static String getMD5(File file) throws Exception{
		if(file.isDirectory())
			return "d";
		MessageDigest digest = MessageDigest.getInstance("MD5");
		FileInputStream input = new FileInputStream(file);
		byte[] buffer = new byte[1024];
		int len = -1;
		while((len = input.read(buffer))!= -1){
			digest.update(buffer,0,len);
		}
		input.close();
		byte[] by = digest.digest();
		BigInteger bigInt = new BigInteger(1,by);
		return bigInt.toString();
	}
}
