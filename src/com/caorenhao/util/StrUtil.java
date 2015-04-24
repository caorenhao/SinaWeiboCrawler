package com.caorenhao.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 字符串操作类.
 *
 * @author renhao.cao.
 *         Created 2015年2月2日.
 */
public final class StrUtil {

	/** 编码格式*/
	public static final String UTF8 = "UTF-8";
	/** 编码格式*/
	public static final String ENCODING = UTF8;
	
	/**
	 * String转byte.
	 *
	 * @param in
	 * @return byte
	 */
	public static byte strToByte(String in) {
		return (byte)Integer.parseInt(in);
	}
	
	/**
	 * 去掉字符串前后的空格.
	 *
	 * @param str
	 * @return String
	 */
	public static String trim(String str) {
		if(str == null)
			return null;
		if(!str.isEmpty()) {
			if((int)str.charAt(0) == 65279) {
				str = str.substring(1);
			}
		}
		return str.trim();
	}
	
	/**
	 * 空字符串转换成null
	 * @param str 输入字符串
	 * @param trimmed 是否先trim字符串
	 * @return String
	 */
	public static String emptyStringToNull(String str, boolean trimmed) {
		if(str != null && trimmed)
			str = trim(str);
		return (str == null || str.isEmpty()) ? null : str;
	}
	
	/**
	 * null转换成空字符串
	 * @param str 输入字符串
	 * @param trimmed 是否先trim字符串
	 * @return String
	 */
	public static String nullToEmptyString(String str, boolean trimmed) {
		if(str != null && trimmed)
			str = trim(str);
		return (str == null) ? "" : str;
	}
	
	public static String quoteWith(String str) {
		return quoteWith(str, "");
	}
	
	public static String quoteWith(String str, String quote) {
		return quote + str + quote;
	}
	
	public static void changeEncoding(String in) throws Exception {
		String[] encs = {"UTF-8", "GB18030", "GBK", "ISO8859-1", "BIG5"};
		int encLen = encs.length;
		for(int i = 0; i < encLen; ++i) {
			System.out.print(encs[i] + "=> Null ");
			System.out.println(new String(in.getBytes(encs[i])));
			System.out.print("Null =>" + encs[i] + " ");
			System.out.println(new String(in.getBytes(), encs[i]));
			for(int j = 0; j < encLen; ++j) {
				if(i == j)
					continue;
				System.out.print(encs[i] + "=>" + encs[j] + "  ");
				String k = new String(in.getBytes(encs[i]), encs[j]);
				System.out.print(k + "  ");
				/*byte[] bs = k.getBytes();
				for(byte b : bs) {
					System.out.print((int)(b & 0x0FF) + "  ");
				}
				System.out.print(" //// ");
				bs = in.getBytes(encs[i]);
				for(byte b : bs) {
					System.out.print((int)(b & 0x0FF) + "  ");
				}*/
				System.out.println();
			}
		}
	}
	
	public static String join(Object[] array) {
		return join(array,' ');
	}
	
	/**
	 * 带分隔符的join
	 * @param array
	 * @param split
	 * @return
	 */
	public static String join(Object[] array, char split) {
		StringBuilder sb = new StringBuilder(256);
		if(array != null && array.length > 0) {
			sb.append(array[0]);
			for(int i = 1; i < array.length; ++i) {
				sb.append(split).append(array[i]);
			}
		}
		return sb.toString();
	}
	
	public static String join(Object[] array, String split) {
		StringBuilder sb = new StringBuilder(256);
		if(array != null && array.length > 0) {
			sb.append(array[0]);
			for(int i = 1; i < array.length; ++i) {
				sb.append(split).append(array[i]);
			}
		}
		return sb.toString();
	}
	
	public static String join(int[] array, String split) {
		StringBuilder sb = new StringBuilder(256);
		if(array != null && array.length > 0) {
			sb.append(array[0]);
			for(int i = 1; i < array.length; ++i) {
				sb.append(split).append(array[i]);
			}
		}
		return sb.toString();
	}
	
	public static String join(Collection<?> col, char split) {
		if(col == null)
			return "";
		return join(col.toArray(), split);
	}
	
	public static String join(Collection<?> col, String split) {
		if(col == null)
			return "";
		return join(col.toArray(), split);
	}
	
	private static void appendRandomStr_(StringBuilder sb, int len) {
		String str = UUID.randomUUID().toString();
		for(int i = 0; i < str.length(); ++i) {
			if(sb.length() >= len)
				return;
			char c = str.charAt(i);
			if(c == '-')
				continue;
			sb.append(c);
		}
	}
	
	public static String createRandomStr(int len) {
		StringBuilder sb = new StringBuilder(len);
		while(sb.length() < len) {
			appendRandomStr_(sb, len);
		}
		return sb.toString();
	}
	
