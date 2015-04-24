package com.caorenhao.tpl;

import java.io.File;

import com.caorenhao.vcrawler.vextractor.CssSelectorTemplate;
import com.caorenhao.vcrawler.vextractor.ExtractorTemplateItf;

/**
 * 模板文件的基本信息
 * @author scotte.ye
 *
 */
public class TplFileInfo {

	/**
	 * 文件路径
	 */
	private String tplFile;
	
	/**
	 * 记录文件修改时间 
	 */
	private long modifyTime;
	
	/**
	 * 记录文件的大小
	 */
	private long size;
	
	/**
	 * 是否已经更新
	 */
	private boolean isUpdate = false;
	
	/**
	 * 真正的模板
	 */
	private CssSelectorTemplate tpl;
	
	public TplFileInfo(String tplFile, long modifyTime, long size) {
		this.tplFile = tplFile;
		this.modifyTime = modifyTime;
		this.size = size;
		// 第一次生成的时候，文件一定是要被更新的
		isUpdate = true;
	}
	
	/**
	 * 判断模板文件是否有更新, 同时更新模板文件的时间
	 * @param modifyTime 文件修改时间
	 * @param size	文件大小
	 * @return	如果没有修改返回false,否则返回true
	 */
	public synchronized void update(long modifyTime, long size) {
		if (this.modifyTime != modifyTime || this.size != size) {
			this.modifyTime = modifyTime;
			this.size = size;
			isUpdate = true;
		} else {
			isUpdate = false;
		}
	}
	
	/**
	 * 获取模板
	 * @return 获取成功返回模板，否则返回NULL
	 * @throws Exception
	 */
	public synchronized ExtractorTemplateItf getTpl() throws Exception {
		if (isUpdate) {
			File file = new File(tplFile);
			tpl = new CssSelectorTemplate(file);
			isUpdate = false;
		} 
		return tpl;
	}
	
	public String getTplFile() {
		return tplFile;
	}

	public void setTplFile(String tplFile) {
		this.tplFile = tplFile;
	}

}
