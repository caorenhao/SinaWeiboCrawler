package com.caorenhao.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.TTCCLayout;
import org.apache.log4j.WriterAppender;

public class LoggerConfig {
	
	private static String logPath = "./logs";
	
	public static void initLog(String logPath) {
		initLog(logPath, "INFO");
	}
	
	public static void initLog(String logPath, String level) {
		// 自动创建目录
		File logFile = new File(logPath);
		if(!logFile.getParentFile().exists())
			logFile.getParentFile().mkdirs();
		LoggerConfig.logPath = logPath;
		try {
			LoggerConfig.ConfigLog(LoggerConfig.logPath, level, 
					LogAppenderName.DailyRollingFileAppender, 
					LogAppenderLayout.PatternLayout,
					"%d{yyyy-MM-dd HH:mm:ss} %-5p [%t] %c{1} - %m%n", "'.'yyyy-MM-dd");
		} catch (Exception e) {
			throw new RuntimeException("Init log failed!", e);
		}
		Logger log = Logger.getRootLogger();
		Enumeration<?> em = log.getAllAppenders();
		while (em.hasMoreElements()) {
			Object element = em.nextElement();
			if (element instanceof ConsoleAppender) {
				log.removeAppender((ConsoleAppender) element);
			}
		}
	}
	
	/** 枚举log4j appender name*/
	public enum LogAppenderName{
		ConsoleAppender,
		FileAppender,
		DailyRollingFileAppender,
		WriterAppender
	}
	
	/** 枚举log4j 日志输出格式*/
	public enum LogAppenderLayout{
		HTMLLayout,
		PatternLayout,
		SimpleLayout,
		TTCCLayout
	}
	
	/**
	 * 动态配置log4j
	 * @param className 输入类名
	 * @param path	日志输出路径
	 * @param level 日志级别
	 * @param appenderName	appender名称，见{@link LogAppenderName}
	 * @param layoutType	日志layout类型, 见{@link LogAppenderLayout}
	 * @param conversionPattern	日志输出内容格式与log4j配置文件中的layout.ConversionPattern
	 * @param datePattern	日志文件名后缀日期格式 DatePattern='.'yyyy-MM-dd
	 * @throws Exception
	 */
	public static void ConfigLog(String path, String level,
			LogAppenderName appenderName, LogAppenderLayout layoutType, 
			String conversionPattern, String datePattern
			)throws Exception{

		
		Logger log = Logger.getRootLogger();
		log.setLevel(Level.toLevel(level));
		Layout layout = null;
		Appender appender = null;
		switch (layoutType) {
		case HTMLLayout:
			layout = new HTMLLayout();
			break;

		case PatternLayout:
			layout = new PatternLayout(conversionPattern);
			break;

		case SimpleLayout:
			layout = new SimpleLayout();
			break;

		case TTCCLayout:
			layout = new TTCCLayout();
			break;

		default:
			break;
		}
		switch (appenderName) {
		case ConsoleAppender:
			appender = new ConsoleAppender(layout, path);
			break;

		case FileAppender:
			appender = new FileAppender(layout, path);
			break;

		case DailyRollingFileAppender:
			appender = new DailyRollingFileAppender(layout, path, datePattern);
			break;

		case WriterAppender:
			OutputStream os = new FileOutputStream(path, true);
			appender = new WriterAppender(layout, os);
			break;

		default:
			appender = new ConsoleAppender(layout, path);
			break;
		}
		log.addAppender(appender);
	}
	
	public static Log getLog(Class<?> clazz) {
		return LogFactory.getLog(clazz);
	}
}