	public static String encodeUrl(String string) {
		try {
			return URLEncoder.encode(string, ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String decodeUrl(String string) {
		try {
			return URLDecoder.decode(string, ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 过滤输入数组中空的 和 重复 的字符串
	 * @param array 输入的数据
	 * @return 没有任何 非空 以及 不重复的 字符时 时 返回 null
	 */
	public static String[] toNonEmptyAndDupArray(String[] array) {
		if(array == null || array.length == 0)
			return null;
		Set<String> list = new HashSet<String>();
		for(String str : array) {
			if(str == null || str.isEmpty())
				continue;
			list.add(str);
		}
		
		if(list.isEmpty())
			return null;		
		return list.toArray(new String[list.size()]);
	}
	
	/**
	 * 用于处理模板字符串的填充
	 * @param pattern 格式如：XXXXX%NNN%XXXX%kk% 
	 * 		 任何一个要被替换的位置都用%%包起来，注意最大的填充数限制为20个
	 * @param args 替换使用的字符串，其个数要与上面格式串的需要填充的位数相同
	 * @return
	 */
	public static String format(String pattern, String ... args) {
		String [] segs = pattern.split("%[^%]*%", 20);
		if (args.length != segs.length-1) {
			throw new IllegalArgumentException("The args num is not match the pattern positions");
		}
		if (segs.length <= 1) {
			return pattern;
		}
		String ret = segs[0];
		for (int i = 0; i < args.length; i++) {
			ret += args[i];
			ret += segs[i+1];
		}
		return ret;
	}
	
	/**
	 * 字符串替换，根据输入的map里的key域替换成其value
	 * @param pattern 格式如：XXXXX%key%XXXX%key1% 
	 * 		 任何一个要被替换的位置都用%%包起来,里面放key
	 * @param params
	 * @return 返回所有替换的字符串，用于判断是否产生替换，如果里面为空表示没有替换，如果有多条
	 */
	public static String format(String pattern, Map<String, String> params
			, Set<String> strSet) {
		if (pattern == null || params == null) {
			return pattern;
		}
		
		if(strSet != null)
			strSet.clear();
		
		String result = pattern;
		for (Map.Entry<String, String> entry : params.entrySet()) {
			String replaceStr = "%" + entry.getKey() + "%";
			if(pattern.contains(replaceStr)) {
				result = result.replaceAll(replaceStr, entry.getValue());
				if(strSet != null)
					strSet.add(entry.getKey());
			}	
		}
		return result;
	}
	
	public static String convertEscapeSequnce(String in)
            throws StrParserException {
        StringBuffer buf = new StringBuffer(in.length());
        int len = in.length();
        for (int i = 0; i < len; ++i) {
            char c = in.charAt(i);
            if (c == '\\') {
                if (i + 1 == len)
                    throw new StrParserException(
                            "Escape Sequence '\\' is at the end of: " + in);
                ++i;
                buf.append(convertEscapeSequnce(in.charAt(i)));
                continue;
            }

            buf.append(c);
        }

        return buf.toString();
    }

    public static char convertEscapeSequnce(char in) {
        switch (in) {
        case 'n':
            return '\n';
        case 't':
            return '\t';
        case 'b':
            return '\b';
        case 'f':
            return '\f';
        case 'r':
            return '\r';
        default:
            return in;
        }
    }
    
    public static String quote(Object obj) {
        return quote(obj.toString());
    }
    
    /**
     * Produce a string in double quotes with backslash sequences in all the
     * right places. A backslash will be inserted within </, allowing JSON
     * text to be delivered in HTML. In JSON text, a string cannot contain a
     * control character or an unescaped quote or backslash.
     * @param string A String
     * @return  A String correctly formatted for insertion in a JSON text.
     */
    public static String quote(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }

        char         b;
        char         c = 0;
        int          i;
        int          len = string.length();
        StringBuffer sb = new StringBuffer(len + 4);
        String       t;

        sb.append('"');
        for (i = 0; i < len; i += 1) {
            b = c;
            c = string.charAt(i);
            switch (c) {
            case '\\':
            case '"':
                sb.append('\\');
                sb.append(c);
                break;
            case '/':
                if (b == '<') {
                    sb.append('\\');
                }
                sb.append(c);
                break;
            case '\b':
                sb.append("\\b");
                break;
            case '\t':
                sb.append("\\t");
                break;
            case '\n':
                sb.append("\\n");
                break;
            case '\f':
                sb.append("\\f");
                break;
            case '\r':
                sb.append("\\r");
                break;
            default:
                if (c < ' ' || (c >= '\u0080' && c < '\u00a0') ||
                               (c >= '\u2000' && c < '\u2100')) {
                    t = "000" + Integer.toHexString(c);
                    sb.append("\\u" + t.substring(t.length() - 4));
                } else {
                    sb.append(c);
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }
    
    /**
     * 判断一个字符是否为占位字符
     * @param c
     * @return
     */
    public static boolean isIgnoreChar(char c) {
		if(Character.isHighSurrogate(c) || Character.isLowSurrogate(c))
			return true;
		return false;
	}

    /**
     * 过滤掉高低位占位符
     * @param src
     * @return
     */
    public static String filterSurrogate(String src) {
    	if (src == null || src.isEmpty()) {
			return src;
		}
    	StringBuffer sb = new StringBuffer();
    	boolean isStart = false;
    	for(int i = 0; i < src.length(); i++) {
    		char c = src.charAt(i);
    		if (Character.isHighSurrogate(c)) {
				isStart = true;
				continue;
			} else if(Character.isLowSurrogate(c)) {
				isStart = false;
			} else if (!isStart) {
				sb.append(c);
			} else {
				continue;
			}
    	}
    	return sb.toString();
    }
    
    public static String safeSubstring(String str, int startIdx) {
    	return safeSubstring(str, startIdx, -1);
    }
    
    /**
     * String subtring handle indexofbound exception
     * @param str
     * @param startIdx
     * @param endIdx
     * @return
     */
    public static String safeSubstring(String str, int startIdx, int endIdx) {
    	int strLen = str.length();
    	if(endIdx < 0 || endIdx > strLen) {
    		endIdx = strLen;
    	}
    	
    	if(startIdx < 0 || startIdx > endIdx) {
    		return "";
    	}
    	
    	return str.substring(startIdx, endIdx);
    }
}
