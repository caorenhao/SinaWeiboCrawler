package com.caorenhao.wbcrawler.task;

import org.apache.commons.logging.Log;

import com.caorenhao.util.JedisWrapper;
import com.caorenhao.util.LoggerConfig;
import com.caorenhao.util.Pair;
import com.caorenhao.wbcrawler.WBHttpClientProxy;
import com.caorenhao.wbcrawler.WBHttpClientVerify;
import com.caorenhao.wbcrawler.common.WBVar;

/**
 * 代理可用性检验类.
 *
 * @author renhao.cao.
 *         Created 2015年2月5日.
 */
public class WBProxyUpdateMgr {

private Log LOGGER = LoggerConfig.getLog(getClass());
	
	/**
	 * 验证代理的可用性.
	 * 
	 * @param verify 
	 * @param proxy 
	 * @param jedis 
	 * @throws Exception 
	 */
	public void proxyMgr(WBHttpClientVerify verify, WBHttpClientProxy proxy, 
			JedisWrapper jedis) throws Exception {
		Pair<String, Integer> pair = WBVar.proxy;
		if(pair != null) {
			boolean verification = verify.verifyProxy(pair);
			if(!verification) {
				LOGGER.info("代理:" + pair.toString() + "不可用, 更换新代理");
				WBVar.proxy = null;
			} else {
				LOGGER.info("代理:" + pair.toString() + "仍然可用, 继续使用");
			}
		}
	}
}
