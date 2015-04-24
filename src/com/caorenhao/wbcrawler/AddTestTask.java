package com.caorenhao.wbcrawler;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.caorenhao.util.JedisWrapper;
import com.caorenhao.wbcrawler.common.WBConst;
import com.caorenhao.wbcrawler.conf.ConfigSingleton;
import com.caorenhao.wbcrawler.conf.WBCrawlerConfig;

/**
 * TODO Put here a description of what this class does.
 *
 * @author renhao.cao.
 *         Created 2015-1-21.
 */
public class AddTestTask {

	/**
	 * TODO Put here a description of what this method does.
	 * 
	 * @param jedis
	 */
	public void showTask(JedisWrapper jedis) {
		List<String> tasks_userinfo = jedis.lrange(
				WBConst.WEIBO_TASK_GETUSERINFO, 0, -1);
		List<String> tasks = jedis.lrange(WBConst.WEIBO_TASK, 0, -1);
		
		System.out.println("the current tasks are : ");
		for(String task : tasks_userinfo) {
			System.out.println(task);
		}
		for(String task : tasks) {
			System.out.println(task);
		}
	}
	
	/**
	 * 删除任务.
	 *
	 * @param jedis
	 */
	public static void delTask(JedisWrapper jedis) {
		jedis.del(WBConst.WEIBO_TASK_GETUSERINFO);
		jedis.del(WBConst.WEIBO_TASK);
	}
	
	private int i = 0;
	
	/**
	 * TODO Put here a description of what this method does.
	 *
	 * @param jedis
	 */
	public void addTask(JedisWrapper jedis) {
		String[] urls = {"http://weibo.com/3808007273/CbqInmZ3Y", 
				"http://weibo.com/5303117480/Cbr8Tx3wb",
				"http://weibo.com/1863138950/Cbr8LzaLE",
				"http://weibo.com/3233866315/Cbr8wcO3M",
				"http://weibo.com/1880465055/Cbr8vB9AM",
				"http://weibo.com/1973579363/Cbr8v0BCp",
				"http://weibo.com/5553266738/Cbr8uoXiR"};
		
		for(String url :urls) {
			JSONObject rootJson = new JSONObject();
			rootJson.put("type", "userInfo");
			rootJson.put("userInfo", i + "\t" + url);
			//jedis.lpush(WBConst.WEIBO_TASK_GETUSERINFO, rootJson.toJSONString());
			i++;
		}
		
		String[] strs = {"王澜","雷军","薛蛮子","中国移动","浙江移动","浙江卫视","湖南卫视","李开复"};
		for(String str : strs) {
			JSONObject rootJson = new JSONObject();
			rootJson.put("type", "people");
			rootJson.put("people", str);
			rootJson.put("total", 5000);
			//jedis.lpush(WBConst.WEIBO_TASK, rootJson.toJSONString());
		}
		/*
		String[] blogs = {"巴萨","华为","P8","D8","手机","年终奖","贪污","腐败", 
				"王澜","雷军","薛蛮子","中国移动","浙江移动","浙江卫视","湖南卫视","李开复", 
				"震撼强拆", "众创空间", "好声音第四季", "赵薇投资阿里影业", "空少反串武媚娘", 
				"张默刑期已满", "四川公安巨贪", "中国第一狗仔卓伟", "顺丰涨价", "春晚吉祥物", 
				"朝阳门麻疹", "斗轮挖掘机", "糖粥藕西施", "上海华瑞银行", "卸妆见网友", 
				"vpn被封", "中国首富易主", "长征七号", "180万天价奖学金"};
		String[] blogss = {"武大樱花","湖北现最牛钉子户","睡衣空姐","建邺区王德宝被查",
				"中国慈善排行榜","cba历年总冠军","裴秀智承认自己整容","果敢招募中国退伍兵", 
				"重庆交警神似何以琛","深化体制机制改革","章子怡被疑怀孕","张作骥因性侵将入狱",
				"泰国辣妈网上走红","摔角手暴毙擂台","姑娘为行乞老太买饭","徐若瑄承认怀孕"};
				*/
		String[] blogss = {"黄冈", "治安", "警察", "暴力", "拆迁", 
				"鄂州", "投诉", "上访", "检举", "公安", "警察", 
				"无锡人民银行", "杭州工商信托", "新安化工", "杭州金投集团", "西子电梯", "浙大圆正", "物产元通汽车", 
				"湖南科技厅", "科研经费", "腐败", "贪污", "贿赂"};
		for(String blog : blogss) {
			JSONObject rootJson = new JSONObject();
			rootJson.put("type", "blog");
			rootJson.put("blog", blog);
			rootJson.put("total", 10);
			jedis.lpush(WBConst.WEIBO_TASK, rootJson.toJSONString());
		}
	}
	
	/**
	 * TODO Put here a description of what this method does.
	 * 
	 * @param args 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		WBCrawlerConfig config = ConfigSingleton.getWBCrawlerConfig();
		String jedisHost = config.redisConf.host;
		int jedisPort = config.redisConf.port;
		System.out.println(jedisHost);
		jedisHost = "172.16.0.138";
		JedisWrapper jedis = JedisWrapper.getInstance(jedisHost, jedisPort, 50);
		AddTestTask t = new AddTestTask();
		//t.delTask(jedis);
		//for(int i = 0; i < 10; i++) {
			//t.addTask(jedis);
		//}
		t.showTask(jedis);
	}
	
}