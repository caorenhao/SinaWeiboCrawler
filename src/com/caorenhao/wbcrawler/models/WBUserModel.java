package com.caorenhao.wbcrawler.models;

/**
 * 微博用户个人资料类.
 *
 * @author renhao.cao.
 *         Created 2015-1-12.
 */
public class WBUserModel {

	/** 微博账户的唯一uid(用于获取每条微博链接)*/
	private String uid;
	
	/** 昵称*/
	private String nick;
	
	/** 粉丝数*/
	private int fans;
	
	/** 头像链接*/
	private String imgUrl;
	
	/** 性别*/
	private String gender;
	
	/** 地区*/
	private String location;
	
	/** 生日*/
	private String birthday;

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	/**
	 * @return the fans
	 */
	public int getFans() {
		return fans;
	}

	/**
	 * @param fans the fans to set
	 */
	public void setFans(int fans) {
		this.fans = fans;
	}
	
}
