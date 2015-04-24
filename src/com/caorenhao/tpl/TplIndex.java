package com.caorenhao.tpl;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.caorenhao.util.BooleanLock;
import com.caorenhao.util.LoggerConfig;
import com.caorenhao.wbcrawler.conf.ProcessCtr;

/**
 * 模板索引
 * @author scotte.ye
 *
 */
public class TplIndex {
	
	private static Log LOGGER = LoggerConfig.getLog(TplIndex.class);

	/** 模板路径 */
	private static final String tplPath = "data/parse_tpls";

	/** 是否在刷新模板 */
	public static BooleanLock isRefresh = new BooleanLock(false);
	
	/**
	 * 模板索引信息
	 */
	private static Map<String, TplIndexItem> tplIndex = new HashMap<String, TplIndexItem>();
	
	/**
	 * 保存一个模板文件，或是更新一个模板文件
	 * @param domain 域名
	 * @param tplFile 模板文件
	 * @param isList 是否为列表，true列表，false文章
	 */
	public static void saveTpl(String domain, File tplFile, boolean isList, Map<String, TplIndexItem> newTplIndex) {
		TplIndexItem item = newTplIndex.get(domain);
		if (item == null) {
			item = new TplIndexItem();
			newTplIndex.put(domain, item);
		}
		item.updateTpl(tplFile, isList);
	}

	/**
	 * 获取当前域名下面的所有可用模板
	 * @param domain 域名
	 * @param isList 是否为列表，true列表，false文章
	 * @return 如果存在模板返回模板集合，否则返回null
	 */
	public static Set<TplFileInfo> getTpls(String domain, boolean isList) {
		TplIndexItem item = tplIndex.get(domain);
		if (item == null) {
			LOGGER.info("domain[" + domain + "] isList[" + isList + "] get tpls null");
			return null;
		}
		return item.getTpls(isList);
	}
	
	/**
	 * 更新索引, 第一次调用就是加载
	 */
	public static void refreshIndex() {
		synchronized (isRefresh) {
			Map<String, TplIndexItem> newTplIndex = new HashMap<String, TplIndexItem>();

			File tplDir = null;
			try {
				tplDir = getRelativeFile(tplPath);
			} catch (Exception e) {
				LOGGER.warn("Template refresh failed for tpls dir[" + tplPath 
						+ "] was not exist");
				return;
			}
			//获取所有域名
			File [] domainDirs = tplDir.listFiles(new FileFilter() {
				
				@Override
				public boolean accept(File pathname) {
					//过滤隐藏文件，主要如.svn
					if (pathname.getName().startsWith(".")) {
						return false;
					}
					return pathname.isDirectory();
				}
			});
			
			//解析下面的模板
			for(File domainDir: domainDirs) {
				loadTemplate(domainDir, newTplIndex);
			}	
			
			// 更新索引
			tplIndex = newTplIndex;
		}
	}
	
	private static void loadTemplate(File domainDir, Map<String, TplIndexItem> newTplIndex) {
		File listDir = new File(domainDir, "list");
		LOGGER.info("Load template[" + domainDir.getName() + "]");
		if (listDir.exists()) {
			File [] listFiles = listDir.listFiles(new FileFilter() {
				
				@Override
				public boolean accept(File pathname) {
					//过滤隐藏文件，主要如.svn
					if (pathname.getName().startsWith(".")) {
						return false;
					}
					return pathname.isFile();
				}
			});
			for(File file: listFiles) {
				try {
					// 保存模板
					saveTpl(domainDir.getName(), file, true, newTplIndex);
				} catch (Exception e) {
					LOGGER.warn("Template parse error Css selector list template[" + file.getName() + "] in" +
							" domain[" + domainDir.getName() + "] parse failed:", e);
				}
			}
		}
		File articleDir = new File(domainDir, "article");
		if (articleDir.exists()) {
			File [] articleFiles = articleDir.listFiles(new FileFilter() {
				
				@Override
				public boolean accept(File pathname) {
					//过滤隐藏文件，主要如.svn
					if (pathname.getName().startsWith(".")) {
						return false;
					}
					return pathname.isFile();
				}
			});
			for(File file: articleFiles) {
				try {
					saveTpl(domainDir.getName(), file, false, newTplIndex);
				} catch (Exception e) {
					LOGGER.warn("Template parse error Css selector article template[" + file.getName() + "] in" +
							" domain[" + domainDir.getName() + "] parse failed:", e);
				}
			}
		}		
	}
	
	/**
	 * 初始化第一次加载
	 */
	public static void init() {
		refreshIndex();
	}
	
	/**
	 * 获取相对路径
	 * @param relPath
	 * @return
	 * @throws Exception
	 */
	public static File getRelativeFile(String relPath) throws Exception {
		return new File(ProcessCtr.getLocalSIndexRoot(), relPath);
	}
	
	public static String toItemString() {
		StringBuffer buffer = new StringBuffer();
		for (Map.Entry<String, TplIndexItem> entry : tplIndex.entrySet()) {
			buffer.append(entry.getKey() + ":\r\n");
			buffer.append("\tlist:\r\n");
			for(TplFileInfo fInfo : entry.getValue().getTpls(true)) {
				buffer.append("\t\tfile[" + fInfo.getTplFile() + "]\r\n");
			}
			buffer.append("\tarticle:\r\n");
			for(TplFileInfo fInfo : entry.getValue().getTpls(false)) {
				buffer.append("\t\tfile[" + fInfo.getTplFile() + "]\r\n");
			}
		}
		return buffer.toString();
	}
	
}
