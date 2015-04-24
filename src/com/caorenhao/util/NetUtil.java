package com.caorenhao.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

/**
 * 网络操作类.
 *
 * @author renhao.cao.
 *         Created 2015年2月2日.
 */
public final class NetUtil {
	
	/** IP正则*/
	public static final Pattern IP_PATTERN = Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");
	
	private static Set<String> localAddress = new HashSet<String>();
	/** 主机名 */
	private static String localHostname = null;
	
	/**
	 * 讲输入的IP 地址转换成 byte 数组，如果输入的不是IP地址，返回null
	 * @param input IP地址
	 * @return 返回IP地址的byte数组; 若输入非IP地址，返回null
	 */
	public static byte[] toIpAddressBytes(String input) {
		if(input == null)
			return null;
		Matcher m = IP_PATTERN.matcher(input);
		if(m.matches() == false)
			return null;
		byte[] ret = new byte[4];
		for(int i = 0; i < 4; ++i) {
			ret[i] = StrUtil.strToByte(m.group(i+1));
		}
		return ret;
	}
	
	/**
	 * 判断address是否ip地址
	 * @param address
	 * @return true 表示是ip地址，false表示不是，可能是域名
	 */
	public static boolean isIPAddress(String address) {
		return IP_PATTERN.matcher(address).matches();
	}
	
	public static InetAddress getInetAddress(String address) 
			throws UnknownHostException {
		return InetAddress.getByName(address);
	}
	
	public static SocketAddress getSocketAddress(int port) throws Exception {
		return new InetSocketAddress(port);
	}
	
	public static SocketAddress getSocketAddress(String host, int port) throws Exception {
		return new InetSocketAddress(getInetAddress(host), port);
	}
	
	/**
	 * 获得列表中第一个 SocketAddress
	 * @param addrs
	 * @return null 如果列表为空 
	 * @throws Exception
	 */
	public static SocketAddress getFirstSocketAddress(List<Pair<String, Integer>> addrs) 
			throws Exception {
		if(addrs == null || addrs.isEmpty())
			return null;
		Pair<String, Integer> first = addrs.get(0); 
		return getSocketAddress(first.first, first.second);
	}
	
	/**
	 * 获得列表中出去第一个以外的SocketAddress
	 * @param addrs
	 * @return null 如果列表中的数目少于2个
	 * @throws Exception
	 */
	public static SocketAddress[] getOtherSocketAddresses(List<Pair<String, Integer>> addrs) 
			throws Exception {
		if(addrs == null || addrs.size() < 2)
			return null;
		int size = addrs.size();
		SocketAddress[] ret = new SocketAddress[size - 1];
		for(int i = 1; i < size; ++i) {
			Pair<String, Integer> cur = addrs.get(i);
			ret[ i - 1 ] =  getSocketAddress(cur.first, cur.second);
		}
		return ret;
	}
	
	public static void sleep(long millis) {
		if(millis <= 0)
			return;
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {}
	}
	
	public static Calendar getCalendar() {
		return Calendar.getInstance();
	}
	
	/**
	 * 返回基于毫秒的时间戳（单位是毫秒）
	 * @return
	 */
	public static long getTimeInMillis() {
		return Calendar.getInstance().getTimeInMillis();
	}
	
	/**
	 * 返回基于秒的时间戳（单位是秒）
	 * @return
	 */
	public static long getTimeInSecs() {
		return (long)(getCalendar().getTimeInMillis() / 1000);
	}
	
	public static InetAddress getLocalHost() throws UnknownHostException {
		return InetAddress.getLocalHost();
	}
	
	public static String getLocalHostname() throws UnknownHostException {
		if(localHostname == null)
			localHostname = getLocalHost().getHostName();
		
		return localHostname;
	}
	
	public static Set<String> getLocalAddrs() throws UnknownHostException {
		if(localAddress.isEmpty()) {
			String hostName = getLocalHostname();
			InetAddress[] addrs = InetAddress.getAllByName(hostName);
			for(InetAddress addr : addrs) {
				localAddress.add(addr.getHostAddress());
			}
		}
		return localAddress;
	}
	
