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

import org.xmpp.component.Component;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.packet.Packet;

public class OpenfireRemoteComponent implements IRemoteComponent
{
	private Component component;
	private ComponentManager componentManager;

	public OpenfireRemoteComponent(Component component, ComponentManager componentManager)
	{
		this.component = component;
		this.componentManager = componentManager;
	}

	@Override
	public void sendPacket(Packet packet) throws RemoteComponentException
	{
		try
		{
			componentManager.sendPacket(component, packet);
		}
		catch (ComponentException e)
		{
			throw new RemoteComponentException("Failed to send packet to Openfire.", e);
		}
	}
}
