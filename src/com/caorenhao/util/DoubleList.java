package com.caorenhao.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * 双列表，用于多线程工作的模式
 * 一个列表用于做缓冲区  BufferList, 同时给多个线程使用
 * 一个列表用于做批量处理 BatchList，只能被一个线程访问
 * @author vernkin
 *
 */
public class DoubleList<T> {

	/**
	 * 列表模式
	 * @author vernkin
	 *
	 */
	public static enum ListType {
		ArrayList,
		LinkedList;
	}
	
	private List<T>[] lists;
	
	private int bufferIdx;
	
	
	private BooleanLock BufferListLock = new BooleanLock(true);
	
	/**
	 * 默认 ArrayList方式
	 */
	public DoubleList() {
		this(ListType.ArrayList);
	}
	
	/**
	 * 传递list类别来创建
	 * @param type
	 */
	@SuppressWarnings("unchecked")
	public DoubleList(ListType type) {
		if(type == null)
			throw new IllegalArgumentException("ListType can't be null");
		List<T>[] l = (List<T>[])(new List[2]);
		if(type == ListType.ArrayList) {
			l[0] = new ArrayList<T>();
			l[1] = new ArrayList<T>();
		} else if(type == ListType.LinkedList) {
			l[0] = new LinkedList<T>();
			l[1] = new LinkedList<T>();
		}
		initialize(l);
	}
	
	/**
	 * 直接传入原始的 具有两个元素的数组
	 * @param lists
	 */
	public DoubleList(List<T>[] lists) {
		initialize(lists);
	}
	
	private void initialize(List<T>[] lists) {
		if(lists == null || lists.length != 2)
			throw new IllegalArgumentException("lists length must be 2");
		if(lists[0] == null || lists[1] == null)
			throw new IllegalArgumentException("two elements in lists " +
					"can't not be null");
			
		this.lists = lists;
		bufferIdx = 0;
	}
	
	/**
	 * 添加到 BufferList
	 * @param obj 一个元素
	 */
	public void addToBufferList(T obj) {
		synchronized(BufferListLock) {
			lists[bufferIdx].add(obj);
		}
	}
	
	/**
	 * 添加到 BufferList
	 * @param objs 多个元素
	 */
	public void addToBufferList(Collection<T> objs) {
		synchronized(BufferListLock) {
			lists[bufferIdx].addAll(objs);
		}
	}
	
	/**
	 * 获取BatchList，只能被一个线程访问
	 * @return
	 */
	public List<T> getBatchList() {
		return lists[1 - bufferIdx];
	}
	
	/**
	 * 交换 BatchList 和  BufferList
	 */
	public void swap() {
		synchronized(BufferListLock) {
			bufferIdx = 1 - bufferIdx;
		}
	}
	
	public int size() {
		int ret = 0;
		synchronized(BufferListLock) {
			ret += lists[bufferIdx].size();
			ret += lists[1 - bufferIdx].size();
		}
		return ret;
	}
	
	public int bufferListSize() {
		synchronized(BufferListLock) {
			return lists[bufferIdx].size();
		}
	}
}
