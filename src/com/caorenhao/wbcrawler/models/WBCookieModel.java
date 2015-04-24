package com.caorenhao.wbcrawler.models;

/**
 * TODO Put here a description of what this class does.
 *
 * @author renhao.cao.
 *         Created 2014-12-2.
 */
public class WBCookieModel {

	/** 账号*/
	private String username;
	
	/** 密码*/
	private String password;
	
	/** 登陆后的cookie*/
	private String cookie;

	/** cookie是否可用的标记*/
	private Boolean isAvailable;
	
	/** 账号登陆时间Long型*/
	private long loginTimeL;
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public Boolean getIsAvailable() {
		return isAvailable;
	}
	
	public void setIsAvailable(Boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

	public long getLoginTimeL() {
		return loginTimeL;
	}

	public void setLoginTimeL(long loginTimeL) {
		this.loginTimeL = loginTimeL;
	}
	
}
