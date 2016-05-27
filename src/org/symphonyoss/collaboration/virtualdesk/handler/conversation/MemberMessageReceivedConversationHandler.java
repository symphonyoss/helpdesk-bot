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
import org.symphonyoss.collaboration.virtualdesk.command.InvalidCommand;
import org.symphonyoss.collaboration.virtualdesk.command.TakeQuestionCommand;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.handler.AbstractActionHandler;
import org.symphonyoss.collaboration.virtualdesk.muc.Conversation;
import org.symphonyoss.collaboration.virtualdesk.muc.IDeskDirectory;
import org.xmpp.packet.Message;
import org.symphonyoss.collaboration.virtualdesk.command.ICommand;
import org.symphonyoss.collaboration.virtualdesk.packet.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemberMessageReceivedConversationHandler extends AbstractActionHandler
{
	private static final Logger Logger = LoggerFactory.getLogger(MemberMessageReceivedConversationHandler.class);
	
	private Message message;

	private Conversation conversationRoom;
	
	private IDeskDirectory deskDirectory;

	private ICommand command;

	public MemberMessageReceivedConversationHandler(Message message, Conversation deskRoom, IDeskDirectory deskDirectory)
	{
		this.message = message;
		this.conversationRoom = deskRoom;
		this.deskDirectory = deskDirectory;

		this.command = new TakeQuestionCommand();
		command.setNext(new InvalidCommand());
	}

	@Override
	protected void internalHandle()
	{
		MDC.put("deskjid", message.getTo().toBareJID());
		
		String messageBody = message.getBody().trim();

		User senderUser = conversationRoom.getOccupantByJID(message.getFrom());

		if (messageBody.startsWith("@"))
		{
			Logger.info("{} has sent the command to desk [{}]", message.getFrom(), conversationRoom.getJID());

			packetList.addAll(command.process(messageBody, senderUser, conversationRoom, deskDirectory));
		}
		else
		{
			Logger.info("{} has sent the message to desk [{}]", message.getFrom(), conversationRoom.getJID());

			packetList.addAll(MessageResponse.createMessageResponse(senderUser, conversationRoom.getCurrentMembers(), conversationRoom.getJID(), message));
			packetList.addAll(MessageResponse.createMessageResponse(conversationRoom.getVirtualDeskUser(), conversationRoom.getCurrentParticipants(), conversationRoom.getJID(), message));
		}
	}
}
