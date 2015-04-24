package com.caorenhao.wbcrawler.conf;

import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import com.caorenhao.net.NetGlobalVars;
import com.caorenhao.util.ConfigException;
import com.caorenhao.util.LoggerConfig;
import com.caorenhao.util.NetUtil;
import com.caorenhao.util.StrUtil;

/**
 * 根据环境选择不同的配置文件组
 * @author vernkin
 *
 */
public class ConfSelector {

	private static ConfSelector instance;
	
	public static ConfSelector getInstance() throws Exception {
		if(instance == null)
			instance = new ConfSelector();
		return instance;
	}
	
	/**
	 * Selector配置文件基本
	 * @author vernkin
	 *
	 */
	private class SelectorBase {
		public String conf;
		
		public String description;
		
		public void parse(Element ele) throws Exception {
			conf = ele.getChildText("conf");
			if(conf == null) {
				throw new ConfigException("'conf' is not set");
			}
			
			Element despEle = ele.getChild("description");
			if(despEle != null)
				description = despEle.getText();
		}
		
		public String toString() {
			return "Selctor[" + conf + "|" + description + "]";
		}
	}
	
	/**
	 * 选择条件
	 * @author vernkin
	 *
	 */
	private abstract class Condition {
		
		protected boolean active = true;
		
		public void parse(Element ele) throws Exception {
			Attribute activeAttr = ele.getAttribute("active");
			if(activeAttr != null && activeAttr.getValue().equalsIgnoreCase("false")) {
				active = false;
			}
		}
		
		public boolean isActive() {
			return active;
		}
		
		/**
		 * 是否匹配
		 * @param param 传递的参数，不同类别的Condition传入不同
		 * @return
		 */
		public abstract boolean isMatched(Object param);
	}
	
	/**
	 * 根据域名或者IP作为选择条件
	 * @author vernkin
	 *
	 */
	private class DomainCond extends Condition {
		public Set<String> domains = new HashSet<String>();

		@Override
		public void parse(Element ele) throws Exception {
			super.parse(ele);
			for(Object obj : ele.getChildren("domain")) {
				if((obj instanceof Element) == false)
					continue;
				Element curEle = (Element)obj;
				String curDomain = StrUtil.emptyStringToNull(curEle.getText(), true);
				if(curDomain != null)
					domains.add(curDomain);
			}
		}

		@Override
		public boolean isMatched(Object param) {
			if(domains.isEmpty())
				return false;
			
			for(String domain : domains) {
				try {
					if(NetUtil.isLocalAddress(domain))
						return true;
				} catch (UnknownHostException e) {
					// Ignore Invalid Host
					continue;
				}
			}
			
			return false;
		}
	}
	
	/**
	 * 根据环境变量作为选择条件
	 * @author vernkin
	 *
	 */
	private class EnvCond extends Condition {
		private String key;
		private String value;

		@Override
		public void parse(Element ele) throws Exception {
			super.parse(ele);
			key = StrUtil.emptyStringToNull(ele.getChildText("key"), true);
			value = StrUtil.nullToEmptyString(ele.getChildText("value"), true);
		}

		@Override
		public boolean isMatched(Object param) {
			if(key == null)
				return false;
			// 读取环境变量
			String envValue = System.getenv(key);
			if(envValue == null)
				return false;
			return envValue.equals(value);
		}
	}
	
	private class Selector extends SelectorBase {
	
		private boolean active = true;
		private List<Condition> conds = new ArrayList<Condition>();
		
		public void parse(Element ele) throws Exception {
			super.parse(ele);
			
			Attribute activeAttr = ele.getAttribute("active");
			if(activeAttr != null && activeAttr.getValue().equalsIgnoreCase("false")) {
				active = false;
			}
			
			// 解析条件
			for(Object obj : ele.getChild("conditions").getChildren()) {
				if((obj instanceof Element) == false)
					continue;
				Element curEle = (Element)obj;
				Condition curCond = null;
				if(curEle.getName().equals("envCond")) {
					curCond = new EnvCond();
				} else if(curEle.getName().equals("domainCond")) {
					curCond = new DomainCond();
				} else {
					LOGGER.warn("Unknonw Selector: " + curEle.getName());
					continue;
				}
				
				curCond.parse(curEle);
				conds.add(curCond);
			}
		}
		
		public boolean isActive() {
			return active;
		}
		
		/**
		 * 检查是否符合Condition列表中的一条
		 * @param myDomains
		 * @return
		 */
		public boolean isMatched(Set<String> myDomains) {
			if(conds.isEmpty())
				return false;
			for(Condition cond : conds) {
				if(cond.isActive() == false)
					continue;
				if(cond.isMatched(myDomains))
					return true;
			}
			return false;
		}
	}
	
	
	private Log LOGGER = LoggerConfig.getLog(getClass()); 
	
	/** 配置文件的根目录 */
	private File confRootDir;
	private File slectorConfFile;
	
	/** 具体的元素 */
	private SelectorBase defaultSelect = new SelectorBase();
	private List<Selector> selectors = new ArrayList<Selector>();
	
	private ConfSelector() throws Exception {
		confRootDir = new File(ProcessCtr.getLocalSIndexRoot(), "conf");
		slectorConfFile = new File(confRootDir, "conf_selector.xml");
		loadSelectorFile();
	}
	
	private void loadSelectorFile() throws Exception {
		if(NetGlobalVars.confSelect !=  null) {
			LOGGER.info("GlobalVars.confSelect is not null (" + NetGlobalVars.confSelect + 
					"), DO NOT parse conf_selector.xml");
			return;
		}
		LOGGER.info("To parse conf_selector.xml");
		
		SAXBuilder builder = new SAXBuilder();
		Document xmlDoc = builder.build(slectorConfFile);
		Element rootEle = xmlDoc.getRootElement();
		// parse default selector
		defaultSelect.parse(rootEle.getChild("default"));
		
		// parse selectors
		for(Object obj : rootEle.getChildren("selector")) {
			if((obj instanceof Element) == false)
				continue;
			Selector sel = new Selector();
			sel.parse((Element)obj);
			selectors.add(sel);
		}
	}
	
	
	public String getConfSelect() {
		if(NetGlobalVars.confSelect !=  null) {
			return NetGlobalVars.confSelect;
		}
		
		// 最终匹配的 SelectorBase
		SelectorBase selectedConf = null;
		
		if(selectors.isEmpty() == false) {
			// 获取本机所有的IP地址和域名
			for(Selector sel : selectors) {
				if(sel.isActive() == false)
					continue;
				if(sel.isMatched(null)) {
					selectedConf = sel;
					break;
				}
			}
		}
		
		if(selectedConf == null)
			selectedConf = defaultSelect;
		
		LOGGER.info("Analysis Conf Select Result: " + selectedConf.conf 
				+ "(" + selectedConf.description + ")");
		NetGlobalVars.confSelect = selectedConf.conf;
		
		
		return NetGlobalVars.confSelect;
	}
	
	/**
	 * 获取当前配置文件的根目录
	 * @return
	 */
	public File getConfRootDir() throws Exception {
		return new File(new File(ProcessCtr.getLocalSIndexRoot(), 
				"conf"), getConfSelect());
	}
}
