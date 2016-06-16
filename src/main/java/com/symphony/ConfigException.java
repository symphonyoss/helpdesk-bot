package com.symphony;

public class ConfigException extends RuntimeException {

	private static final long serialVersionUID = -509695012370836776L;

	public ConfigException(String message) {
		super(message);
	}

	public ConfigException(String string, Exception ex) {
		super(string, ex);
	}
}
