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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.symphonyoss.collaboration.virtualdesk.config.IServiceConfiguration;
import org.symphonyoss.collaboration.virtualdesk.muc.DeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.packet.IPacketHandler;

import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualDeskService
{
	private static Logger Logger = LoggerFactory.getLogger(VirtualDeskService.class);

	private IServiceConfiguration serviceConfiguration;

	private DeskDirectory deskDirectory;

	private IPacketHandler serviceController;
	private IPacketHandler deskController;
	private IPacketHandler conversationController;

	private boolean isInitialized;
	private Object initializedSync;

	public VirtualDeskService(IServiceConfiguration serviceInfo, DeskDirectory deskDirectory, IPacketHandler serviceController, IPacketHandler deskController, IPacketHandler conversationController)
	{
		this.serviceConfiguration = serviceInfo;

		this.deskDirectory = deskDirectory;

		this.serviceController = serviceController;
		this.deskController = deskController;
		this.conversationController = conversationController;

		isInitialized = false;
		initializedSync = new Object();
	}

	public String getDescription()
	{
		return serviceConfiguration.getServiceDescription();
	}

	public String getName()
	{
		return serviceConfiguration.getVirtualDeskSubDomain();
	}

	public void initialize(JID virtualDeskJID)
	{
		synchronized (initializedSync)
		{
			if (!isInitialized)
			{
				isInitialized = true;

				Logger.info("Virtual Desk service has JID: {}]", virtualDeskJID.toString());

				deskDirectory.setDomain(virtualDeskJID.toBareJID());

				deskDirectory.loadDesks();

				Logger.info("Virtual Desk service has been initialized.");
			}
		}
	}

	public Collection <Packet> processPacket(Packet packet)
	{
		List <Packet> responsePacketList = new ArrayList <Packet>();

		JID toJID = packet.getTo();

		try
		{
			IPacketHandler packetHandler = getPacketHandlerFromJID(toJID);
	
			if (packet instanceof IQ)
			{
				responsePacketList.addAll(packetHandler.processIQ((IQ) packet));
			}
			else if (packet instanceof Presence)
			{
				responsePacketList.addAll(packetHandler.processPresence((Presence) packet));
			}
			else if (packet instanceof Message)
			{
				responsePacketList.addAll(packetHandler.processMessage((Message) packet));
			}
		}
		catch (Throwable e)
		{		
			Logger.warn("Faile to proces package", e);

		}
		
		return responsePacketList;
	}

	private IPacketHandler getPacketHandlerFromJID(JID jid)
	{
		IPacketHandler handler = null;
		
		if (jid.getNode() == null)
		{
			handler = serviceController;
		}
		else if ( deskDirectory.containsConversation(jid.getNode()) )
		{
			handler = conversationController;
		}
		else
		{
			handler = deskController;
		}
		
		Logger.debug("Initial controller {}", handler.getClass().toString());
		
		return handler;
	}
}
