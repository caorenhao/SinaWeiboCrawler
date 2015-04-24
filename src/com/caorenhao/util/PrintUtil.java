package com.caorenhao.util;

import java.util.Iterator;
import java.util.List;

/**
 * 打印辅助函数.
 *
 * @author renhao.cao.
 *         Created 2015-1-21.
 */
public final class PrintUtil {

	/**
	 * 输出IP和端口.
	 *
	 * @param addrs
	 * @return String
	 */
	public static String printIPPorts(List<Pair<String, Integer>> addrs) {
		StringBuffer buf = new StringBuffer(20 * addrs.size());
		Iterator<Pair<String, Integer>> itr = addrs.iterator();
		while(true) {
			if(itr.hasNext()) {
				Pair<String, Integer> cur = itr.next();
				if(buf.length() != 0) {
					buf.append(", ");
				}
				buf.append(cur.first).append(":").append(cur.second);
			} else {
				break;
			}
		}
		return buf.toString();
	}
	
	
	/**
	 * 输出byte数组内容.
	 *
	 * @param bytes
	 * @return String
	 */
	public static String printByteArray(byte[] bytes) {
		StringBuffer sb = new StringBuffer(20 + 4*bytes.length);
		for(byte b : bytes) {
			sb.append(b).append(" ");
		}
		
		sb.append("len: ").append(bytes.length);
		return sb.toString();
	}
}
