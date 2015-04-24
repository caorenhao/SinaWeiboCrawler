package com.caorenhao.vcrawler.vextractor;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.caorenhao.tpl.TplFileInfo;
import com.caorenhao.tpl.TplIndexSpecial;
import com.caorenhao.util.LoggerConfig;
import com.caorenhao.util.html.URLWrapper;

/**
 * CSS选择器模板管理器，特殊可以指定路径的模板工厂
 * @author scotte.ye
 *
 */
public class CssSelectorTplSpecialFactory {
	
	private static Log LOGGER = LoggerConfig.getLog(CssSelectorTplSpecialFactory.class);
	
	public static boolean isLoad = false;

	public static void init() throws Exception {
		if (isLoad) {
			return;
		}

		TplIndexSpecial.init();
		isLoad = true;
	}

	public static void init(String tplPath) throws Exception {
		if (isLoad) {
			return;
		}

		TplIndexSpecial.init(tplPath);
		isLoad = true;
	}
	
	public static JSONObject parse(String url, String html, boolean isList, boolean isUseCommonEx
			) throws Exception {
		if (!isLoad) {
			init();
		}
		if (html == null || html.trim().isEmpty()) {
			return null;
		}
		String domain = null;
		try {
			domain = new URLWrapper(url).getShortDomain();
		} catch (Exception e) {
			throw new CssTemplateException("Domain[" + domain + "] Url[" + url + "] isList[" + 
						isList + "] is illegal.", e);
		}
		
		Set<TplFileInfo> tpls = TplIndexSpecial.getTpls(domain, isList);
		JSONObject result = null;
		if (tpls != null && !tpls.isEmpty()) {
			Document doc = Jsoup.parse(html);
			for(TplFileInfo tpl : tpls) {
				try {
					ExtractorTemplateItf etpl = tpl.getTpl();
					if (etpl == null) {
						continue;
					}
					if (!etpl.tplTest(doc, html)) {
						continue;
					}
					result = etpl.parse(url, doc, html);
					if (result != null) {
						if (isList) {
							JSONArray urls = result.getJSONArray("urls");
							if (urls == null || urls.isEmpty()) {
								result = null;
							}
						} else {
							// 判断标题是否为空
							JSONArray title = result.getJSONArray("title");
							JSONArray content = result.getJSONArray("content");
							JSONArray eventTime = result.getJSONArray("eventTime");
							if (title == null) {
								result = null;
							} else if (title.size() > 0 && ((String)title.get(0)).trim().isEmpty()) {
								result = null;
							} else if (content == null) {
								result = null;
							} else if (content.size() > 0 && ((String)content.get(0)).trim().isEmpty()) {
								result = null;
							} else if (eventTime == null) {
								result = null;
							} else if (eventTime.size() > 0 && ((String)eventTime.get(0)).trim().isEmpty()) {
								result = null;
							} 
						}
						if (result != null) {
							LOGGER.info("Url[" + url + "] Template[" + tpl.getTplFile() + "]");
							break;
						}
					}
				} catch (Exception e) {
					LOGGER.info("Template [" + tpl.getTplFile() + "] parse failed", e);
					continue;
				}
			}
		}
		// FIXME 添加对正文和标题的严格过滤，如果没有正文和标题抽取都是失败的
		if (result != null) {
			
		}
		if (result == null) {
			LOGGER.warn("Template parse error Domain[" + domain + "] Url[" + url + "] isList[" +
					isList + "] no template match, check it.");
			if (isUseCommonEx) {
				// result = SCommonExtractHelper.parse(url, html, isList);
				if (result == null) {
					throw new CssTemplateException("Common extract failed Domain[" + domain + "] Url[" + url + "] isList[" +
							isList + "], check it.");
				}
			} else {
				throw new CssTemplateException("All extract failed Domain[" + domain + "] Url[" + url + "] isList[" +
						isList + "], check it.");
			}

		}
		
		LOGGER.info("Template parse done Url[" + url + "]");
		//LOGGER.info(JSONObject.toJSONString(result, true));
		return result;
	}
}
