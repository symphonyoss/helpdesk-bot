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

package org.symphonyoss.collaboration.virtualdesk.handler.conversation;

import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.MDC;
import org.symphonyoss.collaboration.virtualdesk.handler.AbstractActionHandler;
import org.symphonyoss.collaboration.virtualdesk.muc.Conversation;
import org.symphonyoss.collaboration.virtualdesk.packet.MessageResponse;
import org.symphonyoss.collaboration.virtualdesk.packet.PresenceResponse;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;
import org.xmpp.packet.Presence;
import org.symphonyoss.collaboration.virtualdesk.data.Affiliation;
import org.symphonyoss.collaboration.virtualdesk.data.PresenceType;
import org.symphonyoss.collaboration.virtualdesk.data.Role;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.data.UserState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OccupantJoinedConversationHandler extends AbstractActionHandler
{
	private static final Logger Logger = LoggerFactory.getLogger(OccupantJoinedConversationHandler.class);
	
	private Presence presence;

	private Conversation conversationRoom;

	public OccupantJoinedConversationHandler(Presence presence, Conversation conversationRoom)
	{
		this.presence = presence;
		this.conversationRoom = conversationRoom;
	}

	@Override
	protected void internalHandle()
	{
		JID senderJID = presence.getFrom();
		String senderNickname = presence.getTo().getResource();

		MDC.put("deskjid", presence.getTo().toBareJID());
		
		Logger.debug("{} is joinning the conversation [{}]", senderJID, conversationRoom.getJID());

		String senderBareJID = senderJID.toBareJID();

		if (conversationRoom.isNicknameExisted(senderNickname))
		{
			// Nickname has already existed, so return nickname conflict
			packetList.add(PresenceResponse.createPresenceError(presence, PacketError.Condition.conflict));
			
			return;
		}
		
		User senderUser;
		
		if (conversationRoom.hasDeskMemberPrivilege(senderBareJID))
		{
			senderUser = new User(senderJID, senderNickname, Affiliation.owner, Role.moderator);

			// Send all occupants's presences to this new joiner user
			packetList.addAll(PresenceResponse.createJoinResponse(conversationRoom.getCurrentMembers(), senderUser, conversationRoom.getJID(), false));
			packetList.addAll(PresenceResponse.createJoinResponse(conversationRoom.getCurrentParticipants(), senderUser, conversationRoom.getJID(), false));

			// Update virtual conversation's presence to online to all participants if this user is the first admin in the conversation
			if (conversationRoom.getCurrentMemberCount() == 0)
			{
				Logger.debug("Updating virtual conversation user's presence to all participants in conversation [{}] from first conversation member joining",
						conversationRoom.getJID());
				
				packetList.addAll(PresenceResponse.createPresenceUpdate(conversationRoom.getVirtualDeskUser(), conversationRoom.getCurrentParticipants(), conversationRoom.getJID(), PresenceType.Online));
			}
			
			conversationRoom.setMemberJoinedState();
			
			// Check that participant has already rejected before member join conversation,
			// If so, notify the member that there will be no one join.
			notifyInviteReject(conversationRoom.getParticipantRejectInvite(), 
					senderUser,
					"Poster rejected the invitation to conversation.");
		}
		else
		{
			senderUser = new User(senderJID, senderNickname, Affiliation.none, Role.participant);

			if (conversationRoom.isMembersOnly())
			{
				Set <String> participantList = conversationRoom.getParticipants();
				if (!participantList.contains(senderUser.getJID().toBareJID()))
				{
					packetList.add(PresenceResponse.createPresenceError(senderUser,
							conversationRoom.getJID(),
							PacketError.Condition.not_allowed));

					Logger.info("{} cannot join the conversation [{}] because conversation is members only and user is not in the member list.",
							senderJID, conversationRoom.getJID());
					
					return;
				}
			}

			packetList.add(PresenceResponse.createJoinResponse(conversationRoom.getVirtualDeskUser(), senderUser, conversationRoom.getJID(), false));

			// Update virtual desk's presence to new joiner if there is member in the room
			packetList.add(PresenceResponse.createPresenceUpdate(conversationRoom.getVirtualDeskUser(), senderUser, conversationRoom.getJID(), conversationRoom.getDeskPresence()));
			
			conversationRoom.setParticipantJoinedState();
			
			// Check that member has already rejected before participant join conversation,
			// If so, notify the participant that there will be no member join.
			notifyInviteReject(conversationRoom.getMemberRejectInvite(), 
					senderUser,
					"Desk member cannot join to conversation. Please wait for other members to response.");
		}

		// Notify all members that there is a new joiner
		packetList.addAll(PresenceResponse.createJoinResponse(senderUser, conversationRoom.getCurrentMembers(), conversationRoom.getJID(), false));

		// Send join response to the new joiner
		Presence responsePresence = (Presence)PresenceResponse.createJoinResponse(senderUser, senderUser, conversationRoom.getJID(), false);
		responsePresence.setID(presence.getID());

		packetList.add(responsePresence);

		conversationRoom.addOccupant(senderUser);
		
		Logger.info("{} has joined the desk [{}] with affiliation: {}", 
				new Object[] {senderJID, conversationRoom.getJID(), senderUser.getAffiliation()});
		
		// Send the questions to joiner
		sendQuestions(senderUser);
	}
	
	private void notifyInviteReject(boolean counterPartyRejectInvite, User user, String message)
	{
		if (counterPartyRejectInvite)
		{
			packetList.add(MessageResponse.createDeskMessageResponse(message, user, conversationRoom.getJID()));
			
			Logger.info("Notify {} that counterparty has rejected the invitation.", user.getJID());
		}
	}
	
	private void sendQuestions(User joinedUser)
	{
		UserState question = conversationRoom.getQuestion();
		
		String questionMessage = StringUtils.join(question.getQuestions(), "\r\n");
		
		packetList.add(MessageResponse.createHistoryMessage(question.getPosterNickname(), joinedUser.getJID().toString(), questionMessage, question.getTimestamp(), conversationRoom.getJID()));
	}
}
