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

import org.apache.log4j.MDC;
import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.handler.AbstractActionHandler;
import org.symphonyoss.collaboration.virtualdesk.muc.Conversation;
import org.symphonyoss.collaboration.virtualdesk.packet.PresenceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OccupantLeaveConversationHandler extends AbstractActionHandler
{
	private static final Logger Logger = LoggerFactory.getLogger(OccupantLeaveConversationHandler.class);
	
	private Presence presence;

	private Conversation conversation;

	public OccupantLeaveConversationHandler(Presence presence, Conversation conversation)
	{
		this.presence = presence;
		this.conversation = conversation;
	}

	@Override
	protected void internalHandle()
	{
		JID senderJID = presence.getFrom();

		User senderUser = conversation.getOccupantByJID(senderJID);
		
		MDC.put("deskjid", conversation.getJID().toBareJID());

		if (senderUser != null)
		{
			conversation.removeOccupant(senderUser);

			// Send leave response to him/herself
			packetList.add(PresenceResponse.createLeaveResponse(senderUser, senderUser, conversation.getJID()));

			// Notify all members that there is user left the room
			packetList.addAll(PresenceResponse.createLeaveResponse(senderUser, conversation.getCurrentMembers(), conversation.getJID()));
		
			Logger.info("{} has left the conversation [{}]", senderJID, conversation.getJID());
		}
		else
		{
			Logger.warn("{} is not in {} but sent leave presence packet to conversation", senderJID, conversation.getJID());
		}

		if (conversation.getCurrentMemberCount() == 0)
		{
			boolean isQuestionClosed = conversation.closeConversation();
			
			Logger.info("Conversation {} is closed. [QuestionClosed: {}]", conversation.getJID(), isQuestionClosed);
		}
	}
}
