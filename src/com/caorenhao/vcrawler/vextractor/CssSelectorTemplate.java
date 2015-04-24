package com.caorenhao.vcrawler.vextractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caorenhao.util.IOUtil;
import com.caorenhao.util.LoggerConfig;
/**
 * CSS选择器抽取模板
 * @author scotte.ye
 *
 */
public class CssSelectorTemplate implements ExtractorTemplateItf{

	private Log LOGGER = LoggerConfig.getLog(getClass());
	
	private File tplFile;
	
	private JSONObject root;
	
	public static final String TESTCOND = "_testcond";
	
	public CssSelectorTemplate(File file) throws Exception {
		this.tplFile = file;

		if (tplFile == null) {
			throw new IllegalArgumentException("Template file[" + file.getAbsolutePath() 
					+ "] mustn't be null");
		}
		if (!tplFile.exists()) {
			throw new IllegalArgumentException("Template file[" + file.getAbsolutePath() 
					+ "] must be exist");
		}
		String tplContent = IOUtil.readFileAsString(tplFile);
		try {
			parseTpl(tplContent);
		} catch (Exception e) {
			throw new CssTemplateException("Template file[" + file.getAbsolutePath() 
					+ "] parse failed:" + e.getMessage());
		}
	}
	
	public CssSelectorTemplate(String tplContent) throws Exception {
		parseTpl(tplContent);
	}
	
	/**
	 * 解析模板，如果解析失败抛出异常
	 * @throws Exception
	 */
	private void parseTpl(String tplContent) throws Exception {
		root = JSON.parseObject(tplContent);
		if (root == null) {
			throw new CssTemplateException("Template parse failed for error format!");
		}
		//模板一定要测试条件
		String testCond = root.getString(TESTCOND);
		if (testCond == null || testCond.isEmpty()) {
			throw new CssTemplateException("Template parse failed! '_testcond' is necessary");
		}
 	}
	
	/**
	 * 抽取页面内信息
	 * @param html 页面原文
	 * @return
	 * @throws Exception
	 */
	public JSONObject parse(String url, String html) throws Exception {
		Document doc = Jsoup.parse(html);
		return parse(url, doc, html);
	}
	// 兼容老接口
	public JSONObject parse(String html) throws Exception {
		Document doc = Jsoup.parse(html);
		return parse("", doc, html);
	}

	/**
	 * 抽取页面信息 
	 * @param doc 页面jsoup解析的document树
	 * @return
	 * @throws Exception
	 */
	@Override
	public JSONObject parse(String url, Document doc, String html) throws Exception {
		// 清理无用标签
		doc.getElementsByTag("script").remove();
		doc.getElementsByTag("style").remove();
		doc.getElementsByTag("iframe").remove();
		return parse(url, doc, root);
		
	}
	// 兼容老接口
	public JSONObject parse(Document doc) throws Exception {
		return parse("", doc, root);
		
	}

	/**
	 * 模板测试，看模板是否匹配
	 * @param doc
	 * @return 匹配返回true，否则返回false
	 */
	@Override
	public boolean tplTest(Document doc, String html) {
		String testCond = root.getString(TESTCOND);
		try {
			List<String> testResult = extract(doc, testCond);
			if (testResult == null || testResult.isEmpty()) {
				return false;
			}
		} catch (Exception e) {
			LOGGER.warn("Template test exception", e);
			return false;
		}
		return true;
	}
	
