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

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xmpp.component.RemoteNodeEventHandler;

public class RemoteNodeEventHandlerFactoryImplTest
{
	private RemoteNodeEventHandlerFactoryImpl remoteNodeEventFactoryImpl;
	
	@Before
	public void SetUp()
	{
		remoteNodeEventFactoryImpl = new RemoteNodeEventHandlerFactoryImpl("Description", null);
	}
	
	@Test
	public void getRemoteNodeHandler_GetWithNewServerID_ReturnNewServiceProxy()
	{
		RemoteNodeEventHandler eventHandler = remoteNodeEventFactoryImpl.getRemoteNodeHandler("Server1");
		
		Assert.assertNotNull(eventHandler);
	}
	
	@Test
	public void getRemoteNodeHandler_GetWithExistingServerID_ReturnSameServiceProxyObject()
	{
		RemoteNodeEventHandler eventHandler = remoteNodeEventFactoryImpl.getRemoteNodeHandler("Server1");
		
		RemoteNodeEventHandler sameEventHandler = remoteNodeEventFactoryImpl.getRemoteNodeHandler("Server1");
		
		Assert.assertSame(eventHandler, sameEventHandler);
	}
	
	@Test
	public void getRemoteNodeHandler_GetWithDifferentServerID_ReturnDifferentServiceProxyObject()
	{
		RemoteNodeEventHandler server1EventHandler = remoteNodeEventFactoryImpl.getRemoteNodeHandler("Server1");
		
		RemoteNodeEventHandler server2EventHandler = remoteNodeEventFactoryImpl.getRemoteNodeHandler("Server2");
		
		Assert.assertNotSame(server1EventHandler, server2EventHandler);
	}
}
