package com.symphony;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

public class CommandLine {

	@Parameter(names = "--sslTrustAllHosts", description = "Trust all hosts. NOT TO BE USED IN PRODUCTION!", required = false)
	boolean sslTrustAllHosts = false;

	@Parameter(names = "-c", description = "Configuration File. Defaults to \"./helpbot.properties\"", required = false)
	File configFile = new File("helpbot.properties");

	@DynamicParameter(names = "-P", description = "Property Overrides")
	Map<String, Object> propertyOverrides = new HashMap<>();

}
