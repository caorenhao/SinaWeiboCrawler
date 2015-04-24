package com.caorenhao.wbcrawler.conf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

import com.caorenhao.util.ConfigException;
import com.caorenhao.util.Pair;
import com.caorenhao.util.XmlParserHelper;
import com.caorenhao.wbcrawler.common.WBConst;

/**
 * TODO Put here a description of what this class does.
 *
 * @author Administrator.
 *         Created 2014-4-28.
 */
public class WBCrawlerConfig extends JDomConfig {
	
	public static class RedisConf {
		public String host;
		public int port;
		
		private void parse(Element rootEle) throws Exception {
			Element rdEle = rootEle;
			host = rdEle.getChildText("host");
			port = Integer.parseInt(rdEle.getChildText("port"));
		}
	}
	
	public static class KeywordConf {
		public List<Pair<String, Integer[]>> palmAndDeptList = new ArrayList<Pair<String, Integer[]>>();
		
		private void parse(Element rootEle) throws Exception {
			List<?> hostChild = rootEle.getChildren("palm");
			for(Object obj : hostChild) {
				if(!(obj instanceof Element))
					continue;
				Element ele = (Element)obj;
				String palm = ele.getText();
				String deptId = ele.getAttributeValue("deptId");
				String[] depts = deptId.split(",");
				Integer[] deptss = new Integer[depts.length];
				int i = 0;
				for(String dept : depts) {
					deptss[i] = Integer.parseInt(dept);
					i++;
				}
				
				palmAndDeptList.add(new Pair<String, Integer[]>(palm, deptss));
			}
		}
	}
	
	public static class AccountConf {
		public List<Pair<String, String>> accounts = new ArrayList<Pair<String, String>>();
		
		private static String defaultPW;
		
		private void parse(Element rootEle) throws Exception {
			defaultPW = rootEle.getAttributeValue("defaultPW");
			
			List<?> hostChild = rootEle.getChildren("username");
			for(Object obj : hostChild) {
				if(!(obj instanceof Element))
					continue;
				Element ele = (Element)obj;
				String username = ele.getText();
				String password = (ele.getAttributeValue("password") != null) ? ele.getAttributeValue("password") : defaultPW;
				
				accounts.add(new Pair<String, String>(username, password));
			}
		}
	}
	
	public static class WorkDirConf {
		
		/** 工作目录*/	
		public String workDir;
		
		public void parse(Element ele) throws Exception {
			workDir = ele.getChildText("workDir");
			if(workDir == null) {
				throw new ConfigException("'workDir' is not set");
			}
		}
	}
	
	@Override
	public File getDefaultConfigFile() throws Exception {
		String fileName = XmlParserHelper.getConfigFileName(this.getClass());
		return findConfigFile(WBConst.CONF_TYPE_WBCRAWLER, fileName);
	}

	public RedisConf redisConf = new RedisConf();
	public KeywordConf keywordConf = new KeywordConf();
	public AccountConf accountConf = new AccountConf();
	public WorkDirConf workDirConf = new WorkDirConf();
	
	@Override
	protected void parse() throws Exception {
		Element root = getRootEle();
		redisConf.parse(root.getChild("redis"));
		keywordConf.parse(root.getChild("keyword"));
		accountConf.parse(rootEle.getChild("account"));
		workDirConf.parse(rootEle.getChild("workDirMgr"));
	}
}

