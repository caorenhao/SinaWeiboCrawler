package com.caorenhao.wbcrawler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.logging.Log;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import com.caorenhao.util.LoggerConfig;
import com.caorenhao.util.Pair;
import com.caorenhao.wbcrawler.common.WBConst;
import com.caorenhao.wbcrawler.common.WBVar;
import com.caorenhao.wbcrawler.models.WBCookieModel;

/**
 * 新浪微博移动端模拟登录.
 *
 * @author renhao.cao.
 *         Created 2014-11-19.
 */
public class WBHttpClientLogin extends WBHttpClientCommon {
	
	private Log LOGGER = LoggerConfig.getLog(getClass());
	
	/**
	 * 获取登陆参数。主要有三个值：
	 * 		第一个是表单提交地址
	 * 		第二个是密码输入框的名字
	 * 		第三个是vk的值
	 * 
	 * @return 返回登陆参数，string数组，里面的元素：
	 * 		第一个是表单提交地址
	 * 		第二个是密码输入框的名字
	 * 		第三个是vk的值
	 */
	public String[] getLoginParameters() {
		HttpClient httpclient = getHttpClient();
		String url = WBConst.URL_PARAMETERS;
		HttpMethod getMethod = new GetMethod(url);
		setHeader(getMethod);
		
		String retAction = null;
		String retPassword = null;
		String retVk = null;
		String cookie1 = null;
		String cookie2 = null;
		
		try {
			// 获取一个已验证的代理, 并用此代理发送请求
			WBHttpClientProxy wbProxy = new WBHttpClientProxy();
			Pair<String, Integer> proxy = wbProxy.getProxy();
			HostConfiguration hconf = new HostConfiguration();
			hconf.setProxy(proxy.first, proxy.second);
			httpclient.executeMethod(hconf, getMethod);
			
			InputStream content = getMethod.getResponseBodyAsStream();
			
			Header[] headers = getMethod.getResponseHeaders("Set-Cookie");
			cookie1 = headers[0].getValue().split(";")[0];
			//因返回结果中不包含cookie的值了，所以取消
			//cookie2 = headers[1].getValue().split(";")[0];
			
			// 提取登陆参数
			HtmlCleaner cleaner = new HtmlCleaner();
			TagNode tagNode = cleaner.clean(content, "utf-8");
			Object[] action = tagNode.evaluateXPath("//form/@action");
			
			if (action.length > 0) {
				retAction = action[0].toString();
			}
			Object[] passwordKey = tagNode.evaluateXPath(
					"//form//input[@type='password']/@name");
			
			if (passwordKey.length > 0) {
				retPassword = passwordKey[0].toString();
			}
			Object[] vkKey = tagNode.evaluateXPath(
					"//form//input[@name='vk']/@value");
			
			if (vkKey.length > 0) {
				retVk = vkKey[0].toString();
			}
		} catch (Exception e) {
			LOGGER.info("获取登录参数失败" + e.toString());
		} finally {
			if (getMethod != null) {
				getMethod.releaseConnection();
			}
		}
		
		LOGGER.info("请求页面：" + url);
		LOGGER.info("提交地址：" + retAction);
		LOGGER.info("密码输入框名称：" + retPassword);
		LOGGER.info("vk值：" + retVk);
		LOGGER.info("cookie1:" + cookie1);
		LOGGER.info("cookie2:" + cookie2);
		
		return new String[] { retAction, retPassword, retVk, cookie1, cookie2 };
	}
	
