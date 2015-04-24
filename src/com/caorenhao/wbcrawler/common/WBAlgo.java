package com.caorenhao.wbcrawler.common;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

import com.alibaba.fastjson.JSONObject;
import com.caorenhao.util.JedisWrapper;
import com.caorenhao.util.NetUtil;
import com.caorenhao.util.OSUtil;
import com.caorenhao.util.StrUtil;
import com.caorenhao.util.VDateParser;
import com.caorenhao.wbcrawler.models.WBBlogModel;

/**
 * TODO Put here a description of what this class does.
 *
 * @author renhao.cao.
 *         Created 2015-1-12.
 */
public class WBAlgo {
	
	/**
	 * 将微博中的符号代码转换为标点符号.
	 *
	 * @param text
	 * @return String
	 */
	public static String symbolsFormat(String text) {
		if(text == null)
			return null;
		
		text = text.replaceAll("&lt;", "<");
		text = text.replaceAll("&gt;", ">");
		text = text.replaceAll("&nbsp;", " ");
		
		return text;
	}
	
	/**
	 * 将String格式的时间转换成Long格式的时间.
	 *
	 * @param time
	 * @param dateFormat
	 * @return Long
	 */
	public static Long stringToLong(String time, String dateFormat) {
		try {
			Date datet = new SimpleDateFormat(dateFormat).parse(time);
			long datettime = datet.getTime();
			
			return datettime;
		} catch (ParseException exception) {
			return 0L;
		}
	}
	
	/**
	 * 将Long格式的时间转换成String格式的时间.
	 *
	 * @param time
	 * @param dateFormat
	 * @return String
	 */
	public static String longToString(long time, String dateFormat) {
		SimpleDateFormat sdf= new SimpleDateFormat(dateFormat);
		Date dt = new Date(time * 1000);  
		String sDateTime = sdf.format(dt);
		
		return sDateTime;
	}
	
	/**
	 * 对微博发布时间进行标准时间格式化.
	 *
	 * @param time
	 * @return String
	 */
	public static String timeFormat(String time) {
		long nowL = System.currentTimeMillis();
		
		if(time == null) {
			String ret = longToString(nowL/1000, "yyyy-MM-dd HH:mm:ss");
			return ret;
		}
		
		String[] times = time.split(" ");
		
		if("今天".equals(times[0])) {
			String now = longToString(nowL/1000, "yyyy-MM-dd");
			time = now + " " + times[1];
		}
		
		long timeL = VDateParser.parseDate(time);
		String ret = longToString(timeL/1000, "yyyy-MM-dd HH:mm:ss");
		
		return ret;
	}
	
	/**
	 * 将文本格式化.
	 *
	 * @param text
	 * @return String
	 */
	public static String textFormat(String text) {
		if(text == null)
			return null;
		String ret = jsonFormat(text);
		
		String[] rets = ret.split(",");
		if(rets.length == 1)
			ret = ret.replaceAll("赞", "").replaceAll("评论", "").replaceAll("转发", "").trim();
		else if(rets.length > 1) {
			ret = rets[1].replaceAll("赞", "").replaceAll("评论", "").replaceAll("转发", "").trim();
		}
		
		return ret;
	}
	
	/**
	 * 去除解析内容中的符号.
	 *
	 * @param text
	 * @return String
	 */
	public static String jsonFormat(String text) {
		if(text == null)
			return null;
		
		String ret = text.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("      ", "");
		
		return ret;
	}
	
	/** sleep间隔基础时间+随机时间*/
	public static void sleep() {
		Random rand = new Random();
		int time = rand.nextInt(WBConst.TIME_INTERVAL_RANDOM);
		int waitTime = WBConst.TIME_INTERVAL_BASE + time;
		NetUtil.sleep(waitTime);
	}
	
	/**
	 * 以间隔符格式化.
	 *
	 * @param args
	 * @return String
	 */
	public static String toXlsLine(String... args) {
		List<String> attrs = new ArrayList<String>();
		for(String arg : args) {
			arg = StrUtil.nullToEmptyString(arg, true);
			attrs.add(arg);
		}
		
		return StrUtil.join(attrs, WBConst.INTERVAL);
	}
	
	/**
	 * 获取微博的赞、评论、转发的数量.
	 * 
	 * @param content 
	 * @param blogModel 
	 * @param ATTR_SPLIT 分隔符
	 * @return WBBlogModel
	 */
	public static WBBlogModel getStatisticsResult(String content, 
			WBBlogModel blogModel, String ATTR_SPLIT) {
		String[] texts = content.split(ATTR_SPLIT);
		for(String text : texts) {
			if(text.contains("赞["))
				blogModel.setAttitudeNum(Integer.parseInt(textFormat(text)));
			else if(text.contains("转发["))
				blogModel.setRepostum(Integer.parseInt(textFormat(text)));
			else if(text.contains("评论["))
				blogModel.setCommentNum(Integer.parseInt(textFormat(text)));
		}
		
		return blogModel;
	}
	
	/** 
     * 设置时间 
     * @param year 
     * @param month 
     * @param date 
     * @return Calendar
     */  
    private static Calendar setCalendar(int year,int month,int date){  
        Calendar cl = Calendar.getInstance();  
        cl.set(year, month-1, date);  
        return cl;  
    }
	
	/** 
     * 获取当前时间的前一天时间 
     * @param cl 
     * @return 
     */  
    private static Calendar getBeforeDay(Calendar cl){  
        //使用roll方法进行向前回滚  
        //cl.roll(Calendar.DATE, -1);  
        //使用set方法直接进行设置  
        int day = cl.get(Calendar.DATE);  
        cl.set(Calendar.DATE, day-1);  
        return cl;  
    }
	
