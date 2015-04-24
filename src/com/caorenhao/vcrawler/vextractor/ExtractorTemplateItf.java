package com.caorenhao.vcrawler.vextractor;

import java.util.List;

import org.jsoup.nodes.Document;

import com.alibaba.fastjson.JSONObject;

public interface ExtractorTemplateItf {

	/**
	 * 测试模板是否有效
	 * @param doc
	 * @param html
	 * @return
	 * @throws Exception
	 */
	public boolean tplTest(Document doc, String html)throws Exception;
	
	/**
	 * 解析模板返回JSON
	 * @param url
	 * @param doc
	 * @param html
	 * @return
	 * @throws Exception
	 */
	public JSONObject parse(String url, Document doc, String html) throws Exception;
	
	/**
	 * 用于解析直接从搜索页面获取结果的列表页面
	 * @param url
	 * @param doc
	 * @param html
	 * @return
	 * @throws Exception
	 */
	public List<JSONObject> parseList(String url, String html) throws Exception;
	
}
