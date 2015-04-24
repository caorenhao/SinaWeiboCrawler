package com.caorenhao.wbcrawler.models;

import com.caorenhao.wbcrawler.common.WBAlgo;

/**
 * 单条微博模型.
 *
 * @author renhao.cao.
 *         Created 2015-1-12.
 */
public class WBBlogModel {
	
	/** 微博作者的新浪微博uid*/
	private long uid;
	
	/** 微博作者*/
	private String author;
	
	/** 微博作者主页Url*/
	private String authorUrl;
	
	/** 微博内容*/
	private String content;
	
	/** 转发的原文*/
	private String repostContent;
	
	/** 转发的原文作者*/
	private String repostAuthor;
	
	/** 转发的原文链接*/
	private String repostUrl;
	
	/** 微博链接*/
	private String url;
	
	/** 微博发布时间*/
	private String time;
	
	/** 点赞数量*/
	private int attitudeNum;
	
	/** 评论数量*/
	private int commentNum;
	
	/** 转发数量*/
	private int repostNum;

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public int getAttitudeNum() {
		return attitudeNum;
	}

	public void setAttitudeNum(int attitudeNum) {
		this.attitudeNum = attitudeNum;
	}

	public int getRepostNum() {
		return repostNum;
	}

	public void setRepostum(int repostNum) {
		this.repostNum = repostNum;
	}

	public int getCommentNum() {
		return commentNum;
	}

	public void setCommentNum(int commentNum) {
		this.commentNum = commentNum;
	}

	public String getRepostContent() {
		return repostContent;
	}

	public void setRepostContent(String repostContent) {
		this.repostContent = repostContent;
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	/**
	 * @return the repostAuthor
	 */
	public String getRepostAuthor() {
		return repostAuthor;
	}

	/**
	 * @param repostAuthor the repostAuthor to set
	 */
	public void setRepostAuthor(String repostAuthor) {
		this.repostAuthor = repostAuthor;
	}

	/**
	 * @return the repostUrl
	 */
	public String getRepostUrl() {
		return repostUrl;
	}

	/**
	 * @param repostUrl the repostUrl to set
	 */
	public void setRepostUrl(String repostUrl) {
		this.repostUrl = repostUrl;
	}

	/**
	 * @return the authorUrl
	 */
	public String getAuthorUrl() {
		return authorUrl;
	}
	
	/**
	 * @param authorUrl the authorUrl to set
	 */
	public void setAuthorUrl(String authorUrl) {
		this.authorUrl = authorUrl;
	}
	
	public String toOutput() {
		String str = WBAlgo.toXlsLine(author, time, url, content, 
				String.valueOf(attitudeNum), String.valueOf(commentNum), 
				String.valueOf(repostNum), repostAuthor, repostContent);
		
		return str;
	}
}
