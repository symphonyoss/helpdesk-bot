/*
 *
 * Copyright 2016 The Symphony Software Foundation
 *
 * Licensed to The Symphony Software Foundation (SSF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.symphonyoss.collaboration.virtualdesk.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationManager
{
	private static Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);

	private String openfireHost;
	private int openfirePort;

	private String subDomain;
	private String secretKey;

	private boolean connectionListeningMode;

	public void init()
	{
		Properties configFile = new Properties();

		try
		{
			logger.info("Loading configuration from config.properties.");

			configFile.load(new FileInputStream("conf/config.properties"));

			openfireHost = configFile.getProperty("OpenfireHost", "localhost");
			openfirePort = Integer.parseInt(configFile.getProperty("OpenfirePort", "5275"));
			subDomain = configFile.getProperty("Subdomain", "virtualdesk");
			secretKey = configFile.getProperty("SecretKey", "Welcome1");
			connectionListeningMode = Boolean.parseBoolean(configFile.getProperty("ConnectionListeningMode", "false"));

			logger.debug("Loaded configuration. [OpenfireHost: {}:{}, Subdomain: {}, ListeningMode: {}]", new Object[]{openfireHost, openfirePort, subDomain, connectionListeningMode});
		}
		catch (IOException e)
		{
			setDefaultConfiguration();

			logger.error("Failed to load configuration file. Default configuration is loaded.", e);
		}
	}

	public String getOpenfireHost()
	{
		return openfireHost;
	}

	public int getOpenfirePort()
	{
		return openfirePort;
	}

	public String getSubDomain()
	{
		return subDomain;
	}

	public String getSecretKey()
	{
		return secretKey;
	}

	public boolean isConnectionListeningMode()
	{
		return connectionListeningMode;
	}

	private void setDefaultConfiguration()
	{
		openfireHost = "localhost";
		openfirePort = 5275;

		subDomain = "virtualdesk";
		secretKey = "Welcome1";

		connectionListeningMode = false;
	}
}
