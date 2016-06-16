package com.symphony;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelpBotConfig {

	private static Logger logger = LoggerFactory.getLogger(HelpBotConfig.class);

	public static final String HB_USER_NAME = "HelpBOT";
	public static final String WELCOME_MSG = "Hello! Please wait while I contact an agent...";
	public static final String HB_USER_ID = "helpbot";
	public static final String HELPER_USER_NAME = "Agent";
	public static final String HB_ROOM_PREFIX = "HelpBOTRoom_";
	public static final String PLEASE_WAIT = "Please wait...";
	public static final String SESSION_TERMINATED_MESSAGE = "Session has been terminated. Thank you!";

	private final Properties props = new Properties();

	private HelpBotConfig(Map<String, String> propertyOverrides, File file) {

		try {
			if (file.exists()) {
				FileInputStream inputStream = new FileInputStream(file);

				if (inputStream != null) {
					props.load(inputStream);
				}

				Set<Entry<String, String>> o = propertyOverrides.entrySet();
				for (Entry<String, String> entry : o) {
					props.put(entry.getKey(), entry.getValue());
				}
			}
		} catch (Exception ex) {
			throw new ConfigException("Unable to load config file: " + ex.getMessage(), ex);
		}
	}

	public String getPodUrl() {
		return requiredProp("podUrl");
	}

	public static HelpBotConfig init(File propertiesFile) {

		if (propertiesFile == null) {
			throw new RuntimeException("Config file is required");
		}
		if (!propertiesFile.exists()) {
			throw new RuntimeException("Unable to load config file: " + propertiesFile.getAbsolutePath());
		}
		logger.info("Config file: " + propertiesFile.getAbsolutePath());

		return new HelpBotConfig(new HashMap<String, String>(), propertiesFile);
	}

	public File getWorkingDirectory() {
		return new File("working");
	}

	public int getPort() {
		return Integer.parseInt(requiredProp("port"));
	}

	public String getAgentUrl() {
		return requiredProp("agentUrl");
	}

	public String getSessionAuthUrl() {
		return requiredProp("sessionAuthUrl");
	}

	public String getKeyAuthUrl() {
		return requiredProp("keyAuthUrl");
	}

	private String requiredProp(String p) {
		String ret = props.getProperty(p);
		if (ret == null) {
			throw new ConfigException(p + " is a required configuration parameter");
		}
		return ret;
	}

	public File getStaticContentDirectory() {
		String string = props.getProperty("staticContentDirectory");
		File ret = new File(string);
		if (!ret.exists()) {
			throw new ConfigException("staticContentDirectory doesn't exist:" + ret.getAbsolutePath());
		}
		return ret;
	}

	public String getMonikerParameter() {
		return "name";
	}

	public boolean isDebugSymphonyAPI() {
		String ret = props.getProperty("debugrest");
		return ret != null && "true".equals(ret);
	}

	public long getMessagePollPeriod(int messagePollInterval) {
		String ret = props.getProperty("messagePollInterval");

		if (ret == null) {
			return messagePollInterval;
		} else {
			return Long.valueOf(ret);
		}
	}

}
