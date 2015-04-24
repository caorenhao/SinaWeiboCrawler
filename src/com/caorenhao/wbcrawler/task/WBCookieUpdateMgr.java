package com.caorenhao.wbcrawler.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.alibaba.fastjson.JSONObject;
import com.caorenhao.util.JedisWrapper;
import com.caorenhao.util.LoggerConfig;
import com.caorenhao.wbcrawler.WBHttpClientVerify;
import com.caorenhao.wbcrawler.common.WBConst;
import com.caorenhao.wbcrawler.common.WBVar;
import com.caorenhao.wbcrawler.models.WBCookieModel;

/**
 * Cookie更新管理程序.
 *
 * @author renhao.cao.
 *         Created 2015-1-8.
 */
public class WBCookieUpdateMgr {
	
	private Log LOGGER = LoggerConfig.getLog(getClass());
	
	/**
	 * 验证队列中cookie的有效性, 对于无效的cookie使用账号重新登陆.
	 * 
	 * @param login 
	 * @param verify 
	 * @param jedis 
	 * @throws Exception 
	 */
	public void verifyMgr(WBHttpClientVerify verify, JedisWrapper jedis) throws Exception {
		List<Integer> threadIds = new ArrayList<Integer>();
		
		// cookie超过有效期, 需要重新登陆生成新的cookie, 并将cookie池中的对应cookie删除
		for(Map.Entry<Integer, WBCookieModel> entry : WBVar.cookieMaps.entrySet()) {
			boolean flag = true;
			String str = null;
			long nowTimeL = System.currentTimeMillis();
			if(nowTimeL - entry.getValue().getLoginTimeL() > WBConst.TIMEOUT_COOKIE) {
				flag = false;
				str = "cookie因超时而失效";
			}
			
			// cookie因未知原因失效了, 需要重新登陆生成新的cookie, 并将cookie池中的对应cookie删除
			boolean verification = verify.verify(entry.getValue().getCookie());
			if(!verification) {
				flag = false;
				str = "cookie因验证失败而失效";
			}
			
			// 若cookie不可用, 从可用队列中找出一个可用的cookie
			if(!flag) {
				// 将不可用的账号存入待用队列中
				JSONObject rootJson = new JSONObject();
				rootJson.put("username", entry.getValue().getUsername());
				rootJson.put("password", entry.getValue().getPassword());
				rootJson.put("waitBeginTimeL", System.currentTimeMillis());
				jedis.lpush(WBConst.ACCOUNT_UNAVAILABLE_LIST, rootJson.toJSONString());
				threadIds.add(entry.getKey());
				
				this.LOGGER.info("将不可用账号放入待用队列中\t" + str + 
						WBConst.INTERVAL + rootJson.toJSONString());
			} else {
				this.LOGGER.info("账号"+ entry.getValue().getUsername() 
						+"的cookie处于有效期内, 继续使用\t" + entry.getValue().getCookie());
			}
		}
		
		//删除cookie池中失效的cookie
		for(int id : threadIds) {
			WBVar.cookieMaps.remove(id);
		}
	}
	
}
