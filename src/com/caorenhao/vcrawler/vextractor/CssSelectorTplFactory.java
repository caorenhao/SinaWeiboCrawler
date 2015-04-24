package com.caorenhao.vcrawler.vextractor;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.caorenhao.tpl.TplFileInfo;
import com.caorenhao.tpl.TplIndex;
import com.caorenhao.util.BooleanLock;
import com.caorenhao.util.LoggerConfig;
import com.caorenhao.util.html.URLWrapper;

/**
 * CSS选择器模板管理器
 * @author scotte.ye
 *
 */
public class CssSelectorTplFactory {
	
	private static Log LOGGER = LoggerConfig.getLog(CssSelectorTplFactory.class);
	
	public static BooleanLock isLoad = new BooleanLock(false);

	public static void init() throws Exception {
		synchronized (isLoad) {
			if (isLoad.booleanValue()) {
				return;
			}
			TplIndex.init();
			isLoad.setValue(true);	
		}
	}

	public static void refreshTpls() throws Exception {
		TplIndex.init();
	}
	
	public static JSONObject parse(String url, String html, boolean isList, boolean isUseCommonEx
			) throws Exception {
		if (!isLoad.booleanValue()) {
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
		
		Set<TplFileInfo> tpls = TplIndex.getTpls(domain, isList);
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
							// 如果标题，内容或是时间为空，直接采用通用模板抽取
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
