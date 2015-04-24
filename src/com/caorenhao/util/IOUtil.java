package com.caorenhao.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

/**
 * IO操作类.
 *
 * @author renhao.cao.
 *         Created 2015年2月2日.
 */
public final class IOUtil {

	/**
	 * 流操作关闭.
	 *
	 * @param stream
	 */
	public static void forceClose(Closeable stream) {
		if(stream == null)
			return;
		try {
			stream.close();
		} catch(Throwable t){}
	}
	
	public static BufferedReader openBR(File file) throws Exception {
		return new BufferedReader(new FileReader(file));
	}
	
	public static PrintWriter openPW(File file) throws Exception {
		return new PrintWriter(new BufferedWriter(new FileWriter(file)));
	}
	
	public static FileOutputStream openFO(File file) throws Exception {
		return new FileOutputStream(file);
	}
	
	public static ObjectOutputStream openOO(File file) throws Exception {
		return new ObjectOutputStream(openFO(file));
	}
	
	public static ObjectInputStream openOI(File file) throws Exception {
		return new ObjectInputStream(new FileInputStream(file));
	}
	
	public static void writeObjectToFile(File file, Object obj) throws Exception {
		ObjectOutputStream oos = null;
		try {
			oos = openOO(file);
			oos.writeObject(obj);
		} catch (Exception e) {
			throw e;
		}finally{
			forceClose(oos);
		}
	}
	
	public static Object readObjectFromFile(File file) throws Exception {
		ObjectInputStream ois = null;
		Object obj = null;
		try {
			ois = openOI(file);
			obj = ois.readObject();
		} catch (Exception e) {
			throw e;
		}finally {
			forceClose(ois);
		}
		return obj;
	}
	
	/**
	 * Write Objects to String
	 * @param file output file
	 * @param objs object lists
	 * @throws Exception
	 */
	public static<T> void writeObjectsToFile(File file, List<T> objs) throws Exception {
		ObjectOutputStream oos = null;
		try {
			oos = openOO(file);
			for(Object obj : objs)
				oos.writeObject(obj);
		} catch (Exception e) {
			throw e;
		}finally{
			forceClose(oos);
		}
	}
	
	/**
	 * Read Objects from a file
	 * @param file input file
	 * @param output output list. Create a new <code>ArrayList<T></code> If null
	 * @return output list
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static<T> List<T> readObjectsFromFile(File file, List<T> output) throws Exception {
		ObjectInputStream ois = null;
		if(output == null)
			output = new ArrayList<T>();
		try {
			ois = openOI(file);
			while(ois.available() > 0) {
				output.add((T)ois.readObject());
			}
		} catch (Exception e) {
			throw e;
		}finally {
			forceClose(ois);
		}
		return output;
	}
	
	public static void writeToFile(File file, String content) throws Exception {
		PrintWriter pw = null;
		try {
			pw = openPW(file);
			pw.print(content);
		} catch(Exception e) { throw e;
		} finally { forceClose(pw); }
	}
	
	public static void writeToFile(File file, byte [] content) throws Exception {
		FileOutputStream fo = null;
		try {
			fo = openFO(file);
			fo.write(content);
			fo.flush();
		} catch (Exception e) {
			throw e;
		}finally { forceClose(fo); }
	}
	
	/**
	 * 将 输出流 写入到 输入流中。函数不关闭输出流和输入流
	 * @param in 输入流
	 * @param out 输出流
	 * @throws Exception
	 */
	public static void writeOutputStreamToInputStream(InputStream in, 
			OutputStream out) throws Exception {
		byte[] buffer = new byte[512];
		int readLen = -1;
		while ((readLen = in.read(buffer)) > 0) {
			out.write(buffer, 0, readLen);
		}
	}
	
