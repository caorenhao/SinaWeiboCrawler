package com.caorenhao.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Object 转换成具体的类型
 * @author vernkin
 *
 */
public final class NumberFormatUtil {

	/** 0 字符数组，第一个元素有1个0，第二个有两个0，依次类推 */
	private static String[] ZERO_ARRAY = new String[50];
	/** 浮点数格式，第一个为 0（无小数点）， 第二个为 0.0（一个小数点），依次类推 */
	private static DecimalFormat[] DECIMAL_FORMATS = new DecimalFormat[20];
	
	static {
		ZERO_ARRAY[0] = "0";
		for(int i = 1; i < ZERO_ARRAY.length; ++i) {
			ZERO_ARRAY[i] = ZERO_ARRAY[i - 1] + "0";
		}
		
		DECIMAL_FORMATS[0] = new DecimalFormat("0");
		DECIMAL_FORMATS[0].setRoundingMode(RoundingMode.FLOOR);
		for(int i = 1; i < DECIMAL_FORMATS.length; ++i) {
			DECIMAL_FORMATS[i] = new DecimalFormat("0." + getZeroString(i));
			DECIMAL_FORMATS[i].setRoundingMode(RoundingMode.FLOOR);
		}
	}
	
	private static String getZeroString(int len) {
		if(len <= ZERO_ARRAY.length) {
			// 需要填补的 0 在 ZERO_ARRAY 中可获得
			return ZERO_ARRAY[len - 1];
		}
		StringBuffer sb = new StringBuffer(len);
		for(int i = 0; i < len; ++i)
			sb.append('0');
		return sb.toString();
	}
	
	private static DecimalFormat getDecimalFormat(int precision) {
		if(precision < DECIMAL_FORMATS.length)
			return DECIMAL_FORMATS[precision];		
		DecimalFormat ret = new DecimalFormat("0." + getZeroString(precision));
		ret.setRoundingMode(RoundingMode.FLOOR);
		return ret;
	}
	
	public static String formatNumber(long val, int len) {
		boolean isPositive = true;
		if(val < 0) {
			val = -val;
			isPositive = false;
		}
		
		String ret = String.valueOf(val);
		if(ret.length() < len) {
			ret = getZeroString(len - ret.length()) + ret;
		}
		
		if(isPositive)
			return ret;
		
		return "-" + ret;
	}
	
	public static String formatDecimal(double val, int len, int precision) {
		boolean isPositive = true;
		if(val < 0) {
			val = -val;
			isPositive = false;
		}
		
		String ret = getDecimalFormat(precision).format(val);
		int dotIdx = ret.lastIndexOf('.');
		if(dotIdx < 0)
			dotIdx = ret.length();
		if(dotIdx < len) {
			ret = getZeroString(len - dotIdx) + ret;
		}
		
		if(isPositive)
			return ret;
		
		return "-" + ret;
	}
	
	public static int toInt( Object o ){
        if ( o == null )
            throw new NullPointerException( "can't be null" );

        if ( o instanceof Number )
            return ((Number)o).intValue();

        if ( o instanceof Boolean )
            return ((Boolean)o) ? 1 : 0;

        throw new IllegalArgumentException( "Can't convert: " + o.getClass().getName() + 
        		" to int" );
    }
	
	/**
	 * String 转换成 Number的值
	 * @param <T> 标准的Number子类
	 * @param clazz 标准的Number子类
	 * @param val Stirng的值
	 * @return
	 */
	public static<T extends Number> Number stringToNumber(Class<T> clazz, String val) {
		if(clazz == null)
			throw new IllegalArgumentException("clazz CAN'T be NULL");
		
		if(clazz.equals(Integer.class)) {
			return Integer.parseInt(val);
		}
		
		if(clazz.equals(Long.class)) {
			return Long.parseLong(val);
		}
		
		if(clazz.equals(Double.class)) {
			return Double.parseDouble(val);
		}
		
		if(clazz.equals(Long.class)) {
			return Float.parseFloat(val);
		}
		
		if(clazz.equals(Short.class)) {
			return Short.parseShort(val);
		}
		
		if(clazz.equals(Byte.class)) {
			return Byte.parseByte(val);
		}
		
		throw new IllegalArgumentException("Can't Detect Type inherit from Number: " + clazz);
	}
}
