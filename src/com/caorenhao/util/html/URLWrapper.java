package com.caorenhao.util.html;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import com.caorenhao.util.StrUtil;

/**
 * URL的包装类
 * @author vernkin
 *
 */
public class URLWrapper {

	/** 顶级域名列表，全部小写。 参考维基百科，地区代码不可以添加，如cn  */
	public static final String[] TOP_DOMAINS = {"com", "net", "org", 
		"gov", "edu", "mil", "biz", "name", "info", "mobi", "pro", 
		"travel", "museum", "int", "aero", "post", "rec", "asia", "co"};
	
	/** 常见的不是域名的第一个字符串, 全部小写 */
	public static final String[] NOT_DOMAIN_FIRST_TOKEN = {"www", "news"};
	
	public static final Set<String> TOP_DOMAIN_SET = 
		new HashSet<String>(Arrays.asList(TOP_DOMAINS));
	public static final Set<String> NOT_DOMAIN_FIRST_TOKEN_SET = 
		new HashSet<String>(Arrays.asList(NOT_DOMAIN_FIRST_TOKEN));
	
	/** 原始url字符串 */
	private String urlStr;
	/** urlStr 对应的URL对象 */
	private URL url;
	/** 参数列表 */
	private Map<String, String> params = new TreeMap<String, String>();
	/** 二级域名 */
	private String shortDomain;
	
	public URLWrapper(String urlStr) throws Exception {
		this.urlStr = urlStr;
		url = new URL(urlStr);
		initilaize();
	}
	
	private void initilaize() throws Exception {
		String query = url.getQuery();
		query = StrUtil.emptyStringToNull(query, true);
		if(query != null) {
			StringTokenizer st1 = new StringTokenizer(query, "&");
			while(st1.hasMoreTokens()) {
				String token = st1.nextToken();
				int idx1 = token.indexOf("=");
				if(idx1 < 0) {
					params.put(token, "");
				} else {
					params.put(token.substring(0, idx1), token.substring(idx1+1));
				}
			}
		}
	}
	
	public String getParam(String paramName) {
		return params.get(paramName);
	}
	
	public Map<String, String> getParams() {
		return params;
	}
	
	/**
	 * 获得正式的URL，其中
	 * 隐藏默认的端口，参数按照字符顺序排序
	 * @return
	 */
	public String getFormattedURL() {
		StringBuilder sb = new StringBuilder(urlStr.length() + 10);
		sb.append(getFullDomain());
		getFullRelativePathImpl(sb);
		return sb.toString();
	}
	
	/**
	 * 输入 http://redis.io/commands/zrange?a=1&b=ds <br/>
	 * 输出   http://redis.io<br/>
	 * 包含协议名称，主机 和  端口(若是默认端口忽略) 的  域名。 如果urlStr格式错误返回null<br/>
	 * @return
	 */
	public String getFullDomain() {
		return URLUtil.getFullDomain(url);
	}
	
	/**
	 * 返回完整的相对路径（不包含域名）, 包括参数的 实现方法
	 * @return
	 */
	private void getFullRelativePathImpl(StringBuilder sb) {
		sb.append(url.getPath());
		if(!params.isEmpty()) {
			sb.append("?");
			boolean isNotFirstParam = false;
			for(Map.Entry<String, String> entry : params.entrySet()) {
				if(isNotFirstParam) {
					sb.append('&');
				} else {
					isNotFirstParam = true;
				}
				sb.append(entry.getKey()).append("=").append(entry.getValue());
			}
		}
	}
	
	/**
	 * 返回完整的相对路径（不包含域名）, 包括参数
	 * @return
	 */
	public String getFullRelativePath() {
		StringBuilder sb = new StringBuilder(urlStr.length());
		getFullRelativePathImpl(sb);
		return sb.toString();
	}
	
	/**
	 * http://news.qq.com/a.html 返回 news.qq.com
	 * @return
	 */
	public String getDomain() {
		return url.getHost();
	}
	
	/**
	 * http://news.qq.com.cn/a.html 返回 qq.com.cn
	 * @return
	 */
	public String getShortDomain() {
		if(shortDomain != null)
			return shortDomain;
		String domain = url.getHost();
		String[] tokens = domain.split("\\.");
		// 长度不大于2的时候直接返回主机
		if(tokens.length <= 2) {
			shortDomain = domain;
			return shortDomain;
		}
		
		// 测试是否IP地址
		if(tokens.length == 4) {
			boolean isAllDigit = true;
			try {
				for(int i = 0; i < tokens.length; ++i) {
					Integer.parseInt(tokens[i]);
				}
			} catch(NumberFormatException nfe) {
				isAllDigit = false;
			}
			
			if(isAllDigit) {
				shortDomain = domain;
				return shortDomain;
			}
		}
		
		int startIdx = tokens.length - 1;
		boolean hasTopDomain = false;
		while(startIdx >=  0) {
			if(TOP_DOMAIN_SET.contains(tokens[startIdx].toLowerCase())) {
				--startIdx;
				hasTopDomain = true;
				break;
			}
			--startIdx;
		}
		
		if(startIdx < 0) {
			// 有顶级域名， 直接从顶级域名开始取
			if(hasTopDomain) {
				startIdx = 0;
			} else {
				// 否则取倒数两个值
				startIdx = tokens.length - 2;
			}
		} else if(startIdx == 0){
			// 正好指向到第一个元素，判断是否需要包括
			String firstToken = tokens[0];
			// 如果第一个字符不是域名的组成部分，忽略
			if(NOT_DOMAIN_FIRST_TOKEN_SET.contains(firstToken.toLowerCase())) {
				startIdx = 1;
			}
		}
		
		StringBuilder sb = new StringBuilder(domain.length());
		sb.append(tokens[startIdx]);
		for(++startIdx; startIdx < tokens.length; ++startIdx) {
			sb.append('.').append(tokens[startIdx]);
		}
		shortDomain = sb.toString();
		return shortDomain;
	}
	
	/**
	 * 获取二级域名，如果不存在二级域名返回null <br/>
	 * qq.com =》 null
	 * news.qq.com -> news.qq.com
	 * zt.news.qq.com => news.qq.com
	 * @return
	 */
	public String getSecondDomain() {
		String shortDomain = getShortDomain();
		String host = url.getHost();
		if(host.length() <= shortDomain.length() + 1)
			return null;
		host = "." + host;
		int lastIdx = host.length() - shortDomain.length() - 2;
		int idx = host.lastIndexOf('.', lastIdx);
		if(idx < 0)
			return null;
		return host.substring(idx + 1);
	}
	
	/**
	 * 获取所有的域名，从最长域名到最短域名
	 * @return
	 */
	public List<String> getAllDomains() {
		List<String> ret = new ArrayList<String>();
		String shortDomain = getShortDomain();
		String host = url.getHost();
		host = "." + host;
		int endIdx = host.lastIndexOf('.', host.length() - shortDomain.length());
		for(int i = 0; i <= endIdx; ) {
			ret.add(host.substring(i + 1));
			i = host.indexOf('.', i + 1);
			if(i <= 0)
				break;
		}
		return ret;
	}
	
	public String getUrlWithoutParam() {
		String domain = getFullDomain();
		String newUrl = domain + url.getPath();
		return newUrl;
	}
	
	public String getPath() {
		return url.getPath();
	}
	
	public String getOrigUrl() {
		return urlStr;
	}
 	
	public String toString() {
		return "URLWrapper[" + url.toString() +"]";
	}
}