	/**
	 * TODO Put here a description of what this method does.
	 *
	 * @return long
	 */
	public static long getCrawlerTime() {
		long currentTime = System.currentTimeMillis();
		int year = Integer.valueOf(longToString(currentTime/1000, "yyyy"));
		int month = Integer.valueOf(longToString(currentTime/1000, "MM"));
		int day = Integer.valueOf(longToString(currentTime/1000, "dd"));
		int hour = Integer.valueOf(longToString(currentTime/1000, "HH"));
		Calendar cl = setCalendar(Integer.valueOf(year), Integer.valueOf(month), 
        		Integer.valueOf(day));
		cl.set(Integer.valueOf(year), Integer.valueOf(month)-1, Integer.valueOf(day), 
        		11, 0, 0);
		if(hour < 11)
			cl = getBeforeDay(cl);
		long retTime = cl.getTimeInMillis();
		
		return retTime;
	}
	
	/**
	 * 获取本机内网IP.
	 *
	 * @return String
	 */
	public static String getLocalIP() {
		String sIP = "";
        InetAddress ip = null;
        try {
            // 如果是Windows操作系统
            if (OSUtil.getOSType().isWindows()) {
                ip = InetAddress.getLocalHost();
            }
            // 如果是Linux操作系统
            else {
                boolean bFindIP = false;
                Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
                while (netInterfaces.hasMoreElements()) {
                    if (bFindIP) {
                        break;
                    }
                    NetworkInterface ni = netInterfaces
                            .nextElement();
                    // ----------特定情况，可以考虑用ni.getName判断
                    // 遍历所有ip
                    Enumeration<InetAddress> ips = ni.getInetAddresses();
                    while (ips.hasMoreElements()) {
                        ip = ips.nextElement();
                        if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress() // 127.开头的都是lookback地址
                                && ip.getHostAddress().indexOf(":") == -1) {
                            bFindIP = true;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
  
        if (null != ip) {
            sIP = ip.getHostAddress();
        }
        
        return sIP;
	}
	
	/**
	 * 抓取数量统计, 以及最新抓取数据.
	 * 
	 * @param blog 
	 * @param jedis 
	 */
	public static synchronized  void Statistics(JedisWrapper jedis) {
		long currentTime = System.currentTimeMillis()/1000;
		String currentDate = longToString(currentTime, "yyyy-MM-dd");
		String todayHistory = jedis.hget(WBConst.WEIBO_CRAWLER_HISTORY, 
				currentDate);
		int count = 0;
		JSONObject todayJson = new JSONObject();
		if(todayHistory != null && !todayHistory.isEmpty()) {
			todayJson = JSONObject.parseObject(todayHistory);
			if(todayJson.containsKey(WBVar.IP)) {
				String todayLocalHistory = todayJson.getString(WBVar.IP);
				JSONObject todayLocalJson = JSONObject.parseObject(todayLocalHistory);
				count = todayLocalJson.getIntValue("count");
			}
		}
		System.out.println("count:" + count);
		JSONObject newTodayLocalJson = new JSONObject();
		count++;
		newTodayLocalJson.put("count", count);
		//String lastCrawlBlog = blog.toOutput();
		//newTodayLocalJson.put("lastCrawlBlog", lastCrawlBlog);
		todayJson.put(WBVar.IP, newTodayLocalJson.toJSONString());
		jedis.hset(WBConst.WEIBO_CRAWLER_HISTORY, currentDate, 
				todayJson.toJSONString());
	}
	
	/**
	 * 测试.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		//String str = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /><meta http-equiv=\"Cache-Control\" content=\"no-cache\"/><meta id=\"viewport\" name=\"viewport\" content=\"width=device-width,initial-scale=1.0,minimum-scale=1.0, maximum-scale=2.0\" /><meta name=\"MobileOptimized\" content=\"240\"/><title>传说中的木头君的微博</title><style type=\"text/css\" id=\"internalStyle\">html,body,p,form,div,table,textarea,input,span,select{font-size:16px;word-wrap:break-word;}body{background:#F8F9F9;color:#000;padding:1px;margin:1px;}table,tr,td{border-width:0px;margin:0px;padding:0px;}form{margin:0px;padding:0px;border:0px;}textarea{border:1px solid #96c1e6}textarea{width:95%;}a,.tl{color:#2a5492;text-decoration:underline;}/*a:link {color:#023298}*/.k{color:#2a5492;text-decoration:underline;}.kt{color:#F00;}.ib{border:1px solid #C1C1C1;}.pm,.pmy{clear:both;background:#ffffff;color:#676566;border:1px solid #b1cee7;padding:3px;margin:2px 1px;overflow:hidden;}.pms{clear:both;background:#c8d9f3;color:#666666;padding:3px;margin:0 1px;overflow:hidden;}.pmst{margin-top: 5px;}.pmsl{clear:both;padding:3px;margin:0 1px;overflow:hidden;}.pmy{background:#DADADA;border:1px solid #F8F8F8;}.t{padding:0px;margin:0px;height:35px;}.b{background:#e3efff;text-align:center;color:#2a5492;clear:both;padding:4px;}.bl{color:#2a5492;}.n{clear:both;background:#436193;color:#FFF;padding:4px; margin: 1px;}.nt{color:#b9e7ff;}.nl{color:#FFF;text-decoration:none;}.nfw{clear:both;border:1px solid #BACDEB;padding:3px;margin:2px 1px;}.s{border-bottom:1px dotted #666666;margin:3px;clear:both;}.tip{clear:both; background:#c8d9f3;color:#676566;border:1px solid #BACDEB;padding:3px;margin:2px 1px;}.tip2{color:#000000;padding:2px 3px;clear:both;}.ps{clear:both;background:#FFF;color:#676566;border:1px solid #BACDEB;padding:3px;margin:2px 1px;}.tm{background:#feffe5;border:1px solid #e6de8d;padding:4px;}.tm a{color:#ba8300;}.tmn{color:#f00}.tk{color:#ffffff}.tc{color:#63676A;}.c{padding:2px 5px;}.c div a img{border:1px solid #C1C1C1;}.ct{color:#9d9d9d;font-style:italic;}.cmt{color:#9d9d9d;}.ctt{color:#000;}.cc{color:#2a5492;}.nk{color:#2a5492;}.por {border: 1px solid #CCCCCC;height:50px;width:50px;}.me{color:#000000;background:#FEDFDF;padding:2px 5px;}.pa{padding:2px 4px;}.nm{margin:10px 5px;padding:2px;}.hm{padding:5px;background:#FFF;color:#63676A;}.u{margin:2px 1px;background:#ffffff;border:1px solid #b1cee7;}.ut{padding:2px 3px;}.cd{text-align:center;}.r{color:#F00;}.g{color:#0F0;}.bn{background: transparent;border: 0 none;text-align: left;padding-left: 0;}</style><script>if(top != self){top.location = self.location;}</script></head><body><div class=\"n\" style=\"padding: 6px 4px;\"><a href=\"http://weibo.cn/?tf=5_009\" class=\"nl\">首页</a>|<a href=\"http://weibo.cn/msg/?tf=5_010\" class=\"nl\">消息</a>|<a href=\"http://huati.weibo.cn\" class=\"nl\">话题</a>|<a href=\"http://weibo.cn/search/?tf=5_012\" class=\"nl\">搜索</a>|<a href=\"/u/2875617610?rand=4590&amp;p=r\" class=\"nl\">刷新</a></div><div class=\"u\"><table><tr><td valign=\"top\"><a href=\"/2875617610/avatar?rl=0\"><img src=\"http://tp3.sinaimg.cn/2875617610/50/5717395923/1\" alt=\"头像\" class=\"por\" /></a></td><td valign=\"top\"><div class=\"ut\"><span class=\"ctt\">传说中的木头君<img src=\"http://u1.sinaimg.cn/upload/2011/08/16/5547.gif\" alt=\"达人\"/><a href=\"http://vip.weibo.cn/?F=W_tq_zsbs_01\"><img src=\"http://u1.sinaimg.cn/upload/h5/img/hyzs/donate_btn_s.png\" alt=\"M\"/></a>&nbsp;男/浙江    &nbsp;    <a href=\"/attention/add?uid=2875617610&amp;rl=0&amp;st=54f4bb\">加关注</a></span><br /><span class=\"ctt\" style=\"word-break:break-all; width:50px;\">蓝色，天空蓝。</span><br /><a href=\"/im/chat?uid=2875617610&amp;rl=0\">私信</a>&nbsp;<a href=\"/2875617610/info\">资料</a>&nbsp;<a href=\"/2875617610/operation?rl=0\">操作</a>&nbsp;<a href=\"/attgroup/special?fuid=2875617610&amp;st=54f4bb\">特别关注</a>&nbsp;<a href=\"http://new.vip.weibo.cn/vippay/payother?present=1&amp;action=comfirmTime&amp;uid=2875617610\">送Ta会员</a></div></td></tr></table><div class=\"tip2\"><span class=\"tc\">微博[106]</span>&nbsp;<a href=\"/2875617610/follow\">关注[144]</a>&nbsp;<a href=\"/2875617610/fans\">粉丝[250]</a>&nbsp;<a href=\"/attgroup/opening?uid=2875617610\">分组[1]</a>&nbsp;<a href=\"/at/weibo?uid=2875617610\">@他的</a></div></div><div class=\"pmst\"><span class=\"pms\">&nbsp;微博&nbsp;</span><span class=\"pmsl\">&nbsp;<a href=\"/2875617610/photo?tf=6_008\">相册</a>&nbsp;</span></div><div class=\"pms\" >全部-<a href=\"/u/2875617610?filter=1\">原创</a>-<a href=\"/u/2875617610?filter=2\">图片</a>-<a href=\"/attgroup/opening?uid=2875617610\">分组</a>-<a href=\"/2875617610/search?f=u&amp;rl=0\">筛选</a></div><div class=\"c\" id=\"M_C8E86f6ZY\"><div>[<span class=\"kt\">置顶</span>]<span class=\"ctt\">很不要脸的给自己发了个合集。原因只有一个！要粉丝[拜拜]让我丑美一次。</span>&nbsp;[<a href=\"http://weibo.cn/mblog/picAll/C8E86f6ZY?rl=1\">组图共9张</a>]</div><div><a href=\"http://weibo.cn/mblog/pic/C8E86f6ZY?rl=0\"><img src=\"http://ww4.sinaimg.cn/wap180/ab66714ajw1eq67ugsb7gj218g18g7gv.jpg\" alt=\"图片\" class=\"ib\" /></a>&nbsp;<a href=\"http://weibo.cn/mblog/oripic?id=C8E86f6ZY&amp;u=ab66714ajw1eq67ugsb7gj218g18g7gv\">原图</a>&nbsp;<a href=\"http://weibo.cn/attitude/C8E86f6ZY/add?uid=5574149266&amp;rl=0&amp;st=54f4bb\">赞[4]</a>&nbsp;<a href=\"http://weibo.cn/repost/C8E86f6ZY?uid=2875617610&amp;rl=0\">转发[5]</a>&nbsp;<a href=\"http://weibo.cn/comment/C8E86f6ZY?uid=2875617610&amp;rl=0#cmtfrm\" class=\"cc\">评论[3]</a>&nbsp;<a href=\"http://weibo.cn/fav/addFav/C8E86f6ZY?rl=0&amp;st=54f4bb\">收藏</a><!---->&nbsp;<span class=\"ct\">03月15日 10:46&nbsp;来自不会游泳的魅族</span></div></div><div class=\"s\"></div><div class=\"c\" id=\"M_CcblRv3oe\"><div><span class=\"cmt\">转发了&nbsp;<a href=\"http://weibo.cn/lvbear\">时尚熊熊杂志</a><img src=\"http://u1.sinaimg.cn/upload/h5/img/hyzs/donate_btn_s.png\" alt=\"M\"/>&nbsp;的微博:</span><span class=\"ctt\">你们的BF都他妈哪找的？又好看又听话又有钱?</span></div><div><a href=\"http://weibo.cn/mblog/pic/Cc5vUBK6H?rl=0\"><img src=\"http://ww1.sinaimg.cn/wap180/703021d1jw1eqwe0jjbgxj207h06m74g.jpg\" alt=\"图片\" class=\"ib\" /></a>&nbsp;<a href=\"http://weibo.cn/mblog/oripic?id=Cc5vUBK6H&amp;u=703021d1jw1eqwe0jjbgxj207h06m74g\">原图</a>&nbsp;<span class=\"cmt\">赞[107]</span>&nbsp;<span class=\"cmt\">原文转发[192]</span>&nbsp;<a href=\"http://weibo.cn/comment/Cc5vUBK6H?rl=0#cmtfrm\" class=\"cc\">原文评论[71]</a><!----></div><div><span class=\"cmt\">转发理由:</span>一杯咖啡骗来的//<a href=\"/n/%E5%86%BD%E6%B4%8C%E9%85%B4%E9%86%BE%E5%B1%B1%E7%A5%9E%E7%8C%AB\">@冽洌酴醾山神猫</a>: 被卡在电视里被我捡着了！//<a href=\"/n/%E7%B1%B3%E6%9D%BE%E5%BD%A6%E5%AE%87\">@米松彦宇</a>:移动赠送话费 //<a href=\"/n/%E6%88%91%E6%98%AF%E6%A0%BE%E5%8C%85%E5%AD%90\">@我是栾包子</a>:做梦找的//<a href=\"/n/%E5%B0%8F%E8%83%96%E5%91%86%E5%91%86%E7%88%B1%E8%82%89%E8%82%89\">@小胖呆呆爱肉肉</a>: 捡的<a href=\"/n/%E7%AD%89ing%E7%9A%84%E6%98%9F%E6%98%9F\">@等ing的星星</a> [嘻嘻][嘻嘻][嘻嘻]//<a href=\"/n/%E6%B5%81%E7%81%AB-bear\">@流火-bear</a>:拼单送的//<a href=\"/n/%E7%82%BD%E9%98%B3%E5%A4%A9\">@炽阳天</a>:哈哈哈，路边捡的//<a href=\"/n/%E5%A4%B1%E6%8E%A7%E7%9A%84%E8%96%84%E8%8D%B7%E6%80%AA%E8%9C%80%E9%BB%8D\">@失控的薄荷怪蜀黍</a>:右边骗人 京东根本没有卖//<a href=\"/n/%E6%99%9A%E4%B8%8A%E6%88%91%E5%8E%BB%E5%88%A0%E5%BE%AE%E5%8D%9A\">@晚上我去删微博</a>: 京东//<a href=\"/n/%E9%A6%83%E5%86%BB\">@馃冻</a>:&nbsp;&nbsp;<a href=\"http://weibo.cn/attitude/CcblRv3oe/add?uid=5574149266&amp;rl=0&amp;st=54f4bb\">赞[0]</a>&nbsp;<a href=\"http://weibo.cn/repost/CcblRv3oe?uid=2875617610&amp;rl=0\">转发[0]</a>&nbsp;<a href=\"http://weibo.cn/comment/CcblRv3oe?uid=2875617610&amp;rl=0#cmtfrm\" class=\"cc\">评论[0]</a>&nbsp;<a href=\"http://weibo.cn/fav/addFav/CcblRv3oe?rl=0&amp;st=54f4bb\">收藏</a><!---->&nbsp;<span class=\"ct\">今天 16:52&nbsp;来自搜狗高速浏览器</span></div></div><div class=\"s\"></div><div class=\"c\" id=\"M_Cc1xtC2SL\"><div><span class=\"cmt\">转发了&nbsp;<a href=\"http://weibo.cn/579097700\">萧逸森</a><img src=\"http://u1.sinaimg.cn/upload/2011/08/16/5547.gif\" alt=\"达人\"/><img src=\"http://u1.sinaimg.cn/upload/h5/img/hyzs/donate_btn_s.png\" alt=\"M\"/>&nbsp;的微博:</span><span class=\"ctt\"><a href=\"http://weibo.cn/pages/100808topic?extparam=%E8%BD%AC%E5%8F%91%E7%A6%8F%E5%88%A9&amp;from=feed\">#转发福利#</a>新品发布, 转发此微博即刻参与抽奖, 9号晚抽取6名同学??, 以下新款六选一, 包邮送出[飞吻], 店铺地址[向右]<a href=\"http://weibo.cn/sinaurl?f=w&amp;u=http%3A%2F%2Ft.cn%2FRAMVpvz&amp;ep=Cc1xtC2SL%2C2875617610%2CCc1sJAhRR%2C1937882762\">http://t.cn/RAMVpvz</a></span>&nbsp;[<a href=\"http://weibo.cn/mblog/picAll/Cc1sJAhRR?rl=1\">组图共6张</a>]</div><div><a href=\"http://weibo.cn/mblog/pic/Cc1sJAhRR?rl=0\"><img src=\"http://ww4.sinaimg.cn/wap180/7381be8ajw1eqvw4m37tfj20fl0fxtbt.jpg\" alt=\"图片\" class=\"ib\" /></a>&nbsp;<a href=\"http://weibo.cn/mblog/oripic?id=Cc1sJAhRR&amp;u=7381be8ajw1eqvw4m37tfj20fl0fxtbt\">原图</a>&nbsp;<span class=\"cmt\">赞[2]</span>&nbsp;<span class=\"cmt\">原文转发[20]</span>&nbsp;<a href=\"http://weibo.cn/comment/Cc1sJAhRR?rl=0#cmtfrm\" class=\"cc\">原文评论[5]</a><!----></div><div><span class=\"cmt\">转发理由:</span>哎哟，&nbsp;&nbsp;<a href=\"http://weibo.cn/attitude/Cc1xtC2SL/add?uid=5574149266&amp;rl=0&amp;st=54f4bb\">赞[0]</a>&nbsp;<a href=\"http://weibo.cn/repost/Cc1xtC2SL?uid=2875617610&amp;rl=0\">转发[0]</a>&nbsp;<a href=\"http://weibo.cn/comment/Cc1xtC2SL?uid=2875617610&amp;rl=0#cmtfrm\" class=\"cc\">评论[0]</a>&nbsp;<a href=\"http://weibo.cn/fav/addFav/Cc1xtC2SL?rl=0&amp;st=54f4bb\">收藏</a><!---->&nbsp;<span class=\"ct\">04月06日 15:53&nbsp;来自不会游泳的魅族</span></div></div><div class=\"s\"></div><div class=\"c\" id=\"M_CbZDFFt8R\"><div><span class=\"cmt\">转发了&nbsp;<a href=\"http://weibo.cn/u/2669293710\">杰_酱</a><img src=\"http://u1.sinaimg.cn/upload/2011/08/16/5547.gif\" alt=\"达人\"/>&nbsp;的微博:</span><span class=\"ctt\">帮我求个粉好不[可怜][可怜][可怜]谢谢各位</span>&nbsp;[<a href=\"http://weibo.cn/mblog/picAll/CbZ0ND3Ue?rl=1\">组图共5张</a>]</div><div><a href=\"http://weibo.cn/mblog/pic/CbZ0ND3Ue?rl=0\"><img src=\"http://ww3.sinaimg.cn/wap180/9f1a308ejw1eqvl5jjgbyj20f40qo3zw.jpg\" alt=\"图片\" class=\"ib\" /></a>&nbsp;<a href=\"http://weibo.cn/mblog/oripic?id=CbZ0ND3Ue&amp;u=9f1a308ejw1eqvl5jjgbyj20f40qo3zw\">原图</a>&nbsp;<span class=\"cmt\">赞[26]</span>&nbsp;<span class=\"cmt\">原文转发[44]</span>&nbsp;<a href=\"http://weibo.cn/comment/CbZ0ND3Ue?rl=0#cmtfrm\" class=\"cc\">原文评论[78]</a><!----></div><div><span class=\"cmt\">转发理由:</span>杰宝果然是个名媛[doge]&nbsp;&nbsp;<a href=\"http://weibo.cn/attitude/CbZDFFt8R/add?uid=5574149266&amp;rl=0&amp;st=54f4bb\">赞[0]</a>&nbsp;<a href=\"http://weibo.cn/repost/CbZDFFt8R?uid=2875617610&amp;rl=0\">转发[1]</a>&nbsp;<a href=\"http://weibo.cn/comment/CbZDFFt8R?uid=2875617610&amp;rl=0#cmtfrm\" class=\"cc\">评论[1]</a>&nbsp;<a href=\"http://weibo.cn/fav/addFav/CbZDFFt8R?rl=0&amp;st=54f4bb\">收藏</a><!---->&nbsp;<span class=\"ct\">04月06日 11:03&nbsp;来自不会游泳的魅族</span></div></div><div class=\"s\"></div><div class=\"c\" id=\"M_CbIYci2kZ\"><div><span class=\"cmt\">转发了&nbsp;<a href=\"http://weibo.cn/u/2747621955\">辛普辛普晟-</a><img src=\"http://u1.sinaimg.cn/upload/2011/08/16/5547.gif\" alt=\"达人\"/>&nbsp;的微博:</span><span class=\"ctt\">炉火纯青的摄影师</span>&nbsp;[<a href=\"http://weibo.cn/mblog/picAll/CbHjhpWhr?rl=1\">组图共6张</a>]</div><div><a href=\"http://weibo.cn/mblog/pic/CbHjhpWhr?rl=0\"><img src=\"http://ww4.sinaimg.cn/wap180/a3c56243jw1eqtf5gqtm6j20tj18g7bz.jpg\" alt=\"图片\" class=\"ib\" /></a>&nbsp;<a href=\"http://weibo.cn/mblog/oripic?id=CbHjhpWhr&amp;u=a3c56243jw1eqtf5gqtm6j20tj18g7bz\">原图</a>&nbsp;<span class=\"cmt\">赞[5]</span>&nbsp;<span class=\"cmt\">原文转发[2]</span>&nbsp;<a href=\"http://weibo.cn/comment/CbHjhpWhr?rl=0#cmtfrm\" class=\"cc\">原文评论[4]</a><!----></div><div><span class=\"cmt\">转发理由:</span>天了噜。&nbsp;&nbsp;<a href=\"http://weibo.cn/attitude/CbIYci2kZ/add?uid=5574149266&amp;rl=0&amp;st=54f4bb\">赞[1]</a>&nbsp;<a href=\"http://weibo.cn/repost/CbIYci2kZ?uid=2875617610&amp;rl=0\">转发[0]</a>&nbsp;<a href=\"http://weibo.cn/comment/CbIYci2kZ?uid=2875617610&amp;rl=0#cmtfrm\" class=\"cc\">评论[3]</a>&nbsp;<a href=\"http://weibo.cn/fav/addFav/CbIYci2kZ?rl=0&amp;st=54f4bb\">收藏</a><!---->&nbsp;<span class=\"ct\">04月04日 16:36&nbsp;来自不会游泳的魅族</span></div></div><div class=\"s\"></div><div class=\"c\" id=\"M_CaYDICxPD\"><div><span class=\"cmt\">转发了&nbsp;<a href=\"http://weibo.cn/u/2643090193\">夏未晨空宁相思</a><img src=\"http://u1.sinaimg.cn/upload/2011/08/16/5547.gif\" alt=\"达人\"/>&nbsp;的微博:</span><span class=\"ctt\"><a href=\"http://weibo.cn/pages/100808topic?extparam=%E4%B8%8D%E4%B8%80%E6%A0%B7%E7%9A%84%E7%BE%8E%E7%94%B7&amp;from=feed\">#不一样的美男#</a> <a href=\"http://weibo.cn/pages/100808topic?extparam=ladyboyfriends&amp;from=feed\">#ladyboyfriends#</a> 【gif】第十四集简直不能更狗血。。。伤害值太大了。。。就。。。让我死吧。。。[伤心][伤心][伤心]<a href=\"http://weibo.cn/pages/100808topic?extparam=Jet&amp;from=feed\">#Jet#</a> <a href=\"http://weibo.cn/pages/100808topic?extparam=New&amp;from=feed\">#New#</a> <a href=\"http://weibo.cn/pages/100808topic?extparam=Frank&amp;from=feed\">#Frank#</a> <a href=\"http://weibo.cn/pages/100808topic?extparam=Tiw&amp;from=feed\">#Tiw#</a> <a href=\"http://weibo.cn/pages/100808topic?extparam=Wut&amp;from=feed\">#Wut#</a></span>&nbsp;[<a href=\"http://weibo.cn/mblog/picAll/CaVFpBAmV?rl=1\">组图共9张</a>]</div><div><a href=\"http://weibo.cn/mblog/pic/CaVFpBAmV?rl=0\"><img src=\"http://ww3.sinaimg.cn/wap180/9d8a5b11jw1eqnk2ucsy1g209707p4qs.gif\" alt=\"图片\" class=\"ib\" /></a>&nbsp;<a href=\"http://weibo.cn/mblog/oripic?id=CaVFpBAmV&amp;u=9d8a5b11jw1eqnk2ucsy1g209707p4qs\">原图</a>&nbsp;<span class=\"cmt\">赞[184]</span>&nbsp;<span class=\"cmt\">原文转发[744]</span>&nbsp;<a href=\"http://weibo.cn/comment/CaVFpBAmV?rl=0#cmtfrm\" class=\"cc\">原文评论[265]</a><!----></div><div><span class=\"cmt\">转发理由:</span>编剧车赛卡！！！！！能不能好好的甜！！！j拔屌太无情！饭卡破功了么。&nbsp;&nbsp;<a href=\"http://weibo.cn/attitude/CaYDICxPD/add?uid=5574149266&amp;rl=0&amp;st=54f4bb\">赞[0]</a>&nbsp;<a href=\"http://weibo.cn/repost/CaYDICxPD?uid=2875617610&amp;rl=0\">转发[4]</a>&nbsp;<a href=\"http://weibo.cn/comment/CaYDICxPD?uid=2875617610&amp;rl=0#cmtfrm\" class=\"cc\">评论[0]</a>&nbsp;<a href=\"http://weibo.cn/fav/addFav/CaYDICxPD?rl=0&amp;st=54f4bb\">收藏</a><!---->&nbsp;<span class=\"ct\">03月30日 18:40&nbsp;来自不会游泳的魅族</span></div></div><div class=\"s\"></div><div class=\"c\" id=\"M_CaUGVgUzb\"><div><span class=\"cmt\">转发了&nbsp;<a href=\"http://weibo.cn/u/2291420972\">-柴二虎-</a><img src=\"http://u1.sinaimg.cn/upload/2011/08/16/5547.gif\" alt=\"达人\"/>&nbsp;的微博:</span><span class=\"ctt\">查看文章<a href=\"http://weibo.cn/sinaurl?f=w&amp;u=http%3A%2F%2Ft.cn%2FRAbMduo&amp;ep=CaUGVgUzb%2C2875617610%2CCaS8iE12P%2C2291420972\">《你知道什么是柴二虎么？》</a> 深夜科普向。</span>&nbsp;<span class=\"cmt\">赞[4]</span>&nbsp;<span class=\"cmt\">原文转发[5]</span>&nbsp;<a href=\"http://weibo.cn/comment/CaS8iE12P?rl=0#cmtfrm\" class=\"cc\">原文评论[13]</a><!----></div><div><span class=\"cmt\">转发理由:</span>这简直就是交友贴&nbsp;&nbsp;<a href=\"http://weibo.cn/attitude/CaUGVgUzb/add?uid=5574149266&amp;rl=0&amp;st=54f4bb\">赞[0]</a>&nbsp;<a href=\"http://weibo.cn/repost/CaUGVgUzb?uid=2875617610&amp;rl=0\">转发[0]</a>&nbsp;<a href=\"http://weibo.cn/comment/CaUGVgUzb?uid=2875617610&amp;rl=0#cmtfrm\" class=\"cc\">评论[3]</a>&nbsp;<a href=\"http://weibo.cn/fav/addFav/CaUGVgUzb?rl=0&amp;st=54f4bb\">收藏</a><!---->&nbsp;<span class=\"ct\">03月30日 08:37&nbsp;来自不会游泳的魅族</span></div></div><div class=\"s\"></div><div class=\"c\" id=\"M_CaUFrnRrQ\"><div><span class=\"cmt\">转发了&nbsp;<a href=\"http://weibo.cn/msannang\">ChillA-</a><img src=\"http://u1.sinaimg.cn/upload/2011/08/16/5547.gif\" alt=\"达人\"/><img src=\"http://u1.sinaimg.cn/upload/h5/img/hyzs/donate_btn_s.png\" alt=\"M\"/>&nbsp;的微博:</span><span class=\"ctt\">草！！！！！！！<a href=\"http://weibo.cn/pages/100808topic?extparam=%E4%B8%8D%E4%B8%80%E6%A0%B7%E7%9A%84%E7%BE%8E%E7%94%B7&amp;from=feed\">#不一样的美男#</a></span>&nbsp;[<a href=\"http://weibo.cn/mblog/picAll/CaR513w3b?rl=1\">组图共9张</a>]</div><div><a href=\"http://weibo.cn/mblog/pic/CaR513w3b?rl=0\"><img src=\"http://ww3.sinaimg.cn/wap180/66fb31a5jw1eqn0jvt52bj218g0xc7bz.jpg\" alt=\"图片\" class=\"ib\" /></a>&nbsp;<a href=\"http://weibo.cn/mblog/oripic?id=CaR513w3b&amp;u=66fb31a5jw1eqn0jvt52bj218g0xc7bz\">原图</a>&nbsp;<span class=\"cmt\">赞[1200]</span>&nbsp;<span class=\"cmt\">原文转发[4312]</span>&nbsp;<a href=\"http://weibo.cn/comment/CaR513w3b?rl=0#cmtfrm\" class=\"cc\">原文评论[8]</a><!----></div><div><span class=\"cmt\">转发理由:</span>炸了！！！！！！！！//<a href=\"/n/%E8%99%8E%E5%8F%94%E4%B8%B6\">@虎叔丶</a>:卧槽！！！！ //<a href=\"/n/%E9%98%B3%E6%9D%BE%E4%B8%B6\">@阳松丶</a>:wocao !!!!!!!!!!!!!!!!!//<a href=\"/n/%E4%B8%80%E5%8F%AA%E5%87%8D%E7%8A%AC\">@一只凍犬</a>: 卧槽！！！！！！ //<a href=\"/n/Oktavia-\">@Oktavia-</a>:[吃惊]//<a href=\"/n/%E9%85%B8%E9%92%BEW\">@酸钾W</a>: //<a href=\"/n/Nacktr\">@Nacktr</a>:我的妈！！！！！&nbsp;&nbsp;<a href=\"http://weibo.cn/attitude/CaUFrnRrQ/add?uid=5574149266&amp;rl=0&amp;st=54f4bb\">赞[0]</a>&nbsp;<a href=\"http://weibo.cn/repost/CaUFrnRrQ?uid=2875617610&amp;rl=0\">转发[2]</a>&nbsp;<a href=\"http://weibo.cn/comment/CaUFrnRrQ?uid=2875617610&amp;rl=0#cmtfrm\" class=\"cc\">评论[1]</a>&nbsp;<a href=\"http://weibo.cn/fav/addFav/CaUFrnRrQ?rl=0&amp;st=54f4bb\">收藏</a><!---->&nbsp;<span class=\"ct\">03月30日 08:33&nbsp;来自不会游泳的魅族</span></div></div><div class=\"s\"></div><div class=\"c\" id=\"M_CaPADvTyo\"><div><span class=\"cmt\">转发了&nbsp;<a href=\"http://weibo.cn/u/2473037997\">OneChain忘川魄</a><img src=\"http://u1.sinaimg.cn/upload/2011/08/16/5547.gif\" alt=\"达人\"/>&nbsp;的微博:</span><span class=\"ctt\">[眼泪][眼泪][眼泪]伪娘顺<a href=\"/n/%E4%BB%93%E9%BC%A0%E5%8C%85%E5%AD%90%E9%93%BA\">@仓鼠包子铺</a></span>&nbsp;[<a href=\"http://weibo.cn/mblog/picAll/CaP6PeGAS?rl=1\">组图共3张</a>]</div><div><a href=\"http://weibo.cn/mblog/pic/CaP6PeGAS?rl=0\"><img src=\"http://ww2.sinaimg.cn/wap180/936790adjw1eqmrvbv8hwj20qo0zktd7.jpg\" alt=\"图片\" class=\"ib\" /></a>&nbsp;<a href=\"http://weibo.cn/mblog/oripic?id=CaP6PeGAS&amp;u=936790adjw1eqmrvbv8hwj20qo0zktd7\">原图</a>&nbsp;<span class=\"cmt\">赞[1]</span>&nbsp;<span class=\"cmt\">原文转发[11]</span>&nbsp;<a href=\"http://weibo.cn/comment/CaP6PeGAS?rl=0#cmtfrm\" class=\"cc\">原文评论[9]</a><!----></div><div><span class=\"cmt\">转发理由:</span>哈哈哈哈哈哈哈//<a href=\"/n/%E8%99%8E%E5%8F%94%E4%B8%B6\">@虎叔丶</a>: 哈哈哈哈哈&nbsp;&nbsp;<a href=\"http://weibo.cn/attitude/CaPADvTyo/add?uid=5574149266&amp;rl=0&amp;st=54f4bb\">赞[0]</a>&nbsp;<a href=\"http://weibo.cn/repost/CaPADvTyo?uid=2875617610&amp;rl=0\">转发[1]</a>&nbsp;<a href=\"http://weibo.cn/comment/CaPADvTyo?uid=2875617610&amp;rl=0#cmtfrm\" class=\"cc\">评论[0]</a>&nbsp;<a href=\"http://weibo.cn/fav/addFav/CaPADvTyo?rl=0&amp;st=54f4bb\">收藏</a><!---->&nbsp;<span class=\"ct\">03月29日 19:37&nbsp;来自搜狗高速浏览器</span></div></div><div class=\"s\"></div><div class=\"c\" id=\"M_CaLcnwXO6\"><div><span class=\"cmt\">转发了&nbsp;<a href=\"http://weibo.cn/306259138\">阳松丶</a><img src=\"http://u1.sinaimg.cn/upload/2011/08/16/5547.gif\" alt=\"达人\"/>&nbsp;的微博:</span><span class=\"ctt\">难以相信我以前居然这么非主流，不过这皮肤真好。。现在的我真是日了狗</span>&nbsp;[<a href=\"http://weibo.cn/mblog/picAll/CaJdSf8fb?rl=1\">组图共6张</a>]</div><div><a href=\"http://weibo.cn/mblog/pic/CaJdSf8fb?rl=0\"><img src=\"http://ww2.sinaimg.cn/wap180/6f512f29jw1eqm1vbmgtcj20bc0f53yu.jpg\" alt=\"图片\" class=\"ib\" /></a>&nbsp;<a href=\"http://weibo.cn/mblog/oripic?id=CaJdSf8fb&amp;u=6f512f29jw1eqm1vbmgtcj20bc0f53yu\">原图</a>&nbsp;<span class=\"cmt\">赞[11]</span>&nbsp;<span class=\"cmt\">原文转发[12]</span>&nbsp;<a href=\"http://weibo.cn/comment/CaJdSf8fb?rl=0#cmtfrm\" class=\"cc\">原文评论[11]</a><!----></div><div><span class=\"cmt\">转发理由:</span>道理我都懂，但是为何这么水嫩&nbsp;&nbsp;<a href=\"http://weibo.cn/attitude/CaLcnwXO6/add?uid=5574149266&amp;rl=0&amp;st=54f4bb\">赞[0]</a>&nbsp;<a href=\"http://weibo.cn/repost/CaLcnwXO6?uid=2875617610&amp;rl=0\">转发[0]</a>&nbsp;<a href=\"http://weibo.cn/comment/CaLcnwXO6?uid=2875617610&amp;rl=0#cmtfrm\" class=\"cc\">评论[0]</a>&nbsp;<a href=\"http://weibo.cn/fav/addFav/CaLcnwXO6?rl=0&amp;st=54f4bb\">收藏</a><!---->&nbsp;<span class=\"ct\">03月29日 08:27&nbsp;来自不会游泳的魅族</span></div></div><div class=\"s\"></div><div class=\"c\" id=\"M_CaFqbxczT\"><div><span class=\"ctt\">[偷笑][偷笑][偷笑][偷笑][偷笑]笑笑不说话。舒服。</span></div><div><a href=\"http://weibo.cn/mblog/pic/CaFqbxczT?rl=0\"><img src=\"http://ww4.sinaimg.cn/wap180/ab66714ajw1eqll3rf2guj20y00y079x.jpg\" alt=\"图片\" class=\"ib\" /></a>&nbsp;<a href=\"http://weibo.cn/mblog/oripic?id=CaFqbxczT&amp;u=ab66714ajw1eqll3rf2guj20y00y079x\">原图</a>&nbsp;<a href=\"http://weibo.cn/attitude/CaFqbxczT/add?uid=5574149266&amp;rl=0&amp;st=54f4bb\">赞[0]</a>&nbsp;<a href=\"http://weibo.cn/repost/CaFqbxczT?uid=2875617610&amp;rl=0\">转发[0]</a>&nbsp;<a href=\"http://weibo.cn/comment/CaFqbxczT?uid=2875617610&amp;rl=0#cmtfrm\" class=\"cc\">评论[2]</a>&nbsp;<a href=\"http://weibo.cn/fav/addFav/CaFqbxczT?rl=0&amp;st=54f4bb\">收藏</a><!---->&nbsp;<span class=\"ct\">03月28日 17:44&nbsp;来自不会游泳的魅族</span></div></div><div class=\"s\"></div><div class=\"pa\" id=\"pagelist\"><form action=\"/u/2875617610\" method=\"post\"><div><a href=\"/u/2875617610?page=2\">下页</a>&nbsp;<input name=\"mp\" type=\"hidden\" value=\"11\" /><input type=\"text\" name=\"page\" size=\"2\" style='-wap-input-format: \"*N\"' /><input type=\"submit\" value=\"跳页\" />&nbsp;1/11页</div></form></div><div class=\"pm\"><form action=\"/search/\" method=\"post\"><div><input type=\"text\" name=\"keyword\" value=\"\" size=\"15\" /><input type=\"submit\" name=\"smblog\" value=\"搜微博\" /><input type=\"submit\" name=\"suser\" value=\"找人\" /><br/><span class=\"pmf\"><a href=\"/search/mblog/?keyword=%E9%95%BF%E5%AF%BF%E5%95%86%E4%BC%9A%E6%9C%B4%E7%81%BF%E7%83%88&amp;rl=0\" class=\"k\">长寿商会朴灿烈</a>&nbsp;<a href=\"/search/mblog/?keyword=%E4%B8%BA%E5%A5%B9%E8%80%8C%E6%88%98&amp;rl=0\" class=\"k\">为她而战</a>&nbsp;<a href=\"/search/mblog/?keyword=%E4%B8%9C%E4%BA%AC%E9%A3%9F%E5%B0%B8%E9%AC%BC&amp;rl=0\" class=\"k\">东京食尸鬼</a>&nbsp;<a href=\"/search/mblog/?keyword=EXO%E5%87%BA%E9%81%93%E4%B8%89%E5%91%A8%E5%B9%B4%E5%B7%A6%E5%BF%83%E6%88%BF%E4%B8%BA%E4%BD%A0%E6%95%9E%E5%A4%A7%E5%A4%A7&amp;rl=0\" class=\"k\">EXO出道三周年左心房为你敞大大</a>&nbsp;<a href=\"/search/mblog/?keyword=%E8%9C%80%E5%B1%B1%E6%88%98%E7%BA%AA%E4%B9%8B%E5%89%91%E4%BE%A0%E4%BC%A0%E5%A5%87&amp;rl=0\" class=\"k\">蜀山战纪之剑侠传奇</a></span></div></form></div><div class=\"cd\"><a href=\"#top\"><img src=\"http://u1.sinaimg.cn/3g/image/upload/0/62/203/18979/5e990ec2.gif\" alt=\"TOP\"/></a></div><div class=\"pms\"> <a href=\"http://weibo.cn\">首页</a>.<a href=\"http://weibo.cn/topic/240489\">反馈</a>.<a href=\"http://weibo.cn/page/91\">帮助</a>.<a  href=\"http://down.sina.cn/weibo/default/index/soft_id/1/mid/0\"  >客户端</a>.<a href=\"http://weibo.cn/spam/?rl=0&amp;type=3&amp;fuid=2875617610\" class=\"kt\">举报</a>.<a href=\"http://passport.sina.cn/sso/logout?r=http%3A%2F%2Fweibo.cn%2Fpub%2F%3Fvt%3D&amp;entry=mweibo\">退出</a></div><div class=\"c\">设置:<a href=\"http://weibo.cn/account/customize/skin?tf=7_005&amp;st=54f4bb\">皮肤</a>.<a href=\"http://weibo.cn/account/customize/pic?tf=7_006&amp;st=54f4bb\">图片</a>.<a href=\"http://weibo.cn/account/customize/pagesize?tf=7_007&amp;st=54f4bb\">条数</a>.<a href=\"http://weibo.cn/account/privacy/?tf=7_008&amp;st=54f4bb\">隐私</a></div><div class=\"c\">彩版|<a href=\"http://m.weibo.cn/?tf=7_010\">触屏</a>|<a href=\"http://weibo.cn/page/521?tf=7_011\">语音</a></div><div class=\"b\">weibo.cn[04-07 23:20]</div></body></html>";
		//String ret = extract(str, ">粉丝\\[", "\\]我");
		//System.out.println(ret);
		//String host = "172.16.0.138";
		//int port = 6379;
		//JedisWrapper jedis = JedisWrapper.getInstance(host, port, 10);
		//WBVar.IP = "172.16.0.106";
		//Statistics(jedis);
		//getStatisticsResult("\"\",\"原图\",\"赞[85]\",\"转发[60]\",\"评论[63]\",\"收藏\"");
	}
}
