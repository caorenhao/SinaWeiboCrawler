package com.caorenhao.util;

/**
 * Pair<V1, V2>定义.
 *
 * @author renhao.cao.
 *         Created 2015-1-21.
 * @param <V1>
 * @param <V2>
 */
public class Pair<V1, V2> {

	/** 第一个元素*/
	public V1 first;
	
	/** 第二个元素*/
	public V2 second;
	
	
	/** Pair定义.*/
	public Pair() {
		
	}
	
	/**
	 * 带初始化的Pair定义.
	 *
	 * @param first
	 * @param second
	 */
	public Pair(V1 first, V2 second) {
		this.first = first;
		this.second = second;
	}
	
	@Override
	public String toString() {
		return "(" + this.first + "," + this.second + ")";
	}
}
