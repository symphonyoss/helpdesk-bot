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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.xmpp.component.RemoteNodeEventHandler;
import org.xmpp.component.RemoteNodeEventHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteNodeEventHandlerFactoryImpl implements RemoteNodeEventHandlerFactory
{
	private static Logger Log = LoggerFactory.getLogger(RemoteNodeEventHandlerFactoryImpl.class);

	private Map <String, VirtualDeskServiceProxy> mapServerIdToComponent = new ConcurrentHashMap <String, VirtualDeskServiceProxy>();

	private VirtualDeskService virtualDeskService;

	/**
	 * Constructor
	 */
	public RemoteNodeEventHandlerFactoryImpl(String description, VirtualDeskService virtualDeskService)
	{
		this.virtualDeskService = virtualDeskService;
	}

	@Override
	public RemoteNodeEventHandler getRemoteNodeHandler(String serverID)
	{
		VirtualDeskServiceProxy serviceProxy = null;

		if ((serviceProxy = mapServerIdToComponent.get(serverID)) == null)
		{
			Log.info("Create RemoteNodeEventHandlerImpl with server: {}", serverID);

			serviceProxy = new VirtualDeskServiceProxy(serverID, virtualDeskService);
			
			mapServerIdToComponent.put(serverID, serviceProxy);
		}

		return serviceProxy;
	}
}
