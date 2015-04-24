package com.caorenhao.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;


/**
 * 包含一些相同的解析模块
 * @author vernkin
 *
 */
public final class XmlParserHelper {

	/**
	 * 返回域名(字符串) 和 端口(整数) 的 组合
	 * @return
	 */
	public static List<Pair<String, Integer>> getServerAddress(Element networkEle) throws Exception {		
		
		List<Element> addrEles = networkEle.getChildren("address");
		List<Pair<String, Integer>> ret = new ArrayList<Pair<String, Integer>>();		
		if(addrEles == null)
			return ret;
		
		for(Object obj : addrEles) {
			if((obj instanceof Element) == false)
				continue;
			Element ele = (Element)obj;
			String address = ele.getChildTextTrim("host");
			String portStr = ele.getChildTextTrim("port");
			int port = Integer.parseInt(portStr);
			ret.add(new Pair<String, Integer>(address, port));
		}
		
		return ret;
	}
	
	/*public static Properties getDatabaseConfig(Element fatherEle) throws Exception {
		Properties ret = new Properties();
		String vendorName = fatherEle.getChildText("vendor");
		Vendor vendor = RDBMSConnHelper.getVendor(vendorName);
		if(vendor == null) {
			throw new Exception("Can't find database vendor: " + vendorName);
		}
		ret.put("vendor", vendor);
		String[] attrNames = {"host", "port", "database", "user", "password"};
		for(String attrName : attrNames) {
			ret.setProperty(attrName, fatherEle.getChildText(attrName));
		}		
		return ret;
	}*/
	
	public static Map<String, String> getAllChildText(Element fatherEle) throws Exception {
		Map<String, String> ret = new HashMap<String, String>();
		for(Object childObj : fatherEle.getChildren()) {
			if((childObj instanceof Element) == false)
				continue;
			Element child = (Element)childObj;
			String key = child.getName();
			String value = child.getTextTrim();
			ret.put(key, value);
		}
		
		return ret;
	}
	
	/**
	 * 类名必须以Config结尾，对应的配置文件名为 Config前面的字符全部小写 加上 .xml
	 * @param clazz 配置类Class对象
	 * @return
	 * @throws Exception
	 */
	public static String getConfigFileName(Class<?> clazz) throws Exception {
		String origName = clazz.getSimpleName();
		if(!origName.endsWith("Config"))
			throw new ConfigException("Configuration Class Name Not End " +
					"With Config: " + origName);
		String fileName = origName.substring(0, origName.length() - 6).
			toLowerCase() + ".xml";
		return fileName;
	}
	
	/**
	 * 获取子节点的值
	 * @param ele 父节点
	 * @param childName 子结点名称
	 * @param mandatory true表示一定要有，没有抛出异常
	 * @return 对应的子结点的值，如果子结点不存在，返回null
	 * @throws ConfigException 子结点不存在已经 mandatory为true时抛出
	 */
	public static String getChildValue(Element ele, String childName, 
			boolean mandatory) throws ConfigException {
		String ret = ele.getChildText(childName);
		if(mandatory && ret == null)
			throw new ConfigException("Can't Find Child[" + childName + "]");
		return ret;
	}
	
	/**
	 * 获取子节点的整数值
	 * @param <T> 具体的整数值
	 * @param ele 父节点
	 * @param childName 子结点名称
	 * @param mandatory 是否一定要包括
	 * @param defautVal 默认值。 <b>不能为空</b>
	 * @param minVal 最小值，包括。null则忽略
	 * @param maxVal 最大值，不包括。null则忽略
	 * @return 对应的子结点值
	 * @throws ConfigException
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Number> T getChildNumber(Element ele, String childName, 
			boolean mandatory, T defautVal, T minVal, T maxVal) throws ConfigException {
		if(defautVal == null)
			throw new IllegalArgumentException("defautVal CAN'T be NULL");
		String strVal = getChildValue(ele, childName, mandatory);
		if(strVal == null)
			return defautVal;
		// String to Number
		T val = (T)NumberFormatUtil.stringToNumber(defautVal.getClass(), strVal);		
		if(minVal != null && val.doubleValue() < minVal.doubleValue()) {
			throw new ConfigException("Child[" + childName + "] SHOULD >=" + 
					minVal + ", BUT not " + val);
		}
		
		if(maxVal != null && val.doubleValue() >= maxVal.doubleValue()) {
			throw new ConfigException("Child[" + childName + "] SHOULD <" + 
					maxVal + ", BUT not " + val);
		}
		return val;
	}
	
	public static String getValue(Element ele, boolean mandatory) 
			throws ConfigException {
		String ret = ele.getText();
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Number> T getNumber(Element ele, boolean mandatory, 
			T defautVal, T minVal, T maxVal) throws ConfigException {
		if(defautVal == null)
			throw new IllegalArgumentException("defautVal CAN'T be NULL");
		String strVal = getValue(ele, mandatory);
		if(strVal == null)
			return defautVal;
		// String to Number
		T val = (T)NumberFormatUtil.stringToNumber(defautVal.getClass(), strVal);		
		if(minVal != null && val.doubleValue() < minVal.doubleValue()) {
			throw new ConfigException("Element[" + ele.getName() + "] SHOULD >=" + 
					minVal + ", BUT not " + val);
		}
		
		if(maxVal != null && val.doubleValue() >= maxVal.doubleValue()) {
			throw new ConfigException("Element[" + ele.getName() + "] SHOULD <" + 
					maxVal + ", BUT not " + val);
		}
		return val;
	}
}