	/**
	 * 判断输入是本机地址
	 * @param address 域名或者IP
	 * @return
	 * @throws UnknownHostException
	 */
	public static boolean isLocalAddress(String address) throws UnknownHostException {
		InetAddress ia = getInetAddress(address);
		if(ia.isLoopbackAddress())
			return true;
		return getLocalAddrs().contains(ia.getHostAddress());
	}
	
	public static String getIP(String address) throws UnknownHostException {
		InetAddress ia = getInetAddress(address);
		return ia.getHostAddress();
	}
	
	/**
	 * 获取DNS服务器信息
	 * 
	 * @param domain	要获取DNS信息的域名
	 * @param provider	DNS服务器
	 * @param types	信息类型 "A"(IP信息)，"MX"
	 * @param timeout	请求超时
	 * @param retryCount	重试次数
	 * 
	 * @return 所有信息组成的数组
	 * 
	 * @throws NamingException
	 *         
	 */
	public static ArrayList<String> getDNSRecs(String domain, String provider, 
			String [] types, int timeout, int retryCount) throws NamingException {
		
		ArrayList<String> results = new ArrayList<String>(15);
		
		Hashtable<String, String> env = new Hashtable<String, String>();
		
		env.put("java.naming.factory.initial",
				"com.sun.jndi.dns.DnsContextFactory");
		
		//设置域名服务器
		env.put(Context.PROVIDER_URL, "dns://" + provider);
		
		// 连接时间
		env.put("com.sun.jndi.dns.timeout.initial", String.valueOf(timeout));
		
		// 连接次数
		env.put("com.sun.jndi.dns.timeout.retries", String.valueOf(retryCount));
		
		DirContext ictx = new InitialDirContext(env);
		Attributes attrs = ictx.getAttributes(domain, types);
		
		for (Enumeration<? extends Attribute> e = attrs.getAll(); e.hasMoreElements();) {
			Attribute a = e.nextElement();
			int size = a.size();
			for (int i = 0; i < size; i++) {
				results.add((String) a.get(i));
			}
		}
		return results;
	}

	/**
	 * 获取域名所有IP
	 * @param domain	域名
	 * @param dnsServers	DNS服务器列表
	 * @param timeout	请求超时
	 * @param retryCount	重试次数
	 * @return
	 */
	public static Set<String> getAllIPs(String domain, Collection<String> dnsServers,
			int timeout, int retryCount) {
		Set<String> ips = new HashSet<String>();
		
		for(String dnsServer: dnsServers) {
			List<String> ipList;
			try {
				ipList = getDNSRecs(domain, dnsServer, new String[]{"A"},
						timeout, retryCount);
			} catch (NamingException e) {
				continue;
			}
			ips.addAll(ipList);
		}
		
		return ips;
	}
	
	/**
	 * 获取urlStr指定的网络资源
	 * @param urlStr url字符串
	 * @return urlStr 代表的资源
	 * @throws Exception 网络请求中可能出现的异常
	 */
	public static byte[] getURLContent(String urlStr) throws Exception {
		URL url = new URL(urlStr);
        URLConnection yc = url.openConnection();
        return IOUtil.readInputStreamAsByteArray(yc.getInputStream());	
	}
	
	public static void testInetAddress(InetAddress addr) throws Exception {
		System.out.println("========== " + addr);
		System.out.println("getCanonicalHostName: " + addr.getCanonicalHostName());
		System.out.println("getHostAddress: " + addr.getHostAddress());
		System.out.println("getAddress: " + PrintUtil.printByteArray(addr.getAddress()));
		System.out.println("isAnyLocalAddress: " + addr.isAnyLocalAddress());
		System.out.println("isLinkLocalAddress: " + addr.isLinkLocalAddress());
		System.out.println("isLoopbackAddress: " + addr.isLoopbackAddress());
		System.out.println("isMCGlobal: " + addr.isMCGlobal());
		System.out.println("isMCNodeLocal: " + addr.isMCNodeLocal());
		System.out.println("isMCOrgLocal: " + addr.isMCOrgLocal());
		System.out.println("isMCSiteLocal: " + addr.isMCSiteLocal());
		System.out.println("isMulticastAddress: " + addr.isMulticastAddress());
		System.out.println("isReachable: " + addr.isReachable(1000));
		System.out.println("isSiteLocalAddress: " + addr.isSiteLocalAddress());
	}
}
