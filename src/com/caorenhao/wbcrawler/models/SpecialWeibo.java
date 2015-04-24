package com.caorenhao.wbcrawler.models;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;


/**
 * 发布的一条微博
 * 新浪中对应 Status
 * @author vernkin
 *
 */
@Entity(noClassnameStored = true)
@Indexes(@Index("time, officalReply"))
public class SpecialWeibo implements Serializable {

	private static final long serialVersionUID = -205946022954259071L;	
	
	@Id
	/** 微博MID */
	private String mid;
	
	/** 微博原来的url */
	private String url;
	
	/** 微博来源类型 */
	private int sourceType;
	
	
	/** 有 user 解析出来的id，不需要主动设置 */
	private String userId;
	
	/** 用户名称, user中解析 */
	private String userName;
	
	/** 用户头像地址, user中解析 */
	private String userLocation;
	
	/** 用户名称, user中解析 */
	private int userfollowersCount;
	

	/** 微博创建时间(Unix时间戳) */
	private long time;
	
	/** 微博本身的ID */
	private String weiboId;
	
	/** 微博内容 */
	private String text;
	
	/** 微博来源URL */
	private String sourceURL;
	/** 微博来源名称 */
	private String sourceName;	
	/** 转发数 */
	private int repostsCount;
	/** 评论数 */
	private int commentsCount;

	/** 是否负面 */
	private boolean isNegative = false;

	/** 是否有官方回复 ， 0表示没有回复， 1表示回复  */
	private int officalReply = 0;
	
	/** 表示匹配的质量: 0 表示无关联， 1 表示基本关联， 2 表示很关联 */
	private int qualitiy = 0;
	
	/** 判断是否要抓取：0表示无需抓取，1表示需要抓取*/
	private int isNeedCrawler;
	
	private int tryCount;
	
	/** 微博创建时间(Unix时间戳) */
	private long crawlTime;
	
	public long getCrawlTime() {
		return crawlTime;
	}

	public void setCrawlTime(long crawlTime) {
		this.crawlTime = crawlTime;
	}

	public SpecialWeibo()
	{
		
	}
	
	public SpecialWeibo(int sourceType, String weiboId)
	{
		this.sourceType = sourceType;
		this.weiboId = weiboId;
	}


	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}

	public int getSourceType() {
		return sourceType;
	}

	public void setUser(WeiboUser user) {
	//	this.user = user;
		if(user != null) {
			setUserId(user.getId());
			setUserName(user.getScreenName());
			setUserLocation(user.getLocation());
			setUserfollowersCount(user.getFollowersCount());
		}
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserLocation(String userLocation) {
		this.userLocation = userLocation;
	}

	
	public String getUserLocation() {
		return userLocation;
	}

	public long getTime() {
		return time;
	}
	
	public void setTime(long time) {
		this.time = time;
	}
	
	public String getWeiboId() {
		return weiboId;
	}
	
	public void setWeiboId(String id) {
		this.weiboId = id;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getSourceName() {
		return sourceName;
	}
	
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public String getSourceURL() {
		return sourceURL;
	}
	public void setSourceURL(String sourceURL) {
		this.sourceURL = sourceURL;
	}
	
	public int getRepostsCount() {
		return repostsCount;
	}
	public void setRepostsCount(int repostsCount) {
		this.repostsCount = repostsCount;
	}
	public int getCommentsCount() {
		return commentsCount;
	}
	public void setCommentsCount(int commentsCount) {
		this.commentsCount = commentsCount;
	}
	public String getMid() {
		return mid;
	}
	
	public void setMid(String mid) {
		this.mid = mid;
	}

	
	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}
	
	public int getUserfollowersCount() {
		return userfollowersCount;
	}

	public void setUserfollowersCount(int userfollowersCount) {
		this.userfollowersCount = userfollowersCount;
	}

	public boolean isNegative() {
		return isNegative;
	}

	public void setNegative(boolean isNegative) {
		this.isNegative = isNegative;
	}

	public int getOfficalReply() {
		return officalReply;
	}

	public void setOfficalReply(int officalReply) {
		this.officalReply = officalReply;
	}

	public int getQualitiy() {
		return qualitiy;
	}

	public void setQualitiy(int qualitiy) {
		this.qualitiy = qualitiy;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((weiboId == null) ? 0 : weiboId.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SpecialWeibo other = (SpecialWeibo) obj;
		if (weiboId == null) {
			if (other.weiboId != null)
				return false;
		} else if (!weiboId.equals(other.weiboId))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return JSONObject.toJSONString(this);
	}

	public int getIsNeedCrawler() {
		return isNeedCrawler;
	}

	public void setIsNeedCrawler(int isNeedCrawler) {
		this.isNeedCrawler = isNeedCrawler;
	}

	public int getTryCount() {
		return tryCount;
	}

	public void setTryCount(int tryCount) {
		this.tryCount = tryCount;
	}
}
