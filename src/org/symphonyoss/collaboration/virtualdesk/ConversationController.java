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

import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.handler.conversation.InviteRejectConversationHandler;
import org.symphonyoss.collaboration.virtualdesk.handler.conversation.OccupantLeaveConversationHandler;
import org.symphonyoss.collaboration.virtualdesk.handler.desk.SetDeskFormHandler;
import org.symphonyoss.collaboration.virtualdesk.muc.Conversation;
import org.symphonyoss.collaboration.virtualdesk.packet.IPacketHandler;
import org.symphonyoss.collaboration.virtualdesk.type.Namespace;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;
import org.xmpp.packet.Presence;
import org.symphonyoss.collaboration.virtualdesk.config.IServiceConfiguration;
import org.symphonyoss.collaboration.virtualdesk.handler.ErrorActionHandler;
import org.symphonyoss.collaboration.virtualdesk.handler.IActionHandler;
import org.symphonyoss.collaboration.virtualdesk.handler.NoActionHandler;
import org.symphonyoss.collaboration.virtualdesk.handler.conversation.MemberMessageReceivedConversationHandler;
import org.symphonyoss.collaboration.virtualdesk.handler.conversation.OccupantJoinedConversationHandler;
import org.symphonyoss.collaboration.virtualdesk.handler.conversation.ParticipantMessageReceivedConversationHandler;
import org.symphonyoss.collaboration.virtualdesk.handler.desk.DestroyDeskHandler;
import org.symphonyoss.collaboration.virtualdesk.handler.desk.GetDeskFormHandler;
import org.symphonyoss.collaboration.virtualdesk.handler.desk.GetDeskInfoHandler;
import org.symphonyoss.collaboration.virtualdesk.handler.desk.GetOccupantListHandler;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.muc.IDeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.packet.PacketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConversationController implements IPacketHandler
{
	private static Logger Logger = LoggerFactory.getLogger(ConversationController.class);
	
	private IDeskDirectory deskDirectory;
	private IServiceConfiguration serviceConfigurarion;

	public ConversationController(IServiceConfiguration serviceConfigurarion, IDeskDirectory deskDirectory)
	{
		this.serviceConfigurarion = serviceConfigurarion;
		this.deskDirectory = deskDirectory;
	}

	@Override
	public Collection <Packet> processIQ(IQ iq)
	{
		Logger.debug("ConversationController processing IQ Packet");
		
		JID deskJID = iq.getTo();

		IActionHandler actionHandler;

		if (deskDirectory.containsConversation(deskJID.getNode()))
		{
			actionHandler = new ErrorActionHandler(iq, PacketError.Condition.not_acceptable);
			
			String queryNamespace = PacketUtils.getIQNamespace(iq);

			Desk deskConversation = deskDirectory.getConversation(deskJID.getNode());

			if (queryNamespace.equalsIgnoreCase(Namespace.DISCO_INFO))
			{
				actionHandler = new GetDeskInfoHandler(iq, deskConversation);
			}
			else if (queryNamespace.equalsIgnoreCase(Namespace.DISCO_ITEMS))
			{
				actionHandler = new GetOccupantListHandler(iq, deskConversation);
			}
			else if (queryNamespace.equalsIgnoreCase(Namespace.MUC_OWNER))
			{
				if (iq.getType() == Type.get)
				{
					actionHandler = new GetDeskFormHandler(iq, deskConversation);
				}
				else if (iq.getType() == Type.set)
				{
					if (PacketUtils.isDestroyElement(iq))
					{
						actionHandler = new DestroyDeskHandler(iq, deskConversation, deskDirectory);
					}
					else
					{
						actionHandler = new SetDeskFormHandler(iq, deskConversation, deskDirectory);
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
		Logger.debug("ConversationController processing Presence Packet");
		
		JID conversationJID = presence.getTo();

		IActionHandler actionHandler = new NoActionHandler();

		Conversation conversation = deskDirectory.getConversation(conversationJID.getNode());
		
		if (presence.isAvailable())
		{
			// User joins into private conversation
			if (conversation != null)
			{
				Conversation deskConversation = deskDirectory.getConversation(conversationJID.getNode());

				JID senderJID = presence.getFrom();
				String senderNickname = presence.getTo().getResource();

				User senderUser = deskConversation.getOccupantByJID(senderJID);

				if (senderUser == null)
				{
					// New user join the room
					actionHandler = new OccupantJoinedConversationHandler(presence, deskConversation);
					// No Action yet
				}
				else if (senderNickname == null)
				{
					// Change presence
//					actionHandler = new PresenceChangedHandler(presence, deskRoom);
					// No Action yet
				}
				else if (!senderUser.getNickname().equalsIgnoreCase(senderNickname))
				{
					// User changes his nickname
//					actionHandler = new NicknameChangedHandler(presence, deskRoom);
					// No Action yet
				}
			}
		}
		else
		{
			// User leaves private conversation
			if (conversation != null)
			{
				actionHandler = new OccupantLeaveConversationHandler(presence, conversation);
			}
		}

		return actionHandler.handle();
	}

	@Override
	public Collection <Packet> processMessage(Message message)
	{
		Logger.debug("ConversationController processing Message Packet");
		
		IActionHandler actionHandler = new NoActionHandler();

		JID conversationJID = message.getTo();

		String conversationName = conversationJID.getNode();

		if (message.getBody() == null || message.getType() != Message.Type.groupchat)
		{
			if (PacketUtils.isInviteReject(message))
			{
				Conversation conversation = deskDirectory.getConversation(conversationName);
						
				actionHandler = new InviteRejectConversationHandler(conversation, message.getFrom());
			}
			
			return actionHandler.handle();
		}

		if (deskDirectory.containsConversation(conversationName))
		{
			Conversation deskConversation = deskDirectory.getConversation(conversationName);

			JID senderJID = message.getFrom();

			User senderUser = deskConversation.getOccupantByJID(senderJID);

			if (senderUser != null)
			{
				actionHandler = senderUser.isDeskMember() ? new MemberMessageReceivedConversationHandler(message, deskConversation, deskDirectory) :
						new ParticipantMessageReceivedConversationHandler(serviceConfigurarion, message, deskConversation);
			}
		}

		return actionHandler.handle();
	}
}
