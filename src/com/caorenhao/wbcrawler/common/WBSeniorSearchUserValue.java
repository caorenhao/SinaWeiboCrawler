package com.caorenhao.wbcrawler.common;

/**
 * TODO Put here a description of what this class does.
 *
 * @author renhao.cao.
 *         Created 2014-12-19.
 */
public enum WBSeniorSearchUserValue {

	// 类型
	/** 全部*/
	TYPE_ALL("all"),
	/** 昵称*/
	TYPE_NICK("nick"),
	/** 标签*/
	TYPE_TAGS("tags"),
	/** 学校*/
	TYPE_SCHO("scho"),
	/** 公司*/
	TYPE_COMP("comp"),

	// 用户
	/** 全部*/
	USER_ALL("all"),
	/** 认证用户*/
	USER_AUTH("1"),
	/** 普通*/
	USER_NOMAL("0"),
	
	// 性别
	/** 全部*/
	GENDER_HOT("all"),
	/** 男*/
	GENDER_MALE("m"),
	/** 女*/
	GENDER_FEMALE("f"),
	
	// 年龄
	/** 不限*/
	AGE_0("0"),
	/** 18岁以下*/
	AGE_18("18"),
	/** 19到22*/
	AGE_22("22"),
	/** 23到29*/
	AGE_29("29"),
	/** 30到39*/
	AGE_39("39"),
	/** 40岁以上*/
	AGE_40("40");
	
	
	/** */
	public String type;
	
	private WBSeniorSearchUserValue(String type) {
		this.type = type;
	}
	
}
