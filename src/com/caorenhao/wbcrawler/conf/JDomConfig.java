package com.caorenhao.wbcrawler.conf;

import java.io.File;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

/**
 * 基于JDOM 解析 XML 配置文件
 * @author vernkin
 *
 */
public abstract class JDomConfig {
	
	protected File configFile;
	protected Document xmlDoc;
	protected Element rootEle;
	
	public JDomConfig() {
		
	}
	
	public boolean loadConfigFile(File configFile) throws Exception {
		return loadConfigFileImpl(configFile);
	}
	
	private boolean loadConfigFileImpl(File configFile) throws Exception {
		this.configFile = configFile;
		SAXBuilder builder = new SAXBuilder();
		xmlDoc = builder.build(configFile);
		rootEle = xmlDoc.getRootElement();
		parse();
		return true;
	}
	
	
	
	public boolean loadDefaultConfigFile() throws Exception {
		return loadConfigFileImpl(getDefaultConfigFile());
	}
	
	public abstract File getDefaultConfigFile() throws Exception;
	protected abstract void parse() throws Exception;

	public Document getDocument() {
		return xmlDoc;
	}

	public Element getRootEle() {
		return rootEle;
	}
	
	/**
	 * 得到关注的配置文件
	 * @return
	 */
	public File getConfigFile() {
		return configFile;
	}
	
	/**
	 * 根据类别和文件名获取配置文件的路径
	 * @param type 配置文件类型 （conf下所属的目录名）
	 * @param filename 配置文件名
	 * @return 配置文件路径
	 * @throws Exception 
	 */
	public static File findConfigFile(String type, String filename) 
			throws Exception {
		String relativePath = type + "/" + filename;
		File confFile = new File(ConfSelector.getInstance().
				getConfRootDir(), relativePath);
		return confFile;
	}

}
