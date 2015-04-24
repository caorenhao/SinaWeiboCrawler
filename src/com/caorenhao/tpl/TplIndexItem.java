package com.caorenhao.tpl;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.caorenhao.vcrawler.vextractor.ExtractorTemplateItf;

/**
 * 一个域名下面的所有模板
 * @author scotte.ye
 *
 */
public class TplIndexItem {

	/**
	 * 域名
	 */
	private String domain;
	
	/**
	 * 文章页的所有模板 文件路径=>模板文件信息
	 */
	private Map<String, TplFileInfo> articleTplFiles = new HashMap<String, TplFileInfo>();

	/**
	 * 列表页面的所有模板 文件路径=>模板文件信息
	 */
	private Map<String, TplFileInfo> listTplFiles = new HashMap<String, TplFileInfo>();
	
	/**
	 * 更新模板文件信息，如果模板存在，更新，如果不存在，添加
	 * @param tplFile 模板文件
	 * @param isList 是否为列表
	 */
	public void updateTpl(File tplFile, boolean isList) {
		String tplFilePath = tplFile.getAbsolutePath();
		if (isList) {
			// 更新队列模板
			TplFileInfo listFile = listTplFiles.get(tplFilePath);
			if (listFile == null) {
				// 如果模板不存在，说明是新增加模板，返回需要更新模板
				listFile = new TplFileInfo(tplFilePath, tplFile.lastModified(),
								tplFile.length());
				listTplFiles.put(tplFilePath, listFile);
			} else {
				listFile.update(tplFile.lastModified(), tplFile.length());
			}
		} else {
			// 更新文章模板
			TplFileInfo articleFile = articleTplFiles.get(tplFilePath);
			if (articleFile == null) {
				articleFile = new TplFileInfo(tplFilePath, tplFile.lastModified(),
						tplFile.length());
				articleTplFiles.put(tplFilePath, articleFile);
			} else {
				articleFile.update(tplFile.lastModified(), tplFile.length());
			}
		}
	}
	
	/**
	 * 获取所有的模板集合
	 * @param isList 是否为列表
	 * @return
	 */
	public Set<TplFileInfo> getTpls(boolean isList) {
		Set<TplFileInfo> tpls = new HashSet<TplFileInfo>();
		if (isList) {
			tpls.addAll(listTplFiles.values());
		} else {
			tpls.addAll(articleTplFiles.values());
		}
		return tpls;
	}
	
	/**
	 * 获取一个模板，直接可以用于抽取
	 * @param tplFile 模板文件名称
	 * @param isList 是否为列表
	 * @return 如果获取成功，返回模板，如果失败，返回NULL
	 * @throws Exception
	 */
	public ExtractorTemplateItf getTpl(String tplFile, boolean isList)
			throws Exception {
		if (isList) {
			TplFileInfo fileInfo = listTplFiles.get(tplFile);
			return fileInfo.getTpl();
		} else {
			TplFileInfo fileInfo = articleTplFiles.get(tplFile);
			return fileInfo.getTpl();
		}
	}
	
	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
}