	public static void appendToFile(File file, String content) throws Exception {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
			pw.print(content);
		} catch(Exception e) { throw e;
		} finally { forceClose(pw); }
	}
	
	/**
	 * Write String List to the specified file
	 * @param file target file
	 * @param strList String list
	 * @throws Exception
	 */
	public static void writeStringListToFile(File file, 
			Collection<String> strList) throws Exception {
		PrintWriter pw = null;
		try {
			pw = openPW(file);
			for(String str : strList)
				pw.println(str);
		} catch(Exception e) { throw e;
		} finally { forceClose(pw); }
	}
	
	/**
	 * Write Properties to the specified file
	 * @param prop properties
	 * @param file output file
	 * @param comment properties comment
	 * @throws Exception
	 */
	public static void writeProperties(File file, Properties prop, 
			String comment) throws Exception {
		PrintWriter pw = null;
		try {
			pw = openPW(file);
			prop.store(pw, comment);
		} catch(Exception e) { throw e;
		} finally { forceClose(pw); }
	}
	
	
	public static Properties readProperties(File file) throws Exception {
		Properties prop = new Properties();
		BufferedReader br = null;
		try {
			br = openBR(file);
			prop.load(br);
		} catch(Exception e) { throw e;
		} finally { forceClose(br); }
		return prop;
	}
	
	/**
	 * Read Line By Line From the specified file
	 * @param file the source file
	 * @param output output list, create one if null
	 * @return output String list
	 * @throws Exception
	 */
	public static List<String> readStringListFromFile(File file, 
			List<String> output) throws Exception {
		BufferedReader br = null;
		try {
			br = openBR(file);
			if(output == null)
				output = new ArrayList<String>();
			String line;
			while(true) {
				line = br.readLine();
				if(line == null)
					break;
				output.add(line);
			}			
			return output;
		} catch(Exception e) { throw e; 
		} finally { forceClose(br); }
	}
	
	public static Set<String> readUniqStringSetFromFile(File file) throws Exception {
		Set<String> ret = new HashSet<String>();
		BufferedReader br = null;
		try {
			br = openBR(file);
			String line;
			while(true) {
				line = br.readLine();
				if(line == null)
					break;
				line = StrUtil.emptyStringToNull(line, true);
				if(line == null)
					continue;
				ret.add(line);
			}			
			return ret;
		} catch(Exception e) { throw e; 
		} finally { forceClose(br); }
	}
	
	public static byte[] readFileAsByteArray(File file) throws Exception {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			byte[] buf = new byte[(int)file.length()];
			fis.read(buf);
			return buf;
		} catch(Exception e) { throw e;
		} finally { forceClose(fis); }
	}
	
	/**
	 * 读取文件当做一个字符串，文件的编码默认跟系统的编码一致。
	 * 如输出的字符串为乱码，参考 readFileAsString(File, String)
	 * @param file 输入文件
	 * @return 文件的内容
	 * @throws Exception
	 */
	public static String readFileAsString(File file) throws Exception {
		byte[] bytes = readFileAsByteArray(file);
		return new String(bytes);
	}
	
	/**
	 * 读取文件当做一个字符串，文件的编码由 charset 指定
	 * 输出的字符串为 utf-8 编码
	 * @param file 输入的文件
	 * @param charset 输入的文件的编码
	 * @return 输出的字符串的 utf-8 形式
	 * @throws Exception
	 */
	public static String readFileAsString(File file, String charset) throws Exception {
		byte[] bytes = readFileAsByteArray(file);
		return new String(bytes, charset);
	}
	
	public static String readFirstLineOfFile(File file) throws Exception {
		BufferedReader br = null;
		try {
			br = openBR(file);
			String line = br.readLine();
			return line;
		} catch(Exception e) { throw e; 
		} finally { forceClose(br); }
	}
	
	/**
	 * 读取InputStream到一个byte数组中
	 * 程序保证关闭 input输入流
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public static byte[] readInputStreamAsByteArray(InputStream input) 
			throws Exception {
		try {
			byte buf[] = new byte[256];
			int readLen = -1;
			ByteArrayOutputStream output = new ByteArrayOutputStream(512);
			while((readLen = input.read(buf)) >= 0) {
				if(readLen > 0)
					output.write(buf, 0, readLen);
			}
			
			output.close();
			input.close();
			return output.toByteArray();
		} catch (Exception e) {
			throw e;
		} finally {
			IOUtil.forceClose(input);
		}
	}
	
	public static String readInputStreamAsLocalString(InputStream input) 
			throws Exception {
		byte bytes[] = readInputStreamAsByteArray(input);
		return new String(bytes, OSUtil.getCmdEncoding());
	} 
	
	public static String readInputStreamAsString(InputStream input, String enc) 
	throws Exception {
		byte bytes[] = readInputStreamAsByteArray(input);
		if(enc == null)
			return new String(bytes);
		else
			return new String(bytes, enc);
	} 
	
	/**
	 * 删除文件/或者目录
	 * @param f 目标文件或者目录
	 * @return true 如果完全删除成功
	 */
	public static boolean deleteFile(File f) {
		return deleteFile(f, null);
	}
	
	/**
	 * 删除目录下符合条件的文件
	 * @param f 目标文件或者目录
	 * @return true 如果完全删除成功
	 */
	public static boolean deleteFile(File f, FileFilter filter) {
		if(f.isFile()) {
			boolean bRet = true;
			if(filter == null || filter.accept(f))
				bRet = f.delete();
			return bRet;
		}
		
		if(!f.isDirectory())
			return false;
		
		boolean ret = true;			
		// 布尔值表示是否遍历过
		Stack<Pair<File, Boolean>> stack = new Stack<Pair<File, Boolean>>();		
		stack.push(new Pair<File, Boolean>(f, false));
				
		while(stack.isEmpty() == false) {
			Pair<File, Boolean> curDir = stack.peek();
			if(curDir.second) {
				if(filter == null || filter.accept(curDir.first))
					ret = ret && curDir.first.delete();
				
				stack.pop();
				continue;
			}
			
			File[] children = curDir.first.listFiles();
			curDir.second = true; // 标记为遍历过
			if(children == null || children.length == 0) {
				if(filter == null || filter.accept(curDir.first))
					ret = ret && curDir.first.delete();
				stack.pop();
				continue;
			}
			
			for(File child : children) {
				if(child.isDirectory()) {
					stack.push(new Pair<File, Boolean>(child, false));
					continue;
				}
				if(filter == null || filter.accept(child))
					ret = ret && child.delete();
			}
		}
		
		return ret;
	}
	
	public static boolean moveFile(File src, File dest) {
		return src.renameTo(dest);
	}

	public static void appendToLogFile(File file, String content) throws Exception {
		long modifyTime = file.lastModified();
		long todayTime = DateUtil.getToDayTime(new Date());
		if (modifyTime < todayTime) {
			String lastLogDate = DateUtil.getDayTimeString(modifyTime);
			String lastLogFile = file.getAbsolutePath() + "." + lastLogDate;
			moveFile(file, new File(lastLogFile));			
		}
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
			pw.print(content);
		} catch(Exception e) { throw e;
		} finally { forceClose(pw); }
	}
	
	public static Map<String, String> readPropertyFile(File file) {
		InputStream inputStream = null;
		Properties p = new Properties();
		Map<String, String> properties = new HashMap<String, String>();
		try {
			inputStream = new FileInputStream(file);
			p.load(inputStream);
			for(Object obj: p.keySet()) {
				String key = (String) obj;
				String value = p.getProperty(key);
				properties.put(key, value);
			}
		} catch (FileNotFoundException e1) {
		} catch (IOException e) {
		} finally {
			forceClose(inputStream);
		}
		return properties;
	}
	
	public static boolean writePropertyToFile(File file, Map<String, String> properties) {
		boolean bRet = true;
		Properties p = new Properties();
		p.putAll(properties);
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(file);
			p.store(outputStream, null);
		} catch (FileNotFoundException e) {
			bRet = false;
		} catch (IOException e) {
			bRet = false;
		} finally {
			forceClose(outputStream);
		}
		return bRet;
	}
	
	public static boolean updatePropertyFile(File file, Map<String, String> properties) {
		boolean bRet = true;
		Map<String, String> oldProperties = readPropertyFile(file);
		oldProperties.putAll(properties);
		bRet = writePropertyToFile(file, oldProperties);
		return bRet;
	}
	
	/**
	 * 将对象通过系列化转换成字节数组
	 */
	public static <T extends Serializable> byte[] objectToByteArray(T obj) 
			throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
		ObjectOutputStream bos = new ObjectOutputStream(baos);
		bos.writeObject(obj);
		bos.close();
		return baos.toByteArray();
	}
	
	/**
	 * 将字节数组还原成原来的对象
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T byteArrayToObject(byte[] input) 
			throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(input);
		ObjectInputStream bis = new ObjectInputStream(bais);
		Object obj = bis.readObject();
		bis.close();
		return (T)obj;
	}
	
	/**
	 * 获取目录下面扩展名为ext的文件
	 * @param dir	获取文件的目录
	 * @param ext	获取文件的扩展名
	 * @return	返回一个文件列表
	 * @throws Exception 如果传入的参数目录不是一个目录，或是目录不存在，抛出异常
	 */
	public static File [] listFilesByExt(File dir, String ext) throws Exception {
		
		if (!dir.exists() || !dir.isDirectory()) {
			throw new Exception("Dir error File[" + dir.getPath() + "] is not exist or not a dir");
		}
		final String _ext = ext;
		File [] files = dir.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				boolean ret = name.endsWith(_ext);
				return ret;
			}
		});
		return files;
	}
	
	// 复制文件
    public static void copyFile(File sourceFile, File targetFile) throws IOException {
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            // 新建文件输入流并对它进行缓冲
            inBuff = new BufferedInputStream(new FileInputStream(sourceFile));

            // 新建文件输出流并对它进行缓冲
            outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));

            // 缓冲数组
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            // 刷新此缓冲的输出流
            outBuff.flush();
        } finally {
            // 关闭流
            if (inBuff != null)
                inBuff.close();
            if (outBuff != null)
                outBuff.close();
        }
    }

    // 复制文件夹
    public static void copyDirectiory(String sourceDir, String targetDir) throws IOException {
        // 新建目标目录
        (new File(targetDir)).mkdirs();
        // 获取源文件夹当前下的文件或目录
        File[] file = (new File(sourceDir)).listFiles();
        for (int i = 0; i < file.length; i++) {
            if (file[i].isFile()) {
                // 源文件
                File sourceFile = file[i];
                // 目标文件
                File targetFile = new File(new File(targetDir).getAbsolutePath() + File.separator + file[i].getName());
                copyFile(sourceFile, targetFile);
            }
            if (file[i].isDirectory()) {
                // 准备复制的源文件夹
                String dir1 = sourceDir + "/" + file[i].getName();
                // 准备复制的目标文件夹
                String dir2 = targetDir + "/" + file[i].getName();
                copyDirectiory(dir1, dir2);
            }
        }
    }

    /**
     * 
     * @param srcFileName
     * @param destFileName
     * @param srcCoding
     * @param destCoding
     * @throws IOException
     */
    public static void copyFile(File srcFileName, File destFileName, String srcCoding, String destCoding) throws IOException {// 把文件转换为GBK文件
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(srcFileName), srcCoding));
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destFileName), destCoding));
            char[] cbuf = new char[1024 * 5];
            int len = cbuf.length;
            int off = 0;
            int ret = 0;
            while ((ret = br.read(cbuf, off, len)) > 0) {
                off += ret;
                len -= ret;
            }
            bw.write(cbuf, 0, off);
            bw.flush();
        } finally {
            if (br != null)
                br.close();
            if (bw != null)
                bw.close();
        }
    }
}
