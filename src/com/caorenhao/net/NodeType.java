package com.caorenhao.net;




public enum NodeType {
	/** 第一套节点*/
	CenterTracker("CT"),
	CenterYQManager("CYQM"),
	CenterYQProcessor("CYQP"),
	StatisticManager("SM"),
	BlockJobTracker("BJT"),
	BlockCrawler("BC"),
	BlockProccesor("BP"),
	/** 部署节点 */
	DeployMgr("DP"),
	/** 单进程的IYQ，独立节点 */
	SingleProcessIYQ("SingleProcessIYQ"),
	
	/** 分析节点管理节点*/
	YQMgr("YQMgr"),
	/** 分析节点处理节点 */
	YQProcessor("YQPro"),
	
	/** 分析节点管理节点*/
	TPMgr("TPMgr"),
	/** 分析节点处理节点 */
	TPCrawler("TPCrawler"),
	
	/** 深度爬虫管理节点 */
	VCMgr("VCMgr"),
	/** 深度爬虫处理单元 */
	VCProcessor("VCProcessor"),
	/** 深度爬虫抓取单元 */
	VCCrawler("VCCrawler"),
	/** 深度爬虫管理结点*/
	VDeepCrawler("VDeepCrawler"),
	
	/** 任务抓取框架管理节点 */
	TFMMgr("TFMMgr"),
	/** 任务抓取框架抓取节点 */
	TFMCrawler("TFMCrawler"),
	
	/** 前端接口节点*/
	IYQFrontAPI("IYQFrontAPI"),
	/** 话题舆情初始化 */
	TopicYQInit("TopicYQInit"),
	/** 数据同步节点 */
	DataSync("DataSync"),
	/** 统计节点管理器 */
	StatMgr("StatMgr"),
	/** 统计处理节点 */
	StatProcessor("StatProcessor"),
	/** 单独的统计节点，比StatMgr版本新 */
	STMgr("STMgr"),
	
	/** 部署节点管理器 */
	INodeMgr("INodeMgr"),
	/** 部署节点*/
	INodeClient("INodeClient"),
	/** 邮件服务器节点*/
	MailServer("MailServer"),

	/** 数据存储结点 */
	DataStore("DataStore"),

	/** 数据存储结点 */
	WBStore("WBStore"),
	
	/**订阅*/
	FeedMgr("FeedMgr"),
	FeedCrawler("FeedCrawler"),

	/** 重点人物监控管理节点*/
	KCMgr("KCMgr"),
	/** 重点人物监控抓取解析节点 */
	KCCrawler("KCCrawler"),

	/** 元搜索管理节点 */
	MSMgr("MSMgr"),
	/** 元搜索抓取节点 */
	MSCrawler("MSCrawler"),
	
	/** 微博爬虫*/
	WCrawler("WCrawler"),
	/** 微博爬虫API */
	WAPI("WAPI"),
	/** 微心情API */
	WEAPI("WEAPI"),
	/** 微博刷新回复量进程 */
	WRefresh("WRefresh");
	
	/** 命名前缀 */
	public String prefix;
	NodeType(String prefix) {
		this.prefix = prefix;
	}
}
