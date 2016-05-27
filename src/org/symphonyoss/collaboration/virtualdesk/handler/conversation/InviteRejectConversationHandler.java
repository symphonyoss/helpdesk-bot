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

import java.util.Collection;
import org.apache.log4j.MDC;
import org.xmpp.packet.JID;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.handler.AbstractActionHandler;
import org.symphonyoss.collaboration.virtualdesk.muc.Conversation;
import org.symphonyoss.collaboration.virtualdesk.packet.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InviteRejectConversationHandler extends AbstractActionHandler
{
	private static Logger Logger = LoggerFactory.getLogger(InviteRejectConversationHandler.class);
	
	private Conversation conversation;
	private JID senderJID;
	
	public InviteRejectConversationHandler(Conversation conversation, JID senderJID)
	{
		this.conversation = conversation;
		this.senderJID = senderJID;
	}
	
	@Override
	protected void internalHandle()
	{
		MDC.put("deskjid", conversation.getJID().toBareJID());
		
		conversation.closeConversation();

		if (conversation.hasDeskMemberPrivilege(senderJID.toBareJID()))
		{
			conversation.memberRejectInvite();
			
			notifyInviteReject(conversation.getParticipantJoined(), conversation.getCurrentParticipants(), "Desk member cannot join to conversation. Please wait for other members to response.");
			
			Logger.info("Member {} rejected the invitation of conversation. [Notify: {}]", senderJID, conversation.getParticipantJoined());
		}
		else
		{
			conversation.participantRejectInvite();
			
			notifyInviteReject(conversation.getMemberJoined(), conversation.getCurrentMembers(), "Poster rejected the invitation to conversation.");

			Logger.info("Participant {} rejected the invitation of conversation. [Notify: {}]", senderJID, conversation.getMemberJoined());
		}
		
		conversation.closeConversation();
	}
	
	private void notifyInviteReject(boolean counterPartyJoined, Collection<User> users, String message)
	{
		if (counterPartyJoined)
		{
			packetList.addAll(MessageResponse.createDeskMessageResponse(message, users, conversation.getJID()));
		}
	}
}
