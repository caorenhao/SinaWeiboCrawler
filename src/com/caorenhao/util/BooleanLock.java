package com.caorenhao.util;

/**
 * 封装的布尔值锁
 * @author vernkin
 *
 */
public class BooleanLock {

	/** 布尔值的值 */
	private boolean value;
	
	/**
	 * 默认值是false
	 */
	public BooleanLock() {
		this(false);
	}
	
	/**
	 * 构建布尔值锁
	 * @param initValue 初始值
	 */
	public BooleanLock(boolean initValue) {
		this.value = initValue;
	}
	
	public boolean getValue() {
		return value;
	}
	
	/**
	 * 同getValue(), 兼容类 Boolean
	 * @return
	 */
	public boolean booleanValue() {
		return value;
	}
	
	public void setValue(boolean value) {
		this.value = value;
	}
	
	/**
	 * 颠换现有的值。true变成false，false变成true
	 */
	public void revertValue() {
		this.value = !this.value;
	}

}
