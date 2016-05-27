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

package org.symphonyoss.collaboration.virtualdesk;

import java.util.concurrent.CountDownLatch;
import org.jivesoftware.whack.ExternalComponentManager;
import org.jivesoftware.whack.ListeningExternalComponentManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.symphonyoss.collaboration.virtualdesk.config.ConfigurationManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Virtual Desk as External Component
 *
 */
public class ExternalVirtualDeskComponent
{
	private static Logger Logger = LoggerFactory.getLogger(ExternalVirtualDeskComponent.class);

	private static final String COMPONENT_DESCRIPTION = "Virtual Desk Service";

	private static ComponentManager componentManager;

	private static ConfigurationManager configurationManager;

	private static CountDownLatch waitObject = new CountDownLatch(1);

	/**
	 * Main method to start IMGateway server
	 * 

	 *            need
	 */
	public static void main(String[] args)
	{
		ApplicationContext context = new ClassPathXmlApplicationContext("virtualdesk.xml");

		configurationManager = (ConfigurationManager) context.getBean("configurationManager");

		addShutdownService();

		// Create a manager for the external components
		componentManager = null;

		// Connection Mode
		if (configurationManager.isConnectionListeningMode() == true)
		{
			// Listening Mode for connecting to Openfire plus
			componentManager = new ListeningExternalComponentManager(configurationManager.getOpenfireHost());
		}
		else
		{
			// Connection to Openfire mode
			componentManager = new ExternalComponentManager(configurationManager.getOpenfireHost(), configurationManager.getOpenfirePort());

			// Set the secret key for this component. The server must be using the same secret
			// key otherwise the component won't be able to authenticate with the server
			// Check that the server has the property "component.external.secretKey" 
			// defined and that it is using the same value that we are setting here.
			((ExternalComponentManager) componentManager).setSecretKey(configurationManager.getSubDomain(), configurationManager.getSecretKey());

			// Set the manager to tag components as being allowed to connect
			// multiple times to the same JID.
			((ExternalComponentManager) componentManager).setMultipleAllowed(configurationManager.getSubDomain(), true);
		}

		try
		{
			Logger.info("Starting Virtual Desk External Component");

			// Register that this component will be serving the given subdomain
			// of the server
			Logger.debug("OpenfireHost: {}:{}", configurationManager.getOpenfireHost(), configurationManager.getOpenfirePort());
			Logger.debug("Subdomain: {}", configurationManager.getSubDomain());
			Logger.debug("SecretKey: {}", configurationManager.getSecretKey());
			Logger.debug("ListeningMode: {}", configurationManager.isConnectionListeningMode());

			VirtualDeskService virtualDeskService = (VirtualDeskService) context.getBean("virtualDeskService");

			// Connection Mode
			if (configurationManager.isConnectionListeningMode() == true)
			{
				((ListeningExternalComponentManager) componentManager).addComponent(configurationManager.getOpenfirePort(),
						configurationManager.getSubDomain(),
						configurationManager.getSecretKey(),
						0,
						new RemoteNodeEventHandlerFactoryImpl(COMPONENT_DESCRIPTION, virtualDeskService));
				
				Logger.info("Registering Virtual Desk component to Openfire Plus");
			}
			else
			{
				((ExternalComponentManager) componentManager).addComponent(configurationManager.getSubDomain(),
						new VirtualDeskServiceProxy("VDESK", virtualDeskService));

				Logger.info("Registering Virtual Desk component to Openfire");
			}

			Logger.info("Started Virtual Desk External Component");
			

			// Quick trick to ensure that this application will be running for
			// ever. To stop the application you will need to kill the process
			while (true)
			{ 
				try
				{ 
					waitObject.await();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (ComponentException e)
		{
			Logger.error("Failed to connect to Openfire server.", e);
			

		}
		catch (Exception e)
		{
			Logger.error("Failed to start Virtual Desk service.", e);
			

		}
	}

	/**
	 * Shutdown service for IMGateway
	 */
	private static void addShutdownService()
	{
		Thread runtimeHookThread = new Thread()
		{
			public void run()
			{
				shutdownHook();
			}
		};
		Runtime.getRuntime().addShutdownHook(runtimeHookThread);
	}

	/**
	 * Shutdown Hook method to run when system shutting down
	 */
	private static void shutdownHook()
	{
		Logger.debug("Shutting down Virtual Desk service.");

		final int SHUTDOWN_TIME_LIMIT = 1000;

		long currentTime = System.currentTimeMillis();
		while (true)
		{
			try
			{
				// Remove Component
				componentManager.removeComponent(configurationManager.getSubDomain());
				Thread.sleep(500);
				
				waitObject.countDown();
			}
			catch (Exception e)
			{
				Logger.debug("Failed to shutdown Virtual Desk service.", e);
				break;
			}

			if (System.currentTimeMillis() - currentTime > SHUTDOWN_TIME_LIMIT)
			{
				break;
			}

			Logger.debug("Shutdown Virtual Desk ...");
		}

		Logger.info("Shutdown Virtual Desk service completed.");
		

	}

	/**
	 * Finalize object
	 */
	@Override
	protected void finalize() throws Throwable
	{
		super.finalize();
	}

	/**
	 * Check is server running on listening mode
	 * 
	 * @return true if server running on listening mode
	 */
	public static boolean isConnectionListeningMode()
	{
		return configurationManager.isConnectionListeningMode();
	}
}