	/** 直接向weibo.cn提交
	 * 
	 * @param postAction 登录地址
	 * @param userNameValue 登录用户名
	 * @param passwordValue 登录密码
	 * @param passwordKey 登录密码输入框名称
	 * @param vkValue vk值
	 * @param cookie1 
	 * @param cookie2 
	 */
	private String submitPassword(String postAction, String userNameValue, 
			String passwordValue, String passwordKey, String vkValue, 
			String cookie1, String cookie2) {
		HttpClient httpclient = getHttpClient();
		String url = "http://login.weibo.cn/login/" + postAction;
		LOGGER.info("开始提交账号密码：" + url);
		HttpMethod postMethod = new PostMethod(url);
		setHeader(postMethod);
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new NameValuePair("mobile", userNameValue));
		nvps.add(new NameValuePair(passwordKey, passwordValue));
		nvps.add(new NameValuePair("tryCount", ""));
		nvps.add(new NameValuePair("vk", vkValue));
		nvps.add(new NameValuePair("remember", "on"));
		nvps.add(new NameValuePair("backURL", "http%3A%2F%2Fsina.cn"));
		nvps.add(new NameValuePair("backTitle", "微博"));
		nvps.add(new NameValuePair("submit", "登录"));
		DefaultHttpParams.getDefaultParams().setParameter("http.protocol.cookie-policy", 
				CookiePolicy.BROWSER_COMPATIBILITY);
		NameValuePair[] valuePairs = new NameValuePair[nvps.size()];
		int i = 0;
		for(NameValuePair nvp : nvps) {
			valuePairs[i] = nvp;
			i++;
		}
		postMethod.setQueryString(valuePairs);
		
		String cookie = null;
		StringBuffer sb = new StringBuffer();
		try {
			WBHttpClientProxy wbProxy = new WBHttpClientProxy();
			Pair<String, Integer> proxy = wbProxy.getProxy();
			HostConfiguration hconf = new HostConfiguration();
			hconf.setProxy(proxy.first, proxy.second);
			httpclient.executeMethod(hconf, postMethod);
			
			Header[] setCookie = postMethod.getResponseHeaders("Set-Cookie");
			
			sb.append(setCookie[0].getValue().split(";")[0]);
			for(int j = 1; j < setCookie.length; j++) {
				sb.append(";").append(setCookie[j].getValue().split(";")[0]);
			}
			
			LOGGER.info("setCookie:");
			for(Header header : setCookie) {
				LOGGER.info(header.getValue().split(";")[0]);
			}
			
			if(setCookie.length > 1 && "deleted".equals(setCookie[1].getValue()
					.split(";")[0].split("=")[1])) {
				LOGGER.info("账号 " + userNameValue + " 由于IP被封或者账号被封无法登陆,切换代理IP");
				WBVar.proxy = null;
			}
			if (setCookie != null) {
				cookie = setCookie[1].getValue().split(";")[0] + ";" + 
						setCookie[0].getValue().split(";")[0];
				LOGGER.info("获取到的Cookie：" + cookie);
			}
		} catch (IOException e) {
			return null;
		} finally {
			postMethod.releaseConnection();
		}

		return cookie;
	}
	
	/**
	 * 提交账号密码，登陆
	 * 
	 * @param userNameValue 微博账号
	 * @param passwordValue 微博密码
	 * @param loginParameters 登陆参数
	 * @return 返回cookieModel
	 */
	public WBCookieModel doLogin(String userNameValue, String passwordValue, 
			String[] loginParameters) {
		// 获取登陆页面的参数
		//String[] loginParameters = getLoginParameters();
		String postAction = loginParameters[0];
		String passwordKey = loginParameters[1];
		String vkValue = loginParameters[2];
		String cookie1 = loginParameters[3];
		String cookie2 = loginParameters[4];
		
		// 提交账号密码，获取重定向页面链接与cookie
		String cookie3 = submitPassword(postAction, userNameValue, passwordValue, 
				passwordKey, vkValue, cookie1, cookie2);;
		
		// 提交账号密码出错,返回null值,使用别的账号重新尝试登陆
		if(cookie3 == null)
			return null;
		
		String cookie = cookie3 + ";" + cookie1;
		
		WBCookieModel cookieModel = new WBCookieModel();
		cookieModel.setUsername(userNameValue);
		cookieModel.setPassword(passwordValue);
		cookieModel.setCookie(cookie);
		cookieModel.setIsAvailable(true);
		cookieModel.setLoginTimeL(System.currentTimeMillis());
		
		return cookieModel;
	}
	
	public static void main(String[] args) {
		WBHttpClientLogin login = new WBHttpClientLogin();
		String[] loginParameters = login.getLoginParameters();
		for(String str : loginParameters) {
			System.out.println(str);
		}
		
		WBCookieModel cookieModel = new WBCookieModel();
		cookieModel = login.doLogin("caorenhao0213@163.com", "6223896cao", loginParameters);
		System.out.println(cookieModel.getCookie());
		WBHttpClientVerify verify = new WBHttpClientVerify();
		System.out.println(verify.verify(cookieModel.getCookie()));
	}
}
