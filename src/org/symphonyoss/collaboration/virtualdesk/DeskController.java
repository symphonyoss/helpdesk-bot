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

import org.symphonyoss.collaboration.virtualdesk.config.IServiceConfiguration;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.handler.ErrorActionHandler;
import org.symphonyoss.collaboration.virtualdesk.handler.IActionHandler;
import org.symphonyoss.collaboration.virtualdesk.handler.NoActionHandler;
import org.symphonyoss.collaboration.virtualdesk.muc.IDeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.packet.IPacketHandler;
import org.symphonyoss.collaboration.virtualdesk.packet.PacketUtils;
import org.symphonyoss.collaboration.virtualdesk.type.Namespace;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;
import org.xmpp.packet.Presence;
import org.symphonyoss.collaboration.virtualdesk.handler.desk.CreateDeskHandler;
import org.symphonyoss.collaboration.virtualdesk.handler.desk.DestroyDeskHandler;
import org.symphonyoss.collaboration.virtualdesk.handler.desk.GetDeskFormHandler;
import org.symphonyoss.collaboration.virtualdesk.handler.desk.GetDeskInfoHandler;
import org.symphonyoss.collaboration.virtualdesk.handler.desk.GetOccupantListHandler;
import org.symphonyoss.collaboration.virtualdesk.handler.desk.MemberMessageReceivedHandler;
import org.symphonyoss.collaboration.virtualdesk.handler.desk.NicknameChangedHandler;
import org.symphonyoss.collaboration.virtualdesk.handler.desk.OccupantJoinedHandler;
import org.symphonyoss.collaboration.virtualdesk.handler.desk.OccupantLeaveHandler;
import org.symphonyoss.collaboration.virtualdesk.handler.desk.ParticipantMessageReceivedHandler;
import org.symphonyoss.collaboration.virtualdesk.handler.desk.PresenceChangedHandler;
import org.symphonyoss.collaboration.virtualdesk.handler.desk.SetDeskFormHandler;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeskController implements IPacketHandler
{
	private static Logger Logger = LoggerFactory.getLogger(DeskController.class);
	
	
	private IDeskDirectory deskDirectory;
	private IServiceConfiguration serviceConfiguration;

	public DeskController(IServiceConfiguration serviceConfigurarion, IDeskDirectory deskDirectory)
	{
		this.serviceConfiguration = serviceConfigurarion;
		this.deskDirectory = deskDirectory;
	}

	@Override
	public Collection <Packet> processIQ(IQ iq)
	{
		Logger.debug("DeskController processing IQ Packet");
		
		JID deskJID = iq.getTo();
		
		
		
		IActionHandler actionHandler;

		if (deskDirectory.contains(deskJID.getNode()))
		{
			actionHandler = new ErrorActionHandler(iq, PacketError.Condition.not_acceptable);
			
			String queryNamespace = PacketUtils.getIQNamespace(iq);

			Desk deskRoom = deskDirectory.getDesk(deskJID.getNode());

			if (queryNamespace.equalsIgnoreCase(Namespace.DISCO_INFO))
			{
				actionHandler = new GetDeskInfoHandler(iq, deskRoom);
			}
			else if (queryNamespace.equalsIgnoreCase(Namespace.DISCO_ITEMS))
			{
				actionHandler = new GetOccupantListHandler(iq, deskRoom);
			}
			else if (queryNamespace.equalsIgnoreCase(Namespace.MUC_OWNER))
			{
				if (iq.getType() == Type.get)
				{
					actionHandler = new GetDeskFormHandler(iq, deskRoom);
				}
				else if (iq.getType() == Type.set)
				{
					if (PacketUtils.isDestroyElement(iq))
					{
						actionHandler = new DestroyDeskHandler(iq, deskRoom, deskDirectory);
					}
					else
					{
						actionHandler = new SetDeskFormHandler(iq, deskRoom, deskDirectory);
					}
				}
			}
		}
		else 
		{
			actionHandler = new ErrorActionHandler(iq, PacketError.Condition.item_not_found);
		}

		return actionHandler.handle();
	}

	@Override
	public Collection <Packet> processPresence(Presence presence)
	{
		Logger.debug("DeskController processing Presence Packet");
		
		JID deskJID = presence.getTo();

		IActionHandler actionHandler = new NoActionHandler();

		if (presence.isAvailable())
		{
			if (!deskDirectory.contains(deskJID.getNode()))
			{
				actionHandler = new CreateDeskHandler(presence,
						deskDirectory,
						serviceConfiguration);
			}
			else
			{
				Desk deskRoom = deskDirectory.getDesk(deskJID.getNode());

				JID senderJID = presence.getFrom();
				String senderNickname = presence.getTo().getResource();

				User senderUser = deskRoom.getOccupantByJID(senderJID);

				if (senderUser == null)
				{
					// New user join the room
					actionHandler = new OccupantJoinedHandler(presence, deskRoom, serviceConfiguration);
				}
				else if (senderNickname == null)
				{
					// Change presence
					actionHandler = new PresenceChangedHandler(presence, deskRoom);
				}
				else if (!senderUser.getNickname().equalsIgnoreCase(senderNickname))
				{
					// User changes his nickname
					actionHandler = new NicknameChangedHandler(presence, deskRoom);
				}
			}
		}
		else
		{
			if (deskDirectory.contains(deskJID.getNode()))
			{
				Desk deskRoom = deskDirectory.getDesk(deskJID.getNode());

				actionHandler = new OccupantLeaveHandler(presence, deskRoom);
			}
		}

		return actionHandler.handle();
	}

	@Override
	public Collection <Packet> processMessage(Message message)
	{
		Logger.debug("DeskController processing Message Packet");
		
		IActionHandler actionHandler = new NoActionHandler();

		if (message.getBody() == null || message.getType() != Message.Type.groupchat)
		{
			return actionHandler.handle();
		}

		JID deskJID = message.getTo();

		String deskName = deskJID.getNode();

		if (deskDirectory.contains(deskName))
		{
			Desk deskRoom = deskDirectory.getDesk(deskJID.getNode());

			JID senderJID = message.getFrom();

			User senderUser = deskRoom.getOccupantByJID(senderJID);

			if (senderUser != null)
			{
				actionHandler = senderUser.isDeskMember() ? new MemberMessageReceivedHandler(serviceConfiguration, message, deskRoom, deskDirectory) :
						new ParticipantMessageReceivedHandler(serviceConfiguration, message, deskRoom, deskDirectory);
			}
		}

		return actionHandler.handle();
	}
}
