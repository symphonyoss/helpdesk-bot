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

import java.util.Collection;

import org.symphonyoss.collaboration.virtualdesk.component.OpenfirePlusRemoteComponent;
import org.symphonyoss.collaboration.virtualdesk.component.OpenfireRemoteComponent;

import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.component.RemoteNode;
import org.xmpp.component.RemoteNodeEventHandler;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.symphonyoss.collaboration.virtualdesk.component.IRemoteComponent;
import org.symphonyoss.collaboration.virtualdesk.component.RemoteComponentException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualDeskServiceProxy implements RemoteNodeEventHandler
{
	private static Logger Logger = LoggerFactory.getLogger(VirtualDeskServiceProxy.class);
	
	private String serverID;
	
	private VirtualDeskService virtualDeskService;
	private IRemoteComponent remoteComponent;
	
	public VirtualDeskServiceProxy(String serverID, VirtualDeskService virtualDeskService)
	{
		this.serverID = serverID;
		this.virtualDeskService = virtualDeskService;
	}
	
	@Override
	public String getDescription()
	{
		return virtualDeskService.getDescription();
	}

	@Override
	public String getName()
	{
		return virtualDeskService.getName();
	}

	@Override
	public void initialize(JID jid, ComponentManager componentManager) throws ComponentException
	{
		remoteComponent = new OpenfireRemoteComponent(this, componentManager);
		
		virtualDeskService.initialize(jid);

		Logger.info("Virtual Desk proxy has been initialized. [ID: {}]", serverID);
	}

	@Override
	public void processPacket(Packet packet)
	{
		Logger.debug("[{}] Receiving packet: {}", serverID, packet);
		

		
		Collection<Packet> responsePacketList = virtualDeskService.processPacket(packet);
		
		if (responsePacketList.size() > 0)
		{
			for (Packet responsePacket : responsePacketList)
			{

				
				try
				{
					remoteComponent.sendPacket(responsePacket);
					


					Logger.debug("[{}] Sending packet: {}", serverID, responsePacket);
				}
				catch (RemoteComponentException e)
				{

					
					Logger.warn("Cannot send the response packet to Openfire.", e);
				}
				catch (Exception e)
				{

					
					Logger.warn("Cannot send the response packet to Openfire. Unknown Exception ", e);
				}
			}
		}
	}

	@Override
	public void shutdown()
	{
		Logger.info("Virtual Desk has shutdown. [ID: {}]", serverID);
	}

	@Override
	public void start()
	{
		Logger.info("Virtual Desk has started. [ID: {}]", serverID);
	}

	@Override
	public void connectionLost()
	{
		Logger.info("Virtual Desk has lost the connection with Openfire. [ID: {}]", serverID);
	}

	@Override
	public void setRemoteNode(RemoteNode remoteNode, String serverID)
	{
		remoteComponent = new OpenfirePlusRemoteComponent(remoteNode);
		Logger.info("Virtual Desk has been configured to Remote Node. [ID: {}]", serverID);
	}
}
