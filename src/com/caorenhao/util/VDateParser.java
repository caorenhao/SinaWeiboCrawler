package com.caorenhao.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用的日期解析方式
 * @author vernkin
 *
 */
public class VDateParser {
	
	/** 小时的毫秒数 */
	public static final long HOUR = 3600 * 1000;
	
	/** 天的毫秒数 */
	public static final long DAY = 24 * HOUR;
	
	public static final long INVALID_DATE = Long.MIN_VALUE;
	
	/** 字符类型 */
	private enum CharCateType {
		/** 数字 */ DIGIT,
		/** 时间分隔符 */ TIME_SEP,
		/** 日期分隔符 */ DATE_SEP,
		/** 空格 */ SPACE;
	}
	
	private enum CharType {
		DIGIT(CharCateType.DIGIT),
		/** 空格 */ SPACE(CharCateType.SPACE),
		DATE(CharCateType.DATE_SEP),
		TIME(CharCateType.TIME_SEP),
		
		YEAR(CharCateType.DATE_SEP, true),
		MONTH(CharCateType.DATE_SEP, true),
		DAY(CharCateType.DATE_SEP, true),
		
		HOUR(CharCateType.TIME_SEP, true),
		MINUTE(CharCateType.TIME_SEP, true),
		SECOND(CharCateType.TIME_SEP, true)
		;
		
		/** 字符类型 */
		public CharCateType cateType;
		/** 对于时间和日期分隔符，是否指定准确的时间 */
		//public boolean isSpecified;
		
		private CharType(CharCateType cateType) {
			this(cateType, false);
		}
		
		private CharType(CharCateType cateType, boolean isSpecified) {
			this.cateType = cateType;
			//this.isSpecified = isSpecified;
		}
		
		public boolean isSep() {
			return cateType == CharCateType.DATE_SEP || 
				cateType == CharCateType.TIME_SEP;
		}
	}
	
	private static final String fullChar = "　１２３４５６７８９０／－：";
	private static final String halfChar = " 1234567890/-:";
	
	/** 全角字符 到 半角字符的映射 */
	private static final Map<Character, Character> fullHalfCharMap = 
		new HashMap<Character, Character>();
	
	private static final Map<Character, CharType> charTypeMap = 
		new HashMap<Character, CharType>();
	
	static {
		for(int i = 0; i < fullChar.length(); ++i) {
			fullHalfCharMap.put(fullChar.charAt(i), halfChar.charAt(i));
		}
		
		for(int i = 0; i < 10; ++i) {
			charTypeMap.put(String.valueOf(i).charAt(0), CharType.DIGIT);
		}
		charTypeMap.put(' ', CharType.SPACE);
		charTypeMap.put('/', CharType.DATE);
		charTypeMap.put('-', CharType.DATE);
		charTypeMap.put('.', CharType.DATE);
		charTypeMap.put(':', CharType.TIME);
		
		charTypeMap.put('年', CharType.YEAR);
		charTypeMap.put('月', CharType.MONTH);
		charTypeMap.put('日', CharType.DAY);
		
		charTypeMap.put('时', CharType.HOUR);
		charTypeMap.put('点', CharType.HOUR);
		charTypeMap.put('分', CharType.MINUTE);
		charTypeMap.put('秒', CharType.SECOND);
	}
	
	
	private static class DateUnit {
		public boolean isSet = false;
		public boolean hasDefValue = false;
		
		public int value = 0;
		
		/** Calendar 对应的常量 */
		public int calConstant;
		
		public DateUnit(int calConstant) {
			this.calConstant = calConstant;
		}
		
		public DateUnit(int calConstant, int defValue) {
			this.calConstant = calConstant;
			this.value = defValue;
			this.hasDefValue = true;
		}
		
		public void setValue(int value) {
			this.value = value;
			this.isSet = true;
		}
		
		/** 是否有效值。包括有默认值 或者 已经设置的值 */
		public boolean isValidValue() {
			return this.hasDefValue || this.isSet;
		}
	}
	
