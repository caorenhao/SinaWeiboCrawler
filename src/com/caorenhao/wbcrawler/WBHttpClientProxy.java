package com.caorenhao.wbcrawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.DefaultHttpClient;

import com.caorenhao.util.LoggerConfig;
import com.caorenhao.util.NetUtil;
import com.caorenhao.util.Pair;
import com.caorenhao.wbcrawler.common.WBConst;
import com.caorenhao.wbcrawler.common.WBVar;

/**
 * 代理管理.
 *
 * @author renhao.cao.
 *         Created 2015年2月5日.
 */
public class WBHttpClientProxy {

	private Log LOGGER = LoggerConfig.getLog(getClass());
	
	/**
	 * 检查内存中的代理是否存在, 若不存在则使用一个新的代理.
	 * 
	 * @return String
	 */
	public synchronized Pair<String, Integer> getProxy() {
		boolean temp = true;
		while(temp) {
			if(WBVar.proxy == null) {
				LOGGER.info("获取新的代理");
				Pair<String, Integer> proxy = getVerifyProxy();
				WBVar.proxy = proxy;
				LOGGER.info("使用代理: " + proxy.toString());
				temp = false;
			} else {
				WBHttpClientVerify verify = new WBHttpClientVerify();
				boolean flag = verify.verifyProxy(WBVar.proxy);
				if(flag)
					temp = false;
				else {
					LOGGER.info("代理: " + WBVar.proxy.toString() + "已失效");
					WBVar.proxy = null;
				}
			}
		}
		
		return WBVar.proxy;
	}
	
	/**
	 * 获取一个经过验证的可用代理.
	 *
	 * @return Pair<String, Integer>
	 */
	public Pair<String, Integer> getVerifyProxy() {
		Pair<String, Integer> pair = null;
		Boolean flag = false;
		WBHttpClientVerify verify = new WBHttpClientVerify();
		
		while(pair == null) {
			pair = getProxyFromXici();
			if(pair != null) {
				flag = verify.verifyProxy(pair);
				if(!flag)
					pair = null;
			}
			
			NetUtil.sleep(2 * 1000);
		}
		
		return pair;
	}
	
	/**
	 * 从西刺代理获取代理.
	 *
	 * @return String
	 */
	public Pair<String, Integer> getProxyFromXici() {
		String tid = "556817347091655";
		String area = "杭州";
		String num = "1";
		String url = "http://evrx.daili666.com/ip/?tid=" + tid + "&num=" + num 
				+ "&area=" + area + "&filter=on";
		//url = "http://evrx.daili666.com/ip/?tid=" + tid + "&num=" + num 
				//+ "&area=" + area;
		
		BufferedReader reader = null;
		InputStream in = null;
		StringBuffer sb = new StringBuffer();
		Pair<String, Integer> pair = null;
		HttpClient client = null;
		try {
			client = new DefaultHttpClient();
			HttpGet get = new HttpGet(url);
			HttpResponse response = client.execute(get);
			in = response.getEntity().getContent();
	    	reader = new BufferedReader(new InputStreamReader(in, WBConst.CHARSET));
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			if(sb.toString() != null) {
				String[] strs = sb.toString().split(":");
				if(strs.length > 1) 
					pair = new Pair<String, Integer>(strs[0], Integer.parseInt(strs[1]));
			}
		} catch (Exception e) {
			LOGGER.warn(e.toString());
		} finally {
			try {
				if(reader != null)
					reader.close();
			} catch (IOException e) {
				LOGGER.warn(e.toString());
			}
		}
		
		return pair;
	}
	
	/**
	 * TODO Put here a description of what this method does.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		WBHttpClientProxy proxy = new WBHttpClientProxy();
		Pair<String, Integer> pair = proxy.getVerifyProxy();
		System.out.println(pair.first + "," + pair.second);
	}
}
