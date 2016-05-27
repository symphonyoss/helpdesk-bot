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

package org.symphonyoss.collaboration.virtualdesk.component;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.junit.Before;
import org.junit.Test;
import org.xmpp.component.Component;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.packet.Packet;

public class OpenfireRemoteComponentTest
{
	private OpenfireRemoteComponent remoteComponent;

	private Component mockComponent;
	private ComponentManager mockComponentManager;

	@Before
	public void before()
	{
		mockComponent = mock(Component.class);
		mockComponentManager = mock(ComponentManager.class);

		remoteComponent = new OpenfireRemoteComponent(mockComponent, mockComponentManager);
	}

	@Test
	public void sendPacket_CanConnectToOpenfire_CallSendPacketOfComponentManager() throws RemoteComponentException, ComponentException
	{
		remoteComponent.sendPacket(null);

		verify(mockComponentManager).sendPacket(eq(mockComponent), (Packet) any());
	}

	@Test (expected = RemoteComponentException.class)
	public void sendPacket_FailedToSendPacketToOpenfire_ThrowRemoteComponentException() throws ComponentException, RemoteComponentException
	{
		doThrow(new ComponentException()).when(mockComponentManager).sendPacket((Component) any(), (Packet) any());

		remoteComponent.sendPacket(null);
	}
}
