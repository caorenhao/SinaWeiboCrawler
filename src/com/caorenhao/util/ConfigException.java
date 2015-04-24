package com.caorenhao.util;

import java.io.File;

public class ConfigException extends Exception {

	private static final long serialVersionUID = -1553562226557208972L;

	public ConfigException(String msg, File configFile) {
		super(msg + "[ConfigException] From Config File: " + configFile);
	}
	
	public ConfigException(String msg) {
		super(msg + "[ConfigException]");
	}
}
