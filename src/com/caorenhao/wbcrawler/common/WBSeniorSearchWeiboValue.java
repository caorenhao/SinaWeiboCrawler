package com.caorenhao.wbcrawler.common;

/**
 * TODO Put here a description of what this class does.
 *
 * @author renhao.cao.
 *         Created 2014-12-19.
 */
public enum WBSeniorSearchWeiboValue {

	// 微博类型
	/** 原创*/
	TYPE_ORIGINAL("hasori"),
	/** 图片*/
	TYPE_PICTURE("haspic"),
	/** 视频*/
	TYPE_VIDEO("hasvideo"),
	/** 音乐*/
	TYPE_MUSIC("hasmusic"),
	/** 链接*/
	TYPE_LINK("haslink"),

	// 用户类型
	/** 认证用户*/
	USER_AUTH("hasv"),
	/** 我关注的*/
	USER_ATTEN("atten"),
	
	// 微博排序方式
	/** 热度*/
	RANK_HOT("hot"),
	/** 实时*/
	RANK_TIME("time");
	
	/** */
	public String type;
	
	private WBSeniorSearchWeiboValue(String type) {
		this.type = type;
	}
	
}