	private static class DateParserImpl {
		private String input;
		/** 检查的时间 */
		private Calendar checkDate;
		
		/** 处理后的字符串 */
		private String formattedInput;
		
		/** 处理后的日期 */
		private Calendar formattedCal;
		
		private CharType[] types;
		
		private DateUnit[] dateUnits;
		
		public DateParserImpl(String input, Calendar checkDate) {
			this.input = input;
			this.checkDate = checkDate;
		}
		
		/**
		 * 格式化输入
		 */
		private void formatInput() {
			StringBuilder sb = new StringBuilder(input.length());
			CharType lastCharType = null;
			List<CharType> charTypes = new ArrayList<CharType>();
			
			for(int i = 0; i < input.length(); ++i) {
				char c = input.charAt(i);
				Character halfC = fullHalfCharMap.get(c);
				if(halfC != null) {
					c = halfC.charValue();
				}
				
				CharType curCharType = charTypeMap.get(c);
				// 数字后面的字符没有类型，并且前面没有其它的日期单位，忽略最后的数字
				if(curCharType == null) {
					if(lastCharType != null && lastCharType == CharType.DIGIT) {
						boolean toDeleteTrailingDigits = true;
						for(CharType ct : charTypes) {
							if(ct.cateType == CharCateType.DATE_SEP || 
									ct.cateType == CharCateType.TIME_SEP) {
								toDeleteTrailingDigits = false;
								break;
							}
						}
						while(toDeleteTrailingDigits) {
							charTypes.remove(charTypes.size() - 1);
							sb.deleteCharAt(sb.length() - 1);
							
							if(charTypes.isEmpty()) {
								lastCharType = null;
								break;
							} else {
								lastCharType = charTypes.get(charTypes.size() - 1);
								if(lastCharType != CharType.DIGIT)
									break;
							}
						}
					}
					continue;
				}
				// 忽略起始空格和连续空格
				if(curCharType == CharType.SPACE) {
					if(lastCharType == null || 
							lastCharType == CharType.SPACE)
						continue;
				}
				
				// 忽略非两个数字之间的空格
				if(lastCharType == CharType.SPACE) {
					if(curCharType != CharType.DIGIT || 
							charTypes.get(charTypes.size() - 2) != CharType.DIGIT) {
						sb.deleteCharAt(sb.length() - 1);
						charTypes.remove(charTypes.size() - 1);
					}
				}
				
				// 分隔符必须在数字后面
				if(curCharType.cateType == CharCateType.DATE_SEP || 
						curCharType.cateType == CharCateType.TIME_SEP) {
					if(lastCharType == null || lastCharType != CharType.DIGIT) {
						continue;
					}
				}
				
				sb.append(c);
				charTypes.add(curCharType);
				lastCharType = curCharType;
			}
			
			// 删除结束的空格
			while(!charTypes.isEmpty() && charTypes.get(charTypes.size() - 1) 
					== CharType.SPACE) {
				sb.deleteCharAt(sb.length() - 1);
				charTypes.remove(charTypes.size() - 1);
			}
			
			formattedInput = sb.toString();
			//System.out.println("formatInput:" + formattedInput + " <= " + input);
			types = charTypes.toArray(new CharType[charTypes.size()]);
			
			boolean isAddDigit = true;
			for(CharType ct : types) {
				if(ct != CharType.DIGIT) {
					isAddDigit = false;
					break;
				}
			}
			
			// 针对 yyyyMMDD 的情况
			if(isAddDigit && types.length == 8) {
				String newInput = formattedInput.substring(0, 4) + "-" + 
						formattedInput.substring(4, 6) + "-" + formattedInput.substring(6, 8);
				this.input = newInput;
				// 重新format
				formatInput();
			}
		}
		
