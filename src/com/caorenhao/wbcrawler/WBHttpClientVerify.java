package com.caorenhao.wbcrawler;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.http.HttpStatus;

import com.caorenhao.util.LoggerConfig;
import com.caorenhao.util.Pair;
import com.caorenhao.wbcrawler.common.WBConst;

/**
 * 验证Cookie是否有效的类
 * 		方法:通过访问 人民网 的微博然后查看是否可以翻页来进行判断.
 * 
 * @author renhao.cao.
 *         Created 2015-1-16.
 */
public class WBHttpClientVerify extends WBHttpClientCommon {

	private Log LOGGER = LoggerConfig.getLog(getClass());
	
	/**
	 * 抽取测试网页的翻页页码(只有登陆后的账号才可以翻页).
	 *
	 * @param text
	 * @return 返回的抽取验证信息, 如果不为null则cookie有效
	 */
	public String extract(String text) {
	    String regex = "跳页(.*?)页";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(text);
		String verification = null;
		while (m.find()){
			verification = m.group(1).replace("\" />&nbsp;","");
		}
		
		return verification;
	}
	
	/**
	 * 获取测试微博主页的翻页信息.
	 *
	 * @param cookie 测试cookie
	 * @return boolean false为失效, true为有效
	 * @throws Exception 
	 */
	public boolean verify(String cookie) {
		HttpMethod getMethod = new GetMethod(WBConst.URL_VERIFY);
		setHeader(getMethod);
		addCookie(getMethod, cookie);
		
		boolean flag = true;
		HttpClient httpclient = getHttpClient();
		String content = executeMethod(httpclient, getMethod);
		/*
		int num = 0;
		while(content == null || content.isEmpty()) {
			num++;
			if(num < WBConst.NUM_RETRY) {
				LOGGER.warn("获取页面内容为空, 等待" + (WBConst.TIMEWAIT_RELINK/1000) 
						+ "s重新请求");
				NetUtil.sleep(WBConst.TIMEWAIT_RELINK);
				content = executeMethod(httpclient, getMethod);
			} else {
				return false;
			}
		}
		*/
		LOGGER.info(cookie + WBConst.INTERVAL + content);
		
		String verification = extract(content);
		if(verification == null)
			flag = false;
		
		return flag;
	}
	
	/**
	 * 验证代理是否可用.
	 *
	 * @param pair
	 * @return boolean
	 */
	public boolean verifyProxy(Pair<String, Integer> pair) {
		HttpClient httpclient = getHttpClient(); 
		HostConfiguration hconf = new HostConfiguration();
		hconf.setProxy(pair.first, pair.second);
		HttpMethod getMethod = new GetMethod(WBConst.URL_PROXY_VERITY);
		
		boolean flag = false;
		try {
			httpclient.executeMethod(hconf, getMethod);
			
			if (getMethod.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				flag = true;
			}
		} catch (HttpException e) {
			return false;
		} catch (IOException e) {
			return false;
		} finally {
			getMethod.releaseConnection();
		}
		
		return flag;
	}
	
	/**
	 * TODO Put here a description of what this method does.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		WBHttpClientVerify verify = new WBHttpClientVerify();
		boolean flag = verify.verifyProxy(
				new Pair<String, Integer>("115.200.167.205",37564));
		System.out.println(flag);
	}
}
