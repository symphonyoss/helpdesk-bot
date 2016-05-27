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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import junit.framework.Assert;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;
import org.junit.Before;
import org.junit.Test;

public class ConfigurationManagerTest
{
	private ConfigurationManager configManager;

	@Before
	public void before()
	{
		configManager = new ConfigurationManager();
	}

	@Test
	public void init_CannotLoadConfigFile_UseDefaultValues() throws IOException
	{
		new Expectations()
		{
			@Mocked @NonStrict Properties configFile;
			{
				configFile.load((InputStream) any); result = new IOException();
			}
		};
		
		configManager.init();
		
		Assert.assertEquals("localhost", configManager.getOpenfireHost());
		Assert.assertEquals(5275, configManager.getOpenfirePort());
		Assert.assertEquals("virtualdesk", configManager.getSubDomain());
		Assert.assertEquals("Welcome1", configManager.getSecretKey());
		Assert.assertEquals(false, configManager.isConnectionListeningMode());
	}
	
	@Test
	public void init_LoadConfigFileSuccessfully_UseSettingFromConfigFile() throws IOException
	{
		new Expectations()
		{
			@Mocked @NonStrict Properties configFile;
			{
				configFile.getProperty("OpenfireHost", anyString); result = "OFP01";
				configFile.getProperty("OpenfirePort", anyString); result = "1234";
				configFile.getProperty("Subdomain", anyString); result = "virtual_desk";
				configFile.getProperty("SecretKey", anyString); result = "password";
				configFile.getProperty("ConnectionListeningMode", anyString); result = "true";
			}
		};
		
		configManager.init();
		
		Assert.assertEquals("OFP01", configManager.getOpenfireHost());
		Assert.assertEquals(1234, configManager.getOpenfirePort());
		Assert.assertEquals("virtual_desk", configManager.getSubDomain());
		Assert.assertEquals("password", configManager.getSecretKey());
		Assert.assertEquals(true, configManager.isConnectionListeningMode());
	}
}