		/** 识别日期 */
		private void detectDate() {
			dateUnits = new DateUnit[6];
			// 依次是年月日 时分秒 初始化
			dateUnits[0] = new DateUnit(Calendar.YEAR, checkDate.get(Calendar.YEAR));
			dateUnits[1] = new DateUnit(Calendar.MONTH);
			dateUnits[2] = new DateUnit(Calendar.DAY_OF_MONTH);
			dateUnits[3] = new DateUnit(Calendar.HOUR_OF_DAY, 0);
			dateUnits[4] = new DateUnit(Calendar.MINUTE, 0);
			dateUnits[5] = new DateUnit(Calendar.SECOND, 0);
			
			
			
			// 至多解析两层结构
			int nextStartIdx = parseNextStartIdx(0);
			if(nextStartIdx > 0) {
				parseNextStartIdx(nextStartIdx);
			}
			
			// 存储当前的日历
			Calendar cal = Calendar.getInstance();
			
			// 检查日期合法性
			for(DateUnit du : dateUnits) {
				if(!du.isValidValue())
					return;
				if(du.calConstant == Calendar.MONTH)
					cal.set(du.calConstant, du.value - 1);
				else
					cal.set(du.calConstant, du.value);
			}
			
			// 年份主动设置的检查是否两位年份 
			if(dateUnits[0].isSet) {
				if(dateUnits[0].value < 100) {
					int yearVal = dateUnits[0].value; 
					int base = checkDate.get(dateUnits[0].calConstant);
					base = base - (base % 100); // 去除个位和十位
					while(true) {
						cal.set(dateUnits[0].calConstant, base + yearVal);
						if(checkDate.after(cal)) {
							break;
						}
						base -= 100;
					}
				}
			}
			else {
				//年份不是主动设置的时候的值
				// 如果比检查时间早的话，主动往前退一年
				if(checkDate.before(cal)) {
					cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) - 1);
				}
			}
			// 清除毫秒
			cal.set(Calendar.MILLISECOND, 0);
			formattedCal = cal;
		}
		
		private int parseNextStartIdx(int beginOffset) {
			// 寻找第一个数字, 做为开始处理的位移
			int startIdx = -1;
			
			for(int i = beginOffset; i < types.length; ++i) {
				if(types[i] == CharType.DIGIT) {
					startIdx = i;
					break;
				}
			}
			
			// 不是以数字开头，直接忽略
			if(startIdx < 0)
				return -1;
			
			// 寻找第一个分隔符
			int firstSepIdx = -1;
			for(int i = startIdx + 1; i < types.length; ++i) {
				if(types[i].isSep()) {
					firstSepIdx = i;
					break;
				}
			}
			
			// 不存在第一个分割符，无效日期
			if(firstSepIdx < 0)
				return -1;
			
			// 确定是时间还是日期分隔符
			CharCateType sepType = types[firstSepIdx].cateType;
			
			int firstSepEndIdx = findSepEndIdx(firstSepIdx, sepType); // 时间可能越界
			int secondStartIdx = -1;
			if(sepType == CharCateType.DATE_SEP) {
				secondStartIdx = parseDate(startIdx, firstSepEndIdx);
			} else {
				secondStartIdx = parseTime(startIdx, firstSepEndIdx);
			}
			
			return secondStartIdx;
		}
		
		private int findSepEndIdx(int sepStartIdx, CharCateType sepType) {
			int i = sepStartIdx + 1;
			int lastSepIdx = i; // 前一个分隔符
			for( ; i < types.length; ++i) {
				if(types[i] == CharType.SPACE) {
					break;
				}
				
				// 出现类型不同的分隔符， 往前推到前一个分隔符
				if(types[i].isSep()) {
					if(types[i].cateType != sepType) {
						i = lastSepIdx;
						break;
					}
					lastSepIdx = i;
				}
			}
			
			return i;
		}
		
		private void extractDigitAndSeps(int beginIdx, int endIdx, 
				List<Integer> digitA, List<CharType> typeA) {
			int i = beginIdx;
			while(i < endIdx) {
				CharType curCT = types[i];
				if(curCT == CharType.DIGIT) {
					int j = i + 1;
					for(; j < endIdx; ++j) {
						if(types[j] != CharType.DIGIT)
							break;
					}
					digitA.add(Integer.parseInt(formattedInput.substring(i, j)));
					i = j;
					continue;
				}
				typeA.add(types[i]);
				++i;
			}
		}
		
		/**
		 * 解析时间
		 * @param beginIdx 起始位移，包括
		 * @param endIdx 终止位移，不包括
		 * @return 最终停止解析的位移, -1表示解析失败
		 */
		public int parseDate(int beginIdx, int endIdx) {
			// 单独的时间
			if(endIdx < types.length && types[endIdx] == CharType.DATE) {
				int digitCnt = 0;
				int idx = endIdx + 1;
				for(; idx < types.length; ++idx) {
					if(types[idx] == CharType.DIGIT)
						++digitCnt;
					else
						break;
				}
				
				// 校验没有空格的错误
				if(digitCnt == 4) {
					endIdx += 3;
				}
			}
			
			List<Integer> digitA = new ArrayList<Integer>(3);
			List<CharType> typeA = new ArrayList<CharType>(3);
			
			extractDigitAndSeps(beginIdx, endIdx, digitA, typeA);

			int digitSize = digitA.size();
			// 年月日
			if(digitSize == 3) {
				// 没有指定的间隔，直接按照年月日划分
				if(typeA.get(0) == CharType.DATE) {
					dateUnits[0].setValue(digitA.get(0));
					dateUnits[1].setValue(digitA.get(1));
					dateUnits[2].setValue(digitA.get(2));
				} else {
					int idx = -1;
					boolean[] setUnit = {false, false, false};
					for(CharType ct : typeA) {
						++idx;
						switch(ct) {
						case YEAR:
							setUnit[0] = true;
							dateUnits[0].setValue(digitA.get(idx));
							break;
						case MONTH:
							setUnit[1] = true;
							dateUnits[1].setValue(digitA.get(idx));
							break;
						case DAY:
							setUnit[2] = true;
							dateUnits[2].setValue(digitA.get(idx));
							break;
						default:
							return -1;
						}
					}
					
					if(idx < 2) {
						for(int i = 0; i < setUnit.length; ++i) {
							if(!setUnit[i]) {
								dateUnits[i].setValue(digitA.get(idx + 1));
								break;
							}
						}
					}
				}
			} else if(digitSize == 2) {
				// 没有指定的间隔，直接按照月日划分
				if(typeA.get(0) == CharType.DATE) {
					dateUnits[1].setValue(digitA.get(0));
					dateUnits[2].setValue(digitA.get(1));
				} else {
					int idx = -1;
					boolean[] setUnit = {false, false};
					for(CharType ct : typeA) {
						++idx;
						switch(ct) {
						case MONTH:
							setUnit[0] = true;
							dateUnits[1].setValue(digitA.get(idx));
							break;
						case DAY:
							setUnit[1] = true;
							dateUnits[2].setValue(digitA.get(idx));
							break;
						default:
							return -1;
						}
					}
					
					if(idx < 1) {
						for(int i = 0; i < setUnit.length; ++i) {
							if(!setUnit[i]) {
								dateUnits[i+1].setValue(digitA.get(idx + 1));
								break;
							}
						}
					}
				}
			} else {
				return -1;
			}
			
			return endIdx;
		}
		
		public int parseTime(int beginIdx, int endIdx) {
			List<Integer> digitA = new ArrayList<Integer>(3);
			List<CharType> typeA = new ArrayList<CharType>(3);
			
			extractDigitAndSeps(beginIdx, endIdx, digitA, typeA);

			int digitSize = digitA.size();
			// 时分秒
			if(digitSize == 3) {
				// 没有指定的间隔，直接按照时分秒划分
				if(typeA.get(0) == CharType.TIME) {
					dateUnits[3].setValue(digitA.get(0));
					dateUnits[4].setValue(digitA.get(1));
					dateUnits[5].setValue(digitA.get(2));
				} else {
					int idx = -1;
					boolean[] setUnit = {false, false, false};
					for(CharType ct : typeA) {
						++idx;
						switch(ct) {
						case HOUR:
							setUnit[0] = true;
							dateUnits[3].setValue(digitA.get(idx));
							break;
						case MINUTE:
							setUnit[1] = true;
							dateUnits[4].setValue(digitA.get(idx));
							break;
						case SECOND:
							setUnit[2] = true;
							dateUnits[5].setValue(digitA.get(idx));
							break;
						default:
							return -1;
						}
					}
					
					if(idx < 2) {
						for(int i = 0; i < setUnit.length; ++i) {
							if(!setUnit[i]) {
								dateUnits[i + 3].setValue(digitA.get(idx + 1));
								break;
							}
						}
					}
				}
			} else if(digitSize == 2) {
				// 没有指定的间隔，直接按照时分划分
				if(typeA.get(0) == CharType.TIME) {
					dateUnits[3].setValue(digitA.get(0));
					dateUnits[4].setValue(digitA.get(1));
				} else {
					int idx = -1;
					boolean[] setUnit = {false, false};
					for(CharType ct : typeA) {
						++idx;
						switch(ct) {
						case HOUR:
							setUnit[0] = true;
							dateUnits[3].setValue(digitA.get(idx));
							break;
						case MINUTE:
							setUnit[1] = true;
							dateUnits[4].setValue(digitA.get(idx));
							break;
						default:
							return -1;
						}
					}
					
					if(idx < 1) {
						for(int i = 0; i < setUnit.length; ++i) {
							if(!setUnit[i]) {
								dateUnits[i+3].setValue(digitA.get(idx + 1));
								break;
							}
						}
					}
				}
			} else {
				return -1;
			}
			
			return endIdx;
		}
		
		public long parse() {
			long preRet = preParse();
			if(preRet != INVALID_DATE)
				return preRet;
			formatInput(); // 格式化输入
			if(formattedInput.isEmpty())
				return INVALID_DATE;
			detectDate();
			return formattedCal == null ? INVALID_DATE : formattedCal.getTimeInMillis();
		}
		
		/**
		 * 特殊情况检查
		 * @return
		 */
		public long preParse() {
			// 替换今天，昨天
			Calendar replaceCal = Calendar.getInstance();
			replaceCal.setTimeInMillis(checkDate.getTimeInMillis());
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd "); // with extra space
			input = input.replaceAll("今天", df.format(replaceCal.getTime()));
			replaceCal.add(Calendar.DATE, -1);
			input = input.replaceAll("昨天", df.format(replaceCal.getTime()));
			replaceCal.add(Calendar.DATE, -1);
			input = input.replaceAll("前天", df.format(replaceCal.getTime()));
			
			// 转换两个字的时间单位为一个字
			input = input.replaceAll("小时", "时");
			input = input.replaceAll("分钟", "分");
			input = input.replaceAll("天", "日");
			
			int idx = input.indexOf('前');
			if(idx <= 0)
				return INVALID_DATE;
			
			char typeChar = input.charAt(idx - 1);
			String digitStr = "";
			for(int i = idx - 2; i >= 0; --i) {
				char c = input.charAt(i);
				Character nc = fullHalfCharMap.get(c);
				if(nc != null)
					c = nc;
				CharType ct = charTypeMap.get(c);
				// 忽略数字前的空格
				if(ct == CharType.SPACE && digitStr.isEmpty())
					continue;
				if(ct == CharType.DIGIT) {
					digitStr = String.valueOf(c) + digitStr;
				} else {
					break;
				}
			}
			
			//没有找到合适的数字
			if(digitStr.isEmpty())
				return INVALID_DATE;
			
			int unit = Integer.MIN_VALUE;
			// 时间的单位
			CharType unitType = charTypeMap.get(typeChar);
			if(unitType == null)
				return INVALID_DATE;
			switch(unitType) {
			case DAY:
				unit = Calendar.DATE;
				break;
			case HOUR:
				unit = Calendar.HOUR;
				break;
			case MINUTE:
				unit = Calendar.MINUTE;
				break;
			case SECOND:
				unit = Calendar.SECOND;
				break;
			case MONTH:
				unit = Calendar.MONTH;
				break;
			case YEAR:
				unit = Calendar.YEAR;
				break;
			default:
				break;
			}
			
			if(unit == Integer.MIN_VALUE)
				return INVALID_DATE;
			
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(checkDate.getTimeInMillis());
			
			// 根据周期调整时间
			cal.add(unit, -Integer.parseInt(digitStr));		
			return cal.getTimeInMillis();
		}
	}
	
	/**
	 * 解析时间。用当前时间参考时间，并且解析失败时候返回当前时间
	 * @param in
	 * @return
	 */
	public static long parseDate(String in) {
		return parseDate(in, true);
	}
	
	/**
	 * 解析时间， 用当前时间做为参考时间
	 * @param in 时间字符串
	 * @param failToReturnCheckDate true的时候当解析时间不成功时候返回参考时间
	 * @return
	 */
	public static long parseDate(String in, boolean failToReturnCheckDate) {
		long ret = 0;
		Calendar checkDate = NetUtil.getCalendar();
		if(in == null || in.isEmpty()) {
			ret = INVALID_DATE;
		} else {
			DateParserImpl dpi = new DateParserImpl(in, checkDate);
			ret = dpi.parse();
		}
		
		if(failToReturnCheckDate && ret == INVALID_DATE) {
			return checkDate.getTimeInMillis();
		}
		return ret;
	}
	
	/**
	 * 解析时间，解析失败时候返回参考时间
	 * @param in 时间字符串
	 * @param checkDate 参考时间
	 * @return
	 */
	public static long parseDate(String in, Calendar checkDate) {
		return parseDate(in, checkDate, true);
	}
	
	/**
	 * 解析时间
	 * @param in 时间字符串
	 * @param checkDate 参考时间
	 * @param failToReturnCheckDate true的时候当解析时间不成功时候返回参考时间
	 * @return
	 */
	public static long parseDate(String in, Calendar checkDate, 
			boolean failToReturnCheckDate) {
		long ret = 0;
		if(in == null || in.isEmpty()) {
			return INVALID_DATE;
		} else {
			DateParserImpl dpi = new DateParserImpl(in, checkDate);
			ret = dpi.parse();
		}
		
		if(failToReturnCheckDate && ret == INVALID_DATE) {
			return checkDate.getTimeInMillis();
		}
		return ret;
	}
	
	/**
	 * 年月日一致的情况下，如果时分秒都为0的情况下，使用checkDate的时分秒
	 * @param t 初步的时间
	 * @param checkDate 检查的时间
	 * @return 最终的时间
	 */
	public static long fillEmptyTime(long t, Calendar checkDate) {
		Calendar cal = NetUtil.getCalendar();
		cal.setTimeInMillis(t);
		if(cal.get(Calendar.YEAR) == checkDate.get(Calendar.YEAR) 
				&& cal.get(Calendar.MONTH) == checkDate.get(Calendar.MONDAY)
				&& cal.get(Calendar.DAY_OF_MONTH) == checkDate.get(Calendar.DAY_OF_MONTH)) {
			if(cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0
					&& cal.get(Calendar.SECOND) == 0) {
				return checkDate.getTimeInMillis();
			}
		}
		return t;
	}
	
	/**
	 * 是否只包括年月日
	 * @param time
	 * @return
	 */
	public static boolean isDateAlign(long timeInMs) {
		Calendar cal = NetUtil.getCalendar();
		cal.setTimeInMillis(timeInMs);
		return cal.get(Calendar.HOUR_OF_DAY) == 0 
				&& cal.get(Calendar.MINUTE) == 0 
				&& cal.get(Calendar.SECOND) == 0
				&& cal.get(Calendar.MILLISECOND) == 0;
	}
}
