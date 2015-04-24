package com.caorenhao.wbcrawler.conf;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.caorenhao.util.LoggerConfig;
import com.caorenhao.util.SourceCodeSyncUtil;

/**
 * 获得各个配置文件的示例
 * @author vernkin
 *
 */
public final class ConfigSingleton {
	
	private static ConfigSingleton instance = new ConfigSingleton();
	
	public static ConfigSingleton getInstance() {
		return instance;
	}
	
	public static WBCrawlerConfig getWBCrawlerConfig() throws Exception {
		return instance.getConfig(WBCrawlerConfig.class);
	}
	
	/** 配置单元 */
	private static class ConfigUnit {
		public JDomConfig config;
		public Class<? extends JDomConfig> configClass;
		/** 配置文件上一次长度 */
		private long lastLength;
		/** 配置文件上一次校验码 */
		private String lastChecknum;
		
		/** 
		 * 初始化配置文件
		 * @param clazz 第一次调用不能为空，第二次以后为空使用上一次记录 
		 */
		public JDomConfig initConfig(Class<? extends JDomConfig> clazz) 
				throws Exception {
			if(clazz == null) {
				clazz = configClass;
			} else {
				configClass = clazz;
			}
			config = clazz.newInstance();
			config.loadDefaultConfigFile();
			lastLength = config.getConfigFile().length();
			lastChecknum = SourceCodeSyncUtil.getMD5(config.getConfigFile());
			return config;
		}
		
		/**
		 * 是否配置文件已经改变
		 * @return
		 */
		public boolean isConfigChanged() throws Exception {
			if(config == null)
				return false;
			if( config.getConfigFile().length() != lastLength )
				return true;
			String curChecksum = SourceCodeSyncUtil.getMD5(config.getConfigFile());
			return (lastChecknum == null || lastChecknum.equals(curChecksum) );
		}
		
		/**
		 * 配置文件改变的时候触发监听器
		 */
		public void onConfigChange() throws Exception {
			initConfig(null);
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder(256);
			if(configClass == null) {
				sb.append("[Empty Config]");
			} else {
				sb.append("Config[").append(configClass.getCanonicalName()).
					append(", File: ").append(config.getConfigFile()).append("]");
			}			
			return sb.toString();
		}
	}
	
	private final Log LOGGER = LoggerConfig.getLog(getClass());
	
	/**
	 * 配置类map
	 */
	private Map<Class<? extends JDomConfig>, ConfigUnit> configMap = 
		new HashMap<Class<? extends JDomConfig>, ConfigUnit>();
	
	private ConfigSingleton() {
	}
	
	/**
	 * 获取配置文件
	 * @param clazz 配置文件类
	 * @return clazz指定的配置文件
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public synchronized <T extends JDomConfig> T getConfig(Class<T> clazz) 
			throws Exception {
		ConfigUnit cu = getConfigUnit(clazz);
		if(cu.config == null) {
			LOGGER.info("Load Configuration " + clazz.getCanonicalName());
			return (T)cu.initConfig(clazz);
		}
		return (T)cu.config;
	}
	
	/**
	 * 尝试获取新的配置文件
	 * @param clazz 配置文件类
	 * @return 如果配置文件没有修改, 返回null。否则新的对象
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public synchronized <T extends JDomConfig> T getNewConfig(Class<T> clazz) 
			throws Exception {
		ConfigUnit cu = getConfigUnit(clazz);
		if(cu.config == null) {
			return null;	
		}
		
		if(cu.isConfigChanged() == false)
			return null;
		LOGGER.info("Load Configuration Latest " + clazz.getCanonicalName());
		cu.onConfigChange();
		return (T)cu.config;
	}
	
	private synchronized ConfigUnit getConfigUnit(Class<? extends JDomConfig> clazz) {
		ConfigUnit ret = configMap.get(clazz);
		if( ret == null ) {
			ret = new ConfigUnit();
			configMap.put(clazz, ret);
		}
		return ret;
	}
}
