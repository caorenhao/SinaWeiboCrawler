package com.caorenhao.net;

import java.net.UnknownHostException;

import org.jdom2.Element;

import com.caorenhao.util.ConfigException;
import com.caorenhao.util.NetUtil;
import com.caorenhao.util.XmlParserHelper;

/**
 * 分布式爬虫节点基本配置类
 * @author vernkin
 *
 */
public class NodeConf {
	
	/** 节点的序号，可选 */
	public int id;

	/** 主机名 或者 IP */
	public String host;
	
	/** 对应的用户名，默认为root */
	public String user;
	
	/** 工作目录*/	
	public String workDir;
		
	/** 是否激活状态(可用) */
	public boolean isActive = true;
	
	public NodeConf() {
		user = "dicl";
	}
	
	public void parse(Element ele) throws Exception {
		parse(ele, null);
	}
	
	public void parse(Element ele, String defaultWorkDir) throws Exception {
		host = ele.getChildText("host");
		if(host == null) {
			throw new ConfigException("'host' is not set");
		}
		
		workDir = ele.getChildText("workDir");
		if(workDir == null) {
			if(defaultWorkDir == null)
				throw new ConfigException("'workDir' is not set");
			workDir = defaultWorkDir;
		}
		
		String userTmp = ele.getChildText("user");
		if(userTmp != null)
			user = userTmp;
		
		id = XmlParserHelper.getChildNumber(ele, "id", false, 0, 1, null);
		
		// 是否属于被激活的状态
		String activeTmp = ele.getAttributeValue("active");
		if(activeTmp != null) {
			isActive = activeTmp.equalsIgnoreCase("false");
		}
	}
	
	/**
	 * 本机host是不是本地文件
	 * @return
	 * @throws UnknownHostException
	 */
	public boolean isLocalAddress() throws UnknownHostException {
		return NetUtil.isLocalAddress(host);
	}
	
	/**
	 * 返回: ssh [username]@[host] 
	 * @return
	 */
	public String getSshCmd() {
		return "ssh " + user + "@" + host;
	}
	
	/**
	 * 获取完全的键名
	 * @return
	 */
	public String getGlobalID() throws Exception {
		return ProcessUtil.buildGlobalID(this);
	}
	
	public String toString() {
		try {
			return getGlobalID();
		} catch (Exception e) {
			return "InCompleteNode";
		}
	}
}
