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

package org.symphonyoss.collaboration.virtualdesk.handler.desk;

import org.apache.log4j.MDC;
import org.symphonyoss.collaboration.virtualdesk.command.GetHistoryCommand;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.handler.AbstractActionHandler;
import org.xmpp.packet.Message;
import org.symphonyoss.collaboration.virtualdesk.command.ICommand;
import org.symphonyoss.collaboration.virtualdesk.command.InvalidCommand;
import org.symphonyoss.collaboration.virtualdesk.config.IServiceConfiguration;
import org.symphonyoss.collaboration.virtualdesk.data.UserState;
import org.symphonyoss.collaboration.virtualdesk.data.WorkflowState;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.muc.IDeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.packet.MessageResponse;

import org.symphonyoss.collaboration.virtualdesk.search.HistorySearcherSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParticipantMessageReceivedHandler extends AbstractActionHandler
{
	private static final Logger Logger = LoggerFactory.getLogger(ParticipantMessageReceivedHandler.class);
	
	private IServiceConfiguration serviceConfigurarion;
	
	private Message message;

	private Desk deskRoom;
	
	private IDeskDirectory deskDirectory;

	public ParticipantMessageReceivedHandler(IServiceConfiguration serviceConfigurarion, Message message, Desk deskRoom, IDeskDirectory deskDirectory)
	{
		this.serviceConfigurarion = serviceConfigurarion;
		this.message = message;
		this.deskRoom = deskRoom;
		this.deskDirectory = deskDirectory;
	}

	@Override
	protected void internalHandle()
	{
		MDC.put("deskjid", message.getTo().toBareJID());
		
		User senderUser = deskRoom.getOccupantByJID(message.getFrom());

		String messageBody = message.getBody().trim();
		
		if (messageBody.startsWith("@"))
		{
			// Process command
			ICommand command = new GetHistoryCommand(serviceConfigurarion, HistorySearcherSingleton.getInstance());
			command.setNext(new InvalidCommand());

			packetList.add(MessageResponse.createMessageResponse(senderUser, senderUser, deskRoom.getJID(), message));
			packetList.addAll(command.process(messageBody, senderUser, deskRoom, deskDirectory));
			
			return;
		}
		
		// User posts question
		UserState userState = deskRoom.getQuestion(senderUser.getNickname());

		packetList.addAll(MessageResponse.createMessageResponse(senderUser, deskRoom.getCurrentMembers(), deskRoom.getJID(), message));

		if (userState == null)
		{
			// User just posts the new question to desk
			deskRoom.addNewQuestion(senderUser.getNickname(), senderUser.getJID().toString(), message.getBody());

			packetList.add(MessageResponse.createMessageResponse(senderUser, senderUser, deskRoom.getJID(), message));
			packetList.add(MessageResponse.createDeskMessageResponse(serviceConfigurarion.getQuestionReceivedMessage(),
					senderUser, deskRoom.getJID()));
					
			Logger.info("{} has posted the new question to desk [{}]", message.getFrom(), deskRoom.getJID());
			

		}
		else
		{
			// User has already posted the question
			if (userState.getState() == WorkflowState.AwaitResponse)
			{
				// Question has not been taken yet, so message will be appended into the same question
				userState.addQuestion(message.getBody());
				
				deskRoom.updateQuestion(senderUser.getNickname());

				packetList.add(MessageResponse.createMessageResponse(senderUser, senderUser, deskRoom.getJID(), message));
				packetList.add(MessageResponse.createDeskMessageResponse(serviceConfigurarion.getQuestionReceivedMessage(),
						senderUser, deskRoom.getJID()));
				
				Logger.info("{} has appended the question to desk [{}]", message.getFrom(), deskRoom.getJID());
			}
			else
			{
				// Question has already been taken, ignore the message and response error message
				packetList.add(MessageResponse.createMessageResponse(senderUser, senderUser, deskRoom.getJID(), message));
				packetList.add(MessageResponse.createDeskMessageResponse("You are in conversation with Desk member. You cannot post the new questions to Desk until current conversation with Desk member is closed.",
						senderUser, deskRoom.getJID()));
				
				Logger.info("{} cannot post or append the question to desk [{}] because user has the conversation with desk member", message.getFrom(), deskRoom.getJID());
			}
		}
	}
}
