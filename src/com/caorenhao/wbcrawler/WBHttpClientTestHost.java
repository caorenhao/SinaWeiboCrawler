package com.caorenhao.wbcrawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;

import com.caorenhao.util.Pair;

/**
 * 验证HttpClient代理是否可用.
 *
 * @author renhao.cao.
 *         Created 2015年3月30日.
 */
public class WBHttpClientTestHost {
	
	/**
	 * 查找IP对应的地址.
	 *
	 * @param ip
	 * @return String
	 * @throws Exception
	 */
	public static String ip2addr(String ip) throws Exception {
		BufferedReader reader = null;
		StringBuffer sb = new StringBuffer();
		GetMethod getMethod = null;
		try {
			HttpClient httpclient = new HttpClient();
			
			//创建HttpGet对象
			getMethod = new GetMethod("http://www.ip138.com/ips138.asp?ip=" + ip + "&action=2");
			
			httpclient.executeMethod(getMethod);
			InputStream in = getMethod.getResponseBodyAsStream();
	    	reader = new BufferedReader(new InputStreamReader(in, "gb2312"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			getMethod.releaseConnection();
		}
		
		System.out.println(sb.toString());
        /**
         * 利用Parser解析HTML，将标签<li>下的内容存储到nodeList里，并获取第一个<li>下的内容，用split分割后获取最终的结果是 日本
         */
        Parser myParser =Parser.createParser(sb.toString(), "gb2312");
        NodeFilter filter =new TagNameFilter ("li");
        
        NodeList nodeList =myParser.parse(filter);
        System.out.println(nodeList);
        String result = nodeList.elementAt(0).toPlainTextString();
        System.out.println(result);
        String address = result.split("：")[1];
        
		return address;
	}
	
	/**
	 * TODO Put here a description of what this method does.
	 *
	 * @param proxy
	 * @return String
	 * @throws Exception
	 */
	public static String localIP(Pair<String, Integer> proxy) throws Exception {
		BufferedReader reader = null;
		StringBuffer sb = new StringBuffer();
		GetMethod getMethod = null;
		try {
			HttpClient httpclient = new HttpClient();
			//创建HttpGet对象
			getMethod = new GetMethod("http://city.ip138.com/ip2city.asp");
			//使用execute方法发送HTTPGET请求，并返回HttpResponse对象
			if(proxy == null)
				httpclient.executeMethod(getMethod);
			else {
				System.out.println("use proxy");
				HostConfiguration hconf = new HostConfiguration();
				hconf.setProxy(proxy.first, proxy.second);
				httpclient.executeMethod(hconf, getMethod);
			}
			
			InputStream in = getMethod.getResponseBodyAsStream();
	    	reader = new BufferedReader(new InputStreamReader(in, "gb2312"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			getMethod.releaseConnection();
		}
		
		System.out.println(sb.toString());
		String address = WBCrawlHelper.extract(sb.toString(), "您的IP地址是：\\[", "\\]");
        
		return address;
	}
	
	public static void main(String[] args) {
		try {
			// 获取代理
			WBHttpClientProxy proxy = new WBHttpClientProxy();
			Pair<String, Integer> pair = new Pair<String, Integer>();
			pair = proxy.getVerifyProxy();
			System.out.println(pair.toString());
			
			// 获取本机IP
			String ip = localIP(pair);
			System.out.println(ip);
			
			// 获取本机地址
			ip2addr(ip);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