	/**
	 * 给定一个结点和一个条件进行抽取，返回一个结果队列
	 * @param ele
	 * @param cond
	 * @return
	 * @throws Exception
	 */
	private List<String> extract(Element ele, String cond) throws Exception {
		if (cond == null || cond.isEmpty() || ele == null) {
			//如果条件为空，直接返回null
			return null;
		}
		String [] rules = cond.split("///");
		Elements eles = ele.select(rules[0]);
		List<String> results = new ArrayList<String>();
		for(Element e : eles) {
			if (rules.length > 1) {
				if (rules[1].startsWith("text")) {
					results.add(e.ownText());
				} else if (rules[1].startsWith("html")) {
					results.add(e.html());
				} else if (rules[1].startsWith("outhtml")) {
					results.add(e.outerHtml());
				} else if (rules[1].startsWith("alltext")) {
					results.add(e.text());
				} else if (rules[1].startsWith("attr")) {
					int attrPos = rules[1].indexOf(':');
					if (attrPos < 0 || attrPos == rules[1].length()) {
						throw new CssTemplateException("Cond format exception");
					}
					String attr = rules[1].substring(attrPos + 1);
					results.add(e.attr(attr));
				}
			}else {
				results.add(e.ownText());
			}
		}
		return results;
	}
	
	/**
	 * 抽取整个模板
	 * @param ele 要抽取的内容
	 * @param conds 抽取的条件JSON格式
	 * @return 如果抽取结果里什么内容也没有，直接返回null;
	 * @throws Exception
	 */
	public JSONObject parse(String url, Element ele, JSONObject conds) throws Exception {
		
		JSONObject result = new JSONObject();
		for (Map.Entry<String, Object> cond : conds.entrySet() ) {
			if (cond.getValue() instanceof String) {
				//如果是字符串，即为抽取条件进行直接抽取
				
				//过滤cond这个为关键字，其为当前层次抽取之前进行预处理
				if (cond.getKey().equals("cond") || cond.getKey().equals(TESTCOND)) {
					continue;
				}
				
				String baseRule = (String)cond.getValue();
				//不做空判断，在extract里面会判断
				try {
					List<String> results = extract(ele, baseRule);
					
					if (results == null || results.isEmpty()) {
						//记录抽取不出结果的模板和位置抽取的链接只能前后文记录了
						/*String errStr = "Template parse error Url[" + url + "] " +
								"Template[";
						if (tplFile != null) {
							errStr += tplFile.getAbsolutePath();
						}
						errStr += "] parse failed  for line[" + cond.getKey() + 
								":" + cond.getValue() +"]";
						LOGGER.warn(errStr); */
					} else {
						result.put(cond.getKey(), results);
					}
				} catch (Exception e) {
					String errStr = "Template parse error Url[" + url + "] Template";
					if (tplFile != null) {
						errStr += "[" + tplFile.getAbsolutePath() + "]";
					}
					errStr += " parse failed  for line[" + cond.getKey() + 
							":" + cond.getValue() +"]";
					throw new CssTemplateException(errStr);
				}
			} else {
				JSONObject curCond = (JSONObject)cond.getValue();
				//如果一个结点是子结点，先获取cond进行预抽取，再对抽取出来的每一个结果进行详细抽取
				
				String condStr = curCond.getString("cond");
				//模板抽取结点一定要有cond，如果没有或是为空则返回失败
				if (condStr == null || condStr.isEmpty()) {
					String errStr = "Template parse error Url[" + url + "] Template";
					if (tplFile != null) {
						errStr += "[" + tplFile.getAbsolutePath() + "]";
					}
					errStr += " parse failed  for template format error no cond on node[" 
										+ cond.getKey() + "]";
					throw new CssTemplateException(errStr);
				}
				Elements subEles = ele.select(condStr);
				List<JSONObject> results = new ArrayList<JSONObject>();
				for(Element e : subEles) {
					JSONObject subResult = parse(url, e, curCond);
					if(subResult != null && !subResult.isEmpty())
						results.add(subResult);
				}
				if (!results.isEmpty()) {
					result.put(cond.getKey(), results);
				}
			}
		}
		//如果抽取结果中一个对象都没有返回null
		if (result.keySet().isEmpty()) {
			return null;
		}
		return result;
	}

	@Override
	public List<JSONObject> parseList(String url, String html) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}

