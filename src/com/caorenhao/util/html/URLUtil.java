package com.caorenhao.util.html;

import java.net.URL;

public class URLUtil {

	/**
	 * 输入 http://redis.io/commands/zrange?a=1&b=ds
	 * 输出   http://redis.io
	 * 包含协议名称，主机 和  端口(若是默认端口忽略) 的  域名。 如果urlStr格式错误返回null
	 * @param urlStr 输入的链接字符串
	 * @return 
	 * @throws Exception
	 */
	public static String getFullDomain(String urlStr) {
		try {
			URL url = new URL(urlStr);
			return getFullDomain(url);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String getFullDomain(URL url) {
		try {
			StringBuilder sb = new StringBuilder(50);
			sb.append(url.getProtocol()).append("://").append(url.getHost());
			if(url.getPort() != -1 && url.getPort() != url.getDefaultPort()) {
				sb.append(':').append(url.getPort());
			}
			return sb.toString();
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * 获取纯域名。如http://www.baidu.com/index.html?i=1 TO www.baidu.com
	 * @param urlStr
	 * @return
	 */
	public static String getDomain(String urlStr) {
		try {
			URL url = new URL(urlStr);
			return getDomain(url);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String getDomain(URL url) {
		try {
			StringBuilder sb = new StringBuilder(50);
			sb.append(url.getHost());
			return sb.toString();
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 获取规范化的url，如果解析出错返回null
	 * @param urlStr 原始url字符串
	 * @return
	 */
	public static String getFormatURL(String urlStr) {
		try {
			URLWrapper urlW = new URLWrapper(urlStr);
			return urlW.getFormattedURL();
		} catch(Throwable t) {
			return null;
		}
	}

	/**
	 * 不再支持更新，用{@link makeAbsoluteURL} 代替
	 * @param srcUrl
	 * @param nextPageUrl
	 * @return
	 */
	@Deprecated
	public static String getAbsolutelyUrl(String srcUrl, String nextPageUrl) {
		if (nextPageUrl == null || nextPageUrl.isEmpty()) {
			return nextPageUrl;
		}
		if (nextPageUrl.startsWith("http") || nextPageUrl.startsWith("https"))
		{
			return nextPageUrl;
		} else if (nextPageUrl.startsWith("/")) {
			String baseUrl = getFullDomain(srcUrl);
			if (baseUrl.endsWith("/")) {
				baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf('/'));
			}
			nextPageUrl = baseUrl + nextPageUrl;
			return nextPageUrl;
		} else {
			String result = srcUrl.substring(0, srcUrl.lastIndexOf('/'));
			if (!result.endsWith("/")) {
				result += "/";
			}
			result = result + nextPageUrl;
			return result;
		}
	}
	
	/**
	 * 获取绝对路径URL
	 * 
	 * @param sourceURL
	 *            当前请求的URL
	 * @param relativedUrl
	 *            页面解析出来的URL
	 * @return
	 * @throws Exception
	 */
	public static String makeAbsoluteURL(String sourceURL, String relativedUrl)
			throws Exception {

		if (relativedUrl == null) {
			return relativedUrl;
		}
		// 如果相对路径已经是完整的路径，直接返回
		if (relativedUrl != null
				&& (relativedUrl.toLowerCase().startsWith("http://")
						|| relativedUrl.toLowerCase().startsWith("https://") || relativedUrl
						.toLowerCase().startsWith("ftp://"))) {
			return relativedUrl;
		}

		// 如果是js链接，直接返回
		if (relativedUrl.startsWith("javascript:")) {
			return relativedUrl;
		}
		
		// 如果原始链接为null或是为空字符串，直接返回
		if (sourceURL == null || sourceURL.length() == 0) {
			return relativedUrl;
		}

		String absoluteURL = relativedUrl;

		URL srcURL = new URL(sourceURL);

		if (srcURL != null) {
			// url is not absolute, compute it
			absoluteURL = URLUtil.getFullDomain(sourceURL);

			String path = srcURL.getPath();
			// 统一格式，将http://XXX.XXX.XXX/ 统一成 http://XXX.XXX.XXX
			if (path.equals("/")) {
				path = "";
			}

			if (relativedUrl.startsWith("..")) {
				int pathLevel = 0;
				pathLevel++;
				String tmpUrl = relativedUrl.substring(2);
				while (tmpUrl.startsWith("/..")) {
					pathLevel++;
					tmpUrl = tmpUrl.substring(3);
				}
				relativedUrl = tmpUrl;
				String [] paths = path.split("/");
				if (pathLevel > paths.length-1) {
					pathLevel = paths.length-1;
				}				
				String tmpPath = paths[0] + "/";
				for(int i = 1; i < paths.length - pathLevel - 1; i++) {
					tmpPath += paths[i] + "/";
				}
				if (absoluteURL.endsWith("/")) {
					absoluteURL = absoluteURL.substring(0, absoluteURL.length()-1);
				}
				absoluteURL += tmpPath;
			} else if (relativedUrl.startsWith(".")) {
				String tmpUrl = relativedUrl.substring(1);
				while (tmpUrl.startsWith("/.")) {
					tmpUrl = tmpUrl.substring(2);
				}
				if (!absoluteURL.endsWith("/")) {
					absoluteURL += "/"; 
				}
				if (path != null && !path.equals("")) {
					int i = path.lastIndexOf("/");
					if (i > 0) {
						absoluteURL += path.substring(1,i);
					}
				}
				relativedUrl = tmpUrl;
			} else if (!relativedUrl.startsWith("/")) {
				if (!absoluteURL.endsWith("/")) {
					absoluteURL += "/";
				}
				if (path != null && !path.equals("")) {
					int i = path.lastIndexOf("/");
					if (i > 0) {
						absoluteURL += path.substring(1,i);
					}
				}
			} 
			if (!absoluteURL.endsWith("/")) {
				absoluteURL += "/";
			}
			if (relativedUrl.startsWith("/")) {
				relativedUrl = relativedUrl.substring(1);
			}
			absoluteURL += relativedUrl;
		}

		return absoluteURL;
	}
	
	public static void main(String[] args) throws Exception {
		String urlStr = "http://redis.io/commands/zrange?a=1&b=ds";
		URL url = new URL(urlStr);
		System.out.println("File:" + url.getFile());
		System.out.println("Host: " + url.getHost());
		System.out.println("Authority: " + url.getAuthority());
		System.out.println("getDefaultPort: " + url.getDefaultPort());
		System.out.println("Path: " + url.getPath());
		System.out.println("Port: " + url.getPort());
		System.out.println("Protocol: " + url.getProtocol());
		System.out.println("Query: " + url.getQuery());
		System.out.println("Ref: " + url.getRef());
		System.out.println("UserInfo: " + url.getUserInfo());
		System.out.println("Content: " + url.getContent());
	}
}
